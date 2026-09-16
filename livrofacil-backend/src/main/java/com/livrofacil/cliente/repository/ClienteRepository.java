package com.livrofacil.cliente.repository;

import com.livrofacil.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

	List<Cliente> findByNomeContainingIgnoreCase(String nome);

	List<Cliente> findByEmailContainingIgnoreCase(String email);

	Optional<Cliente> findByEmailIgnoreCase(String email);

	Optional<Cliente> findByEmailAndSenha(String email, String senha);

	List<Cliente> findByNomeContainingIgnoreCaseAndEmailContainingIgnoreCase(String nome, String email);

	boolean existsByEmail(String email);

	boolean existsByTelefone(String telefone);

	boolean existsByEmailAndIdNot(String email, Long id);

	boolean existsByTelefoneAndIdNot(String telefone, Long id);

	boolean existsByCpf(String cpf);

	boolean existsByCpfAndIdNot(String cpf, Long id);
}