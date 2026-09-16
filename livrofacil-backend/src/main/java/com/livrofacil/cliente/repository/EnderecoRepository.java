package com.livrofacil.cliente.repository;

import com.livrofacil.cliente.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    List<Endereco> findByClienteId(Long clienteId);
    Optional<Endereco> findByIdAndClienteId(Long id, Long clienteId);
}