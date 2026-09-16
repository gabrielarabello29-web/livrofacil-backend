package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AutorRepository extends  JpaRepository<Autor, Long> {
}
