package com.livrofacil.modulos.cliente.repository;

import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BandeiraPagamentoRepository extends JpaRepository<BandeiraPagamento, Long> {
    Optional<BandeiraPagamento> findByNomeIgnoreCase(String nome);
}
