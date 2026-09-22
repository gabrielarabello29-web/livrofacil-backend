package com.livrofacil.modulos.livro.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "livro")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "liv_id")
    private Long id;

    @Column(name = "liv_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "liv_titulo", nullable = false, length = 200)
    private String titulo;

    @Column(name = "liv_ano", nullable = false)
    private Integer ano;

    @Column(name = "liv_edicao", nullable = false)
    private Integer edicao;

    @Column(name = "liv_isbn", nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(name = "liv_numero_paginas", nullable = false)
    private Integer numeroPaginas;

    @Column(name = "liv_sinopse", nullable = false, length = 5000)
    private String sinopse;

    @Column(name = "liv_imagem_url", nullable = false, length = 500)
    private String imagemUrl;

    @Column(name = "liv_codigo_barras", nullable = false, unique = true, length = 50)
    private String codigoBarras;

    @Column(name = "liv_valor_venda", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorVenda;

    @Column(name = "liv_ativo", nullable = false)
    private Boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aut_id", nullable = false)
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "edi_id", nullable = false)
    private Editora editora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grp_pre_id", nullable = false)
    private GrupoPrecificacao grupoPrecificacao;

    @Embedded
    private Dimensao dimensao;

    @ManyToMany
    @JoinTable(
            name = "livro_categoria",
            joinColumns = @JoinColumn(name = "liv_id"),
            inverseJoinColumns = @JoinColumn(name = "cat_id")
    )
    private Set<Categoria> categorias = new HashSet<>();

    public Livro() {
    }

    public Livro(
            String codigo,
            String titulo,
            Integer ano,
            Integer edicao,
            String isbn,
            Integer numeroPaginas,
            String sinopse,
            String imagemUrl,
            String codigoBarras,
            BigDecimal valorVenda,
            Boolean ativo,
            Autor autor,
            Editora editora,
            GrupoPrecificacao grupoPrecificacao,
            Dimensao dimensao
    ) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.ano = ano;
        this.edicao = edicao;
        this.isbn = isbn;
        this.numeroPaginas = numeroPaginas;
        this.sinopse = sinopse;
        this.imagemUrl = imagemUrl;
        this.codigoBarras = codigoBarras;
        this.valorVenda = valorVenda;
        this.ativo = ativo;
        this.autor = autor;
        this.editora = editora;
        this.grupoPrecificacao = grupoPrecificacao;
        this.dimensao = dimensao;
    }

    public Long getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getEdicao() {
        return edicao;
    }

    public void setEdicao(Integer edicao) {
        this.edicao = edicao;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getNumeroPaginas() {
        return numeroPaginas;
    }

    public void setNumeroPaginas(Integer numeroPaginas) {
        this.numeroPaginas = numeroPaginas;
    }

    public String getSinopse() {
        return sinopse;
    }

    public void setSinopse(String sinopse) {
        this.sinopse = sinopse;
    }

    public String getImagemUrl() {
        return imagemUrl;
    }

    public void setImagemUrl(String imagemUrl) {
        this.imagemUrl = imagemUrl;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public BigDecimal getValorVenda() {
        return valorVenda;
    }

    public void setValorVenda(BigDecimal valorVenda) {
        this.valorVenda = valorVenda;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }

    public Editora getEditora() {
        return editora;
    }

    public void setEditora(Editora editora) {
        this.editora = editora;
    }

    public GrupoPrecificacao getGrupoPrecificacao() {
        return grupoPrecificacao;
    }

    public void setGrupoPrecificacao(GrupoPrecificacao grupoPrecificacao) {
        this.grupoPrecificacao = grupoPrecificacao;
    }

    public Dimensao getDimensao() {
        return dimensao;
    }

    public void setDimensao(Dimensao dimensao) {
        this.dimensao = dimensao;
    }

    public Set<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(Set<Categoria> categorias) {
        this.categorias = categorias;
    }

    public void adicionarCategoria(Categoria categoria) {
        this.categorias.add(categoria);
    }

    public void removerCategoria(Categoria categoria) {
        this.categorias.remove(categoria);
    }
}