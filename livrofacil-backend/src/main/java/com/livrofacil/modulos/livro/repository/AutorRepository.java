package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AutorRepository extends  JpaRepository<Autor, Long> {
}
