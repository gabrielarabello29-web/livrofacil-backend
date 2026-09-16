package com.livrofacil.cliente.controller;

import com.livrofacil.cliente.dto.EnderecoRequest;
import com.livrofacil.cliente.dto.EnderecoResponse;
import com.livrofacil.cliente.service.EnderecoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/{clienteId}/enderecos")
public class EnderecoController {

    private final EnderecoUseCase enderecoUseCase;

    public EnderecoController(EnderecoUseCase enderecoUseCase) {
        this.enderecoUseCase = enderecoUseCase;
    }

    // Cadastrar endereco do cliente
    @PostMapping
    public ResponseEntity<EnderecoResponse> criar(@PathVariable Long clienteId, @Valid @RequestBody EnderecoRequest request) {
        return ResponseEntity.status(201).body(enderecoUseCase.criar(clienteId, request));
    }

    // Listar enderecos do cliente
    @GetMapping
    public ResponseEntity<List<EnderecoResponse>> listar(@PathVariable Long clienteId) {
        return ResponseEntity.ok(enderecoUseCase.listar(clienteId));
    }

    // Buscar endereco por ID
    @GetMapping("/{enderecoId}")
    public ResponseEntity<EnderecoResponse> buscarPorId(@PathVariable Long clienteId, @PathVariable Long enderecoId) {
        return ResponseEntity.ok(enderecoUseCase.buscarPorId(clienteId, enderecoId));
    }

    // Editar endereco do cliente
    @PutMapping("/{enderecoId}")
    public ResponseEntity<EnderecoResponse> atualizar(@PathVariable Long clienteId, @PathVariable Long enderecoId,
                                                       @Valid @RequestBody EnderecoRequest request) {
        return ResponseEntity.ok(enderecoUseCase.atualizar(clienteId, enderecoId, request));
    }

    // Excluir endereco do cliente
    @DeleteMapping("/{enderecoId}")
    public ResponseEntity<Void> excluir(@PathVariable Long clienteId, @PathVariable Long enderecoId) {
        enderecoUseCase.excluir(clienteId, enderecoId);
        return ResponseEntity.noContent().build();
    }
}