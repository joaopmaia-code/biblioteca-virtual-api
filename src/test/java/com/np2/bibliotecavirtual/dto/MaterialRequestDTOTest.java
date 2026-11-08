package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.TipoMaterial;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Confere as validações do MaterialRequestDTO (ano, URL, licenças e autores) sem subir o Spring.
 */
class MaterialRequestDTOTest {

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
    void aceitaMaterialValido() {
        assertThat(erros(material(1899, "https://acervo.escola.edu.br/a.pdf", 3, List.of(1L)))).isEmpty();
    }

    @Test
    void aceitaAnoAtual() {
        assertThat(erros(material(Year.now().getValue(), "https://acervo.escola.edu.br/a.pdf", 3, List.of(1L))))
                .isEmpty();
    }

    @Test
    void recusaAnoNoFuturo() {
        assertThat(erros(material(Year.now().getValue() + 1, "https://acervo.escola.edu.br/a.pdf", 3, List.of(1L))))
                .containsExactly("O ano de publicação não pode ser maior que o ano atual");
    }

    @Test
    void recusaAnoAntesDe1500() {
        assertThat(erros(material(1499, "https://acervo.escola.edu.br/a.pdf", 3, List.of(1L))))
                .containsExactly("O ano de publicação deve ser 1500 ou depois");
    }

    @Test
    void recusaUrlInvalida() {
        assertThat(erros(material(1899, "arquivo.pdf", 3, List.of(1L))))
                .containsExactly("A URL do arquivo deve ser uma URL válida");
    }

    @Test
    void recusaZeroLicencas() {
        assertThat(erros(material(1899, "https://acervo.escola.edu.br/a.pdf", 0, List.of(1L))))
                .containsExactly("A quantidade de licenças deve ser pelo menos 1");
    }

    @Test
    void recusaListaDeAutoresVazia() {
        assertThat(erros(material(1899, "https://acervo.escola.edu.br/a.pdf", 3, List.of())))
                .containsExactly("Informe pelo menos um autor");
    }

    private MaterialRequestDTO material(Integer ano, String url, Integer licencas, List<Long> autoresIds) {
        return new MaterialRequestDTO("Dom Casmurro", TipoMaterial.LIVRO, ano, null, url, null, licencas,
                1L, null, autoresIds, null);
    }

    @Test
    void recusaUrlDeCapaInvalidaMasAceitaSemCapa() {
        MaterialRequestDTO valido = material(1899, "https://acervo.escola.edu.br/a.pdf", 3, List.of(1L));
        MaterialRequestDTO capaInvalida = new MaterialRequestDTO(valido.titulo(), valido.tipo(), valido.anoPublicacao(),
                null, valido.urlArquivo(), null, valido.quantidadeLicencas(), 1L, null, List.of(1L), "capa.jpg");

        assertThat(erros(capaInvalida)).containsExactly("A URL da capa deve ser uma URL válida");
        assertThat(erros(valido)).isEmpty();
    }

    private Set<String> erros(MaterialRequestDTO dto) {
        return validator.validate(dto).stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }
}
