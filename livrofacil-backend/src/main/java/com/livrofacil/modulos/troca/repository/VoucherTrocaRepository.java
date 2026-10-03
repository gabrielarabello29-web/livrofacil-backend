package com.livrofacil.modulos.troca.repository;

import com.livrofacil.modulos.troca.entity.VoucherTroca;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoucherTrocaRepository extends JpaRepository<VoucherTroca, Long> {
    Optional<VoucherTroca> findByCodigoIgnoreCase(String codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select voucher from VoucherTroca voucher where upper(voucher.codigo) = upper(:codigo)")
    Optional<VoucherTroca> findByCodigoForUpdate(@Param("codigo") String codigo);
}