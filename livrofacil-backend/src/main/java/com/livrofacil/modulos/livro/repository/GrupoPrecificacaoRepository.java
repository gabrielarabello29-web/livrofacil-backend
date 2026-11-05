package com.livrofacil.modulos.livro.repository;

import com.livrofacil.modulos.livro.entity.GrupoPrecificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GrupoPrecificacaoRepository extends JpaRepository<GrupoPrecificacao, Long> {
    Optional<GrupoPrecificacao> findFirstByNomeIgnoreCase(String nome);
    List<GrupoPrecificacao> findByNomeContainingIgnoreCase(String nome);
}
