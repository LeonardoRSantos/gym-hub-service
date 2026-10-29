package br.com.gymhub.mapeador;

import org.springframework.data.domain.Page;

import br.com.gymhub.controlador.requisicao.AlunoCadastro;
import br.com.gymhub.controlador.requisicao.AlunoEdicao;
import br.com.gymhub.controlador.resposta.AlunoSalvo;
import br.com.gymhub.dominio.entidade.AlunoEntidade;

public final class AlunoMapper {

	  private AlunoMapper() {

	  }

	public static AlunoSalvo paraAlunoSalvo(AlunoEntidade aluno) {
		return AlunoSalvo.builder()
				.uuid(aluno.getUuid())
				.nome(aluno.getNome())
				.cpf(aluno.getCpf())
				.email(aluno.getEmail())
				.dataNascimento(aluno.getDataNascimento())
				.criadoEm(aluno.getCriadoEm())
				.build();

	}

	public static Page<AlunoSalvo> paraPaginaAlunoSalvo(Page<AlunoEntidade> alunos){
		return alunos.map(AlunoMapper::paraAlunoSalvo);
	}

	public static AlunoEntidade paraEntidade(AlunoCadastro alunoCadastro) {

		return AlunoEntidade.builder()
				.nome(alunoCadastro.getNome())
				.cpf(alunoCadastro.getCpf())
				.email(alunoCadastro.getEmail())
				.dataNascimento(alunoCadastro.getDataNascimento())
				.build();
	}

	public static void atualizarAlunoEntidade(AlunoEntidade aluno, AlunoEdicao alunoEdicao) {
		if(alunoEdicao.getNome() != null && !alunoEdicao.getNome().isBlank()) {
			aluno.setNome(alunoEdicao.getNome());
		}
		if(alunoEdicao.getCpf() != null && !alunoEdicao.getCpf().isBlank()) {
			aluno.setCpf(alunoEdicao.getCpf());
		}
		if(alunoEdicao.getDataNascimento() != null) {
			aluno.setDataNascimento(alunoEdicao.getDataNascimento());
		}
		if(alunoEdicao.getEmail() != null && !alunoEdicao.getEmail().isBlank()) {
			aluno.setEmail(alunoEdicao.getEmail());
		}
	}
}
