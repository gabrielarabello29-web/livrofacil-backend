package com.livrofacil.modulos.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record EnviarMensagemRequest(
        @NotNull UUID clienteId,
        UUID sessaoId,
        @NotBlank @Size(max = 2000) String mensagem
) {
}
