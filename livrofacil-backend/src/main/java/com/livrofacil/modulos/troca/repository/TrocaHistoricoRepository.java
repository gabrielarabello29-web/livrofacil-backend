package com.livrofacil.modulos.troca.repository;

import com.livrofacil.modulos.troca.entity.TrocaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrocaHistoricoRepository extends JpaRepository<TrocaHistorico, Long> {
    List<TrocaHistorico> findByTrocaIdOrderByCriadoEmAsc(Long trocaId);
}