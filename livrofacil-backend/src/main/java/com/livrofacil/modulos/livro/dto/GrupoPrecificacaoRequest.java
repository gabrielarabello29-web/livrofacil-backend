package com.livrofacil.modulos.livro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class GrupoPrecificacaoRequest {

    @NotBlank(message = "O nome do grupo de precificacao e obrigatorio")
    @Size(max = 100, message = "O nome do grupo de precificacao deve ter no maximo 100 caracteres")
    private String nome;

    @DecimalMin(value = "0.0", inclusive = false, message = "O percentual de margem deve ser maior que zero")
    private BigDecimal percentualMargem;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public BigDecimal getPercentualMargem() {
        return percentualMargem;
    }

    public void setPercentualMargem(BigDecimal percentualMargem) {
        this.percentualMargem = percentualMargem;
    }
}
