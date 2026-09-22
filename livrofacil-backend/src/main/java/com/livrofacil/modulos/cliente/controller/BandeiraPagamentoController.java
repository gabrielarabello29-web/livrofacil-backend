package com.livrofacil.modulos.cliente.controller;

import com.livrofacil.modulos.cliente.dto.AtualizarBandeiraPagamentoRequest;
import com.livrofacil.modulos.cliente.dto.BandeiraPagamentoResponse;
import com.livrofacil.modulos.cliente.service.BandeiraPagamentoUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pagamentos/bandeiras")
public class BandeiraPagamentoController {
    private final BandeiraPagamentoUseCase useCase;

    public BandeiraPagamentoController(BandeiraPagamentoUseCase useCase) {
        this.useCase = useCase;
    }

    @GetMapping
    public ResponseEntity<List<BandeiraPagamentoResponse>> listar() {
        return ResponseEntity.ok(useCase.listar());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BandeiraPagamentoResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarBandeiraPagamentoRequest request) {
        return ResponseEntity.ok(useCase.atualizar(id, request));
    }
}
