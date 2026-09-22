package com.livrofacil.modulos.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ClienteLoginRequest {

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "Informe um e-mail valido")
    private String email;

    @NotBlank(message = "A senha e obrigatoria")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$", message = "A senha deve ter no minimo 8 caracteres, uma letra maiuscula e um caractere especial")
    private String senha;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
