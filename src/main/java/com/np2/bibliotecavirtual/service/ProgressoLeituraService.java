package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.ProgressoLeituraRequestDTO;
import com.np2.bibliotecavirtual.dto.ProgressoLeituraResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.ProgressoLeitura;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.ProgressoLeituraRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Progresso de leitura (RN13–RN14): o aluno registra a página em que parou num material
 * que está com ele, e a API calcula o percentual lido.
 */
@Service
@RequiredArgsConstructor
public class ProgressoLeituraService {

    private final ProgressoLeituraRepository progressoRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioRepository usuarioRepository;

    /** Cria o progresso do empréstimo ou atualiza o que já existe. */
    @Transactional
    public ProgressoLeituraResponseDTO registrar(Long emprestimoId, ProgressoLeituraRequestDTO dto) {
        Emprestimo emprestimo = buscarEmprestimo(emprestimoId);
        Material material = emprestimo.getMaterial();

        // RN13: só lê quem está com o material (empréstimo ATIVO ou ATRASADO).
        if (!EmprestimoRegras.EM_USO.contains(emprestimo.getStatus())) {
            throw new RegraNegocioException("O empréstimo já foi devolvido; não dá para registrar a leitura");
        }
        // RN14: a página vai de 1 até o número de páginas do material.
        if (material.getNumeroPaginas() == null) {
            throw new RegraNegocioException(
                    "O material '" + material.getTitulo() + "' não tem número de páginas cadastrado (ex.: vídeo)");
        }
        if (dto.paginaAtual() > material.getNumeroPaginas()) {
            throw new RegraNegocioException("A página " + dto.paginaAtual() + " passa do total de "
                    + material.getNumeroPaginas() + " páginas do material");
        }

        ProgressoLeitura progresso = progressoRepository.findByEmprestimoId(emprestimoId)
                .orElseGet(() -> {
                    ProgressoLeitura novo = new ProgressoLeitura();
                    novo.setEmprestimo(emprestimo);
                    return novo;
                });
        progresso.setPaginaAtual(dto.paginaAtual());
        progresso.setAtualizadoEm(LocalDateTime.now());
        return ProgressoLeituraResponseDTO.de(emprestimo, progressoRepository.save(progresso));
    }

    /** Progresso de um empréstimo; se o aluno nunca registrou, vem com página 0. */
    @Transactional(readOnly = true)
    public ProgressoLeituraResponseDTO consultar(Long emprestimoId) {
        Emprestimo emprestimo = buscarEmprestimo(emprestimoId);
        return ProgressoLeituraResponseDTO.de(emprestimo, progressoRepository.findByEmprestimoId(emprestimoId).orElse(null));
    }

    /** O que o usuário está lendo agora: os empréstimos ATIVOS ou ATRASADOS, retirados mais recentemente primeiro. */
    @Transactional(readOnly = true)
    public List<ProgressoLeituraResponseDTO> leiturasDoUsuario(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com id " + usuarioId);
        }

        List<Emprestimo> emUso = emprestimoRepository.findByUsuarioId(usuarioId).stream()
                .filter(emprestimo -> EmprestimoRegras.EM_USO.contains(emprestimo.getStatus()))
                .sorted(Comparator.comparing(Emprestimo::getDataRetirada).reversed())
                .toList();
        if (emUso.isEmpty()) {
            return List.of();
        }

        Map<Long, ProgressoLeitura> progressoPorEmprestimo = progressoRepository
                .findByEmprestimoIdIn(emUso.stream().map(Emprestimo::getId).toList()).stream()
                .collect(Collectors.toMap(progresso -> progresso.getEmprestimo().getId(), Function.identity()));

        return emUso.stream()
                .map(emprestimo -> ProgressoLeituraResponseDTO.de(emprestimo, progressoPorEmprestimo.get(emprestimo.getId())))
                .toList();
    }

    private Emprestimo buscarEmprestimo(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado com id " + id));
    }
}
