package com.livrofacil.modulos.chat.entity;

import com.livrofacil.modulos.cliente.entity.Cliente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_feedback", uniqueConstraints = @UniqueConstraint(
        name = "uk_chat_feedback_cliente_mensagem", columnNames = {"cli_id", "men_id"}))
public class ChatFeedback {
    public enum Avaliacao {
        UTIL,
        NAO_UTIL
    }

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "fee_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cli_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "men_id", nullable = false)
    private ChatMensagem mensagem;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_avaliacao", nullable = false, length = 10)
    private Avaliacao avaliacao;

    @Column(name = "fee_comentario", nullable = false, columnDefinition = "text")
    private String comentario;

    @Column(name = "fee_criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "fee_atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    protected ChatFeedback() {
    }

    public ChatFeedback(Cliente cliente, ChatMensagem mensagem, Avaliacao avaliacao, String comentario) {
        this.cliente = cliente;
        this.mensagem = mensagem;
        atualizar(avaliacao, comentario);
    }

    public UUID getId() {
        return id;
    }

    public ChatMensagem getMensagem() {
        return mensagem;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public String getComentario() {
        return comentario;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void atualizar(Avaliacao avaliacao, String comentario) {
        this.avaliacao = avaliacao;
        this.comentario = comentario;
        this.atualizadoEm = Instant.now();
    }
}
