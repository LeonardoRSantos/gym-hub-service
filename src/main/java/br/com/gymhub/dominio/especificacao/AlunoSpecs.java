package br.com.gymhub.dominio.especificacao;

import java.util.LinkedList;
import java.util.Objects;

import org.springframework.data.jpa.domain.Specification;

import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.dominio.entidade.AlunoEntidade;
import jakarta.persistence.criteria.Predicate;

public class AlunoSpecs {
	
	public static Specification<AlunoEntidade> filtro(AlunoFiltro filtro) {		
		return (root, query, criteriaBuilder) -> {
			
			var predicates = new LinkedList<Predicate>();
			
			if(Objects.nonNull(filtro.getNome())) {
				predicates.add(criteriaBuilder.like
						(criteriaBuilder.lower(root.get("nome")),
								"%" + filtro.getNome() + "%"));
			}
			
			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};		
	}
}
