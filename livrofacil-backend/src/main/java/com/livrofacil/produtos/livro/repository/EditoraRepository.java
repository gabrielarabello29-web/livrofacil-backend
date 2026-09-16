package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.entity.Editora;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EditoraRepository extends JpaRepository<Editora, Long> {
}
