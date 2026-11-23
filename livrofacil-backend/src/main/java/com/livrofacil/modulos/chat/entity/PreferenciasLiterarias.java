package com.livrofacil.modulos.chat.entity;

import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.livro.entity.Categoria;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "chat_preferencia", uniqueConstraints = @UniqueConstraint(
        name = "uk_chat_preferencia_cliente", columnNames = "cli_id"))
public class PreferenciasLiterarias {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "pref_id")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", nullable = false, unique = true)
    private Cliente cliente;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chat_preferencia_categoria",
            joinColumns = @JoinColumn(name = "pref_id"),
            inverseJoinColumns = @JoinColumn(name = "cat_id")
    )
    private Set<Categoria> categorias = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "chat_preferencia_tema", joinColumns = @JoinColumn(name = "pref_id"))
    @Column(name = "tem_tema", nullable = false, length = 100)
    @OrderColumn(name = "ordem")
    private List<String> temasDeInteresse = new ArrayList<>();

    @Column(name = "pref_preco_minimo", precision = 10, scale = 2)
    private BigDecimal precoMinimo;

    @Column(name = "pref_preco_maximo", precision = 10, scale = 2)
    private BigDecimal precoMaximo;

    protected PreferenciasLiterarias() {
    }

    public PreferenciasLiterarias(Cliente cliente) {
        this.cliente = cliente;
    }

    public Set<Categoria> getCategorias() {
        return categorias;
    }

    public List<String> getTemasDeInteresse() {
        return temasDeInteresse;
    }

    public BigDecimal getPrecoMinimo() {
        return precoMinimo;
    }

    public BigDecimal getPrecoMaximo() {
        return precoMaximo;
    }

    public void atualizar(Set<Categoria> categorias, List<String> temas, BigDecimal precoMinimo, BigDecimal precoMaximo) {
        this.categorias.clear();
        this.categorias.addAll(categorias);
        this.temasDeInteresse.clear();
        this.temasDeInteresse.addAll(temas);
        this.precoMinimo = precoMinimo;
        this.precoMaximo = precoMaximo;
    }
}
