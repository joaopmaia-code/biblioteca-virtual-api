package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    /** Listagem: já traz usuário e material na mesma consulta (evita uma consulta por empréstimo). */
    @EntityGraph(attributePaths = {"usuario", "material"})
    List<Emprestimo> findAllByOrderByIdAsc();

    /** RN01: quantas licenças do material estão em uso. */
    long countByMaterialIdAndStatusIn(Long materialId, Collection<StatusEmprestimo> status);

    /** RN04: quantos empréstimos em uso o usuário tem. */
    long countByUsuarioIdAndStatusIn(Long usuarioId, Collection<StatusEmprestimo> status);

    /** RN03: o usuário já está com este material? */
    boolean existsByUsuarioIdAndMaterialIdAndStatusIn(Long usuarioId, Long materialId, Collection<StatusEmprestimo> status);

    /** RN05: empréstimos ATIVOS com prazo vencido. */
    List<Emprestimo> findByStatusAndDataPrevistaDevolucaoBefore(StatusEmprestimo status, LocalDate data);

    /** Usado ao excluir usuário: ele tem histórico de empréstimos? */
    boolean existsByUsuarioId(Long usuarioId);

    /**
     * Busca o material travando a linha até o fim da transação, para que dois empréstimos
     * ao mesmo tempo não peguem a mesma última licença (RN01).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Material m where m.id = :id")
    Optional<Material> buscarMaterialComTrava(@Param("id") Long id);
}
