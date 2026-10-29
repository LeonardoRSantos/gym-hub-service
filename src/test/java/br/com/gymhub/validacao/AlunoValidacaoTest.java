package br.com.gymhub.validacao;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.stream.Stream;

import br.com.gymhub.controlador.requisicao.AlunoCadastro;
import br.com.gymhub.dominio.repositorio.AlunoRepository;
import br.com.gymhub.excecao.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AlunoValidacaoTest {

    private AlunoRepository alunoRepository;
    private AlunoValidacao alunoValidacao;
    private AlunoCadastro cadastro;

    @BeforeEach
    void setUp() {
        alunoRepository = mock(AlunoRepository.class);
        alunoValidacao = new AlunoValidacao(alunoRepository);
        cadastro = AlunoCadastro.builder().nome("Ana Silva").cpf("52998224725")
                .email("ana@example.com").dataNascimento(LocalDate.of(2000, 1, 2)).build();
    }

    @Test
    @DisplayName("Dado cadastro valido e unico, deve aceitar")
    void dadoCadastroValidoDeveAceitar() {
        assertThatCode(() -> alunoValidacao.validar(cadastro)).doesNotThrowAnyException();

        verify(alunoRepository).existsByCpf(cadastro.getCpf());
        verify(alunoRepository).existsByEmail(cadastro.getEmail());
    }

    @Test
    @DisplayName("Dado cadastro nulo, deve rejeitar antes de consultar o banco")
    void dadoCadastroNuloDeveRejeitar() {
        assertThatThrownBy(() -> alunoValidacao.validar(null))
                .isInstanceOf(RegraNegocioException.class).hasMessage("O Aluno não pode ser nulo.");

        verifyNoInteractions(alunoRepository);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cadastrosInvalidos")
    @DisplayName("Dados obrigatorios ausentes ou CPF invalido devem ser rejeitados")
    void dadoCadastroInvalidoDeveRejeitar(String cenario, Consumer<AlunoCadastro> alterar, String mensagem) {
        alterar.accept(cadastro);

        assertThatThrownBy(() -> alunoValidacao.validar(cadastro))
                .isInstanceOf(RegraNegocioException.class).hasMessage(mensagem);

        verifyNoInteractions(alunoRepository);
    }

    static Stream<Arguments> cadastrosInvalidos() {
        return Stream.of(
                invalido("Nome nulo", c -> c.setNome(null), "O nome do aluno é obrigatório."),
                invalido("Nome em branco", c -> c.setNome(" "), "O nome do aluno é obrigatório."),
                invalido("CPF nulo", c -> c.setCpf(null), "O CPF é obrigatório."),
                invalido("CPF em branco", c -> c.setCpf(" "), "O CPF é obrigatório."),
                invalido("Nascimento nulo", c -> c.setDataNascimento(null), "A data de nascimento é obrigatória."),
                invalido("Email nulo", c -> c.setEmail(null), "O e-mail é obrigatório."),
                invalido("Email em branco", c -> c.setEmail(" "), "O e-mail é obrigatório."),
                invalido("CPF curto", c -> c.setCpf("123"), "O CPF deve conter exatamente 11 dígitos numéricos."),
                invalido("CPF com digito incorreto", c -> c.setCpf("52998224724"), "O CPF é inválido, informar um CPF válido."),
                invalido("CPF com digitos repetidos", c -> c.setCpf("11111111111"), "O CPF é inválido, informar um CPF válido.")
        );
    }

    private static Arguments invalido(String nome, Consumer<AlunoCadastro> alterar, String mensagem) {
        return Arguments.of(nome, alterar, mensagem);
    }

    @Test
    @DisplayName("Dado CPF ja cadastrado, deve rejeitar antes de consultar email")
    void dadoCpfDuplicadoDeveRejeitar() {
        when(alunoRepository.existsByCpf(cadastro.getCpf())).thenReturn(true);

        assertThatThrownBy(() -> alunoValidacao.validar(cadastro))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O CPF já está cadastrado, informar um novo CPF.");

        verify(alunoRepository).existsByCpf(cadastro.getCpf());
        verify(alunoRepository, never()).existsByEmail(anyString());
    }

    @Test
    @DisplayName("Dado email ja cadastrado, deve rejeitar")
    void dadoEmailDuplicadoDeveRejeitar() {
        when(alunoRepository.existsByEmail(cadastro.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> alunoValidacao.validar(cadastro))
                .isInstanceOf(RegraNegocioException.class).hasMessage("E-mail já cadastrado.");

        verify(alunoRepository).existsByCpf(cadastro.getCpf());
        verify(alunoRepository).existsByEmail(cadastro.getEmail());
    }
}
