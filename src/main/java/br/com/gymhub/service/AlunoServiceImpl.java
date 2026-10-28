package br.com.gymhub.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gymhub.controller.request.AlunoFiltro;
import br.com.gymhub.controller.response.AlunoSalvo;
import br.com.gymhub.domain.entity.AlunoEntidade;
import br.com.gymhub.domain.repository.AlunoRepository;
import br.com.gymhub.domain.specs.AlunoSpecs;
import br.com.gymhub.mapper.AlunoMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlunoServiceImpl implements AlunoService{
	
	private final AlunoRepository alunoRepository;


	@Override
	@Transactional(readOnly = true)
	public Page<AlunoSalvo> filtro(AlunoFiltro filtro, Pageable pageable) {
		Specification<AlunoEntidade> spec = AlunoSpecs.filtro(filtro);
		Page<AlunoEntidade> alunoPage = alunoRepository.findAll(spec, pageable);
		return AlunoMapper.paraPaginaAlunoSalvo(alunoPage);
	}
	

}
