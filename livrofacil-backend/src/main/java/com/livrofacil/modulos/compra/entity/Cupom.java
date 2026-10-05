package com.livrofacil.modulos.compra.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cupom")
public class Cupom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cup_id")
    private Long id;

    @Column(name = "cup_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "cup_tipo_desconto", nullable = false, length = 20)
    private String tipoDesconto = "PERCENTUAL";

    @Column(name = "cup_percentual_desconto", precision = 5, scale = 2)
    private BigDecimal percentualDesconto;

    @Column(name = "cup_valor_desconto", precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "cup_data_fim_vigencia")
    private LocalDate dataFimVigencia;

    @Column(name = "cup_numero_uso_maximo")
    private Integer numeroUsoMaximo;

    @Column(name = "cup_numero_uso_atual")
    private Integer numeroUsoAtual = 0;

    @Column(name = "cup_ativo", nullable = false)
    private Boolean ativo = true;

    public Long getId() { return id; }
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
    public Integer getNumeroUsoAtual() { return numeroUsoAtual; }
    public void setNumeroUsoAtual(Integer numeroUsoAtual) { this.numeroUsoAtual = numeroUsoAtual; }
    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public boolean isDisponivel() {
        if (this.ativo != null && !Boolean.TRUE.equals(this.ativo)) {
            return false;
        }
        if (this.dataFimVigencia != null && LocalDate.now().isAfter(this.dataFimVigencia)) {
            return false;
        }
        if (this.numeroUsoMaximo != null && this.numeroUsoAtual != null && this.numeroUsoAtual >= this.numeroUsoMaximo) {
            return false;
        }
        return true;
    }

    public void registrarUso() {
        if (this.numeroUsoAtual == null) {
            this.numeroUsoAtual = 0;
        }
        this.numeroUsoAtual = this.numeroUsoAtual + 1;
        if (this.numeroUsoMaximo != null && this.numeroUsoAtual >= this.numeroUsoMaximo) {
            this.ativo = false;
        }
    }
}