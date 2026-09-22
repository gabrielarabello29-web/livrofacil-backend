package com.livrofacil.modulos.cliente.service;

import com.livrofacil.exception.RecursoNaoEncontradoException;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.cliente.dto.AtualizarBandeiraPagamentoRequest;
import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;
import com.livrofacil.modulos.cliente.repository.BandeiraPagamentoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BandeiraPagamentoUseCaseTest {
    @Mock private BandeiraPagamentoRepository repository;
    @InjectMocks private BandeiraPagamentoUseCase useCase;

    @Test
    void deveListarBandeiras() {
        when(repository.findAll()).thenReturn(List.of(new BandeiraPagamento("VISA")));

        assertEquals(1, useCase.listar().size());
    }

    @Test
    void deveAtualizarDisponibilidade() {
        BandeiraPagamento bandeira = new BandeiraPagamento("VISA");
        AtualizarBandeiraPagamentoRequest request = new AtualizarBandeiraPagamentoRequest();
        request.setDisponivel(false);
        when(repository.findById(1L)).thenReturn(Optional.of(bandeira));
        when(repository.save(bandeira)).thenReturn(bandeira);

        var response = useCase.atualizar(1L, request);

        assertEquals(false, response.isDisponivel());
    }

    @Test
    void deveRejeitarBandeiraInexistenteAoAtualizar() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.atualizar(1L, request()));
    }

    @Test
    void deveBuscarBandeiraDisponivel() {
        BandeiraPagamento bandeira = new BandeiraPagamento("VISA");
        when(repository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.of(bandeira));

        assertEquals(bandeira, useCase.buscarDisponivel("VISA"));
    }

    @Test
    void deveRejeitarBandeiraIndisponivel() {
        BandeiraPagamento bandeira = new BandeiraPagamento("VISA");
        bandeira.setDisponivel(false);
        when(repository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.of(bandeira));

        assertThrows(RegraDeNegocioException.class, () -> useCase.buscarDisponivel("VISA"));
    }

    @Test
    void deveRejeitarBandeiraNaoEncontrada() {
        when(repository.findByNomeIgnoreCase("VISA")).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> useCase.buscarDisponivel("VISA"));
    }

    private AtualizarBandeiraPagamentoRequest request() {
        AtualizarBandeiraPagamentoRequest request = new AtualizarBandeiraPagamentoRequest();
        request.setDisponivel(true);
        return request;
    }
}
