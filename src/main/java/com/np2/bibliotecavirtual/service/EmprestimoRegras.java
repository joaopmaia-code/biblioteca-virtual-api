package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Regras de empréstimo (RN01–RN04) separadas do EmprestimoService para a fila de espera
 * (RN11, Pessoa 1) poder reaproveitar: ao atender uma reserva, valem as mesmas regras.
 * <p>
 * Um empréstimo "em uso" ocupa licença: status ATIVO ou ATRASADO.
 */
@Component
public class EmprestimoRegras {

    public static final List<StatusEmprestimo> EM_USO = List.of(StatusEmprestimo.ATIVO, StatusEmprestimo.ATRASADO);

    private final EmprestimoRepository emprestimoRepository;
    private final int prazoDias;
    private final int maxAtivos;

    /** RN02 e RN04: os valores podem ser trocados no application.properties sem mexer no código. */
    public EmprestimoRegras(EmprestimoRepository emprestimoRepository,
                            @Value("${biblioteca.emprestimo.prazo-dias:14}") int prazoDias,
                            @Value("${biblioteca.emprestimo.max-ativos:3}") int maxAtivos) {
        this.emprestimoRepository = emprestimoRepository;
        this.prazoDias = prazoDias;
        this.maxAtivos = maxAtivos;
    }

    public int getMaxAtivos() {
        return maxAtivos;
    }

    /** RN01: licenças do material menos as que estão em uso. */
    public long licencasDisponiveis(Material material) {
        return material.getQuantidadeLicencas()
                - emprestimoRepository.countByMaterialIdAndStatusIn(material.getId(), EM_USO);
    }

    /** RN03: o usuário já está com este material? */
    public boolean jaTemEmprestado(Usuario usuario, Material material) {
        return emprestimoRepository.existsByUsuarioIdAndMaterialIdAndStatusIn(usuario.getId(), material.getId(), EM_USO);
    }

    /** RN04: o usuário já tem o máximo de empréstimos em uso? */
    public boolean atingiuLimite(Usuario usuario) {
        return emprestimoRepository.countByUsuarioIdAndStatusIn(usuario.getId(), EM_USO) >= maxAtivos;
    }

    /** Para a fila (RN11): o usuário pode receber este material agora? */
    public boolean podeReceber(Usuario usuario, Material material) {
        return !jaTemEmprestado(usuario, material) && !atingiuLimite(usuario);
    }

    /** Cria o empréstimo ATIVO. RN02: sem data informada, usa o prazo padrão. */
    public Emprestimo registrar(Usuario usuario, Material material, LocalDate dataPrevista) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setUsuario(usuario);
        emprestimo.setMaterial(material);
        emprestimo.setDataRetirada(LocalDateTime.now());
        emprestimo.setDataPrevistaDevolucao(dataPrevista != null ? dataPrevista : LocalDate.now().plusDays(prazoDias));
        emprestimo.setStatus(StatusEmprestimo.ATIVO);
        return emprestimoRepository.save(emprestimo);
    }
}
