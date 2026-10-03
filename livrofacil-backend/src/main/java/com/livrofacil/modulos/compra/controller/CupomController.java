package com.livrofacil.modulos.compra.controller;

import com.livrofacil.modulos.compra.dto.ValidarCupomRequest;
import com.livrofacil.modulos.compra.dto.ValidarCupomResponse;
import com.livrofacil.modulos.compra.service.CupomService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cupons")
public class CupomController {
    private final CupomService service;

    public CupomController(CupomService service) {
        this.service = service;
    }

    @PostMapping("/validar")
    public ResponseEntity<ValidarCupomResponse> validar(@Valid @RequestBody ValidarCupomRequest request) {
        return ResponseEntity.ok(service.validar(request.getCodigo()));
    }
}