package com.livrofacil.produtos.livro.dto;

import com.livrofacil.produtos.livro.entity.Autor;
import com.livrofacil.produtos.livro.entity.Categoria;
import com.livrofacil.produtos.livro.entity.Editora;
import com.livrofacil.produtos.livro.entity.GrupoPrecificacao;

import java.math.BigDecimal;

public class LivroOpcaoResponse {

    private Long id;
    private String nome;
    private BigDecimal percentualMargem;

    public LivroOpcaoResponse(Autor autor) {
        this(autor.getId(), autor.getNome(), null);
    }

    public LivroOpcaoResponse(Editora editora) {
        this(editora.getId(), editora.getNome(), null);
    }

    public LivroOpcaoResponse(Categoria categoria) {
        this(categoria.getId(), categoria.getNome(), null);
    }

    public LivroOpcaoResponse(GrupoPrecificacao grupoPrecificacao) {
        this(grupoPrecificacao.getId(), grupoPrecificacao.getNome(), grupoPrecificacao.getPercentualMargem());
    }

    private LivroOpcaoResponse(Long id, String nome, BigDecimal percentualMargem) {
        this.id = id;
        this.nome = nome;
        this.percentualMargem = percentualMargem;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getPercentualMargem() {
        return percentualMargem;
    }
}