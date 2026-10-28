package br.com.gymhub.controlador;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.controlador.resposta.AlunoSalvo;
import br.com.gymhub.servico.AlunoService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/aluno")
@RequiredArgsConstructor
public class AlunoController{
	
	private final AlunoService alunoService;
	
	@GetMapping
	public ResponseEntity<Page<AlunoSalvo>> filtro(AlunoFiltro filtro, Pageable pageable){
		return ResponseEntity.ok(alunoService.filtro(filtro, pageable));
	}
}
