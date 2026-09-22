package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.ItemCarrinho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemCarrinhoRepository extends JpaRepository<ItemCarrinho, Long> {
    Optional<ItemCarrinho> findByIdAndCarrinhoId(Long id, Long carrinhoId);
    Optional<ItemCarrinho> findByCarrinhoIdAndLivroId(Long carrinhoId, Long livroId);
}