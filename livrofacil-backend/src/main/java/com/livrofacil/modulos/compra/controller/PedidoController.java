package com.livrofacil.modulos.compra.controller;

import com.livrofacil.modulos.compra.dto.*;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {
    private final PedidoService service;

    public PedidoController(PedidoService service) { this.service = service; }

    @PostMapping("/iniciar")
    public ResponseEntity<PedidoResponse> iniciar(@Valid @RequestBody IniciarCompraRequest request) {
        return ResponseEntity.status(201).body(service.iniciar(request));
    }

    @PostMapping("/{pedidoId}/finalizar")
    public ResponseEntity<PedidoResponse> finalizar(@PathVariable Long pedidoId, @RequestParam UUID clienteId,
                                                     @Valid @RequestBody FinalizarCompraRequest request) {
        return ResponseEntity.ok(service.finalizar(pedidoId, clienteId, request));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponse>> listarCliente(@PathVariable UUID clienteId) {
        return ResponseEntity.ok(service.listarCliente(clienteId));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<PedidoResponse> buscarCliente(@PathVariable Long pedidoId, @RequestParam UUID clienteId) {
        return ResponseEntity.ok(service.buscarDetalhe(pedidoId, clienteId));
    }

    @PatchMapping("/{pedidoId}/cancelar")
    public ResponseEntity<PedidoResponse> cancelar(@PathVariable Long pedidoId, @RequestParam UUID clienteId) {
        return ResponseEntity.ok(service.cancelarCliente(pedidoId, clienteId));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<PedidoResponse>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/admin/{pedidoId}")
    public ResponseEntity<PedidoResponse> buscarDetalheAdmin(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(service.buscarDetalheAdmin(pedidoId));
    }

    @PatchMapping("/admin/{pedidoId}/status")
    public ResponseEntity<PedidoResponse> atualizarStatus(@PathVariable Long pedidoId,
                                                            @Valid @RequestBody AtualizarStatusPedidoRequest request) {
        return ResponseEntity.ok(service.atualizarStatus(pedidoId, request.getStatus()));
    }

    @PatchMapping("/admin/{pedidoId}/cancelar")
    public ResponseEntity<PedidoResponse> cancelarAdmin(@PathVariable Long pedidoId) {
        return ResponseEntity.ok(service.atualizarStatus(pedidoId, StatusPedido.CANCELADO));
    }
}