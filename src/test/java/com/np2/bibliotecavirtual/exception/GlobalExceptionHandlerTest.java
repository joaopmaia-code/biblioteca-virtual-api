package com.np2.bibliotecavirtual.exception;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testa o GlobalExceptionHandler com um controller falso, sem subir banco nem o Spring inteiro.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void configurar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerDeTeste())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recursoNaoEncontradoRetorna404() throws Exception {
        mockMvc.perform(get("/teste/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Not Found"))
                .andExpect(jsonPath("$.mensagem").value("Registro não encontrado com id 1"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.campos").doesNotExist());
    }

    @Test
    void regraNegocioRetorna409() throws Exception {
        mockMvc.perform(get("/teste/regra-negocio"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflict"))
                .andExpect(jsonPath("$.mensagem").value("Já existe um registro com esse nome"));
    }

    @Test
    void validacaoRetorna400ComMensagemDeCadaCampo() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.campos", hasSize(2)))
                .andExpect(jsonPath("$.campos[0].campo").value("cor"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("A cor é obrigatória"))
                .andExpect(jsonPath("$.campos[1].campo").value("nome"))
                .andExpect(jsonPath("$.campos[1].mensagem").value("O nome é obrigatório"));
    }

    @Test
    void valorDeEnumInexistenteRetorna400IndicandoOCampo() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\": \"Teste\", \"cor\": \"ROXO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("cor"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("Valor 'ROXO' inválido. Valores aceitos: [AZUL, VERDE]"));
    }

    @Test
    void jsonMalFormatadoRetorna400() throws Exception {
        mockMvc.perform(post("/teste/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{nome: "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Corpo da requisição ausente ou com JSON inválido"));
    }

    @Test
    void parametroComTipoErradoRetorna400() throws Exception {
        mockMvc.perform(get("/teste/id/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("O parâmetro 'id' recebeu o valor 'abc', que não é válido"));
    }

    enum Cor { AZUL, VERDE }

    record EntradaDeTeste(
            @NotBlank(message = "O nome é obrigatório") String nome,
            @NotNull(message = "A cor é obrigatória") Cor cor) {
    }

    @RestController
    static class ControllerDeTeste {

        @GetMapping("/teste/nao-encontrado")
        void naoEncontrado() {
            throw new RecursoNaoEncontradoException("Registro não encontrado com id 1");
        }

        @GetMapping("/teste/regra-negocio")
        void regraNegocio() {
            throw new RegraNegocioException("Já existe um registro com esse nome");
        }

        @PostMapping("/teste/validacao")
        void validacao(@Valid @RequestBody EntradaDeTeste entrada) {
        }

        @GetMapping("/teste/id/{id}")
        void porId(@PathVariable Long id) {
        }
    }
}
