package com.livrofacil.modulos.cliente.service;

import com.livrofacil.modulos.cliente.dto.ClienteResponse;
import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.repository.EnderecoRepository;
import com.livrofacil.exception.RegraDeNegocioException;
import com.livrofacil.modulos.compra.repository.PedidoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteUseCaseLoginTest {
    @Mock private ClienteRepository clienteRepository;
    @Mock private EnderecoRepository enderecoRepository;
    @Mock private PedidoRepository pedidoRepository;
    @InjectMocks private ClienteUseCase clienteUseCase;

    @Test
    void loginDeveRetornarClienteQuandoCredenciaisForemValidas() throws Exception {
        Cliente cliente = new Cliente("Cliente Padrao", "cliente@livrofacil.com", "(11) 99999-9999");
        setField(cliente, "id", UUID.randomUUID());
        cliente.setCpf("12345678909");
        cliente.setSenha("LivroFacil@2026");
        cliente.setAtivo("S");
        cliente.setPerfil("CLIENTE");
        when(clienteRepository.findByEmailAndSenha("cliente@livrofacil.com", "LivroFacil@2026"))
                .thenReturn(Optional.of(cliente));

        ClienteResponse resposta = clienteUseCase.login("cliente@livrofacil.com", "LivroFacil@2026");

        assertEquals(cliente.getId(), resposta.getId());
        assertEquals("cliente@livrofacil.com", resposta.getEmail());
        assertEquals("CLIENTE", resposta.getPerfil());
    }

    @Test
    void loginDeveLancarExcecaoQuandoSenhaForIncorreta() {
        when(clienteRepository.findByEmailAndSenha("cliente@livrofacil.com", "SenhaErrada@1"))
                .thenReturn(Optional.empty());

        assertThrows(RegraDeNegocioException.class,
                () -> clienteUseCase.login("cliente@livrofacil.com", "SenhaErrada@1"));
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
