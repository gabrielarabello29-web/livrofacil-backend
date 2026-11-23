package com.livrofacil.modulos.chat.repository;

import com.livrofacil.modulos.chat.entity.PreferenciasLiterarias;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PreferenciasLiterariasRepository extends JpaRepository<PreferenciasLiterarias, UUID> {
    Optional<PreferenciasLiterarias> findByClienteId(UUID clienteId);
}
