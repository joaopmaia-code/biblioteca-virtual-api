package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.ConquistaResponseDTO;
import com.np2.bibliotecavirtual.dto.ConquistaUsuarioDTO;
import com.np2.bibliotecavirtual.dto.PerfilLeitorResponseDTO;
import com.np2.bibliotecavirtual.dto.PontuacaoDTO;
import com.np2.bibliotecavirtual.dto.RankingResponseDTO;
import com.np2.bibliotecavirtual.dto.ResumoDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.model.Avaliacao;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Gamificação (RN15–RN17): pontos, nível, conquistas e ranking de leitores.
 * <p>
 * Nada é gravado: tudo é calculado a cada consulta a partir de empréstimos, progresso de
 * leitura e avaliações. Assim os pontos nunca ficam desatualizados (se um empréstimo é
 * excluído, os pontos dele somem junto) e não foi preciso alterar o código de Empréstimo.
 */
@Service
@RequiredArgsConstructor
public class GamificacaoService {

    static final int PONTOS_LEITURA_CONCLUIDA = 10;
    static final int PONTOS_DEVOLUCAO_NO_PRAZO = 3;
    static final int PONTOS_AVALIACAO = 2;
    static final int PONTOS_COMENTARIO = 1;
    static final int ANO_CLASSICO = 1900;

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final ProgressoLeituraRepository progressoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    @Transactional(readOnly = true)
    public PerfilLeitorResponseDTO perfil(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com id " + usuarioId));

        List<Emprestimo> emprestimos = emprestimoRepository.findByUsuarioId(usuarioId);
        List<ProgressoLeitura> progressos = emprestimos.isEmpty() ? List.of()
                : progressoRepository.findByEmprestimoIdIn(emprestimos.stream().map(Emprestimo::getId).toList());
        List<Avaliacao> avaliacoes = avaliacaoRepository.buscar(null, usuarioId);

        EstatisticasLeitor estatisticas = estatisticas(emprestimos, porEmprestimo(progressos), avaliacoes);
        List<PontuacaoDTO> extrato = extrato(estatisticas);
        int pontos = somar(extrato);
        NivelLeitor nivel = NivelLeitor.de(pontos);
        NivelLeitor proximo = nivel.proximo();

        return new PerfilLeitorResponseDTO(
                new ResumoDTO(usuario.getId(), usuario.getNome()),
                pontos,
                nivel,
                nivel.getNome(),
                proximo == null ? null : proximo.getNome(),
                proximo == null ? null : proximo.getPontosMinimos() - pontos,
                extrato,
                estatisticas,
                Arrays.stream(Conquista.values()).map(conquista -> ConquistaUsuarioDTO.de(conquista, estatisticas)).toList());
    }

    /**
     * Os alunos com mais pontos. Quem tem 0 ponto fica de fora; empatados dividem a posição
     * (ex.: 1, 1, 3) e aparecem em ordem alfabética. O nome vai abreviado, por privacidade.
     */
    @Transactional(readOnly = true)
    public List<RankingResponseDTO> ranking(int limite) {
        Map<Long, List<Emprestimo>> emprestimosPorUsuario = emprestimoRepository.findAllByOrderByIdAsc().stream()
                .collect(Collectors.groupingBy(emprestimo -> emprestimo.getUsuario().getId()));
        Map<Long, ProgressoLeitura> progressoPorEmprestimo = porEmprestimo(progressoRepository.findAll());
        Map<Long, List<Avaliacao>> avaliacoesPorUsuario = avaliacaoRepository.findAll().stream()
                .collect(Collectors.groupingBy(avaliacao -> avaliacao.getUsuario().getId()));

        record Pontuado(Usuario usuario, int pontos, EstatisticasLeitor estatisticas) {
        }

        List<Pontuado> pontuados = usuarioRepository.findAll().stream()
                .map(usuario -> {
                    EstatisticasLeitor estatisticas = estatisticas(
                            emprestimosPorUsuario.getOrDefault(usuario.getId(), List.of()),
                            progressoPorEmprestimo,
                            avaliacoesPorUsuario.getOrDefault(usuario.getId(), List.of()));
                    return new Pontuado(usuario, somar(extrato(estatisticas)), estatisticas);
                })
                .filter(pontuado -> pontuado.pontos() > 0)
                .sorted(Comparator.comparingInt(Pontuado::pontos).reversed()
                        .thenComparing(pontuado -> pontuado.usuario().getNome()))
                .limit(limite)
                .toList();

        List<RankingResponseDTO> ranking = new ArrayList<>();
        for (int i = 0; i < pontuados.size(); i++) {
            Pontuado atual = pontuados.get(i);
            // Empate: repete a posição de quem está logo acima.
            int posicao = i > 0 && atual.pontos() == pontuados.get(i - 1).pontos() ? ranking.get(i - 1).posicao() : i + 1;
            int conquistas = (int) Arrays.stream(Conquista.values())
                    .filter(conquista -> conquista.desbloqueada(atual.estatisticas()))
                    .count();
            ranking.add(new RankingResponseDTO(posicao,
                    new ResumoDTO(atual.usuario().getId(), abreviar(atual.usuario().getNome())),
                    atual.pontos(), NivelLeitor.de(atual.pontos()), conquistas));
        }
        return ranking;
    }

