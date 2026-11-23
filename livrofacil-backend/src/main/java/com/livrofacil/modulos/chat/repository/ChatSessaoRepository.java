package com.livrofacil.modulos.chat.repository;

import com.livrofacil.modulos.chat.entity.ChatSessao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatSessaoRepository extends JpaRepository<ChatSessao, UUID> {
    Optional<ChatSessao> findByIdAndClienteId(UUID id, UUID clienteId);
}
