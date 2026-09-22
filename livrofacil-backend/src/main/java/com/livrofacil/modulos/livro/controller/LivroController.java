package com.livrofacil.modulos.livro.controller;

import com.livrofacil.modulos.livro.dto.LivroRequest;
import com.livrofacil.modulos.livro.dto.LivroResponse;
import com.livrofacil.modulos.livro.dto.LivroCatalogoResponse;
import com.livrofacil.modulos.livro.dto.LivroOpcaoResponse;
import com.livrofacil.modulos.livro.dto.EstoqueRequest;
import com.livrofacil.modulos.livro.dto.EstoqueResponse;
import com.livrofacil.modulos.livro.service.CadastrarLivroUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/livros")
public class LivroController {

    private final CadastrarLivroUseCase cadastrarLivroUseCase;

    public LivroController(CadastrarLivroUseCase cadastrarLivroUseCase) {
        this.cadastrarLivroUseCase = cadastrarLivroUseCase;
    }

    @PostMapping
    public ResponseEntity<LivroResponse> criar(@Valid @RequestBody LivroRequest request) {
        LivroResponse response = cadastrarLivroUseCase.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LivroResponse>> listar() {
        return ResponseEntity.ok(cadastrarLivroUseCase.listar());
    }

    @GetMapping("/ativos")
    public ResponseEntity<List<LivroResponse>> listarAtivos() {
        return ResponseEntity.ok(cadastrarLivroUseCase.listarAtivos());
    }

    @GetMapping("/catalogo")
    public ResponseEntity<List<LivroCatalogoResponse>> listarCatalogo() {
        return ResponseEntity.ok(cadastrarLivroUseCase.listarCatalogo());
    }

    @GetMapping("/catalogo/buscar")
    public ResponseEntity<List<LivroCatalogoResponse>> buscarCatalogo(@RequestParam String titulo) {
        return ResponseEntity.ok(cadastrarLivroUseCase.buscarNoCatalogoPorTitulo(titulo));
    }

    @GetMapping("/catalogo/{id}")
    public ResponseEntity<LivroCatalogoResponse> buscarLivroCatalogo(@PathVariable Long id) {
        return ResponseEntity.ok(cadastrarLivroUseCase.buscarNoCatalogo(id));
    }

    @GetMapping("/opcoes/autores")
    public ResponseEntity<List<LivroOpcaoResponse>> listarAutores() {
        return ResponseEntity.ok(cadastrarLivroUseCase.listarAutores());
    }

    @GetMapping("/opcoes/editoras")
    public ResponseEntity<List<LivroOpcaoResponse>> listarEditoras() {
        return ResponseEntity.ok(cadastrarLivroUseCase.listarEditoras());
    }

    @GetMapping("/opcoes/categorias")
    public ResponseEntity<List<LivroOpcaoResponse>> listarCategorias() {
        return ResponseEntity.ok(cadastrarLivroUseCase.listarCategorias());
    }

    @GetMapping("/opcoes/grupos-precificacao")
    public ResponseEntity<List<LivroOpcaoResponse>> listarGruposPrecificacao() {
        return ResponseEntity.ok(cadastrarLivroUseCase.listarGruposPrecificacao());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<LivroResponse>> buscarPorTitulo(@RequestParam String titulo) {
        return ResponseEntity.ok(cadastrarLivroUseCase.buscarPorTitulo(titulo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivroResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cadastrarLivroUseCase.buscarPorId(id));
    }

    @GetMapping("/{id}/estoque")
    public ResponseEntity<EstoqueResponse> buscarEstoque(@PathVariable Long id) {
        return ResponseEntity.ok(cadastrarLivroUseCase.buscarEstoque(id));
    }

    @PutMapping("/{id}/estoque")
    public ResponseEntity<EstoqueResponse> atualizarEstoque(
            @PathVariable Long id,
            @Valid @RequestBody EstoqueRequest request
    ) {
        return ResponseEntity.ok(cadastrarLivroUseCase.atualizarEstoque(id, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LivroResponse> atualizar(@PathVariable Long id, @Valid @RequestBody LivroRequest request) {
        return ResponseEntity.ok(cadastrarLivroUseCase.atualizar(id, request));
    }

    @PatchMapping("/{id}/ativar")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        cadastrarLivroUseCase.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<Void> inativar(@PathVariable Long id) {
        cadastrarLivroUseCase.inativar(id);
        return ResponseEntity.noContent().build();
    }
}