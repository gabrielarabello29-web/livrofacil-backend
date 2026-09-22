package com.livrofacil.modulos.cliente.service;

import com.livrofacil.modulos.cliente.dto.EnderecoRequest;
import com.livrofacil.modulos.cliente.dto.EnderecoResponse;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.entity.Endereco;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.EnderecoRepository;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EnderecoUseCase {

    private final EnderecoRepository enderecoRepository;
    private final ClienteRepository clienteRepository;

    public EnderecoUseCase(EnderecoRepository enderecoRepository, ClienteRepository clienteRepository) {
        this.enderecoRepository = enderecoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public EnderecoResponse criar(UUID clienteId, EnderecoRequest request) {
        Cliente cliente = buscarCliente(clienteId);
        if (request.isPrincipal()) {
            desmarcarPrincipais(clienteId);
        }
        Endereco endereco = new Endereco(
            request.getTipoEndereco(), request.getLogradouro(), request.getNumero(), request.getComplemento(),
                request.getBairro(), request.getCidade(), request.getEstado(), request.getCep(),
                request.isPrincipal(), cliente
        );
        return new EnderecoResponse(enderecoRepository.save(endereco));
    }

    @Transactional(readOnly = true)
    public List<EnderecoResponse> listar(UUID clienteId) {
        buscarCliente(clienteId);
        return enderecoRepository.findByClienteId(clienteId).stream().map(EnderecoResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public EnderecoResponse buscarPorId(UUID clienteId, Long enderecoId) {
        buscarCliente(clienteId);
        return new EnderecoResponse(buscarEndereco(clienteId, enderecoId));
    }

    @Transactional
    public EnderecoResponse atualizar(UUID clienteId, Long enderecoId, EnderecoRequest request) {
        buscarCliente(clienteId);
        Endereco endereco = buscarEndereco(clienteId, enderecoId);
        if (request.isPrincipal()) {
            desmarcarPrincipais(clienteId);
        }
        endereco.setLogradouro(request.getLogradouro());
        endereco.setTipoEndereco(request.getTipoEndereco());
        endereco.setNumero(request.getNumero());
        endereco.setComplemento(request.getComplemento());
        endereco.setBairro(request.getBairro());
        endereco.setCidade(request.getCidade());
        endereco.setEstado(request.getEstado());
        endereco.setCep(request.getCep());
        endereco.setPrincipal(request.isPrincipal());
        return new EnderecoResponse(enderecoRepository.save(endereco));
    }

    @Transactional
    public void excluir(UUID clienteId, Long enderecoId) {
        enderecoRepository.delete(buscarEndereco(clienteId, enderecoId));
    }

    private Cliente buscarCliente(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + id));
    }

    private Endereco buscarEndereco(UUID clienteId, Long enderecoId) {
        return enderecoRepository.findByIdAndClienteId(enderecoId, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereco nao encontrado: " + enderecoId));
    }

    private void desmarcarPrincipais(UUID clienteId) {
        enderecoRepository.findByClienteId(clienteId).forEach(endereco -> {
            endereco.setPrincipal(false);
            enderecoRepository.save(endereco);
        });
    }
}