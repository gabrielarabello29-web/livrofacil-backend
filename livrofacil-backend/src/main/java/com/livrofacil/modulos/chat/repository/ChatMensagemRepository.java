package com.livrofacil.modulos.chat.repository;

import com.livrofacil.modulos.chat.entity.ChatMensagem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMensagemRepository extends JpaRepository<ChatMensagem, UUID> {
    List<ChatMensagem> findTop20BySessaoIdOrderByCriadoEmDesc(UUID sessaoId, Pageable pageable);

    Optional<ChatMensagem> findByIdAndSessaoClienteIdAndTipo(
            UUID id, UUID clienteId, ChatMensagem.Tipo tipo);
}
