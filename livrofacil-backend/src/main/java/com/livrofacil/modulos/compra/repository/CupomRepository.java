package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.Cupom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CupomRepository extends JpaRepository<Cupom, Long> {
    Optional<Cupom> findByCodigoIgnoreCaseAndAtivoTrue(String codigo);
}