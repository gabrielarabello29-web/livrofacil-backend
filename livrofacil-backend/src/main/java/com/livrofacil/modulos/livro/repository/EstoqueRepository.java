package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Optional<Estoque> findByLivroId(Long livroId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Estoque> findWithLockByLivroId(Long livroId);
}