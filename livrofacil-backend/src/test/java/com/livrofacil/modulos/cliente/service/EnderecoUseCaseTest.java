package com.livrofacil.modulos.cliente.service;

import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.modulos.cliente.dto.EnderecoRequest;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.entity.Endereco;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.EnderecoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnderecoUseCaseTest {
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private ClienteRepository clienteRepository;
    @InjectMocks private EnderecoUseCase useCase;
    private UUID clienteId;
    private Cliente cliente;

    @BeforeEach
    void setup() {
        clienteId = UUID.randomUUID();
        cliente = new Cliente("Cliente", "email", "999");
        org.mockito.Mockito.lenient().when(clienteRepository.findById(clienteId)).thenReturn(Optional.of(cliente));
    }

    @Test
    void deveCriarEnderecoPrincipalDesmarcandoOsDemais() {
        Endereco anterior = endereco(true);
        when(enderecoRepository.findByClienteId(clienteId)).thenReturn(List.of(anterior));
        when(enderecoRepository.save(any(Endereco.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.criar(clienteId, request(true));

        assertEquals(true, response.isPrincipal());
        assertEquals(false, anterior.isPrincipal());
        verify(enderecoRepository).save(anterior);
    }

    @Test
    void deveListarEnderecos() {
        when(enderecoRepository.findByClienteId(clienteId)).thenReturn(List.of(endereco(false)));

        assertEquals(1, useCase.listar(clienteId).size());
    }

    @Test
    void deveAtualizarEndereco() {
        Endereco endereco = endereco(false);
        when(enderecoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(endereco));
        when(enderecoRepository.save(endereco)).thenReturn(endereco);

        var response = useCase.atualizar(clienteId, 1L, request(true));

        assertEquals(true, response.isPrincipal());
        assertEquals("Nova rua", response.getLogradouro());
    }

    @Test
    void deveExcluirEndereco() {
        Endereco endereco = endereco(false);
        when(enderecoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.of(endereco));

        useCase.excluir(clienteId, 1L);

        verify(enderecoRepository).delete(endereco);
    }

    @Test
    void deveRejeitarClienteInexistente() {
        when(clienteRepository.findById(clienteId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.listar(clienteId));
    }

    @Test
    void deveRejeitarEnderecoInexistente() {
        when(enderecoRepository.findByIdAndClienteId(1L, clienteId)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscarPorId(clienteId, 1L));
        verify(enderecoRepository, never()).delete(any());
    }

    private Endereco endereco(boolean principal) {
        return new Endereco("CASA", "Rua", "10", "", "Centro", "Cidade", "SP", "01000-000", principal, cliente);
    }

    private EnderecoRequest request(boolean principal) {
        EnderecoRequest request = new EnderecoRequest();
        request.setTipoEndereco("CASA"); request.setLogradouro("Nova rua"); request.setNumero("20");
        request.setBairro("Centro"); request.setCidade("Cidade"); request.setEstado("SP");
        request.setCep("01000-000"); request.setPrincipal(principal);
        return request;
    }
}
