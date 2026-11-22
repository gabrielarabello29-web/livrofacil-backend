package com.livrofacil.modulos.chat.repository;

import com.livrofacil.modulos.chat.entity.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, UUID> {
    Optional<ChatFeedback> findByClienteIdAndMensagemId(UUID clienteId, UUID mensagemId);

    @EntityGraph(attributePaths = {
            "cliente", "mensagem", "mensagem.sessao", "mensagem.livros",
            "mensagem.livros.autor", "mensagem.livros.categorias"
    })
    List<ChatFeedback> findByAvaliacaoOrderByCriadoEmDesc(ChatFeedback.Avaliacao avaliacao);
}
