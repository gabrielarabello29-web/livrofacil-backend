package com.livrofacil.produtos.livro.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "livros")
public class Livro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private Integer ano;

    @Column(nullable = false)
    private Integer edicao;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false)
    private Integer numeroPaginas;

    @Column(nullable = false, length = 5000)
    private String sinopse;

    @Column(nullable = false, unique = true, length = 50)
    private String codigoBarras;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorVenda;

    @Column(nullable = false)
    private Boolean ativo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "autor_id", nullable = false)
    private Autor autor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "editora_id", nullable = false)
    private Editora editora;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grupo_precificacao_id", nullable = false)
    private GrupoPrecificacao grupoPrecificacao;

    @Embedded
    private Dimensao dimensao;

    @ManyToMany
    @JoinTable(
            name = "livro_categoria",
            joinColumns = @JoinColumn(name = "livro_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
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