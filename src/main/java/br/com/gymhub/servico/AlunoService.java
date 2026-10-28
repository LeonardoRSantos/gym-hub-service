package br.com.gymhub.servico;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;

import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.controlador.resposta.AlunoSalvo;

public interface AlunoService {

	Page<AlunoSalvo> filtro(AlunoFiltro filtro, Pageable pageable);

	
}
