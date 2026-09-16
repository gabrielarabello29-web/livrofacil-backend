package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
public interface LivroRepository extends JpaRepository<Livro, Long> {

    Optional<Livro> findByCodigo(String codigo);

    Optional<Livro> findByIsbn(String isbn);

    Optional<Livro> findByCodigoBarras(String codigoBarras);

    List<Livro> findByTituloContainingIgnoreCase(String titulo);

    List<Livro> findByAtivo(Boolean ativo);
}
