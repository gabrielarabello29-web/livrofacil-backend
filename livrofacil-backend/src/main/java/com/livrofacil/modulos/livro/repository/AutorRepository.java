package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AutorRepository extends  JpaRepository<Autor, Long> {

	Optional<Autor> findFirstByNomeIgnoreCase(String nome);

	List<Autor> findByNomeContainingIgnoreCase(String nome);
}
