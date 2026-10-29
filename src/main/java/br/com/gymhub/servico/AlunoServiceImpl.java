package br.com.gymhub.servico;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.gymhub.controlador.requisicao.AlunoCadastro;
import br.com.gymhub.controlador.requisicao.AlunoEdicao;
import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.controlador.resposta.AlunoSalvo;
import br.com.gymhub.controlador.resposta.RespostaMensagem;
import br.com.gymhub.dominio.entidade.AlunoEntidade;
import br.com.gymhub.dominio.repositorio.AlunoRepository;
import br.com.gymhub.excecao.RegraNegocioException;
import br.com.gymhub.dominio.especificacao.AlunoSpecs;
import br.com.gymhub.mapeador.AlunoMapper;
import br.com.gymhub.validacao.AlunoValidacao;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AlunoServiceImpl implements AlunoService{

	private final AlunoRepository alunoRepository;
	private final AlunoValidacao alunoValidacao;
	private final String ALUNO_DELETADO_COM_SUCESSO = "Aluno deletado com sucesso!";


	@Override
	@Transactional(readOnly = true)
	public Page<AlunoSalvo> filtro(AlunoFiltro filtro, Pageable pageable) {
		Specification<AlunoEntidade> spec = AlunoSpecs.filtro(filtro);
		Page<AlunoEntidade> alunoPage = alunoRepository.findAll(spec, pageable);
		return AlunoMapper.paraPaginaAlunoSalvo(alunoPage);
	}


	@Override
	@Transactional
	public AlunoSalvo salvar(AlunoCadastro alunoCadastro) {

		alunoValidacao.validar(alunoCadastro);

		AlunoEntidade aluno = AlunoMapper.paraEntidade(alunoCadastro);
		AlunoEntidade alunoSalvo = alunoRepository.save(aluno);

		return AlunoMapper.paraAlunoSalvo(alunoSalvo);
	}


	@Override
	@Transactional
	public AlunoSalvo atualizar(UUID uuid, @Valid AlunoEdicao alunoEdicao) {

		AlunoEntidade alunoEntidade = alunoRepository.buscarPorUuid(uuid)
				.orElseThrow(() -> new RuntimeException("O identificador do aluno não existe"));

	    AlunoMapper.atualizarAlunoEntidade(alunoEntidade, alunoEdicao);

		return AlunoMapper.paraAlunoSalvo(alunoEntidade);
	}

	@Override
	@Transactional(readOnly = true)
	public AlunoSalvo buscarAlunoPorId(UUID uuid) {

	    return alunoRepository.buscarPorUuid(uuid)
	            .map(AlunoMapper::paraAlunoSalvo)
	            .orElseThrow(() ->
	                    new RegraNegocioException("Aluno não encontrado."));
	}


	@Override
	@Transactional
	public RespostaMensagem deletarAluno(UUID uuid) {
		AlunoEntidade alunoEntidade = validarUuidERetornaAluno(uuid);
		alunoRepository.delete(alunoEntidade);
		return new RespostaMensagem(ALUNO_DELETADO_COM_SUCESSO);
	}

	private AlunoEntidade validarUuidERetornaAluno(UUID uuid) {
		if(Objects.isNull(uuid)) {
			throw new RegraNegocioException("O ID é obrigatório para a exclusão.");
		}

		return alunoRepository.buscarPorUuid(uuid)
				.orElseThrow(() -> new RegraNegocioException("Aluno com o ID fornecido não encontrado."));
	}
}
