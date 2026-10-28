package br.com.gymhub.servico;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.controlador.resposta.AlunoSalvo;
import br.com.gymhub.dominio.entidade.AlunoEntidade;
import br.com.gymhub.dominio.repositorio.AlunoRepository;
import br.com.gymhub.dominio.especificacao.AlunoSpecs;
import br.com.gymhub.mapeador.AlunoMapper;
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
