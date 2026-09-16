package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.entity.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByLivroId(Long livroId);
}