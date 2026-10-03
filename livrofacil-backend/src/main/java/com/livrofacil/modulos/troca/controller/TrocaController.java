package com.livrofacil.modulos.troca.controller;

import com.livrofacil.modulos.troca.dto.ReceberTrocaRequest;
import com.livrofacil.modulos.troca.dto.RecusarTrocaRequest;
import com.livrofacil.modulos.troca.dto.SolicitarTrocaRequest;
import com.livrofacil.modulos.troca.dto.TrocaResponse;
import com.livrofacil.modulos.troca.service.TrocaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trocas")
public class TrocaController {
    private final TrocaService service;

    public TrocaController(TrocaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TrocaResponse> solicitar(@Valid @RequestBody SolicitarTrocaRequest request) {
        return ResponseEntity.status(201).body(service.solicitar(request));
    }

    @GetMapping
    public ResponseEntity<List<TrocaResponse>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<TrocaResponse>> listarCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(service.listarCliente(clienteId));
    }

    @PatchMapping("/{trocaId}/autorizar")
    public ResponseEntity<TrocaResponse> autorizar(@PathVariable Long trocaId) {
        return ResponseEntity.ok(service.autorizar(trocaId));
    }

    @PatchMapping("/{trocaId}/recusar")
    public ResponseEntity<TrocaResponse> recusar(@PathVariable Long trocaId,
                                                  @Valid @RequestBody RecusarTrocaRequest request) {
        return ResponseEntity.ok(service.recusar(trocaId, request.getMotivo()));
    }

    @PatchMapping("/{trocaId}/receber")
    public ResponseEntity<TrocaResponse> receber(@PathVariable Long trocaId,
                                                  @RequestBody(required = false) ReceberTrocaRequest request) {
        boolean retornarEstoque = request != null && Boolean.TRUE.equals(request.getRetornarEstoque());
        return ResponseEntity.ok(service.receber(trocaId, retornarEstoque));
    }
}