package br.com.gymhub.service;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;

import br.com.gymhub.controller.request.AlunoFiltro;
import br.com.gymhub.controller.response.AlunoSalvo;

public interface AlunoService {

	Page<AlunoSalvo> filtro(AlunoFiltro filtro, Pageable pageable);

	
}
