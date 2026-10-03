package com.livrofacil.modulos.compra.service;

import com.livrofacil.modulos.compra.repository.CupomRepository;
import com.livrofacil.modulos.troca.entity.VoucherTroca;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CupomServiceTest {
    @Mock private CupomRepository cupomRepository;
    @Mock private VoucherTrocaRepository voucherRepository;

    @Test
    void deveValidarVoucherDeValorFixo() {
        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo("TR-ABC123");
        voucher.setValor(new BigDecimal("25.00"));
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("TR-ABC123")).thenReturn(Optional.empty());
        when(voucherRepository.findByCodigoIgnoreCase("TR-ABC123")).thenReturn(Optional.of(voucher));

        var response = new CupomService(cupomRepository, voucherRepository).validar(" TR-ABC123 ");

        assertTrue(response.isValido());
        assertEquals("FIXO", response.getTipo());
        assertEquals(new BigDecimal("25.00"), response.getValor());
    }

    @Test
    void naoDeveValidarVoucherJaResgatado() {
        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo("TR-ABC123");
        voucher.setValor(new BigDecimal("25.00"));
        voucher.setResgatadoEm(LocalDateTime.now());
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("TR-ABC123")).thenReturn(Optional.empty());
        when(voucherRepository.findByCodigoIgnoreCase("TR-ABC123")).thenReturn(Optional.of(voucher));

        var response = new CupomService(cupomRepository, voucherRepository).validar("TR-ABC123");

        assertFalse(response.isValido());
    }
}