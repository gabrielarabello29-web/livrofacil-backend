package com.livrofacil.modulos.cliente.repository;

import com.livrofacil.modulos.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {

	List<Cliente> findByNomeContainingIgnoreCase(String nome);

	List<Cliente> findByEmailContainingIgnoreCase(String email);

	Optional<Cliente> findByEmailIgnoreCase(String email);

	Optional<Cliente> findByEmailAndSenha(String email, String senha);

	List<Cliente> findByNomeContainingIgnoreCaseAndEmailContainingIgnoreCase(String nome, String email);

	boolean existsByEmail(String email);

	boolean existsByTelefone(String telefone);

	boolean existsByEmailAndIdNot(String email, UUID id);

	boolean existsByTelefoneAndIdNot(String telefone, UUID id);

	boolean existsByCpf(String cpf);

	boolean existsByCpfAndIdNot(String cpf, UUID id);

	@Query("select coalesce(max(c.numeroRegistro), 0) from Cliente c")
	Long maiorNumeroRegistro();

	List<Cliente> findByDataExclusaoBeforeAndDadosAnonimizadosFalse(LocalDateTime data);

	@Query(value = """
			select distinct c.* from cliente c
			left join endereco e on e.cli_id = c.cli_id
			where (cast(:registro as bigint) is null or c.cli_numero_registro = cast(:registro as bigint))
			and (cast(:nome as text) is null or lower(c.cli_nome) like '%' || lower(cast(:nome as text)) || '%')
			and (cast(:email as text) is null or lower(c.cli_email) like '%' || lower(cast(:email as text)) || '%')
			and (cast(:cpf as text) is null or c.cli_cpf like '%' || cast(:cpf as text) || '%')
			and (cast(:telefone as text) is null or c.cli_telefone like '%' || cast(:telefone as text) || '%')
			and (cast(:dataNascimento as date) is null or c.cli_data_nascimento = cast(:dataNascimento as date))
			and (cast(:genero as text) is null or lower(c.cli_genero) like '%' || lower(cast(:genero as text)) || '%')
			and (cast(:tipoEndereco as text) is null or lower(e.end_tipo) like '%' || lower(cast(:tipoEndereco as text)) || '%')
			and (cast(:endereco as text) is null or lower(coalesce(e.end_logradouro, '') || ' ' || coalesce(e.end_numero, '')) like '%' || lower(cast(:endereco as text)) || '%')
			and (cast(:complemento as text) is null or lower(e.end_complemento) like '%' || lower(cast(:complemento as text)) || '%')
			and (cast(:bairro as text) is null or lower(e.end_bairro) like '%' || lower(cast(:bairro as text)) || '%')
			and (cast(:cidade as text) is null or lower(e.end_cidade) like '%' || lower(cast(:cidade as text)) || '%')
			and (cast(:estado as text) is null or lower(e.end_estado) like '%' || lower(cast(:estado as text)) || '%')
			and (cast(:cep as text) is null or e.end_cep like '%' || cast(:cep as text) || '%')
			""", nativeQuery = true)
	List<Cliente> buscarComFiltros(@Param("registro") Long registro, @Param("nome") String nome, @Param("email") String email,
			@Param("cpf") String cpf, @Param("telefone") String telefone,
			@Param("dataNascimento") LocalDate dataNascimento, @Param("genero") String genero,
			@Param("tipoEndereco") String tipoEndereco, @Param("endereco") String endereco,
			@Param("complemento") String complemento, @Param("bairro") String bairro,
			@Param("cidade") String cidade, @Param("estado") String estado,
			@Param("cep") String cep);
}