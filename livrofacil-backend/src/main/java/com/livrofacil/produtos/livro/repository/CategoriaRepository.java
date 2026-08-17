package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
