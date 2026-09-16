package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.model.Editora;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EditoraRepository extends JpaRepository<Editora, Long> {
}
