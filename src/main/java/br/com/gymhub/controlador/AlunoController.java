package br.com.gymhub.controlador;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.gymhub.controlador.requisicao.AlunoCadastro;
import br.com.gymhub.controlador.requisicao.AlunoEdicao;
import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.controlador.resposta.AlunoSalvo;
import br.com.gymhub.controlador.resposta.RespostaMensagem;
import br.com.gymhub.servico.AlunoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/aluno")
@RequiredArgsConstructor
public class AlunoController{

	private final AlunoService alunoService;

	@GetMapping
	public ResponseEntity<Page<AlunoSalvo>> filtro(AlunoFiltro filtro, Pageable pageable) {
		return ResponseEntity.ok(alunoService.filtro(filtro, pageable));
	}

	@PostMapping
	public ResponseEntity<AlunoSalvo> salvar(@Valid @RequestBody AlunoCadastro alunoCadastro){

		var resposta = alunoService.salvar(alunoCadastro);

		return ResponseEntity.ok(resposta);
	}

	@PutMapping("/{uuid}")
	public ResponseEntity<AlunoSalvo> atualizar(
			@PathVariable UUID uuid,
			@Valid @RequestBody AlunoEdicao alunoEdicao){

		var resposta = alunoService.atualizar(uuid, alunoEdicao);

		return ResponseEntity.ok(resposta);
	}

	@GetMapping("/{uuid}")
	public ResponseEntity<AlunoSalvo> buscarAlunoPorId(@PathVariable("uuid") UUID uuid){
		return ResponseEntity.ok(alunoService.buscarAlunoPorId(uuid));
	}

	@DeleteMapping("/{uuid}")
	public ResponseEntity<RespostaMensagem> deletar(@PathVariable("uuid") UUID uuid){
		return ResponseEntity.ok(alunoService.deletarAluno(uuid));
	}
}
