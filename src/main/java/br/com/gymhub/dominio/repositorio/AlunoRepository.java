package br.com.gymhub.dominio.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.gymhub.dominio.entidade.AlunoEntidade;

public interface AlunoRepository extends JpaRepository<AlunoEntidade, Long>, JpaSpecificationExecutor<AlunoEntidade> {	

}
