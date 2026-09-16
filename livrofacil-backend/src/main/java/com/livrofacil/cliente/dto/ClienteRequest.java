package com.livrofacil.cliente.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;

public class ClienteRequest {

    private String nome;

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "Informe um e-mail valido")
    private String email;

        @NotBlank(message = "O telefone e obrigatorio")
        @Pattern(
            regexp = "^\\([1-9][0-9]\\) (?:9[0-9]{4}|[0-9]{4})-[0-9]{4}$",
            message = "O telefone deve seguir o formato (DD) 99999-9999 ou (DD) 9999-9999"
        )
    private String telefone;

    @PastOrPresent(message = "A data de nascimento nao pode ser futura")
    private LocalDate dataNascimento;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

}