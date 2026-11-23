package com.livrofacil.modulos.compra.service;

import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.compra.dto.CriarCupomRequest;
import com.livrofacil.modulos.compra.dto.CupomResponse;
import com.livrofacil.modulos.compra.dto.ValidarCupomResponse;
import com.livrofacil.modulos.compra.entity.Cupom;
import com.livrofacil.modulos.compra.repository.CupomRepository;
import com.livrofacil.modulos.troca.repository.VoucherTrocaRepository;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
public class CupomService {
    private final CupomRepository cupomRepository;
    private final VoucherTrocaRepository voucherRepository;

    public CupomService(CupomRepository cupomRepository, VoucherTrocaRepository voucherRepository) {
        this.cupomRepository = cupomRepository;
        this.voucherRepository = voucherRepository;
    }

    @Transactional
    public CupomResponse criar(CriarCupomRequest request) {
        if (request == null || request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new RegraDeNegocioException("O codigo do cupom e obrigatorio");
        }
        String codigoNormalizado = request.getCodigo().trim();
        if (codigoNormalizado.length() < 3) {
            throw new RegraDeNegocioException("O codigo do cupom deve ter no minimo 3 caracteres");
        }
        String tipoDesconto = normalizarTipoDesconto(request.getTipoDesconto());
        BigDecimal percentual = request.getPercentualDesconto();
        BigDecimal valor = request.getValorDesconto();
        if ("PERCENTUAL".equalsIgnoreCase(tipoDesconto)) {
            if (percentual == null) {
                throw new RegraDeNegocioException("O percentual de desconto e obrigatorio para cupom percentual");
            }
            if (percentual.compareTo(BigDecimal.ZERO) <= 0 || percentual.compareTo(new BigDecimal("100.00")) > 0) {
                throw new RegraDeNegocioException("O percentual de desconto deve estar entre 0,01 e 100");
            }
        } else if ("FIXO".equalsIgnoreCase(tipoDesconto)) {
            if (valor == null) {
                throw new RegraDeNegocioException("O valor fixo do cupom e obrigatorio para cupom em reais");
            }
            if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RegraDeNegocioException("O valor fixo do cupom deve ser maior que zero");
            }
        } else {
            throw new RegraDeNegocioException("Tipo de desconto invalido. Use PERCENTUAL ou FIXO");
        }
        if (request.getNumeroUsoMaximo() != null && request.getNumeroUsoMaximo() <= 0) {
            throw new RegraDeNegocioException("O numero de usos maximos deve ser maior que zero");
        }
        if (request.getDataFimVigencia() != null && request.getDataFimVigencia().isBefore(LocalDate.now())) {
            throw new RegraDeNegocioException("A data final de vigencia nao pode ser anterior a hoje");
        }
        if (cupomRepository.findByCodigoIgnoreCase(codigoNormalizado).isPresent()) {
            throw new RegraDeNegocioException("Ja existe um cupom com este codigo");
        }

