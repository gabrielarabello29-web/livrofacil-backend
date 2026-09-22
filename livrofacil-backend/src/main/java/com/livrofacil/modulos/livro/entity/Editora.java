package com.livrofacil.modulos.livro.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "editora")
public class Editora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "edi_id")
    private Long id;

    @Column(name = "edi_nome", nullable = false, length = 150)
    private String nome;

    public Editora() {
    }

    public Editora(String nome) {
        this.nome = nome;
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
}