    public List<ConquistaResponseDTO> conquistas() {
        return Arrays.stream(Conquista.values()).map(ConquistaResponseDTO::de).toList();
    }

    /**
     * Junta os números do histórico de um aluno.
     * RN15: uma leitura só conta quando o progresso chega à última página, e cada material conta
     * uma vez só, mesmo que o aluno pegue de novo (evita ganhar pontos relendo o mesmo material).
     */
    static EstatisticasLeitor estatisticas(List<Emprestimo> emprestimos, Map<Long, ProgressoLeitura> progressoPorEmprestimo,
                                           List<Avaliacao> avaliacoes) {
        Map<Long, Material> concluidos = emprestimos.stream()
                .filter(emprestimo -> concluiu(emprestimo, progressoPorEmprestimo.get(emprestimo.getId())))
                .map(Emprestimo::getMaterial)
                .collect(Collectors.toMap(Material::getId, Function.identity(), (primeiro, repetido) -> primeiro));

        int categorias = (int) concluidos.values().stream().map(material -> material.getCategoria().getId()).distinct().count();
        int classicos = (int) concluidos.values().stream().filter(material -> material.getAnoPublicacao() < ANO_CLASSICO).count();
        int devolucoesNoPrazo = (int) emprestimos.stream().filter(GamificacaoService::devolvidoNoPrazo).count();
        int comComentario = (int) avaliacoes.stream().filter(avaliacao -> avaliacao.getComentario() != null).count();

        return new EstatisticasLeitor(concluidos.size(), categorias, classicos, devolucoesNoPrazo,
                avaliacoes.size(), comComentario);
    }

    /** RN16: pontos por leitura concluída, devolução no prazo, avaliação e comentário. Só soma, nunca tira. */
    static List<PontuacaoDTO> extrato(EstatisticasLeitor estatisticas) {
        return List.of(
                PontuacaoDTO.de("Leituras concluídas", estatisticas.leiturasConcluidas(), PONTOS_LEITURA_CONCLUIDA),
                PontuacaoDTO.de("Devoluções no prazo", estatisticas.devolucoesNoPrazo(), PONTOS_DEVOLUCAO_NO_PRAZO),
                PontuacaoDTO.de("Avaliações", estatisticas.avaliacoes(), PONTOS_AVALIACAO),
                PontuacaoDTO.de("Comentários nas avaliações", estatisticas.avaliacoesComComentario(), PONTOS_COMENTARIO));
    }

    /** "Ana Beatriz Souza" → "Ana S."; nome de uma palavra só fica como está. */
    static String abreviar(String nome) {
        String[] partes = nome.trim().split("\\s+");
        return partes.length == 1 ? partes[0] : partes[0] + " " + partes[partes.length - 1].charAt(0) + ".";
    }

    private static boolean concluiu(Emprestimo emprestimo, ProgressoLeitura progresso) {
        Integer paginas = emprestimo.getMaterial().getNumeroPaginas();
        return progresso != null && paginas != null && progresso.getPaginaAtual() >= paginas;
    }

    /** RN16: devolvido até o fim do dia previsto. */
    private static boolean devolvidoNoPrazo(Emprestimo emprestimo) {
        return emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO
                && emprestimo.getDataDevolucao() != null
                && !emprestimo.getDataDevolucao().toLocalDate().isAfter(emprestimo.getDataPrevistaDevolucao());
    }

    private static Map<Long, ProgressoLeitura> porEmprestimo(List<ProgressoLeitura> progressos) {
        return progressos.stream()
                .collect(Collectors.toMap(progresso -> progresso.getEmprestimo().getId(), Function.identity()));
    }

    private static int somar(List<PontuacaoDTO> extrato) {
        return extrato.stream().mapToInt(PontuacaoDTO::total).sum();
    }
}
