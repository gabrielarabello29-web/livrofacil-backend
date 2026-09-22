package com.livrofacil.modulos.cliente.dto;

import com.livrofacil.modulos.cliente.entity.Cliente;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;

public class ClienteResponse {

    private UUID id;
    private Long numeroRegistro;
    private String nome;
    private String email;
    private String cpf;
    private String telefone;
    private LocalDateTime dataCadastro;
    private LocalDate dataNascimento;
    private String genero;
    private Boolean ativo;
    private String perfil;
    private LocalDateTime dataExclusao;
    private LocalDateTime dataRemocaoDefinitiva;

    public ClienteResponse(Cliente cliente) {
        this.id = cliente.getId();
        this.numeroRegistro = cliente.getNumeroRegistro();
        this.nome = cliente.getNome();
        this.email = cliente.getEmail();
        this.cpf = formatarCpf(cliente.getCpf());
        this.telefone = cliente.getTelefone();
        this.dataCadastro = cliente.getDataCadastro();
        this.dataNascimento = cliente.getDataNascimento();
        this.genero = cliente.getGenero();
        this.ativo = "S".equals(cliente.getAtivo());
        this.perfil = cliente.getPerfil();
        this.dataExclusao = cliente.getDataExclusao();
        this.dataRemocaoDefinitiva = cliente.getDataExclusao() == null
            ? null
            : cliente.getDataExclusao().plusDays(30);
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

    public UUID getId() {
        return id;
    }

    public Long getNumeroRegistro() {
        return numeroRegistro;
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

    public LocalDateTime getDataExclusao() {
        return dataExclusao;
    }

    public LocalDateTime getDataRemocaoDefinitiva() {
        return dataRemocaoDefinitiva;
    }
}