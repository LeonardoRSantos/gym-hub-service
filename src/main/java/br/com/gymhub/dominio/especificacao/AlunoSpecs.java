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
			
			String cpfLimpo = filtro.getCpf() != null ?					
					filtro.getCpf().replaceAll("\\D", "")
					: null; 
			
			if(Objects.nonNull(cpfLimpo) && !cpfLimpo.isBlank()) {
				predicates.add(criteriaBuilder.like
						(criteriaBuilder.lower(root.get("cpf")),
								"%" + cpfLimpo + "%"));
			}
			
			if(Objects.nonNull(filtro.getEmail())) {
				predicates.add(criteriaBuilder.like
						(criteriaBuilder.lower(root.get("email")),
								"%" + filtro.getEmail() + "%"));
			}
			
			if(Objects.nonNull(filtro.getDataNascimento())) {
				predicates.add(criteriaBuilder.equal(root.get("dataNascimento"),
						filtro.getDataNascimento()));
			}
			

			return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
		};		
	}
}
