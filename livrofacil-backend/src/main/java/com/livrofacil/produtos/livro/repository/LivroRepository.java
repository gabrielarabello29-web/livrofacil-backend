package com.livrofacil.produtos.livro.repository;

import com.livrofacil.produtos.livro.entity.Livro;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface LivroRepository extends JpaRepository<Livro, Long> {

    Optional<Livro> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    Optional<Livro> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    Optional<Livro> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarrasAndIdNot(String codigoBarras, Long id);

    List<Livro> findByTituloContainingIgnoreCase(String titulo);

    List<Livro> findByAtivo(Boolean ativo);

    List<Livro> findByAtivoTrue();
}
