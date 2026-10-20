package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.ItemPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {
	Optional<ItemPedido> findByIdAndPedidoId(Long id, Long pedidoId);
	Optional<ItemPedido> findFirstByPedidoIdAndLivroId(Long pedidoId, Long livroId);
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select item from ItemPedido item where item.id = :id and item.pedido.id = :pedidoId")
	Optional<ItemPedido> findByIdAndPedidoIdForUpdate(@Param("id") Long id, @Param("pedidoId") Long pedidoId);
}