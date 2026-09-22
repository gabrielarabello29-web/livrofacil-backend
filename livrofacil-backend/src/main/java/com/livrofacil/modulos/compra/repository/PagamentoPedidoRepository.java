package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.PagamentoPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoPedidoRepository extends JpaRepository<PagamentoPedido, Long> {
	boolean existsByFormaPagamentoId(Long formaPagamentoId);
}