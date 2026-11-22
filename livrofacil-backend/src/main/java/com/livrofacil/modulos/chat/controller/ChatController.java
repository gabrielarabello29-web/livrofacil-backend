package com.livrofacil.modulos.chat.controller;

import com.livrofacil.modulos.chat.dto.EnviarMensagemRequest;
import com.livrofacil.modulos.chat.dto.EnviarMensagemResponse;
import com.livrofacil.modulos.chat.dto.PreferenciasLiterariasRequest;
import com.livrofacil.modulos.chat.dto.PreferenciasLiterariasResponse;
import com.livrofacil.modulos.chat.dto.RegistrarFeedbackRequest;
import com.livrofacil.modulos.chat.dto.RegistrarFeedbackResponse;
import com.livrofacil.modulos.chat.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat/mensagens")
    public ResponseEntity<EnviarMensagemResponse> enviarMensagem(@Valid @RequestBody EnviarMensagemRequest request) {
        return ResponseEntity.ok(chatService.enviarMensagem(request));
    }

    @PostMapping("/chat/feedback")
    public ResponseEntity<RegistrarFeedbackResponse> registrarFeedback(
            @Valid @RequestBody RegistrarFeedbackRequest request
    ) {
        return ResponseEntity.ok(chatService.registrarFeedback(request));
    }

    @GetMapping("/clientes/{clienteId}/preferencias-literarias")
    public ResponseEntity<PreferenciasLiterariasResponse> buscarPreferencias(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(chatService.buscarPreferencias(clienteId));
    }

    @PutMapping("/clientes/{clienteId}/preferencias-literarias")
    public ResponseEntity<PreferenciasLiterariasResponse> atualizarPreferencias(
            @PathVariable UUID clienteId,
            @Valid @RequestBody PreferenciasLiterariasRequest request
    ) {
        return ResponseEntity.ok(chatService.atualizarPreferencias(clienteId, request));
    }
}
