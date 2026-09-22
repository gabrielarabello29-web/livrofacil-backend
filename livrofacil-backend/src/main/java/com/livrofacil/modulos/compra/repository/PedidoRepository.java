package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.Pedido;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Collection;
import java.util.UUID;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteIdOrderByCriadoEmDesc(UUID clienteId);
    Optional<Pedido> findByIdAndClienteId(Long id, UUID clienteId);
    Optional<Pedido> findFirstByCarrinhoIdAndClienteIdAndStatusInOrderByCriadoEmDesc(
                Long carrinhoId, UUID clienteId, Collection<StatusPedido> status);
            List<Pedido> findByCarrinhoIdAndClienteId(Long carrinhoId, UUID clienteId);
    List<Pedido> findByStatusAndReservaExpiraEmBefore(StatusPedido status, LocalDateTime data);
    List<Pedido> findByStatusInAndAtualizadoEmBefore(Collection<StatusPedido> status, LocalDateTime data);
    boolean existsByClienteIdAndStatusIn(UUID clienteId, Collection<StatusPedido> status);
}