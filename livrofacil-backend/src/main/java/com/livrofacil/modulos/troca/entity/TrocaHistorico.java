package com.livrofacil.modulos.troca.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "troca_historico")
public class TrocaHistorico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tha_id")
    private Long id;

    @jakarta.persistence.ManyToOne(fetch = jakarta.persistence.FetchType.LAZY, optional = false)
    @jakarta.persistence.JoinColumn(name = "tro_id", nullable = false)
    private Troca troca;

    @Column(name = "tha_status", nullable = false, length = 30)
    private String status;

    @Column(name = "tha_observacao", length = 1000)
    private String observacao;

    @Column(name = "tha_criado_em", nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    protected TrocaHistorico() { }

    public TrocaHistorico(Troca troca, String status, String observacao) {
        this.troca = troca;
        this.status = status;
        this.observacao = observacao;
    }

    public Long getId() { return id; }
    public Long getTrocaId() { return troca.getId(); }
    public String getStatus() { return status; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}