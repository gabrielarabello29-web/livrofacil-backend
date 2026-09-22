package com.livrofacil.modulos.cliente.repository;

import com.livrofacil.modulos.cliente.entity.Endereco;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnderecoRepository extends JpaRepository<Endereco, Long> {
    List<Endereco> findByClienteId(UUID clienteId);
    Optional<Endereco> findByIdAndClienteId(Long id, UUID clienteId);
}