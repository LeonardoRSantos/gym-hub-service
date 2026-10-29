package br.com.gymhub.servico;

import br.com.gymhub.controlador.requisicao.AlunoCadastro;
import br.com.gymhub.controlador.requisicao.AlunoEdicao;
import br.com.gymhub.controlador.requisicao.AlunoFiltro;
import br.com.gymhub.dominio.entidade.AlunoEntidade;
import br.com.gymhub.dominio.repositorio.AlunoRepository;
import br.com.gymhub.excecao.RegraNegocioException;
import br.com.gymhub.validacao.AlunoValidacao;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlunoServiceImplTest {

    private AlunoRepository alunoRepository;
    private AlunoValidacao alunoValidacao;
    private AlunoServiceImpl alunoService;
    private AlunoEntidade aluno;

    @BeforeEach
    void setUp() {
        alunoRepository = mock(AlunoRepository.class);
        alunoValidacao = mock(AlunoValidacao.class);
        alunoService = new AlunoServiceImpl(alunoRepository, alunoValidacao);
        aluno = AlunoEntidade.builder().id(10L).nome("Ana Silva")
                .cpf("52998224725").email("ana@example.com")
                .dataNascimento(LocalDate.of(2000, 1, 2))
                .criadoEm(LocalDateTime.of(2026, 10, 29, 10, 0)).build();
    }

    @Test
    @DisplayName("Dado cadastro valido, deve validar antes de persistir e retornar os dados salvos")
    void dadoCadastroValidoDeveSalvarAluno() {
        var cadastro = AlunoCadastro.builder().nome(aluno.getNome()).cpf(aluno.getCpf())
                .email(aluno.getEmail()).dataNascimento(aluno.getDataNascimento()).build();
        when(alunoRepository.save(any(AlunoEntidade.class))).thenReturn(aluno);

        var resposta = alunoService.salvar(cadastro);

        var entidade = ArgumentCaptor.forClass(AlunoEntidade.class);
        var ordem = inOrder(alunoValidacao, alunoRepository);
        ordem.verify(alunoValidacao).validar(cadastro);
        ordem.verify(alunoRepository).save(entidade.capture());
        assertThat(entidade.getValue().getUuid()).isNotNull();
        assertThat(entidade.getValue().getId()).isNull();
        assertThat(entidade.getValue().getNome()).isEqualTo(cadastro.getNome());
        assertThat(entidade.getValue().getCpf()).isEqualTo(cadastro.getCpf());
        assertThat(entidade.getValue().getEmail()).isEqualTo(cadastro.getEmail());
        assertThat(entidade.getValue().getDataNascimento()).isEqualTo(cadastro.getDataNascimento());
        assertThat(resposta.getUuid()).isEqualTo(aluno.getUuid());
        assertThat(resposta.getCriadoEm()).isEqualTo(aluno.getCriadoEm());
    }

    @Test
    @DisplayName("Dado cadastro rejeitado pela validacao, nao deve persistir")
    void dadoCadastroInvalidoNaoDeveSalvar() {
        var cadastro = new AlunoCadastro();
        var falha = new RegraNegocioException("O nome do aluno é obrigatório.");
        doThrow(falha).when(alunoValidacao).validar(cadastro);

        assertThatThrownBy(() -> alunoService.salvar(cadastro)).isSameAs(falha);

        verify(alunoValidacao).validar(cadastro);
        verifyNoInteractions(alunoRepository);
    }

    @Test
    @DisplayName("Dado UUID existente, deve retornar aluno mapeado")
    void dadoUuidExistenteDeveBuscarAluno() {
        when(alunoRepository.buscarPorUuid(aluno.getUuid())).thenReturn(Optional.of(aluno));

        var resposta = alunoService.buscarAlunoPorId(aluno.getUuid());

        assertThat(resposta.getUuid()).isEqualTo(aluno.getUuid());
        assertThat(resposta.getNome()).isEqualTo(aluno.getNome());
        assertThat(resposta.getCpf()).isEqualTo(aluno.getCpf());
        assertThat(resposta.getEmail()).isEqualTo(aluno.getEmail());
        assertThat(resposta.getDataNascimento()).isEqualTo(aluno.getDataNascimento());
        verify(alunoRepository).buscarPorUuid(aluno.getUuid());
    }

    @Test
    @DisplayName("Dado UUID inexistente na busca, deve informar aluno nao encontrado")
    void dadoUuidInexistenteDeveRejeitarBusca() {
        var uuid = UUID.randomUUID();
        when(alunoRepository.buscarPorUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.buscarAlunoPorId(uuid))
                .isInstanceOf(RegraNegocioException.class).hasMessage("Aluno não encontrado.");

        verify(alunoRepository).buscarPorUuid(uuid);
    }

    @Test
    @DisplayName("Dada edicao completa, deve atualizar dados e preservar identidade e criacao")
    void dadaEdicaoCompletaDeveAtualizarAluno() {
        var uuid = aluno.getUuid();
        var criadoEm = aluno.getCriadoEm();
        var edicao = AlunoEdicao.builder().uuid(uuid).nome("Ana Atualizada")
                .cpf("11144477735").email("atualizada@example.com")
                .dataNascimento(LocalDate.of(2001, 5, 6)).build();
        when(alunoRepository.buscarPorUuid(uuid)).thenReturn(Optional.of(aluno));

        var resposta = alunoService.atualizar(uuid, edicao);

        assertThat(resposta.getNome()).isEqualTo(edicao.getNome());
        assertThat(resposta.getCpf()).isEqualTo(edicao.getCpf());
        assertThat(resposta.getEmail()).isEqualTo(edicao.getEmail());
        assertThat(resposta.getDataNascimento()).isEqualTo(edicao.getDataNascimento());
        assertThat(resposta.getUuid()).isEqualTo(uuid);
        assertThat(resposta.getCriadoEm()).isEqualTo(criadoEm);
        assertThat(aluno.getId()).isEqualTo(10L);
        verify(alunoRepository).buscarPorUuid(uuid);
    }

    @Test
    @DisplayName("Dada edicao parcial, deve preservar campos ausentes ou em branco")
    void dadaEdicaoParcialDevePreservarCampos() {
        var edicao = AlunoEdicao.builder().uuid(aluno.getUuid()).nome(" ").cpf("")
                .email("novo@example.com").build();
        when(alunoRepository.buscarPorUuid(aluno.getUuid())).thenReturn(Optional.of(aluno));

        var resposta = alunoService.atualizar(aluno.getUuid(), edicao);

        assertThat(resposta.getNome()).isEqualTo("Ana Silva");
        assertThat(resposta.getCpf()).isEqualTo("52998224725");
        assertThat(resposta.getDataNascimento()).isEqualTo(LocalDate.of(2000, 1, 2));
        assertThat(resposta.getEmail()).isEqualTo("novo@example.com");
    }

    @Test
    @DisplayName("Dado UUID inexistente na edicao, deve rejeitar a atualizacao")
    void dadoUuidInexistenteDeveRejeitarEdicao() {
        var uuid = UUID.randomUUID();
        when(alunoRepository.buscarPorUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.atualizar(uuid, new AlunoEdicao()))
                .isInstanceOf(RuntimeException.class).hasMessage("O identificador do aluno não existe");

        verify(alunoRepository).buscarPorUuid(uuid);
        verify(alunoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Dado UUID existente, deve excluir o aluno e retornar mensagem")
    void dadoUuidExistenteDeveExcluirAluno() {
        when(alunoRepository.buscarPorUuid(aluno.getUuid())).thenReturn(Optional.of(aluno));

        var resposta = alunoService.deletarAluno(aluno.getUuid());

        assertThat(resposta.message()).isEqualTo("Aluno deletado com sucesso!");
        verify(alunoRepository).buscarPorUuid(aluno.getUuid());
        verify(alunoRepository).delete(aluno);
    }

    @Test
    @DisplayName("Dado UUID nulo, deve rejeitar exclusao sem consultar o banco")
    void dadoUuidNuloDeveRejeitarExclusao() {
        assertThatThrownBy(() -> alunoService.deletarAluno(null))
                .isInstanceOf(RegraNegocioException.class).hasMessage("O ID é obrigatório para a exclusão.");

        verifyNoInteractions(alunoRepository);
    }

    @Test
    @DisplayName("Dado UUID inexistente, nao deve excluir nenhum aluno")
    void dadoUuidInexistenteDeveRejeitarExclusao() {
        var uuid = UUID.randomUUID();
        when(alunoRepository.buscarPorUuid(uuid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alunoService.deletarAluno(uuid))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Aluno com o ID fornecido não encontrado.");

        verify(alunoRepository).buscarPorUuid(uuid);
        verify(alunoRepository, never()).delete(any(AlunoEntidade.class));
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
