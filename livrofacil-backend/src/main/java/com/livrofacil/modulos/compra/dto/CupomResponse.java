package com.livrofacil.modulos.compra.dto;

import com.livrofacil.modulos.compra.entity.Cupom;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CupomResponse {
    private Long id;
    private String codigo;
    private String tipoDesconto;
    private BigDecimal percentualDesconto;
    private BigDecimal valorDesconto;
    private LocalDate dataFimVigencia;
    private Integer numeroUsoMaximo;
    private Integer numeroUsoAtual;
    private Boolean ativo;

    public CupomResponse(Cupom cupom) {
        this.id = cupom.getId();
        this.codigo = cupom.getCodigo();
        this.tipoDesconto = cupom.getTipoDesconto();
        this.percentualDesconto = cupom.getPercentualDesconto();
        this.valorDesconto = cupom.getValorDesconto();
        this.dataFimVigencia = cupom.getDataFimVigencia();
        this.numeroUsoMaximo = cupom.getNumeroUsoMaximo();
        this.numeroUsoAtual = cupom.getNumeroUsoAtual();
        this.ativo = cupom.getAtivo();
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getTipoDesconto() { return tipoDesconto; }
    public BigDecimal getPercentualDesconto() { return percentualDesconto; }
    public BigDecimal getValorDesconto() { return valorDesconto; }
    public LocalDate getDataFimVigencia() { return dataFimVigencia; }
    public Integer getNumeroUsoMaximo() { return numeroUsoMaximo; }
    public Integer getNumeroUsoAtual() { return numeroUsoAtual; }
    public Boolean getAtivo() { return ativo; }
}
