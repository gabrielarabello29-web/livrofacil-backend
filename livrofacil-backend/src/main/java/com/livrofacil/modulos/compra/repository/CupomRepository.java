package com.livrofacil.modulos.compra.repository;

import com.livrofacil.modulos.compra.entity.Cupom;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CupomRepository extends JpaRepository<Cupom, Long> {
    Optional<Cupom> findByCodigoIgnoreCaseAndAtivoTrue(String codigo);
    Optional<Cupom> findByCodigoIgnoreCase(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select cupom from Cupom cupom where upper(cupom.codigo) = upper(:codigo)")
    Optional<Cupom> findByCodigoIgnoreCaseForUpdate(@Param("codigo") String codigo);
}