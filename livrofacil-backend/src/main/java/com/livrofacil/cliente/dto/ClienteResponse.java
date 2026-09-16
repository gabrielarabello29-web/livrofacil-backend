package com.livrofacil.cliente.dto;

import com.livrofacil.cliente.entity.Cliente;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class ClienteResponse {

    private Long id;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDateTime dataCadastro;
    private LocalDate dataNascimento;
    private String genero;
    private Boolean ativo;
    private String perfil;

    public ClienteResponse(Cliente cliente) {
        this.id = cliente.getId();
        this.nome = cliente.getNome();
        this.email = cliente.getEmail();
        this.cpf = formatarCpf(cliente.getCpf());
        this.telefone = cliente.getTelefone();
        this.dataCadastro = cliente.getDataCadastro();
        this.dataNascimento = cliente.getDataNascimento();
        this.genero = cliente.getGenero();
        this.ativo = "S".equals(cliente.getAtivo());
        this.perfil = cliente.getPerfil();
    }

    private String formatarCpf(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return cpf;
        }

        String apenasNumeros = cpf.replaceAll("\\D", "");
        if (apenasNumeros.length() != 11) {
            return cpf;
        }

        return apenasNumeros.substring(0, 3) + "." + apenasNumeros.substring(3, 6) + "." + apenasNumeros.substring(6, 9) + "-" + apenasNumeros.substring(9);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getCpf() {
        return cpf;
    }

    public String getTelefone() {
        return telefone;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getGenero() {
        return genero;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public String getPerfil() {
        return perfil;
    }
}