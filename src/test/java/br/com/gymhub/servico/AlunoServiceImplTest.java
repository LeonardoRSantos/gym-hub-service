package br.com.gymhub.servico;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.dominio.entidade.AlunoEntidade;
import br.com.gymhub.dominio.repositorio.AlunoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlunoServiceImplTest {

    private AlunoServiceImpl alunoService;
    private AlunoRepository alunoRepository;

    @BeforeEach
    void setUp() {
        alunoRepository = mock(AlunoRepository.class);
        alunoService = new AlunoServiceImpl(alunoRepository);
    }

    @Test
    @DisplayName("Dado filtro com resultados, deve retornar pagina com os dados do aluno")
    void dadoFiltroComResultadosDeveRetornarAlunos() {
        var filtro = AlunoFiltro.builder().nome("Ana").build();
        var pageable = PageRequest.of(1, 5);
        var aluno = AlunoEntidade.builder()
                .uuid(UUID.randomUUID())
                .nome("Ana Silva")
                .cpf("12345678909")
                .email("ana@example.com")
                .dataNascimento(LocalDate.of(2000, 1, 2))
                .criadoEm(LocalDateTime.of(2026, 10, 28, 10, 0))
                .build();
        when(alunoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(aluno), pageable, 6));

        var resposta = alunoService.filtro(filtro, pageable);

        assertThat(resposta.getTotalElements()).isEqualTo(6);
        assertThat(resposta.getPageable()).isEqualTo(pageable);
        assertThat(resposta.getContent()).singleElement().satisfies(salvo -> {
            assertThat(salvo.getUuid()).isEqualTo(aluno.getUuid());
            assertThat(salvo.getNome()).isEqualTo(aluno.getNome());
            assertThat(salvo.getCpf()).isEqualTo(aluno.getCpf());
            assertThat(salvo.getEmail()).isEqualTo(aluno.getEmail());
            assertThat(salvo.getDataNascimento()).isEqualTo(aluno.getDataNascimento());
            assertThat(salvo.getCriadoEm()).isEqualTo(aluno.getCriadoEm());
        });
        verify(alunoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Dado filtro sem resultados, deve retornar pagina vazia")
    void dadoFiltroSemResultadosDeveRetornarPaginaVazia() {
        var filtro = AlunoFiltro.builder().nome("Inexistente").build();
        var pageable = PageRequest.of(0, 10);
        when(alunoRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        var resposta = alunoService.filtro(filtro, pageable);

        assertThat(resposta.getContent()).isEmpty();
        assertThat(resposta.getTotalElements()).isZero();
        assertThat(resposta.getPageable()).isEqualTo(pageable);
        verify(alunoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    @DisplayName("Dada falha no repository, deve propagar a excecao")
    void dadaFalhaNoRepositoryDevePropagarExcecao() {
        var filtro = new AlunoFiltro();
        var pageable = PageRequest.of(0, 10);
        var falha = new IllegalStateException("Falha ao consultar alunos");
        when(alunoRepository.findAll(any(Specification.class), eq(pageable))).thenThrow(falha);

        assertThatThrownBy(() -> alunoService.filtro(filtro, pageable)).isSameAs(falha);

        verify(alunoRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
}
