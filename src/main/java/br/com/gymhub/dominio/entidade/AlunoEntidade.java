package br.com.gymhub.dominio.entidade;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "aluno")
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AlunoEntidade {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aluno_seq")
	@SequenceGenerator(name = "aluno_seq", sequenceName = "aluno_seq", allocationSize = 1)
	private Long id;
	
	@Column(name = "uuid", nullable = false, updatable = false)
	@Builder.Default
	private UUID uuid = UUID.randomUUID();
	
	@Column(nullable = false)
	private String nome;
	
	@Column(nullable = false, unique = true)
	private String cpf;
	
	@Column(nullable = false, unique = true)
	private String email;
	
	@Column(name = "data_nascimento", nullable = false, unique = true)
    private LocalDate dataNascimento;
	
    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;
}
