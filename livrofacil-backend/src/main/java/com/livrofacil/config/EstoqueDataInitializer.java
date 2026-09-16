package com.livrofacil.config;

import com.livrofacil.produtos.livro.entity.Estoque;
import com.livrofacil.produtos.livro.repository.EstoqueRepository;
import com.livrofacil.produtos.livro.repository.LivroRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EstoqueDataInitializer {

    @Bean
    CommandLineRunner sincronizarEstoques(LivroRepository livroRepository, EstoqueRepository estoqueRepository) {
        return args -> livroRepository.findAll().forEach(livro -> {
            if (estoqueRepository.findByLivroId(livro.getId()).isEmpty()) {
                estoqueRepository.save(new Estoque(livro));
            }
        });
    }
}