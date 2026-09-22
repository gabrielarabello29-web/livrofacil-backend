package com.livrofacil.modulos.cliente.controller;

import com.livrofacil.modulos.cliente.dto.FormaPagamentoRequest;
import com.livrofacil.modulos.cliente.dto.FormaPagamentoResponse;
import com.livrofacil.modulos.cliente.service.FormaPagamentoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes/{clienteId}/formas-pagamento")
public class FormaPagamentoController {

    private final FormaPagamentoUseCase formaPagamentoUseCase;

    public FormaPagamentoController(FormaPagamentoUseCase formaPagamentoUseCase) {
        this.formaPagamentoUseCase = formaPagamentoUseCase;
    }

    // Cadastrar forma de pagamento do cliente
    @PostMapping
    public ResponseEntity<FormaPagamentoResponse> criar(@PathVariable UUID clienteId,
                                                         @Valid @RequestBody FormaPagamentoRequest request) {
        return ResponseEntity.status(201).body(formaPagamentoUseCase.criar(clienteId, request));
    }

    // Listar formas de pagamento do cliente
    @GetMapping
    public ResponseEntity<List<FormaPagamentoResponse>> listar(@PathVariable UUID clienteId,
                                                                @RequestParam(defaultValue = "desc") String ordem) {
        if (!ordem.equalsIgnoreCase("asc") && !ordem.equalsIgnoreCase("desc")) {
            throw new com.livrofacil.exception.RegraDeNegocioException("A ordem deve ser asc ou desc");
        }
        return ResponseEntity.ok(formaPagamentoUseCase.listar(clienteId, ordem.equalsIgnoreCase("asc")));
    }

    // Buscar forma de pagamento por ID
    @GetMapping("/{formaPagamentoId}")
    public ResponseEntity<FormaPagamentoResponse> buscarPorId(@PathVariable UUID clienteId,
                                                               @PathVariable Long formaPagamentoId) {
        return ResponseEntity.ok(formaPagamentoUseCase.buscarPorId(clienteId, formaPagamentoId));
    }

    // Editar forma de pagamento do cliente
    @PutMapping("/{formaPagamentoId}")
    public ResponseEntity<FormaPagamentoResponse> atualizar(@PathVariable UUID clienteId,
                                                             @PathVariable Long formaPagamentoId,
                                                             @Valid @RequestBody FormaPagamentoRequest request) {
        return ResponseEntity.ok(formaPagamentoUseCase.atualizar(clienteId, formaPagamentoId, request));
    }

    @PatchMapping("/{formaPagamentoId}/preferencial")
    public ResponseEntity<FormaPagamentoResponse> definirPreferencial(@PathVariable UUID clienteId,
                                                                       @PathVariable Long formaPagamentoId) {
        return ResponseEntity.ok(formaPagamentoUseCase.definirPreferencial(clienteId, formaPagamentoId));
    }

    // Inativar forma de pagamento do cliente
    @DeleteMapping("/{formaPagamentoId}")
    public ResponseEntity<FormaPagamentoResponse> excluir(@PathVariable UUID clienteId, @PathVariable Long formaPagamentoId) {
        return ResponseEntity.ok(formaPagamentoUseCase.excluir(clienteId, formaPagamentoId));
    }

    // Excluir cartao definitivamente do cliente
    @DeleteMapping("/{formaPagamentoId}/excluir")
    public ResponseEntity<Void> excluirDefinitivamente(@PathVariable UUID clienteId,
                                                        @PathVariable Long formaPagamentoId) {
        formaPagamentoUseCase.excluirDefinitivamente(clienteId, formaPagamentoId);
        return ResponseEntity.noContent().build();
    }

    // Reativar cartao do cliente
    @PatchMapping("/{formaPagamentoId}/reativar")
    public ResponseEntity<FormaPagamentoResponse> reativar(@PathVariable UUID clienteId,
                                                            @PathVariable Long formaPagamentoId) {
        return ResponseEntity.ok(formaPagamentoUseCase.reativar(clienteId, formaPagamentoId));
    }
}