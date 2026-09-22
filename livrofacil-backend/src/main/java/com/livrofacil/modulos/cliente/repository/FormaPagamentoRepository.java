package com.livrofacil.modulos.cliente.repository;

import com.livrofacil.modulos.cliente.entity.FormaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {
    List<FormaPagamento> findByClienteIdOrderByCriadoEmDesc(UUID clienteId);
    List<FormaPagamento> findByClienteIdOrderByCriadoEmAsc(UUID clienteId);
    Optional<FormaPagamento> findByIdAndClienteId(Long id, UUID clienteId);
}