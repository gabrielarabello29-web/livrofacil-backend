package com.livrofacil.modulos.compra.controller;

import com.livrofacil.modulos.compra.dto.*;
import com.livrofacil.modulos.compra.service.CarrinhoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/carrinhos")
public class CarrinhoController {
    private final CarrinhoService service;

    public CarrinhoController(CarrinhoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<CarrinhoResponse> criarOuAssociar(@RequestBody CriarCarrinhoRequest request) {
        return ResponseEntity.ok(service.criarOuAssociar(request));
    }

    @GetMapping("/{carrinhoId}")
    public ResponseEntity<CarrinhoResponse> buscar(@PathVariable Long carrinhoId,
                                                   @RequestParam(required = false) UUID clienteId,
                                                   @RequestParam(required = false) String token) {
        return ResponseEntity.ok(service.buscar(carrinhoId, clienteId, token));
    }

    @PostMapping("/{carrinhoId}/itens")
    public ResponseEntity<ItemCarrinhoResponse> adicionar(@PathVariable Long carrinhoId,
                                                            @RequestParam(required = false) UUID clienteId,
                                                            @RequestParam(required = false) String token,
                                                            @Valid @RequestBody ItemCarrinhoRequest request) {
        return ResponseEntity.status(201).body(service.adicionar(carrinhoId, clienteId, token, request));
    }

    @PutMapping("/{carrinhoId}/itens/{itemId}")
    public ResponseEntity<ItemCarrinhoResponse> atualizar(@PathVariable Long carrinhoId, @PathVariable Long itemId,
                                                            @RequestParam(required = false) UUID clienteId,
                                                            @RequestParam(required = false) String token,
                                                            @RequestParam Integer quantidade) {
        return ResponseEntity.ok(service.atualizarItem(carrinhoId, clienteId, token, itemId, quantidade));
    }

    @DeleteMapping("/{carrinhoId}/itens/{itemId}")
    public ResponseEntity<Void> remover(@PathVariable Long carrinhoId, @PathVariable Long itemId,
                                        @RequestParam(required = false) UUID clienteId,
                                        @RequestParam(required = false) String token) {
        service.removerItem(carrinhoId, clienteId, token, itemId);
        return ResponseEntity.noContent().build();
    }
}