package com.livrofacil.modulos.troca.repository;

import com.livrofacil.modulos.troca.entity.StatusTroca;
import com.livrofacil.modulos.troca.entity.Troca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TrocaRepository extends JpaRepository<Troca, Long> {
    List<Troca> findAllByOrderByCriadoEmDesc();
    List<Troca> findByPedidoClienteIdOrderByCriadoEmDesc(java.util.UUID clienteId);
    boolean existsByItemPedidoIdAndStatusIn(Long itemPedidoId, Collection<StatusTroca> status);
}