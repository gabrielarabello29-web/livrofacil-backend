package com.livrofacil.modulos.chat.controller;

import com.livrofacil.modulos.chat.service.DatasetExportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/chat")
public class DatasetExportController {
    private final DatasetExportService datasetExportService;
    private final String exportToken;
    private final boolean allowUnauthenticated;

    public DatasetExportController(
            DatasetExportService datasetExportService,
            @Value("${livrofacil.chat.dataset.export-token:}") String exportToken,
            @Value("${livrofacil.chat.dataset.allow-unauthenticated:false}") boolean allowUnauthenticated
    ) {
        this.datasetExportService = datasetExportService;
        this.exportToken = exportToken;
        this.allowUnauthenticated = allowUnauthenticated;
    }

    @GetMapping(value = "/dataset.jsonl", produces = "application/x-ndjson")
    public ResponseEntity<String> exportarDataset(
            @RequestHeader(name = "X-Dataset-Export-Token", required = false) String token
    ) {
        ResponseEntity<String> acessoNegado = validarToken(token);
        if (acessoNegado != null) {
            return acessoNegado;
        }
        return baixar("livrofacil-dataset-treino.jsonl", datasetExportService.exportarTreinoJsonl());
    }

    @GetMapping(value = "/dataset/validation.jsonl", produces = "application/x-ndjson")
    public ResponseEntity<String> exportarDatasetValidacao(
            @RequestHeader(name = "X-Dataset-Export-Token", required = false) String token
    ) {
        ResponseEntity<String> acessoNegado = validarToken(token);
        if (acessoNegado != null) {
            return acessoNegado;
        }
        return baixar("livrofacil-dataset-validacao.jsonl", datasetExportService.exportarValidacaoJsonl());
    }

    private ResponseEntity<String> validarToken(String token) {
        if (allowUnauthenticated) {
            return null;
        }
        if (exportToken == null || exportToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        if (token == null || !MessageDigest.isEqual(
                exportToken.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return null;
    }

    private ResponseEntity<String> baixar(String nomeArquivo, String conteudo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/x-ndjson"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(nomeArquivo)
                .build());
        return ResponseEntity.ok().headers(headers).body(conteudo);
    }
}
