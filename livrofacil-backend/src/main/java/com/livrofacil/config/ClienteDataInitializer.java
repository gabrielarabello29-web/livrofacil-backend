package com.livrofacil.config;

import com.livrofacil.modulos.cliente.entity.Cliente;
import com.livrofacil.modulos.cliente.repository.ClienteRepository;
import com.livrofacil.modulos.cliente.entity.BandeiraPagamento;
import com.livrofacil.modulos.cliente.repository.BandeiraPagamentoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class ClienteDataInitializer {

    @Bean
    CommandLineRunner inserirClientesPadrao(ClienteRepository clienteRepository,
                                            BandeiraPagamentoRepository bandeiraPagamentoRepository) {
        return args -> {
            inserirBandeirasPadrao(bandeiraPagamentoRepository);
            criarClientePadrao(
                    clienteRepository,
                    "cliente@livrofacil.com",
                    "LivroFacil@2026",
                    "Cliente Padrao",
                    "12345678909",
                    "(11) 99999-9999",
                    "FEMININO"
            );

            criarClientePadrao(
                    clienteRepository,
                    "admin@livrofacil.com",
                    "Admin@123",
                    "Administrador",
                    "98765432100",
                    "(11) 98888-8888",
                    "MASCULINO"
            );
        };
    }

    private void inserirBandeirasPadrao(BandeiraPagamentoRepository repository) {
        for (String nome : new String[]{"VISA", "MASTERCARD", "ELO", "AMEX", "HIPERCARD"}) {
            if (repository.findByNomeIgnoreCase(nome).isEmpty()) {
                repository.save(new BandeiraPagamento(nome));
            }
        }
    }

    private void criarClientePadrao(ClienteRepository clienteRepository,
                                   String email,
                                   String senha,
                                   String nome,
                                   String cpf,
                                   String telefone,
                                   String genero) {
        Cliente clienteExistente = clienteRepository.findByEmailIgnoreCase(email).orElse(null);

        if (clienteExistente == null) {
            Cliente cliente = new Cliente(nome, email, telefone);
            cliente.setSenha(senha);
            cliente.setCpf(cpf);
            cliente.setNumeroRegistro(clienteRepository.maiorNumeroRegistro() + 1);
            cliente.setPerfil(email.equalsIgnoreCase("admin@livrofacil.com") ? "ADMIN" : "CLIENTE");
            cliente.setDataNascimento(LocalDate.of(1990, 1, 1));
            cliente.setGenero(genero);
            clienteRepository.save(cliente);
            return;
        }

        String perfilEsperado = email.equalsIgnoreCase("admin@livrofacil.com") ? "ADMIN" : "CLIENTE";
        boolean senhaDivergente = clienteExistente.getSenha() == null || !senha.equals(clienteExistente.getSenha());
        boolean perfilDivergente = !perfilEsperado.equalsIgnoreCase(clienteExistente.getPerfil());

        if (senhaDivergente || perfilDivergente) {
            clienteExistente.setSenha(senha);
            clienteExistente.setNome(nome);
            clienteExistente.setTelefone(telefone);
            clienteExistente.setCpf(cpf);
            clienteExistente.setPerfil(perfilEsperado);
            clienteExistente.setGenero(genero);
            clienteExistente.setDataNascimento(LocalDate.of(1990, 1, 1));
            clienteRepository.save(clienteExistente);
        }
    }
}