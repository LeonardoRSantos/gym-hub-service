package br.com.gymhub.controller.response;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.UUID;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlunoSalvo {
	private UUID uuid;
	private String nome;	
    private String cpf;	
	private String email;
	private LocalDate dataNascimento;
	private LocalDateTime criadoEm;
}
