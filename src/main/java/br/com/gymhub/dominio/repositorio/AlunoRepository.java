package br.com.gymhub.dominio.repositorio;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.gymhub.controlador.requisicao.AlunoEdicao;
import br.com.gymhub.dominio.entidade.AlunoEntidade;

public interface AlunoRepository extends JpaRepository<AlunoEntidade, Long>, JpaSpecificationExecutor<AlunoEntidade> {

	boolean existsByCpf(String cpf);

	boolean existsByEmail(String email);

	@Query("select aluno from AlunoEntidade aluno where aluno.uuid = :uuid")
	Optional<AlunoEntidade> buscarPorUuid(@Param("uuid") UUID uuid);

}
