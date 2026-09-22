package com.livrofacil.modulos.cliente.service;

import com.livrofacil.modulos.cliente.dto.AtualizarBandeiraPagamentoRequest;
import com.livrofacil.modulos.cliente.dto.BandeiraPagamentoResponse;
import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;
import com.livrofacil.modulos.cliente.repository.BandeiraPagamentoRepository;
import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.exception.RegraDeNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BandeiraPagamentoUseCase {
    private final BandeiraPagamentoRepository repository;

    public BandeiraPagamentoUseCase(BandeiraPagamentoRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<BandeiraPagamentoResponse> listar() {
        return repository.findAll().stream().map(BandeiraPagamentoResponse::new).toList();
    }

    @Transactional
    public BandeiraPagamentoResponse atualizar(Long id, AtualizarBandeiraPagamentoRequest request) {
        BandeiraPagamento bandeira = repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bandeira de pagamento nao encontrada: " + id));
        bandeira.setDisponivel(request.getDisponivel());
        return new BandeiraPagamentoResponse(repository.save(bandeira));
    }

    @Transactional(readOnly = true)
    public BandeiraPagamento buscarDisponivel(String nome) {
        BandeiraPagamento bandeira = repository.findByNomeIgnoreCase(nome)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bandeira de pagamento nao encontrada: " + nome));
        if (!bandeira.isDisponivel()) {
            throw new RegraDeNegocioException("A bandeira de pagamento nao esta disponivel");
        }
        return bandeira;
    }
}
