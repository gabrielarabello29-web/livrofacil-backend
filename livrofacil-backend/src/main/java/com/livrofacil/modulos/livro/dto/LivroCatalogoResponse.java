package com.livrofacil.modulos.livro.dto;

import com.livrofacil.modulos.livro.entity.Categoria;
import com.livrofacil.modulos.livro.entity.Livro;

import java.math.BigDecimal;
import java.util.List;

public class LivroCatalogoResponse {

    private Long id;
    private String titulo;
    private Integer ano;
    private Integer edicao;
    private Integer numeroPaginas;
    private String sinopse;
    private String imagemUrl;
    private BigDecimal valorVenda;
    private Boolean ativo;
    private String autorNome;
    private String editoraNome;
    private String grupoPrecificacaoNome;
    private List<String> categoriaNomes;

    public LivroCatalogoResponse(Livro livro) {
        this.id = livro.getId();
        this.titulo = livro.getTitulo();
        this.ano = livro.getAno();
        this.edicao = livro.getEdicao();
        this.numeroPaginas = livro.getNumeroPaginas();
        this.sinopse = livro.getSinopse();
        this.imagemUrl = livro.getImagemUrl();
        this.valorVenda = livro.getValorVenda();
        this.ativo = livro.getAtivo();
        this.autorNome = livro.getAutor() == null ? null : livro.getAutor().getNome();
        this.editoraNome = livro.getEditora() == null ? null : livro.getEditora().getNome();
        this.grupoPrecificacaoNome = livro.getGrupoPrecificacao() == null
                ? null
                : livro.getGrupoPrecificacao().getNome();
        this.categoriaNomes = livro.getCategorias() == null
                ? List.of()
                : livro.getCategorias().stream().map(Categoria::getNome).toList();
    }

    public Long getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public Integer getAno() {
        return ano;
    }

    public Integer getEdicao() {
        return edicao;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public String getSinopse() {
        return sinopse;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public String getEditoraNome() {
        return editoraNome;
    }

    public String getGrupoPrecificacaoNome() {
        return grupoPrecificacaoNome;
    }

    public List<String> getCategoriaNomes() {
        return categoriaNomes;
    }
}