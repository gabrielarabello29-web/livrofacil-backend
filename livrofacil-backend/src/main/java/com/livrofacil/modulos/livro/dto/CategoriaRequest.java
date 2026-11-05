package com.livrofacil.modulos.livro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaRequest {

    @NotBlank(message = "O nome da categoria e obrigatorio")
    @Size(max = 100, message = "O nome da categoria deve ter no maximo 100 caracteres")
    private String nome;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
