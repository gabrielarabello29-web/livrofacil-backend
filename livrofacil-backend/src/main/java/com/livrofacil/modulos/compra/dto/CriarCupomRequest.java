package com.livrofacil.modulos.compra.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CriarCupomRequest {
    @NotBlank(message = "O codigo do cupom e obrigatorio")
    private String codigo;

    private String tipoDesconto = "PERCENTUAL";

    @DecimalMin(value = "0.01", message = "O percentual de desconto deve ser maior que zero")
    @DecimalMax(value = "100.00", message = "O percentual de desconto nao pode exceder 100")
    private BigDecimal percentualDesconto;

    @DecimalMin(value = "0.01", message = "O valor fixo do cupom deve ser maior que zero")
    private BigDecimal valorDesconto;

    private LocalDate dataFimVigencia;

    @DecimalMin(value = "1", message = "O numero de usos maximos deve ser no minimo 1")
    private Integer numeroUsoMaximo;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getTipoDesconto() { return tipoDesconto; }
    public void setTipoDesconto(String tipoDesconto) { this.tipoDesconto = tipoDesconto; }
    public BigDecimal getPercentualDesconto() { return percentualDesconto; }
    public void setPercentualDesconto(BigDecimal percentualDesconto) { this.percentualDesconto = percentualDesconto; }
    public BigDecimal getValorDesconto() { return valorDesconto; }
    public void setValorDesconto(BigDecimal valorDesconto) { this.valorDesconto = valorDesconto; }
    public LocalDate getDataFimVigencia() { return dataFimVigencia; }
    public void setDataFimVigencia(LocalDate dataFimVigencia) { this.dataFimVigencia = dataFimVigencia; }
    public Integer getNumeroUsoMaximo() { return numeroUsoMaximo; }
    public void setNumeroUsoMaximo(Integer numeroUsoMaximo) { this.numeroUsoMaximo = numeroUsoMaximo; }
}
