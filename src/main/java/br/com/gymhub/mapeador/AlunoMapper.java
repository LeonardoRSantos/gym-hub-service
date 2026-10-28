package br.com.gymhub.mapeador;

import org.springframework.data.domain.Page;

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
}
