package com.livrofacil.cliente.service;

import com.livrofacil.cliente.dto.FormaPagamentoRequest;
import com.livrofacil.cliente.dto.FormaPagamentoResponse;
import com.livrofacil.cliente.entity.Cliente;
import com.livrofacil.cliente.entity.FormaPagamento;
import com.livrofacil.cliente.repository.ClienteRepository;
import com.livrofacil.cliente.repository.FormaPagamentoRepository;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FormaPagamentoUseCase {

    private final FormaPagamentoRepository formaPagamentoRepository;
    private final ClienteRepository clienteRepository;

    public FormaPagamentoUseCase(FormaPagamentoRepository formaPagamentoRepository, ClienteRepository clienteRepository) {
        this.formaPagamentoRepository = formaPagamentoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public FormaPagamentoResponse criar(Long clienteId, FormaPagamentoRequest request) {
        Cliente cliente = buscarCliente(clienteId);
        if (request.isPreferencial()) {
            desmarcarPreferenciais(clienteId);
        }
        String numeroCartao = request.getNumeroCartao().replaceAll("\\D", "");
        if (numeroCartao.length() != 16) {
            throw new RegraDeNegocioException("O numero do cartao deve possuir exatamente 16 digitos");
        }
        FormaPagamento formaPagamento = new FormaPagamento(
            request.getNomeTitular(), request.getTipoCartao(), numeroCartao.substring(numeroCartao.length() - 4),
            request.getValidade(), request.getBandeira(), request.isPreferencial(), cliente
        );
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    @Transactional(readOnly = true)
    public List<FormaPagamentoResponse> listar(Long clienteId) {
        buscarCliente(clienteId);
        return formaPagamentoRepository.findByClienteId(clienteId).stream()
                .map(FormaPagamentoResponse::new).toList();
    }

    @Transactional(readOnly = true)
    public FormaPagamentoResponse buscarPorId(Long clienteId, Long formaPagamentoId) {
        buscarCliente(clienteId);
        return new FormaPagamentoResponse(buscarFormaPagamento(clienteId, formaPagamentoId));
    }

    @Transactional
    public FormaPagamentoResponse atualizar(Long clienteId, Long formaPagamentoId, FormaPagamentoRequest request) {
        buscarCliente(clienteId);
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        if (request.isPreferencial()) {
            desmarcarPreferenciais(clienteId);
        }
        String numeroCartao = request.getNumeroCartao().replaceAll("\\D", "");
        if (numeroCartao.length() != 16) {
            throw new RegraDeNegocioException("O numero do cartao deve possuir exatamente 16 digitos");
        }
        formaPagamento.setNomeTitular(request.getNomeTitular());
        formaPagamento.setTipoCartao(request.getTipoCartao());
        formaPagamento.setValidade(request.getValidade());
        formaPagamento.setBandeira(request.getBandeira());
        formaPagamento.setUltimosDigitos(numeroCartao.substring(numeroCartao.length() - 4));
        formaPagamento.setPreferencial(request.isPreferencial());
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    @Transactional
    public void excluir(Long clienteId, Long formaPagamentoId) {
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        formaPagamento.setAtivo(false);
        formaPagamentoRepository.save(formaPagamento);
    }

    @Transactional
    public void excluirDefinitivamente(Long clienteId, Long formaPagamentoId) {
        formaPagamentoRepository.delete(buscarFormaPagamento(clienteId, formaPagamentoId));
    }

    @Transactional
    public FormaPagamentoResponse reativar(Long clienteId, Long formaPagamentoId) {
        buscarCliente(clienteId);
        FormaPagamento formaPagamento = buscarFormaPagamento(clienteId, formaPagamentoId);
        formaPagamento.setAtivo(true);
        return new FormaPagamentoResponse(formaPagamentoRepository.save(formaPagamento));
    }

    private Cliente buscarCliente(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado: " + id));
    }

    private FormaPagamento buscarFormaPagamento(Long clienteId, Long id) {
        return formaPagamentoRepository.findByIdAndClienteId(id, clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Forma de pagamento nao encontrada: " + id));
    }

    private void desmarcarPreferenciais(Long clienteId) {
        formaPagamentoRepository.findByClienteId(clienteId).forEach(formaPagamento -> {
            formaPagamento.setPreferencial(false);
            formaPagamentoRepository.save(formaPagamento);
        });
    }
}