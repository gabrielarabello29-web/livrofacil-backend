package com.livrofacil.modulos.cliente.dto;

import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;

public class BandeiraPagamentoResponse {
    private Long id;
    private String nome;
    private boolean disponivel;

    public BandeiraPagamentoResponse(BandeiraPagamento bandeira) {
        this.id = bandeira.getId();
        this.nome = bandeira.getNome();
        this.disponivel = bandeira.isDisponivel();
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public boolean isDisponivel() { return disponivel; }
}
