package com.livrofacil.modulos.compra.service;

import com.livrofacil.modulos.compra.dto.ValidarCupomResponse;
import com.livrofacil.modulos.compra.repository.CupomRepository;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CupomService {
    private final CupomRepository cupomRepository;
    private final VoucherTrocaRepository voucherRepository;

    public CupomService(CupomRepository cupomRepository, VoucherTrocaRepository voucherRepository) {
        this.cupomRepository = cupomRepository;
        this.voucherRepository = voucherRepository;
    }

    @Transactional(readOnly = true)
    public ValidarCupomResponse validar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return new ValidarCupomResponse(false, null, null, codigo, "Informe um codigo de cupom");
        }
        var codigoNormalizado = codigo.trim();
        var cupom = cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue(codigoNormalizado);
        if (cupom.isPresent()) {
            return new ValidarCupomResponse(true, "PERCENTUAL", cupom.get().getPercentualDesconto(),
                cupom.get().getCodigo(), "Cupom valido");
        }
        return voucherRepository.findByCodigoIgnoreCase(codigoNormalizado)
            .filter(voucher -> voucher.getResgatadoEm() == null)
            .map(voucher -> new ValidarCupomResponse(true, "FIXO", voucher.getValor(), voucher.getCodigo(), "Voucher valido"))
            .orElseGet(() -> new ValidarCupomResponse(false, null, null, codigoNormalizado, "Cupom invalido, inativo ou ja utilizado"));
    }
}