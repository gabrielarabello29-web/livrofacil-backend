package com.livrofacil.modulos.compra.controller;

import com.livrofacil.modulos.compra.dto.CriarCupomRequest;
import com.livrofacil.modulos.compra.dto.CupomResponse;
import com.livrofacil.modulos.compra.dto.ValidarCupomRequest;
import com.livrofacil.modulos.compra.dto.ValidarCupomResponse;
import com.livrofacil.modulos.compra.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/cupons")
public class CupomController {
    private final CupomService service;

    public CupomController(CupomService service) {
        this.service = service;
    }

    @PostMapping("/admin")
    public ResponseEntity<CupomResponse> criar(@Valid @RequestBody CriarCupomRequest request) {
        return ResponseEntity.status(201).body(service.criar(request));
    }

    @GetMapping("/admin")
    public ResponseEntity<List<CupomResponse>> listar() {
        return ResponseEntity.ok(service.listar());
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<CupomResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<CupomResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CriarCupomRequest request) {
        return ResponseEntity.ok(service.atualizar(id, request));
    }

    @PatchMapping("/admin/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        service.inativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        service.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/validar")
    public ResponseEntity<ValidarCupomResponse> validar(@Valid @RequestBody ValidarCupomRequest request) {
        return ResponseEntity.ok(service.validar(request.getCodigo()));
    }
}