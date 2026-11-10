package com.livrofacil.modulos.compra.service;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.compra.dto.CriarCupomRequest;
import com.livrofacil.modulos.compra.entity.Cupom;
import com.livrofacil.modulos.compra.repository.CupomRepository;
import com.livrofacil.modulos.troca.entity.VoucherTroca;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        assertNull(voucher.getResgatadoEm());
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

    @Test
    void naoDeveValidarVoucherComSaldoZerado() {
        VoucherTroca voucher = new VoucherTroca();
        voucher.setCodigo("TR-ZERADO");
        voucher.setValor(BigDecimal.ZERO);
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("TR-ZERADO")).thenReturn(Optional.empty());
        when(voucherRepository.findByCodigoIgnoreCase("TR-ZERADO")).thenReturn(Optional.of(voucher));

        var response = new CupomService(cupomRepository, voucherRepository).validar("TR-ZERADO");

        assertFalse(response.isValido());
        assertNull(voucher.getResgatadoEm());
    }

    @Test
    void validarCupomNaoRegistraUso() {
        Cupom cupom = new Cupom();
        cupom.setCodigo("DESC10");
        cupom.setTipoDesconto("PERCENTUAL");
        cupom.setPercentualDesconto(new BigDecimal("10.00"));
        cupom.setNumeroUsoAtual(0);
        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("DESC10")).thenReturn(Optional.of(cupom));

        var response = new CupomService(cupomRepository, voucherRepository).validar("DESC10");

        assertTrue(response.isValido());
        assertEquals(0, cupom.getNumeroUsoAtual());
    }

    @Test
    void deveCriarCupomComValorFixoEConfigExtras() {
        CriarCupomRequest request = new CriarCupomRequest();
        request.setCodigo("DESC20");
        request.setTipoDesconto("FIXO");
        request.setValorDesconto(new BigDecimal("20.00"));
        request.setDataFimVigencia(LocalDate.now().plusDays(30));
        request.setNumeroUsoMaximo(5);

        when(cupomRepository.findByCodigoIgnoreCase(any())).thenReturn(Optional.empty());
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = new CupomService(cupomRepository, voucherRepository).criar(request);

        assertEquals("DESC20", response.getCodigo());
        assertEquals("FIXO", response.getTipoDesconto());
        assertEquals(new BigDecimal("20.00"), response.getValorDesconto());
        assertEquals(5, response.getNumeroUsoMaximo());
        assertTrue(response.getAtivo());
    }

    @Test
    void deveInativarCupomQuandoUsoMaximoForAtingido() {
        Cupom cupom = new Cupom();
        cupom.setCodigo("DESC10");
        cupom.setTipoDesconto("PERCENTUAL");
        cupom.setPercentualDesconto(new BigDecimal("10.00"));
        cupom.setNumeroUsoAtual(2);
        cupom.setNumeroUsoMaximo(2);
        cupom.setAtivo(true);

        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("DESC10")).thenReturn(Optional.of(cupom));
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> invocation.getArgument(0));

        new CupomService(cupomRepository, voucherRepository).registrarUso("DESC10");

        assertFalse(cupom.getAtivo());
        assertEquals(3, cupom.getNumeroUsoAtual());
    }

    @Test
    void deveRejeitarCupomExpirado() {
        Cupom cupom = new Cupom();
        cupom.setCodigo("EXPIRADO");
        cupom.setTipoDesconto("PERCENTUAL");
        cupom.setPercentualDesconto(new BigDecimal("15.00"));
        cupom.setDataFimVigencia(LocalDate.now().minusDays(1));
        cupom.setAtivo(true);

        when(cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue("EXPIRADO")).thenReturn(Optional.of(cupom));

        var response = new CupomService(cupomRepository, voucherRepository).validar("EXPIRADO");

        assertFalse(response.isValido());
    }
}