        Cupom cupom = new Cupom();
        cupom.setCodigo(codigoNormalizado.toUpperCase(Locale.ROOT));
        cupom.setTipoDesconto(tipoDesconto.toUpperCase(Locale.ROOT));
        cupom.setPercentualDesconto(percentual == null ? null : percentual.setScale(2, java.math.RoundingMode.HALF_UP));
        cupom.setValorDesconto(valor == null ? null : valor.setScale(2, java.math.RoundingMode.HALF_UP));
        cupom.setDataFimVigencia(request.getDataFimVigencia());
        cupom.setNumeroUsoMaximo(request.getNumeroUsoMaximo());
        cupom.setNumeroUsoAtual(0);
        cupom.setAtivo(true);
        return new CupomResponse(cupomRepository.save(cupom));
    }

    @Transactional(readOnly = true)
    public List<CupomResponse> listar() {
        return cupomRepository.findAll().stream().map(CupomResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public CupomResponse buscarPorId(Long id) {
        return new CupomResponse(cupomRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom nao encontrado: " + id)));
    }

    @Transactional
    public CupomResponse atualizar(Long id, CriarCupomRequest request) {
        Cupom cupom = cupomRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom nao encontrado: " + id));
        if (request == null || request.getCodigo() == null || request.getCodigo().isBlank()) {
            throw new RegraDeNegocioException("O codigo do cupom e obrigatorio");
        }
        String codigoNormalizado = request.getCodigo().trim();
        if (codigoNormalizado.length() < 3) {
            throw new RegraDeNegocioException("O codigo do cupom deve ter no minimo 3 caracteres");
        }
        cupomRepository.findByCodigoIgnoreCase(codigoNormalizado)
            .filter(outro -> !outro.getId().equals(id))
            .ifPresent(outro -> { throw new RegraDeNegocioException("Ja existe um cupom com este codigo"); });

        String tipoDesconto = normalizarTipoDesconto(request.getTipoDesconto());
        BigDecimal percentual = request.getPercentualDesconto();
        BigDecimal valor = request.getValorDesconto();
        if ("PERCENTUAL".equalsIgnoreCase(tipoDesconto)) {
            if (percentual == null) throw new RegraDeNegocioException("O percentual de desconto e obrigatorio para cupom percentual");
            if (percentual.compareTo(BigDecimal.ZERO) <= 0 || percentual.compareTo(new BigDecimal("100.00")) > 0) {
                throw new RegraDeNegocioException("O percentual de desconto deve estar entre 0,01 e 100");
            }
            cupom.setValorDesconto(null);
            cupom.setPercentualDesconto(percentual.setScale(2, java.math.RoundingMode.HALF_UP));
        } else if ("FIXO".equalsIgnoreCase(tipoDesconto)) {
            if (valor == null) throw new RegraDeNegocioException("O valor fixo do cupom e obrigatorio para cupom em reais");
            if (valor.compareTo(BigDecimal.ZERO) <= 0) throw new RegraDeNegocioException("O valor fixo do cupom deve ser maior que zero");
            cupom.setPercentualDesconto(null);
            cupom.setValorDesconto(valor.setScale(2, java.math.RoundingMode.HALF_UP));
        }
        cupom.setCodigo(codigoNormalizado.toUpperCase(Locale.ROOT));
        cupom.setTipoDesconto(tipoDesconto.toUpperCase(Locale.ROOT));
        cupom.setDataFimVigencia(request.getDataFimVigencia());
        if (request.getNumeroUsoMaximo() != null && request.getNumeroUsoMaximo() <= 0) {
            throw new RegraDeNegocioException("O numero de usos maximos deve ser maior que zero");
        }
        cupom.setNumeroUsoMaximo(request.getNumeroUsoMaximo());
        if (cupom.getNumeroUsoMaximo() != null && cupom.getNumeroUsoAtual() != null && cupom.getNumeroUsoAtual() >= cupom.getNumeroUsoMaximo()) {
            cupom.setAtivo(false);
        }
        return new CupomResponse(cupomRepository.save(cupom));
    }

    @Transactional
    public void inativar(Long id) {
        Cupom cupom = cupomRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom nao encontrado: " + id));
        cupom.setAtivo(false);
        cupomRepository.save(cupom);
    }

    @Transactional
    public void ativar(Long id) {
        Cupom cupom = cupomRepository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cupom nao encontrado: " + id));
        if (cupom.getDataFimVigencia() != null && LocalDate.now().isAfter(cupom.getDataFimVigencia())) {
            throw new RegraDeNegocioException("Nao e possivel ativar um cupom expirado");
        }
        cupom.setAtivo(true);
        cupomRepository.save(cupom);
    }

    @Transactional
    public void registrarUso(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new RegraDeNegocioException("Informe um codigo de cupom");
        }
        Cupom cupom = cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue(codigo.trim())
            .orElseThrow(() -> new RegraDeNegocioException("Cupom invalido ou inativo"));
        cupom.registrarUso();
        cupomRepository.save(cupom);
    }

    @Transactional(readOnly = true)
    public ValidarCupomResponse validar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return new ValidarCupomResponse(false, null, null, codigo, "Informe um codigo de cupom");
        }
        var codigoNormalizado = codigo.trim();
        var cupomAtivo = cupomRepository.findByCodigoIgnoreCaseAndAtivoTrue(codigoNormalizado)
            .or(() -> cupomRepository.findByCodigoIgnoreCase(codigoNormalizado));
        if (cupomAtivo.isPresent()) {
            Cupom cupom = cupomAtivo.get();
            if (!cupomValido(cupom)) {
                return new ValidarCupomResponse(false, null, null, codigoNormalizado, "Cupom invalido, inativo ou expirado");
            }
            if ("FIXO".equalsIgnoreCase(cupom.getTipoDesconto())) {
                return new ValidarCupomResponse(true, "FIXO", cupom.getValorDesconto(), cupom.getCodigo(), "Cupom valido");
            }
            return new ValidarCupomResponse(true, "PERCENTUAL", cupom.getPercentualDesconto(), cupom.getCodigo(), "Cupom valido");
        }
        return voucherRepository.findByCodigoIgnoreCase(codigoNormalizado)
            .filter(voucher -> voucher.getResgatadoEm() == null
                && voucher.getValor() != null
                && voucher.getValor().compareTo(BigDecimal.ZERO) > 0)
            .map(voucher -> new ValidarCupomResponse(true, "FIXO", voucher.getValor(), voucher.getCodigo(), "Voucher valido"))
            .orElseGet(() -> new ValidarCupomResponse(false, null, null, codigoNormalizado, "Cupom invalido, inativo ou ja utilizado"));
    }

    private String normalizarTipoDesconto(String tipoDesconto) {
        if (tipoDesconto == null || tipoDesconto.isBlank()) {
            return "PERCENTUAL";
        }
        if ("FIXO".equalsIgnoreCase(tipoDesconto) || "VALOR_FIXO".equalsIgnoreCase(tipoDesconto) || "$".equals(tipoDesconto)) {
            return "FIXO";
        }
        if ("PERCENTUAL".equalsIgnoreCase(tipoDesconto) || "%".equals(tipoDesconto) || "PORCENTAGEM".equalsIgnoreCase(tipoDesconto)) {
            return "PERCENTUAL";
        }
        return tipoDesconto.trim().toUpperCase(Locale.ROOT);
    }

    private boolean cupomValido(Cupom cupom) {
        if (cupom.getAtivo() != null && !cupom.getAtivo()) {
            return false;
        }
        if (cupom.getDataFimVigencia() != null && LocalDate.now().isAfter(cupom.getDataFimVigencia())) {
            return false;
        }
        if (cupom.getNumeroUsoMaximo() != null && cupom.getNumeroUsoAtual() != null && cupom.getNumeroUsoAtual() >= cupom.getNumeroUsoMaximo()) {
            return false;
        }
        return true;
    }
}