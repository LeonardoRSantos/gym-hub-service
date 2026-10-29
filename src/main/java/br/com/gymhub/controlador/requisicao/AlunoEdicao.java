package br.com.gymhub.controlador.requisicao;

import java.time.LocalDate;
import java.util.UUID;

import org.hibernate.validator.constraints.br.CPF;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class AlunoEdicao {

	@NotNull(message = "A identificação do aluno não pode ser nulo.")
	private UUID uuid;

	private String nome;

	@CPF(message = "CPF inválido")
	private String cpf;


	@Email(message = "E-mail inválido")
	@Size(max = 150)
	private String email;

	private LocalDate dataNascimento;
}
