package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.EmprestimoAtualizacaoDTO;
import com.np2.bibliotecavirtual.dto.EmprestimoRequestDTO;
import com.np2.bibliotecavirtual.dto.EmprestimoResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final UsuarioService usuarioService;
    private final EmprestimoRegras regras;

    /** RF06 com RN01 (licença livre), RN02 (prazo padrão), RN03 (sem repetir material) e RN04 (limite). */
    @Transactional
    public EmprestimoResponseDTO registrar(EmprestimoRequestDTO dto) {
        Usuario usuario = usuarioService.buscarUsuario(dto.usuarioId());
        Material material = buscarMaterialComTrava(dto.materialId());

        if (regras.licencasDisponiveis(material) <= 0) {
            throw new RegraNegocioException("Sem licenças disponíveis. Entre na fila de espera.");
        }
        if (regras.jaTemEmprestado(usuario, material)) {
            throw new RegraNegocioException("O usuário já está com este material emprestado");
        }
        if (regras.atingiuLimite(usuario)) {
            throw new RegraNegocioException(
                    "O usuário atingiu o limite de " + regras.getMaxAtivos() + " empréstimos ativos");
        }

        return EmprestimoResponseDTO.de(regras.registrar(usuario, material, dto.dataPrevistaDevolucao()));
    }

    /** Filtros opcionais: status e usuário. */
    @Transactional(readOnly = true)
    public List<EmprestimoResponseDTO> listar(StatusEmprestimo status, Long usuarioId) {
        return emprestimoRepository.findAllByOrderByIdAsc().stream()
                .filter(e -> status == null || e.getStatus() == status)
                .filter(e -> usuarioId == null || e.getUsuario().getId().equals(usuarioId))
                .map(EmprestimoResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EmprestimoResponseDTO buscarPorId(Long id) {
        return EmprestimoResponseDTO.de(buscarEmprestimo(id));
    }

    /** Edição = renovar o prazo. Um empréstimo ATRASADO renovado volta a ser ATIVO. */
    @Transactional
    public EmprestimoResponseDTO atualizar(Long id, EmprestimoAtualizacaoDTO dto) {
        Emprestimo emprestimo = buscarEmprestimo(id);
        if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new RegraNegocioException("Não é possível alterar um empréstimo já devolvido");
        }

        emprestimo.setDataPrevistaDevolucao(dto.dataPrevistaDevolucao());
        emprestimo.setStatus(StatusEmprestimo.ATIVO);
        return EmprestimoResponseDTO.de(emprestimoRepository.save(emprestimo));
    }

    /**
     * RF07: registra a devolução e libera a licença.
     * A integração com a fila de espera (RF09, atender o primeiro da fila) entra junto com a Reserva.
     */
    @Transactional
    public EmprestimoResponseDTO devolver(Long id) {
        Emprestimo emprestimo = buscarEmprestimo(id);
        if (emprestimo.getStatus() == StatusEmprestimo.DEVOLVIDO) {
            throw new RegraNegocioException("Este empréstimo já foi devolvido");
        }

        emprestimo.setStatus(StatusEmprestimo.DEVOLVIDO);
        emprestimo.setDataDevolucao(LocalDateTime.now());
        return EmprestimoResponseDTO.de(emprestimoRepository.save(emprestimo));
    }

    @Transactional
    public void excluir(Long id) {
        Emprestimo emprestimo = buscarEmprestimo(id);
        emprestimoRepository.delete(emprestimo);
    }

    /** RN05: empréstimos ATIVOS com prazo vencido passam a ATRASADO. Devolve quantos mudaram. */
    @Transactional
    public int atualizarAtrasados() {
        List<Emprestimo> vencidos = emprestimoRepository
                .findByStatusAndDataPrevistaDevolucaoBefore(StatusEmprestimo.ATIVO, LocalDate.now());
        vencidos.forEach(e -> e.setStatus(StatusEmprestimo.ATRASADO));
        emprestimoRepository.saveAll(vencidos);

        if (!vencidos.isEmpty()) {
            log.info("{} empréstimo(s) marcado(s) como ATRASADO", vencidos.size());
        }
        return vencidos.size();
    }

    private Emprestimo buscarEmprestimo(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Empréstimo não encontrado com id " + id));
    }

    private Material buscarMaterialComTrava(Long id) {
        return emprestimoRepository.buscarMaterialComTrava(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material não encontrado com id " + id));
    }
}
