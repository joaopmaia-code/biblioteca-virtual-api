package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutorRepository extends JpaRepository<Autor, Long> {
}
