package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {
    Optional<Carrinho> findByToken(String token);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Carrinho> findByIdAndClienteId(Long id, UUID clienteId);
}