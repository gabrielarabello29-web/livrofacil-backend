package com.livrofacil.cliente.repository;

import com.livrofacil.cliente.entity.FormaPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FormaPagamentoRepository extends JpaRepository<FormaPagamento, Long> {
    List<FormaPagamento> findByClienteId(Long clienteId);
    Optional<FormaPagamento> findByIdAndClienteId(Long id, Long clienteId);
}