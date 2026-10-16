package com.np2.bibliotecavirtual.exception;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.np2.bibliotecavirtual.dto.CampoErroDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Intercepta as exceções lançadas pelos controllers e services e devolve sempre
 * o mesmo formato de erro (ErroResponseDTO) com o status HTTP adequado.
 */
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

    private ResponseEntity<ErroResponseDTO> responder(ErroResponseDTO erro) {
        return ResponseEntity.status(erro.status()).body(erro);
    }
}
