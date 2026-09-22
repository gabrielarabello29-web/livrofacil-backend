package com.livrofacil.modulos.cliente.controller;

import com.livrofacil.modulos.cliente.dto.AlterarSenhaRequest;
import com.livrofacil.modulos.cliente.dto.ClienteCadastroRequest;
import com.livrofacil.modulos.cliente.dto.ClienteLoginRequest;
import com.livrofacil.modulos.cliente.dto.ClienteResponse;
import com.livrofacil.modulos.cliente.dto.ClienteUpdateRequest;
import com.livrofacil.modulos.cliente.service.ClienteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteUseCase clienteUseCase;

    public ClienteController(ClienteUseCase clienteUseCase) {
        this.clienteUseCase = clienteUseCase;
    }

    // Criar cliente
    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteCadastroRequest request) {
        ClienteResponse response = clienteUseCase.criar(request);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(uri).body(response);
    }

    // Listar todos os clientes
    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteUseCase.listar());
    }

    // Buscar clientes pelos campos do cadastro, exceto senha
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponse>> buscar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) Long registro,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String cpf,
            @RequestParam(required = false) String telefone,
            @RequestParam(required = false) LocalDate dataNascimento,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) String tipoEndereco,
            @RequestParam(required = false) String endereco,
            @RequestParam(required = false) String complemento,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String cep
    ) {
        return ResponseEntity.ok(clienteUseCase.buscar(registro, nome, email, cpf, telefone, dataNascimento, genero,
                tipoEndereco, endereco, complemento, bairro, cidade, estado, cep));
    }

    // Buscar cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(clienteUseCase.buscarPorId(id));
    }

    // Login do cliente
    @PostMapping("/login")
    public ResponseEntity<ClienteResponse> login(@Valid @RequestBody ClienteLoginRequest request) {
        return ResponseEntity.ok(clienteUseCase.login(request.getEmail(), request.getSenha()));
    }

    // Editar cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ClienteUpdateRequest request
    ) {
        return ResponseEntity.ok(clienteUseCase.atualizar(id, request));
    }

    // Alterar senha do cliente
    @PatchMapping("/{id}/senha")
    public ResponseEntity<Void> alterarSenha(@PathVariable UUID id,
                                               @Valid @RequestBody AlterarSenhaRequest request) {
        clienteUseCase.alterarSenha(id, request);
        return ResponseEntity.noContent().build();
    }

    // Inativar cliente
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> inativar(@PathVariable UUID id) {
        clienteUseCase.inativar(id);
        return ResponseEntity.noContent().build();
    }
}