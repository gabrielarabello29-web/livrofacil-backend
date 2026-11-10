package com.livrofacil.modulos.troca.repository;

import com.livrofacil.modulos.troca.entity.StatusTroca;
import com.livrofacil.modulos.troca.entity.Troca;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TrocaRepository extends JpaRepository<Troca, Long> {
    List<Troca> findAllByOrderByCriadoEmDesc();
    List<Troca> findByPedidoClienteIdOrderByCriadoEmDesc(java.util.UUID clienteId);
    boolean existsByItemPedidoIdAndStatusIn(Long itemPedidoId, Collection<StatusTroca> status);
    List<Troca> findByItemPedidoIdAndStatusIn(Long itemPedidoId, Collection<StatusTroca> status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select troca from Troca troca where troca.id = :id")
    java.util.Optional<Troca> findByIdForUpdate(@Param("id") Long id);
}