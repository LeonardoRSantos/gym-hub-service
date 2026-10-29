package br.com.gymhub.controlador.requisicao;

import java.time.LocalDate;

import org.hibernate.validator.constraints.br.CPF;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class AlunoCadastro {

	@NotBlank(message = "O nome do aluno é obrigatório.")
	private String nome;

	@NotBlank(message = "CPF é obrigatório")
	@CPF(message = "CPF inválido")
	private String cpf;

	@NotBlank(message = "E-mail é obrigatório")
	@Email(message = "E-mail inválido")
	@Size(max = 150)
	private String email;

	private LocalDate dataNascimento;
}
