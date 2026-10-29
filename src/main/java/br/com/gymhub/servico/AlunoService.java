package br.com.gymhub.servico;

import org.springframework.data.domain.Pageable;

import java.util.UUID;

import org.springframework.data.domain.Page;

import br.com.gymhub.controlador.requisicao.AlunoCadastro;
import br.com.gymhub.controlador.requisicao.AlunoEdicao;
import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.controlador.resposta.AlunoSalvo;
import br.com.gymhub.controlador.resposta.RespostaMensagem;
import jakarta.validation.Valid;

public interface AlunoService {

	Page<AlunoSalvo> filtro(AlunoFiltro filtro, Pageable pageable);

	AlunoSalvo salvar(AlunoCadastro alunoCadastro);

	AlunoSalvo atualizar(UUID uuid, @Valid AlunoEdicao alunoEdicao);

	AlunoSalvo buscarAlunoPorId(UUID uuid);

	RespostaMensagem deletarAluno(UUID uuid);
}
