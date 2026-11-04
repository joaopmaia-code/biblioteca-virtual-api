package com.np2.bibliotecavirtual.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.np2.bibliotecavirtual.dto.CampoErroDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Intercepta as exceções lançadas pelos controllers e services e devolve sempre
 * o mesmo formato de erro (ErroResponseDTO) com o status HTTP adequado.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** ID inexistente → 404. */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return responder(ErroResponseDTO.de(HttpStatus.NOT_FOUND, ex.getMessage()));
    }

    /** Regra de negócio violada (ex.: duplicidade) → 409. */
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraNegocio(RegraNegocioException ex) {
        return responder(ErroResponseDTO.de(HttpStatus.CONFLICT, ex.getMessage()));
    }

    /**
     * O banco recusou a operação por uma constraint (ex.: excluir registro que ainda é usado
     * por outra tabela) → 409. Rede de segurança: o normal é o Service checar antes e lançar
     * RegraNegocioException com uma mensagem mais específica.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponseDTO> tratarIntegridadeDados(DataIntegrityViolationException ex) {
        return responder(ErroResponseDTO.de(HttpStatus.CONFLICT,
                "A operação não pode ser concluída porque o registro está vinculado a outros cadastros ou repete um valor único"));
    }

    /** Falha no @Valid do corpo da requisição → 400 com a mensagem de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        List<CampoErroDTO> campos = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> new CampoErroDTO(erro.getField(), erro.getDefaultMessage()))
                .sorted(Comparator.comparing(CampoErroDTO::campo))
                .toList();

        return responder(ErroResponseDTO.de(HttpStatus.BAD_REQUEST, "Existem campos inválidos na requisição", campos));
    }

    /** JSON mal formatado ou valor que não existe no enum → 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponseDTO> tratarCorpoIlegivel(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException causa
                && causa.getTargetType() != null && causa.getTargetType().isEnum()) {
            String campo = causa.getPath().stream()
                    .map(JsonMappingException.Reference::getFieldName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining("."));
            String mensagem = "Valor '%s' inválido. Valores aceitos: %s"
                    .formatted(causa.getValue(), Arrays.toString(causa.getTargetType().getEnumConstants()));

            return responder(ErroResponseDTO.de(HttpStatus.BAD_REQUEST, "Existem campos inválidos na requisição",
                    List.of(new CampoErroDTO(campo, mensagem))));
        }

        return responder(ErroResponseDTO.de(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou com JSON inválido"));
    }

    /** Parâmetro da URL com tipo errado (ex.: /categorias/abc) → 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponseDTO> tratarTipoParametro(MethodArgumentTypeMismatchException ex) {
        String mensagem = "O parâmetro '%s' recebeu o valor '%s', que não é válido".formatted(ex.getName(), ex.getValue());
        return responder(ErroResponseDTO.de(HttpStatus.BAD_REQUEST, mensagem));
    }

    /** Rota que não existe (ex.: /materiaisss) → 404. */
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ErroResponseDTO> tratarRotaInexistente(HttpServletRequest request) {
        String mensagem = "Rota não encontrada: %s %s".formatted(request.getMethod(), request.getRequestURI());
        return responder(ErroResponseDTO.de(HttpStatus.NOT_FOUND, mensagem));
    }

    /** Rota existe, mas não com esse método (ex.: PATCH /categorias/1) → 405. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarMetodoNaoPermitido(HttpRequestMethodNotSupportedException ex) {
        String mensagem = "O método %s não é aceito nesta rota. Métodos aceitos: %s"
                .formatted(ex.getMethod(), ex.getSupportedHttpMethods());
        return responder(ErroResponseDTO.de(HttpStatus.METHOD_NOT_ALLOWED, mensagem));
    }

    /** Corpo enviado em outro formato que não JSON → 415. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErroResponseDTO> tratarFormatoNaoSuportado(HttpMediaTypeNotSupportedException ex) {
        String mensagem = "O formato '%s' não é aceito. Envie o corpo como application/json".formatted(ex.getContentType());
        return responder(ErroResponseDTO.de(HttpStatus.UNSUPPORTED_MEDIA_TYPE, mensagem));
    }

    /**
     * Qualquer outra exceção. Se for um erro do próprio Spring que já tem status (ex.: 406),
     * mantém o status; senão é um erro inesperado → 500, registrado no log e sem detalhes
     * internos na resposta.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponseDTO> tratarErroInesperado(Exception ex) {
        if (ex instanceof ErrorResponse erroDoSpring) {
            HttpStatus status = HttpStatus.resolve(erroDoSpring.getStatusCode().value());
            if (status != null && status.is4xxClientError()) {
                return responder(ErroResponseDTO.de(status, "A requisição não pôde ser atendida: " + status.getReasonPhrase()));
            }
        }

        log.error("Erro inesperado ao processar a requisição", ex);
        return responder(ErroResponseDTO.de(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno inesperado. Tente novamente; se continuar, avise a equipe"));
    }

    private ResponseEntity<ErroResponseDTO> responder(ErroResponseDTO erro) {
        return ResponseEntity.status(erro.status()).body(erro);
    }
}
