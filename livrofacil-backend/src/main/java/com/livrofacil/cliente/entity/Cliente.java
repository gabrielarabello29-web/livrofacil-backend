package com.livrofacil.cliente.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.LocalDate;

@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cli_id")
    private Long id;

    @Column(name = "cli_nome", length = 100)
    private String nome;

    @Column(name = "cli_email", length = 50, unique = true)
    private String email;

    @Column(name = "cli_senha", length = 100)
    private String senha;

    @Column(name = "cli_cpf", length = 11, unique = true)
    private String cpf;

    @Column(name = "cli_telefone", length = 20, unique = true)
    private String telefone;

    @Column(name = "cli_data_cadastro")
    private LocalDateTime dataCadastro;

    @Column(name = "cli_data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "cli_genero", length = 30)
    private String genero;

    @Column(name = "cli_ativo", length = 1)
    private String ativo;

    @Column(name = "cli_perfil", length = 20)
    private String perfil;

    public Cliente() {
    }

    public Cliente(String nome, String email, String telefone) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.dataCadastro = LocalDateTime.now();
        this.ativo = "S";
        this.perfil = "CLIENTE";
    }

    public Long getId() {
        return id;
    }

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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDateTime getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(LocalDateTime dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getAtivo() {
        return ativo;
    }

    public void setAtivo(String ativo) {
        this.ativo = ativo;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }
}