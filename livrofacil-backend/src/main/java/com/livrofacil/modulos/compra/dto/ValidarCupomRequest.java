package com.livrofacil.modulos.compra.dto;

import jakarta.validation.constraints.NotBlank;

public class ValidarCupomRequest {
    @NotBlank(message = "O codigo do cupom e obrigatorio")
    private String codigo;

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}