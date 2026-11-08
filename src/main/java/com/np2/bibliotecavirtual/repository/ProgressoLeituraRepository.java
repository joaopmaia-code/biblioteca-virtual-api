package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.ProgressoLeitura;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProgressoLeituraRepository extends JpaRepository<ProgressoLeitura, Long> {

    Optional<ProgressoLeitura> findByEmprestimoId(Long emprestimoId);

    /** Leituras do usuário: o progresso de vários empréstimos numa consulta só. */
    List<ProgressoLeitura> findByEmprestimoIdIn(Collection<Long> emprestimoIds);
}
