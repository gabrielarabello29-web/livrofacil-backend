package com.livrofacil.modulos.troca.controller;

import com.livrofacil.modulos.troca.dto.ReceberTrocaRequest;
import com.livrofacil.modulos.troca.dto.RecusarTrocaRequest;
import com.livrofacil.modulos.troca.dto.SolicitarTrocaRequest;
import com.livrofacil.modulos.troca.dto.TrocaResponse;
import com.livrofacil.modulos.troca.dto.TrocaHistoricoResponse;
import com.livrofacil.modulos.troca.dto.VoucherTrocaResponse;
import com.livrofacil.modulos.troca.entity.StatusTroca;
import com.livrofacil.modulos.troca.entity.TipoSolicitacaoTroca;
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
    public ResponseEntity<List<TrocaResponse>> listarTodas(
            @RequestParam(required = false) StatusTroca status,
            @RequestParam(required = false) TipoSolicitacaoTroca tipo) {
        return ResponseEntity.ok(service.listarTodas(status, tipo));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<TrocaResponse>> listarCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(service.listarCliente(clienteId));
    }

    @GetMapping("/cliente/{clienteId}/{trocaId}")
    public ResponseEntity<TrocaResponse> buscarCliente(@PathVariable UUID clienteId, @PathVariable Long trocaId) {
        return ResponseEntity.ok(service.buscarCliente(trocaId, clienteId));
    }

    @GetMapping("/cliente/{clienteId}/{trocaId}/historico")
    public ResponseEntity<List<TrocaHistoricoResponse>> historicoCliente(
            @PathVariable UUID clienteId, @PathVariable Long trocaId) {
        return ResponseEntity.ok(service.listarHistoricoCliente(trocaId, clienteId).stream()
                .map(TrocaHistoricoResponse::new).toList());
    }

    @GetMapping("/{trocaId}")
    public ResponseEntity<TrocaResponse> buscarAdmin(@PathVariable Long trocaId) {
        return ResponseEntity.ok(service.buscarAdmin(trocaId));
    }

    @GetMapping("/{trocaId}/historico")
    public ResponseEntity<List<TrocaHistoricoResponse>> historicoAdmin(
            @PathVariable Long trocaId) {
        return ResponseEntity.ok(service.listarHistorico(trocaId).stream()
                .map(TrocaHistoricoResponse::new).toList());
    }

    @GetMapping("/cliente/{clienteId}/vouchers")
    public ResponseEntity<List<VoucherTrocaResponse>> listarVouchersCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(service.listarVouchersCliente(clienteId));
    }

    @GetMapping("/cliente/{clienteId}/vouchers/{voucherId}")
    public ResponseEntity<VoucherTrocaResponse> buscarVoucherCliente(@PathVariable UUID clienteId,
                                                                      @PathVariable Long voucherId) {
        return ResponseEntity.ok(service.buscarVoucherCliente(voucherId, clienteId));
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
                              @Valid @RequestBody(required = false) ReceberTrocaRequest request) {
        boolean retornarEstoque = request != null && Boolean.TRUE.equals(request.getRetornarEstoque());
        Integer quantidade = request == null ? null : request.getQuantidadeRecebida();
        return ResponseEntity.ok(quantidade == null
            ? service.receber(trocaId, retornarEstoque)
            : service.receber(trocaId, retornarEstoque, quantidade));
    }
}