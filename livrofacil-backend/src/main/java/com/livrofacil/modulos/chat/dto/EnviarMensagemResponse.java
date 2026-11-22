package com.livrofacil.modulos.chat.dto;

import java.util.List;
import java.util.UUID;

public record EnviarMensagemResponse(
        UUID sessaoId,
        UUID mensagemId,
        String texto,
        List<LivroRecomendadoResponse> livros
) {
}
