package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.ItemPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
	Optional<ItemPedido> findByIdAndPedidoId(Long id, Long pedidoId);
	Optional<ItemPedido> findFirstByPedidoIdAndLivroId(Long pedidoId, Long livroId);
}