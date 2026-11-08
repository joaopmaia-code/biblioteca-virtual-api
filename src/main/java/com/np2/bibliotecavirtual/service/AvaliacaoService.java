package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.AvaliacaoAtualizacaoDTO;
import com.np2.bibliotecavirtual.dto.AvaliacaoRequestDTO;
import com.np2.bibliotecavirtual.dto.AvaliacaoResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Avaliacao;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.AvaliacaoRepository;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

/**
 * Avaliações de materiais (RN10–RN12): nota de 1 a 5, só de quem já pegou o material
 * emprestado e uma por aluno e material.
 */
@Service
@RequiredArgsConstructor
public class AvaliacaoService {

    /** Qualquer empréstimo conta, inclusive o que ainda está com o aluno. */
    private static final EnumSet<StatusEmprestimo> QUALQUER_STATUS = EnumSet.allOf(StatusEmprestimo.class);

    private final AvaliacaoRepository avaliacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MaterialRepository materialRepository;
    private final EmprestimoRepository emprestimoRepository;

    @Transactional
    public AvaliacaoResponseDTO cadastrar(AvaliacaoRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com id " + dto.usuarioId()));
        Material material = buscarMaterial(dto.materialId());

        // RN11: só avalia quem já leu, ou seja, quem já pegou o material emprestado.
        if (!emprestimoRepository.existsByUsuarioIdAndMaterialIdAndStatusIn(
                usuario.getId(), material.getId(), QUALQUER_STATUS)) {
            throw new RegraNegocioException("Só pode avaliar o material quem já o pegou emprestado");
        }
        // RN12: uma avaliação por aluno e material (também garantido pela constraint UNIQUE da V40).
        if (avaliacaoRepository.existsByUsuarioIdAndMaterialId(usuario.getId(), material.getId())) {
            throw new RegraNegocioException("Este usuário já avaliou este material; edite a avaliação existente");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUsuario(usuario);
        avaliacao.setMaterial(material);
        preencherNotaEComentario(avaliacao, dto.nota(), dto.comentario());
        return AvaliacaoResponseDTO.de(avaliacaoRepository.save(avaliacao));
    }

    /** Filtros opcionais: material e usuário. */
    @Transactional(readOnly = true)
    public List<AvaliacaoResponseDTO> listar(Long materialId, Long usuarioId) {
        return avaliacaoRepository.buscar(materialId, usuarioId).stream()
                .map(AvaliacaoResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public AvaliacaoResponseDTO buscarPorId(Long id) {
        return AvaliacaoResponseDTO.de(buscarAvaliacao(id));
    }

    @Transactional
    public AvaliacaoResponseDTO atualizar(Long id, AvaliacaoAtualizacaoDTO dto) {
        Avaliacao avaliacao = buscarAvaliacao(id);
        preencherNotaEComentario(avaliacao, dto.nota(), dto.comentario());
        return AvaliacaoResponseDTO.de(avaliacaoRepository.save(avaliacao));
    }

    @Transactional
    public void excluir(Long id) {
        avaliacaoRepository.delete(buscarAvaliacao(id));
    }

    private void preencherNotaEComentario(Avaliacao avaliacao, Integer nota, String comentario) {
        avaliacao.setNota(nota);
        avaliacao.setComentario(comentario == null || comentario.isBlank() ? null : comentario.trim());
        avaliacao.setDataAvaliacao(LocalDateTime.now());
    }

    private Avaliacao buscarAvaliacao(Long id) {
        return avaliacaoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Avaliação não encontrada com id " + id));
    }

    private Material buscarMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material não encontrado com id " + id));
    }
}
