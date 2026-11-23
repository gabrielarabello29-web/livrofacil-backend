package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findFirstByNomeIgnoreCase(String nome);
    List<Categoria> findByNomeContainingIgnoreCase(String nome);
}
