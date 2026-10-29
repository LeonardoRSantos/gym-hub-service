package br.com.gymhub.validacao;

import org.springframework.stereotype.Component;

import br.com.gymhub.controlador.requisicao.AlunoCadastro;
import br.com.gymhub.dominio.repositorio.AlunoRepository;
import br.com.gymhub.excecao.RegraNegocioException;
import br.com.gymhub.util.CpfValidacao;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class AlunoValidacao {

	private final AlunoRepository alunoRepository;

	public void validar(AlunoCadastro alunoCadastro) {
		validarCadastro(alunoCadastro);

		if(!CpfValidacao.validar(alunoCadastro.getCpf())) {
			throw new RegraNegocioException("O CPF é inválido, informar um CPF válido.");
		}
		if(alunoRepository.existsByCpf(alunoCadastro.getCpf())) {
			throw new RegraNegocioException("O CPF já está cadastrado, informar um novo CPF.");
		}
		if(alunoRepository.existsByEmail(alunoCadastro.getEmail())) {
            throw new RegraNegocioException("E-mail já cadastrado.");
		}
	}


	private void validarCadastro(AlunoCadastro alunoCadastro) {
		if(alunoCadastro == null) {
			throw new RegraNegocioException("O Aluno não pode ser nulo.");
		}
		if(alunoCadastro.getNome() == null || alunoCadastro.getNome().isBlank()) {
			throw new RegraNegocioException("O nome do aluno é obrigatório.");
		}
		if(alunoCadastro.getCpf() == null || alunoCadastro.getCpf().isBlank()) {
			throw new RegraNegocioException("O CPF é obrigatório.");
		}
		if(alunoCadastro.getDataNascimento() == null) {
			throw new RegraNegocioException("A data de nascimento é obrigatória.");
		}
		if(alunoCadastro.getEmail() == null || alunoCadastro.getEmail().isBlank()) {
			throw new RegraNegocioException("O e-mail é obrigatório.");
		}

		String cpfLimpo = alunoCadastro.getCpf() != null ?
				alunoCadastro.getCpf().replaceAll("\\D", "")
				: null;
		if(cpfLimpo.length() != 11) {
			throw new RegraNegocioException("O CPF deve conter exatamente 11 dígitos numéricos.");
		}
	}
}
