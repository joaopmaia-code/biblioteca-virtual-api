package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.ConquistaUsuarioDTO;
import com.np2.bibliotecavirtual.dto.PerfilLeitorResponseDTO;
import com.np2.bibliotecavirtual.dto.RankingResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.model.Avaliacao;
import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.model.Conquista;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.EstatisticasLeitor;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.NivelLeitor;
import com.np2.bibliotecavirtual.model.ProgressoLeitura;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.AvaliacaoRepository;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.ProgressoLeituraRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.when;

/**
 * Testa a gamificação (RN15–RN17) com os repositórios simulados (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class GamificacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private ProgressoLeituraRepository progressoRepository;

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @InjectMocks
    private GamificacaoService gamificacaoService;

    private final Usuario ana = usuario(1L, "Ana Beatriz Souza");
    private final Usuario bruno = usuario(2L, "Bruno Lima");
    private final Usuario carla = usuario(3L, "Carla");

    private final Categoria literatura = categoria(1L);
    private final Categoria matematica = categoria(2L);

    private final List<ProgressoLeitura> progressos = new ArrayList<>();
    private long proximoEmprestimoId = 1;

    @Test
    void leituraSoContaNaUltimaPaginaECadaMaterialContaUmaVez() {
        Material domCasmurro = material(10L, 256, 1899, literatura);
        List<Emprestimo> emprestimos = List.of(
                concluido(ana, domCasmurro),                                  // conta
                concluido(ana, domCasmurro),                                  // releitura: não conta de novo
                emLeitura(ana, material(11L, 300, 2020, literatura), 150),    // na metade: não conta
                emLeitura(ana, material(12L, null, 2024, matematica), 0));    // vídeo: não tem páginas

        EstatisticasLeitor estatisticas = GamificacaoService.estatisticas(emprestimos, progressoPorEmprestimo(), List.of());

        assertThat(estatisticas.leiturasConcluidas()).isEqualTo(1);
        assertThat(estatisticas.categoriasLidas()).isEqualTo(1);
        assertThat(estatisticas.classicosLidos()).isEqualTo(1);
    }

    @Test
    void devolucaoSoPontuaSeForAteODiaPrevisto() {
        Material material = material(10L, 100, 2020, literatura);
        Emprestimo noUltimoDia = devolvido(ana, material, LocalDate.now(), LocalDateTime.now());
        Emprestimo atrasado = devolvido(ana, material, LocalDate.now().minusDays(3), LocalDateTime.now());
        Emprestimo ativo = emLeitura(ana, material, 10);

        EstatisticasLeitor estatisticas = GamificacaoService.estatisticas(
                List.of(noUltimoDia, atrasado, ativo), progressoPorEmprestimo(), List.of());

        assertThat(estatisticas.devolucoesNoPrazo()).isEqualTo(1);
    }

    @Test
    void perfilSomaOsPontosCalculaONivelEOProgressoDasConquistas() {
        // 5 leituras de 3 categorias (50) + 2 devoluções no prazo (6) + 2 avaliações, 1 com comentário (4 + 1) = 61
        Categoria historia = categoria(3L);
        List<Emprestimo> emprestimos = new ArrayList<>(List.of(
                concluido(ana, material(10L, 100, 1881, literatura)),
                concluido(ana, material(11L, 100, 2000, literatura)),
                concluido(ana, material(12L, 100, 2001, matematica)),
                concluido(ana, material(13L, 100, 2002, matematica)),
                concluido(ana, material(14L, 100, 2003, historia)),
                devolvido(ana, material(15L, 100, 2004, historia), LocalDate.now(), LocalDateTime.now()),
                devolvido(ana, material(16L, 100, 2005, historia), LocalDate.now(), LocalDateTime.now())));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(emprestimoRepository.findByUsuarioId(1L)).thenReturn(emprestimos);
        when(progressoRepository.findByEmprestimoIdIn(anyCollection())).thenReturn(progressos);
        when(avaliacaoRepository.buscar(null, 1L))
                .thenReturn(List.of(avaliacao(ana, "Muito bom"), avaliacao(ana, null)));

        PerfilLeitorResponseDTO perfil = gamificacaoService.perfil(1L);

        assertThat(perfil.pontos()).isEqualTo(61);
        assertThat(perfil.nivel()).isEqualTo(NivelLeitor.LEITOR);
        assertThat(perfil.proximoNivel()).isEqualTo("Leitor Voraz");
        assertThat(perfil.pontosParaProximoNivel()).isEqualTo(150 - 61);
        assertThat(perfil.extrato()).extracting("total").containsExactly(50, 6, 4, 1);
        assertThat(perfil.conquistas()).filteredOn(ConquistaUsuarioDTO::desbloqueada)
                .extracting(ConquistaUsuarioDTO::codigo)
                .containsExactly(Conquista.PRIMEIRA_PAGINA, Conquista.MARATONISTA, Conquista.EXPLORADOR, Conquista.CLASSICO);
        ConquistaUsuarioDTO pontual = perfil.conquistas().stream()
                .filter(conquista -> conquista.codigo() == Conquista.PONTUAL).findFirst().orElseThrow();
        assertThat(pontual.progresso()).isEqualTo(2);
        assertThat(pontual.meta()).isEqualTo(10);
    }

    @Test
    void perfilDeAlunoSemHistoricoTemZeroPontoENivelInicial() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(emprestimoRepository.findByUsuarioId(1L)).thenReturn(List.of());
        when(avaliacaoRepository.buscar(null, 1L)).thenReturn(List.of());

        PerfilLeitorResponseDTO perfil = gamificacaoService.perfil(1L);

        assertThat(perfil.pontos()).isZero();
        assertThat(perfil.nivel()).isEqualTo(NivelLeitor.LEITOR_INICIANTE);
        assertThat(perfil.pontosParaProximoNivel()).isEqualTo(50);
        assertThat(perfil.conquistas()).noneMatch(ConquistaUsuarioDTO::desbloqueada);
    }

    @Test
    void perfilDeUsuarioInexistenteLanca404() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gamificacaoService.perfil(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado com id 99");
    }

    @Test
    void rankingOrdenaPorPontosEmpatadosDividemAPosicaoEQuemTemZeroFicaDeFora() {
        Usuario davi = usuario(4L, "Davi Rocha");
        List<Emprestimo> todos = List.of(
                concluido(bruno, material(10L, 100, 2000, literatura)),   // Bruno: 10
                concluido(ana, material(11L, 100, 2000, literatura)),     // Ana: 10
                concluido(carla, material(12L, 100, 2000, literatura)),   // Carla: 10 + 10 = 20
                concluido(carla, material(13L, 100, 2000, matematica)),
                emLeitura(davi, material(14L, 100, 2000, literatura), 5)); // Davi: 0
        when(emprestimoRepository.findAllByOrderByIdAsc()).thenReturn(todos);
        when(progressoRepository.findAll()).thenReturn(progressos);
        when(avaliacaoRepository.findAll()).thenReturn(List.of());
        when(usuarioRepository.findAll()).thenReturn(List.of(ana, bruno, carla, davi));

        List<RankingResponseDTO> ranking = gamificacaoService.ranking(10);

        assertThat(ranking).extracting(RankingResponseDTO::posicao).containsExactly(1, 2, 2);
        assertThat(ranking).extracting(linha -> linha.usuario().nome()).containsExactly("Carla", "Ana S.", "Bruno L.");
        assertThat(ranking).extracting(RankingResponseDTO::pontos).containsExactly(20, 10, 10);
        assertThat(ranking.get(0).conquistasDesbloqueadas()).isEqualTo(1);
    }

    @Test
    void rankingRespeitaOLimite() {
        when(emprestimoRepository.findAllByOrderByIdAsc()).thenReturn(List.of(
                concluido(ana, material(10L, 100, 2000, literatura)),
                concluido(bruno, material(11L, 100, 2000, literatura))));
        when(progressoRepository.findAll()).thenReturn(progressos);
        when(avaliacaoRepository.findAll()).thenReturn(List.of());
        when(usuarioRepository.findAll()).thenReturn(List.of(ana, bruno));

        assertThat(gamificacaoService.ranking(1)).hasSize(1);
    }

    @Test
    void nivelMudaExatamenteNoMinimoDeCadaFaixa() {
        assertThat(NivelLeitor.de(49)).isEqualTo(NivelLeitor.LEITOR_INICIANTE);
        assertThat(NivelLeitor.de(50)).isEqualTo(NivelLeitor.LEITOR);
        assertThat(NivelLeitor.de(500)).isEqualTo(NivelLeitor.LENDA_DA_BIBLIOTECA);
        assertThat(NivelLeitor.LENDA_DA_BIBLIOTECA.proximo()).isNull();
    }

    @Test
    void abreviaONomeParaOPrimeiroEAInicialDoUltimo() {
        assertThat(GamificacaoService.abreviar("Ana Beatriz Souza")).isEqualTo("Ana S.");
        assertThat(GamificacaoService.abreviar("  Carla  ")).isEqualTo("Carla");
    }

    // ----- montagem dos dados de teste -----

    private Emprestimo concluido(Usuario usuario, Material material) {
        return emLeitura(usuario, material, material.getNumeroPaginas());
    }

    private Emprestimo emLeitura(Usuario usuario, Material material, Integer pagina) {
        Emprestimo emprestimo = emprestimo(usuario, material, StatusEmprestimo.ATIVO, LocalDate.now().plusDays(14), null);
        if (pagina != null && pagina > 0) {
            ProgressoLeitura progresso = new ProgressoLeitura();
            progresso.setEmprestimo(emprestimo);
            progresso.setPaginaAtual(pagina);
            progressos.add(progresso);
        }
        return emprestimo;
    }

    private Emprestimo devolvido(Usuario usuario, Material material, LocalDate prevista, LocalDateTime devolucao) {
        return emprestimo(usuario, material, StatusEmprestimo.DEVOLVIDO, prevista, devolucao);
    }

    private Emprestimo emprestimo(Usuario usuario, Material material, StatusEmprestimo status,
                                  LocalDate prevista, LocalDateTime devolucao) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(proximoEmprestimoId++);
        emprestimo.setUsuario(usuario);
        emprestimo.setMaterial(material);
        emprestimo.setStatus(status);
        emprestimo.setDataPrevistaDevolucao(prevista);
        emprestimo.setDataDevolucao(devolucao);
        return emprestimo;
    }

    private Map<Long, ProgressoLeitura> progressoPorEmprestimo() {
        Map<Long, ProgressoLeitura> mapa = new java.util.HashMap<>();
        progressos.forEach(progresso -> mapa.put(progresso.getEmprestimo().getId(), progresso));
        return mapa;
    }

    private static Avaliacao avaliacao(Usuario usuario, String comentario) {
        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setNota(5);
        avaliacao.setComentario(comentario);
        return avaliacao;
    }

    private static Material material(Long id, Integer paginas, int ano, Categoria categoria) {
        Material material = new Material();
        material.setId(id);
        material.setTitulo("Material " + id);
        material.setNumeroPaginas(paginas);
        material.setAnoPublicacao(ano);
        material.setCategoria(categoria);
        return material;
    }

    private static Categoria categoria(Long id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        return categoria;
    }

    private static Usuario usuario(Long id, String nome) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome(nome);
        return usuario;
    }
}
