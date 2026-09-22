package com.livrofacil.modulos.compra.entity;

import com.livrofacil.modulos.livro.entity.Livro;
import jakarta.persistence.*;

@Entity
@Table(name = "item_carrinho", uniqueConstraints = {
    @UniqueConstraint(name = "uk_item_carrinho_carrinho_livro", columnNames = {"car_id", "liv_id"})
})
public class ItemCarrinho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ite_car_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "car_id", nullable = false)
    private Carrinho carrinho;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "liv_id", nullable = false)
    private Livro livro;

    @Column(name = "ite_car_quantidade", nullable = false)
    private Integer quantidade;

    public Long getId() { return id; }
    public Carrinho getCarrinho() { return carrinho; }
    public void setCarrinho(Carrinho carrinho) { this.carrinho = carrinho; }
    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }
    public Integer getQuantidade() { return quantidade; }
    public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
}