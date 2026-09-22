package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}
