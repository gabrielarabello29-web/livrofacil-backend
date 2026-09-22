package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.Editora;
import org.springframework.data.jpa.repository.JpaRepository;
public interface EditoraRepository extends JpaRepository<Editora, Long> {
}
