package com.livrofacil.cliente.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AlterarSenhaRequest {

    @NotBlank(message = "A senha atual e obrigatoria")
    private String senhaAtual;

    @NotBlank(message = "A nova senha e obrigatoria")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$", message = "A nova senha deve ter no minimo 8 caracteres, uma letra maiuscula e um caractere especial")
    private String novaSenha;

    @NotBlank(message = "A confirmacao da nova senha e obrigatoria")
    private String confirmarNovaSenha;

    @AssertTrue(message = "A nova senha e a confirmacao devem ser iguais")
    public boolean isSenhasIguais() {
        return novaSenha != null && novaSenha.equals(confirmarNovaSenha);
    }

    public String getSenhaAtual() { return senhaAtual; }
    public void setSenhaAtual(String senhaAtual) { this.senhaAtual = senhaAtual; }
    public String getNovaSenha() { return novaSenha; }
    public void setNovaSenha(String novaSenha) { this.novaSenha = novaSenha; }
    public String getConfirmarNovaSenha() { return confirmarNovaSenha; }
    public void setConfirmarNovaSenha(String confirmarNovaSenha) { this.confirmarNovaSenha = confirmarNovaSenha; }
}