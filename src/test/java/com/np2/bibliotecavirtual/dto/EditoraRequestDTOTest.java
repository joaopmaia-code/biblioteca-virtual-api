package com.np2.bibliotecavirtual.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confere as validações do EditoraRequestDTO (CNPJ e site) sem subir o Spring.
 */
class EditoraRequestDTOTest {

    private static ValidatorFactory fabrica;
    private static Validator validator;

    @BeforeAll
    static void criarValidator() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validator = fabrica.getValidator();
    }

    @AfterAll
    static void fecharValidator() {
        fabrica.close();
    }

    @Test
    void aceitaCnpjComPontuacao() {
        assertThat(errosDoCnpj("11.222.333/0001-81")).isEmpty();
    }

    @Test
    void aceitaCnpjSoComDigitos() {
        assertThat(errosDoCnpj("11222333000181")).isEmpty();
    }

    @Test
    void recusaCnpjComDigitoVerificadorErrado() {
        assertThat(errosDoCnpj("11.222.333/0001-82")).containsExactly("CNPJ inválido");
    }

    @Test
    void recusaSiteQueNaoEUrl() {
        Set<ConstraintViolation<EditoraRequestDTO>> erros = validator.validate(
                new EditoraRequestDTO("Moderna", "11222333000181", "São Paulo", "moderna ponto com"));

        assertThat(erros).extracting(ConstraintViolation::getMessage)
                .containsExactly("O site deve ser uma URL válida");
    }

    private Set<String> errosDoCnpj(String cnpj) {
        return validator.validateProperty(new EditoraRequestDTO("Moderna", cnpj, "São Paulo", null), "cnpj")
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
