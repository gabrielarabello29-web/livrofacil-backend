package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.Editora;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EditoraRepository extends JpaRepository<Editora, Long> {

	Optional<Editora> findFirstByNomeIgnoreCase(String nome);

	List<Editora> findByNomeContainingIgnoreCase(String nome);
}
