package com.livrofacil.modulos.chat.repository;

import com.livrofacil.modulos.chat.entity.ChatFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatFeedbackRepository extends JpaRepository<ChatFeedback, UUID> {
    Optional<ChatFeedback> findByClienteIdAndMensagemId(UUID clienteId, UUID mensagemId);
}
