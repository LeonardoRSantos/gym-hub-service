package br.com.gymhub.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import br.com.gymhub.domain.entity.AlunoEntidade;

public interface AlunoRepository extends JpaRepository<AlunoEntidade, Long>, JpaSpecificationExecutor<AlunoEntidade> {	

}
