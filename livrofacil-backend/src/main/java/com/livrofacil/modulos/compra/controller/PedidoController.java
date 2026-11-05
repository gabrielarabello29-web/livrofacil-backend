package com.livrofacil.modulos.compra.controller;

import com.livrofacil.modulos.compra.dto.*;
import com.livrofacil.modulos.compra.entity.StatusPedido;
import com.livrofacil.modulos.compra.service.PedidoService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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

    @PostMapping("/{pedidoId}/cupons")
    public ResponseEntity<PedidoResponse> aplicarCupom(@PathVariable Long pedidoId, @RequestParam UUID clienteId,
                                                         @Valid @RequestBody AplicarCupomRequest request) {
        return ResponseEntity.ok(service.aplicarCupom(pedidoId, clienteId, request.getCodigo()));
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

    @GetMapping("/admin/relatorios/vendas-por-categoria")
    public ResponseEntity<HistoricoVendasPorCategoriaResponse> historicoVendasPorCategoria(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(value = "categorias", required = false) List<String> categorias,
            @RequestParam(value = "categoriaIds", required = false) List<String> categoriaIds,
            @RequestParam(value = "adminId", required = false) UUID adminId,
            @RequestParam(value = "admin_id", required = false) UUID adminIdAlternativo) {
        List<Long> categoriasResolvidas = resolverCategorias(categorias, categoriaIds);
        UUID adminIdResolvido = adminId != null ? adminId : adminIdAlternativo;
        return ResponseEntity.ok(service.consultarHistoricoVendasPorCategoria(dataInicio, dataFim, categoriasResolvidas, adminIdResolvido));
    }

    public ResponseEntity<HistoricoVendasPorCategoriaResponse> historicoVendasPorCategoria(
            LocalDate dataInicio,
            LocalDate dataFim,
            List<Long> categorias,
            UUID adminId) {
        return ResponseEntity.ok(service.consultarHistoricoVendasPorCategoria(dataInicio, dataFim, categorias, adminId));
    }

    private List<Long> resolverCategorias(List<String> categorias, List<String> categoriaIds) {
        List<String> valores = new ArrayList<>();
        if (categorias != null) valores.addAll(categorias);
        if (categoriaIds != null) valores.addAll(categoriaIds);
        if (valores.isEmpty()) {
            return List.of();
        }
        return valores.stream()
                .flatMap(valor -> Stream.of(valor.split(",")))
                .map(String::trim)
                .filter(valor -> !valor.isBlank())
                .map(Long::valueOf)
                .distinct()
                .toList();
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