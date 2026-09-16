package com.livrofacil.cliente.service;

import com.livrofacil.cliente.dto.ClienteResponse;
import com.livrofacil.cliente.entity.Cliente;
import com.livrofacil.cliente.repository.ClienteRepository;
import com.livrofacil.cliente.repository.EnderecoRepository;
import com.livrofacil.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteUseCaseLoginTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @InjectMocks
    private ClienteUseCase clienteUseCase;

    @Test
    void loginDeveRetornarClienteQuandoCredenciaisForemValidas() throws Exception {
        Cliente cliente = new Cliente("Cliente Padrao", "cliente@livrofacil.com", "(11) 99999-9999");
        setField(cliente, "id", 10L);
        cliente.setCpf("12345678909");
        cliente.setSenha("LivroFacil@2026");
        cliente.setAtivo("S");
        cliente.setPerfil("CLIENTE");

        when(clienteRepository.findByEmailAndSenha("cliente@livrofacil.com", "LivroFacil@2026"))
                .thenReturn(Optional.of(cliente));

        ClienteResponse resposta = clienteUseCase.login("cliente@livrofacil.com", "LivroFacil@2026");

        assertEquals(10L, resposta.getId());
        assertEquals("cliente@livrofacil.com", resposta.getEmail());
        assertEquals("CLIENTE", resposta.getPerfil());
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void loginDeveLancarExcecaoQuandoSenhaForIncorreta() {
        when(clienteRepository.findByEmailAndSenha("cliente@livrofacil.com", "SenhaErrada@1"))
                .thenReturn(Optional.empty());

        assertThrows(RegraDeNegocioException.class,
                () -> clienteUseCase.login("cliente@livrofacil.com", "SenhaErrada@1"));
    }
}
