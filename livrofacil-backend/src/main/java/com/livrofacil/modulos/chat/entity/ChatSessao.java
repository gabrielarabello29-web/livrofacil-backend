package com.livrofacil.modulos.chat.entity;

import com.livrofacil.modulos.cliente.entity.Cliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_sessao")
public class ChatSessao {
    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "ses_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", nullable = false)
    private Cliente cliente;

    @Column(name = "ses_criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "ses_atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    protected ChatSessao() {
    }

    public ChatSessao(Cliente cliente) {
        this.cliente = cliente;
    }

    public UUID getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void atualizarAtividade() {
        atualizadoEm = Instant.now();
    }
}
