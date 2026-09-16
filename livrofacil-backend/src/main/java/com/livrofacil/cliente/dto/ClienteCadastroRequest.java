package com.livrofacil.cliente.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ClienteCadastroRequest {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(max = 100, message = "O nome deve ter no maximo 100 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "Informe um e-mail valido")
    @Size(max = 50, message = "O e-mail deve ter no maximo 50 caracteres")
    private String email;

    @NotBlank(message = "O CPF e obrigatorio")
    @Pattern(regexp = "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$", message = "O CPF deve seguir o formato 123.456.789-09")
    private String cpf;

    @NotBlank(message = "O telefone e obrigatorio")
    @Pattern(regexp = "^\\([1-9][0-9]\\) (?:9[0-9]{4}|[0-9]{4})-[0-9]{4}$", message = "O telefone deve seguir o formato (DD) 99999-9999 ou (DD) 9999-9999")
    private String telefone;

    @NotNull(message = "A data de nascimento e obrigatoria")
    @PastOrPresent(message = "A data de nascimento nao pode ser futura")
    private LocalDate dataNascimento;

    @NotBlank(message = "O genero e obrigatorio")
    @Pattern(regexp = "^(MASCULINO|FEMININO|OUTRO|PREFIRO_NAO_INFORMAR)$", message = "Genero invalido")
    private String genero;

    @NotBlank(message = "A senha e obrigatoria")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$", message = "A senha deve ter no minimo 8 caracteres, uma letra maiuscula e um caractere especial")
    private String senha;

    @NotBlank(message = "A confirmacao da senha e obrigatoria")
    private String confirmarSenha;

    @Valid
    @NotNull(message = "O endereco e obrigatorio no cadastro")
    private EnderecoRequest endereco;

    @AssertTrue(message = "A senha e a confirmacao devem ser iguais")
    public boolean isSenhasIguais() {
        return senha != null && senha.equals(confirmarSenha);
    }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = formatarCpf(cpf); }

    private String formatarCpf(String cpf) {
        if (cpf == null) {
            return null;
        }

        String apenasNumeros = cpf.replaceAll("\\D", "");
        if (apenasNumeros.length() > 11) {
            apenasNumeros = apenasNumeros.substring(0, 11);
        }

        if (apenasNumeros.length() <= 3) {
            return apenasNumeros;
        }
        if (apenasNumeros.length() <= 6) {
            return apenasNumeros.substring(0, 3) + "." + apenasNumeros.substring(3);
        }
        if (apenasNumeros.length() <= 9) {
            return apenasNumeros.substring(0, 3) + "." + apenasNumeros.substring(3, 6) + "." + apenasNumeros.substring(6);
        }

        return apenasNumeros.substring(0, 3) + "." + apenasNumeros.substring(3, 6) + "." + apenasNumeros.substring(6, 9) + "-" + apenasNumeros.substring(9);
    }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public String getConfirmarSenha() { return confirmarSenha; }
    public void setConfirmarSenha(String confirmarSenha) { this.confirmarSenha = confirmarSenha; }
    public EnderecoRequest getEndereco() { return endereco; }
    public void setEndereco(EnderecoRequest endereco) { this.endereco = endereco; }
}