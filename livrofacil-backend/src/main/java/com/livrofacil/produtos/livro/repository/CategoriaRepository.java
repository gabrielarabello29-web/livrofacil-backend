package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
