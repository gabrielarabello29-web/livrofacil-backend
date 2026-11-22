package com.livrofacil.modulos.chat.dto;

import java.math.BigDecimal;

public record LivroRecomendadoResponse(
        Long livroId,
        String titulo,
        String autor,
        String imagemUrl,
        BigDecimal preco,
        String url
) {
}
