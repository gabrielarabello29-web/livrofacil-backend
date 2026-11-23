package com.livrofacil.modulos.chat.entity;

import com.livrofacil.modulos.livro.entity.Livro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "chat_mensagem")
public class ChatMensagem {
    public enum Tipo {
        CLIENTE,
        IA
    }

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "men_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ses_id", nullable = false)
    private ChatSessao sessao;

    @Enumerated(EnumType.STRING)
    @Column(name = "men_tipo", nullable = false, length = 10)
    private Tipo tipo;

    @Column(name = "men_texto", nullable = false, columnDefinition = "text")
    private String texto;

    @Column(name = "men_contexto_json", columnDefinition = "text")
    private String contextoJson;

    @Column(name = "men_criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chat_mensagem_livro",
            joinColumns = @JoinColumn(name = "men_id"),
            inverseJoinColumns = @JoinColumn(name = "liv_id")
    )
    @OrderColumn(name = "ordem")
    private List<Livro> livros = new ArrayList<>();

    protected ChatMensagem() {
    }

    public ChatMensagem(ChatSessao sessao, Tipo tipo, String texto) {
        this.sessao = sessao;
        this.tipo = tipo;
        this.texto = texto;
    }

    public UUID getId() {
        return id;
    }

    public ChatSessao getSessao() {
        return sessao;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getTexto() {
        return texto;
    }

    public String getContextoJson() {
        return contextoJson;
    }

    public void setContextoJson(String contextoJson) {
        this.contextoJson = contextoJson;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public List<Livro> getLivros() {
        return livros;
    }
}
