package br.com.gymhub.controlador;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Transactional
@Sql("/db/aluno/insert_alunos.sql")
@DisplayName("AlunoController - integracao com PostgreSQL")
class AlunoControllerIT {

    private static final String BASE_URL = "/api/v1/aluno";
    private static final String UUID_EXISTENTE = "123e4567-e89b-12d3-a456-426614174001";
    private static final String UUID_INEXISTENTE = "00000000-0000-0000-0000-000000000000";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private EntityManager entityManager;

    private ObjectNode lerRequisicao(String arquivo) throws Exception {
        try (var input = Objects.requireNonNull(getClass().getResourceAsStream("/samples/aluno/" + arquivo))) {
            return (ObjectNode) objectMapper.readTree(new String(input.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    @Nested
    @DisplayName("Filtros de alunos")
    class FiltrarAlunos {

        @Test
        @DisplayName("Dado nome parcial, deve filtrar e paginar os resultados")
        void dadoNomeParcialDeveFiltrar() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL).param("nome", "ana")
                            .param("page", "0").param("size", "1").param("sort", "nome,asc"));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].nome").value("Ana Silva"));
        }

        @Test
        @DisplayName("Dado email parcial, deve consultar os alunos correspondentes")
        void dadoEmailParcialDeveFiltrar() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL).param("email", "bruno@"));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].nome").value("Bruno Lima"));
        }

        @Test
        @DisplayName("Dado nascimento, deve consultar por data exata")
        void dadaDataNascimentoDeveFiltrar() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL).param("dataNascimento", "2000-01-02"));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].nome").value("Ana Silva"));
        }

        @Test
        @DisplayName("Dados filtros de pessoas distintas, deve combinar criterios com AND")
        void dadosFiltrosIncompativeisDeveRetornarPaginaVazia() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL).param("nome", "ana").param("email", "bruno@"));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(0))
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("Dado CPF com pontuacao, deve normalizar e consultar")
        void dadoCpfComPontuacaoDeveFiltrar() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL).param("cpf", "123.456.789-09"));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].nome").value("Ana Silva"));
        }


        @Test
        @DisplayName("Dado nome existente, deve retornar alunos filtrados e paginados")
        void dadoNomeExistenteDeveRetornarAlunosPaginados() throws Exception {

            ResultActions resposta = mockMvc.perform(get(BASE_URL)
                    .param("page", "0")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON));

            resposta.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isNotEmpty())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.content.length()").value(3))
                    .andExpect(jsonPath("$.content[0].uuid").value("123e4567-e89b-12d3-a456-426614174001"))
                    .andExpect(jsonPath("$.content[0].nome").value("Ana Silva"))
                    .andExpect(jsonPath("$.content[0].dataNascimento").value("2000-01-02"))
                    .andExpect(jsonPath("$.content[0].criadoEm").value("2026-10-28T10:00:00"));
        }

        @Test
        @DisplayName("Dado nome inexistente, deve retornar pagina vazia")
        void dadoNomeInexistenteDeveRetornarPaginaVazia() throws Exception {
               ResultActions resposta = mockMvc.perform(get(BASE_URL)
                       .param("page", "0")
                       .param("size", "10")
                       .queryParam("nome", "Inexistente")
                       .contentType(MediaType.APPLICATION_JSON));

               resposta.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Sem filtro, deve retornar todos os alunos ordenados")
        void semFiltroDeveRetornarTodosOsAlunos() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL)
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "nome,asc")
                    .contentType(MediaType.APPLICATION_JSON));

             resposta.andExpect(status().isOk())
                     .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.content[2].nome").value("Bruno Lima"));
        }

        @Test
        @DisplayName("Dado cpf existente, deve retornar os alunos filtrados e paginados")
        void dadoCpfExistenteDeveRetornarAlunosPaginados() throws Exception{
             ResultActions resposta = mockMvc.perform(get(BASE_URL)
                     .param("page", "0")
                     .param("size", "10")
                     .queryParam("cpf", "1")
                     .contentType(MediaType.APPLICATION_JSON));

             resposta.andExpect(status().isOk())
                     .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                   .andExpect(jsonPath("$.content").isNotEmpty())
                   .andExpect(jsonPath("$.totalElements").value(3))
                   .andExpect(jsonPath("$.content[0].cpf").value("12345678909"))
                        .andExpect(jsonPath("$.content[1].cpf").value("98765432100"))
                        .andExpect(jsonPath("$.content[2].cpf").value("11144477735"));
        }

        @Test
        @DisplayName("Dado cpf exato, deve retornar o aluno equivalente filtrado e paginado")
        void dadoCpfEquivalenteDeveRetornarAlunoPaginado() throws Exception{
             ResultActions resposta = mockMvc.perform(get(BASE_URL)
                     .param("page", "0")
                     .param("size", "10")
                     .queryParam("cpf", "12345678909")
                     .contentType(MediaType.APPLICATION_JSON));

             resposta.andExpect(status().isOk())
                     .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                   .andExpect(jsonPath("$.content").isNotEmpty())
                   .andExpect(jsonPath("$.totalElements").value(1))
                   .andExpect(jsonPath("$.content[0].cpf").value("12345678909"));
        }

    }

    @Nested
    @DisplayName("Cadastrar aluno")
    class CadastrarAluno {

        @Test
        @DisplayName("Dado cadastro valido, deve persistir e retornar o aluno com UUID")
        void dadoCadastroValidoDevePersistirAluno() throws Exception {
            var json = lerRequisicao("cadastro_valido.json");

            ResultActions resposta = mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON)
                            .content(json.toString()));

            resposta.andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.uuid").isNotEmpty())
                    .andExpect(jsonPath("$.nome").value("Carla Santos"))
                    .andExpect(jsonPath("$.cpf").value("52998224725"))
                    .andExpect(jsonPath("$.email").value("carla@example.com"))
                    .andExpect(jsonPath("$.dataNascimento").value("1990-07-08"));

            var uuid = UUID.fromString(objectMapper.readTree(resposta.andReturn().getResponse()
                    .getContentAsString()).get("uuid").asText());
            entityManager.flush();
            assertThat(jdbcTemplate.queryForObject("select count(*) from aluno where uuid = ? and cpf = ?",
                    Long.class, uuid, "52998224725")).isEqualTo(1);
            assertThat(jdbcTemplate.queryForObject("select criado_em from aluno where uuid = ?",
                    java.sql.Timestamp.class, uuid)).isNotNull();
        }

        @ParameterizedTest(name = "Campo obrigatorio: {0}")
        @ValueSource(strings = {"nome", "cpf", "email", "dataNascimento"})
        @DisplayName("Dados obrigatorios ausentes devem retornar 400 sem gravar")
        void dadoCampoObrigatorioAusenteDeveRejeitar(String campo) throws Exception {
            var json = lerRequisicao("cadastro_valido.json");
            json.putNull(campo);

            ResultActions resposta = mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(json.toString()));

            resposta.andExpect(status().isBadRequest());

            assertThat(jdbcTemplate.queryForObject("select count(*) from aluno", Long.class)).isEqualTo(3);
        }

        @ParameterizedTest(name = "Campo invalido: {0}")
        @ValueSource(strings = {"nome", "cpf", "email"})
        @DisplayName("Nome em branco, CPF ou email invalidos devem retornar 400")
        void dadoCampoInvalidoDeveRejeitar(String campo) throws Exception {
            var json = lerRequisicao("cadastro_valido.json");
            json.put(campo, campo.equals("nome") ? " " : "invalido");

            ResultActions resposta = mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(json.toString()));

            resposta.andExpect(status().isBadRequest());

            assertThat(jdbcTemplate.queryForObject("select count(*) from aluno", Long.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("Dado CPF duplicado, deve retornar mensagem de negocio e nao gravar")
        void dadoCpfDuplicadoDeveRejeitar() throws Exception {
            var json = lerRequisicao("cadastro_valido.json");
            json.put("cpf", "12345678909");

            ResultActions resposta = mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(json.toString()));

            resposta.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.mensagem").value("O CPF já está cadastrado, informar um novo CPF."))
                    .andExpect(jsonPath("$.dataHora").exists());

            assertThat(jdbcTemplate.queryForObject("select count(*) from aluno", Long.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("Dado email duplicado, deve retornar mensagem de negocio e nao gravar")
        void dadoEmailDuplicadoDeveRejeitar() throws Exception {
            var json = lerRequisicao("cadastro_valido.json");
            json.put("email", "ana@example.com");

            ResultActions resposta = mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content(json.toString()));

            resposta.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado."));

            assertThat(jdbcTemplate.queryForObject("select count(*) from aluno", Long.class)).isEqualTo(3);
        }

        @Test
        @DisplayName("Dado JSON malformado, deve retornar 400")
        void dadoJsonMalformadoDeveRejeitar() throws Exception {
            ResultActions resposta = mockMvc.perform(post(BASE_URL).contentType(MediaType.APPLICATION_JSON).content("{"));

            resposta.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Consultar aluno por UUID")
    class ConsultarAluno {

        @Test
        @DisplayName("Dado UUID existente, deve retornar o aluno correspondente")
        void dadoUuidExistenteDeveRetornarAluno() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL + "/{uuid}", UUID_EXISTENTE));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(UUID_EXISTENTE))
                    .andExpect(jsonPath("$.nome").value("Ana Silva"))
                    .andExpect(jsonPath("$.cpf").value("12345678909"));
        }

        @Test
        @DisplayName("Dado UUID inexistente, deve retornar 400 conforme o handler de negocio")
        void dadoUuidInexistenteDeveRetornarErro() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL + "/{uuid}", UUID_INEXISTENTE));

            resposta.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value("Aluno não encontrado."));
        }

        @Test
        @DisplayName("Dado identificador sem formato UUID, deve retornar 400")
        void dadoUuidMalformadoDeveRejeitar() throws Exception {
            ResultActions resposta = mockMvc.perform(get(BASE_URL + "/invalido"));

            resposta.andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Atualizar aluno")
    class AtualizarAluno {

        @Test
        @DisplayName("Dada edicao completa, deve persistir alteracoes e preservar UUID e criacao")
        void dadaEdicaoCompletaDeveAtualizarAluno() throws Exception {
            var json = lerRequisicao("edicao_valida.json");

            ResultActions resposta = mockMvc.perform(put(BASE_URL + "/{uuid}", UUID_EXISTENTE)
                            .contentType(MediaType.APPLICATION_JSON).content(json.toString()));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.uuid").value(UUID_EXISTENTE))
                    .andExpect(jsonPath("$.nome").value("Ana Atualizada"))
                    .andExpect(jsonPath("$.cpf").value("52998224725"))
                    .andExpect(jsonPath("$.email").value("atualizada@example.com"))
                    .andExpect(jsonPath("$.dataNascimento").value("2001-08-09"))
                    .andExpect(jsonPath("$.criadoEm").value("2026-10-28T10:00:00"));

            entityManager.flush();
            assertThat(jdbcTemplate.queryForObject("select email from aluno where uuid = ?", String.class,
                    UUID.fromString(UUID_EXISTENTE))).isEqualTo("atualizada@example.com");
        }

        @Test
        @DisplayName("Dada edicao parcial, deve preservar os demais campos")
        void dadaEdicaoParcialDevePreservarDados() throws Exception {
            var json = objectMapper.createObjectNode().put("uuid", UUID_EXISTENTE).put("nome", "Ana Nova");

            ResultActions resposta = mockMvc.perform(put(BASE_URL + "/{uuid}", UUID_EXISTENTE)
                            .contentType(MediaType.APPLICATION_JSON).content(json.toString()));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.nome").value("Ana Nova"))
                    .andExpect(jsonPath("$.cpf").value("12345678909"))
                    .andExpect(jsonPath("$.email").value("ana@example.com"))
                    .andExpect(jsonPath("$.dataNascimento").value("2000-01-02"));

            entityManager.flush();
            assertThat(jdbcTemplate.queryForObject("select nome from aluno where uuid = ?", String.class,
                    UUID.fromString(UUID_EXISTENTE))).isEqualTo("Ana Nova");
        }

        @ParameterizedTest(name = "Campo invalido na edicao: {0}")
        @ValueSource(strings = {"cpf", "email"})
        @DisplayName("Edicao com CPF ou email invalido deve retornar 400 sem alterar dados")
        void dadoCampoInvalidoNaoDeveAtualizar(String campo) throws Exception {
            var json = lerRequisicao("edicao_valida.json");
            json.put(campo, "invalido");

            ResultActions resposta = mockMvc.perform(put(BASE_URL + "/{uuid}", UUID_EXISTENTE)
                            .contentType(MediaType.APPLICATION_JSON).content(json.toString()));

            resposta.andExpect(status().isBadRequest());

            assertThat(jdbcTemplate.queryForObject("select nome from aluno where uuid = ?", String.class,
                    UUID.fromString(UUID_EXISTENTE))).isEqualTo("Ana Silva");
        }

        @Test
        @DisplayName("Edicao sem UUID no corpo deve ser rejeitada conforme o DTO atual")
        void dadoCorpoSemUuidDeveRejeitar() throws Exception {
            ResultActions resposta = mockMvc.perform(put(BASE_URL + "/{uuid}", UUID_EXISTENTE)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Ana Nova\"}"));

            resposta.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Dado UUID inexistente na edicao, a implementacao atual propaga excecao sem handler")
        void dadoUuidInexistenteDevePropagarErroAtual() throws Exception {
            var json = lerRequisicao("edicao_valida.json").toString();

            assertThatThrownBy(() -> mockMvc.perform(put(BASE_URL + "/{uuid}", UUID_INEXISTENTE)
                            .contentType(MediaType.APPLICATION_JSON).content(json)))
                    .hasRootCauseInstanceOf(RuntimeException.class)
                    .hasRootCauseMessage("O identificador do aluno não existe");
        }
    }

    @Nested
    @DisplayName("Excluir aluno")
    class ExcluirAluno {

        @Test
        @DisplayName("Dado UUID existente, deve excluir do banco e confirmar a exclusao")
        void dadoUuidExistenteDeveExcluirAluno() throws Exception {
            ResultActions resposta = mockMvc.perform(delete(BASE_URL + "/{uuid}", UUID_EXISTENTE));

            resposta.andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Aluno deletado com sucesso!"));

            entityManager.flush();
            assertThat(jdbcTemplate.queryForObject("select count(*) from aluno where uuid = ?", Long.class,
                    UUID.fromString(UUID_EXISTENTE))).isZero();
            ResultActions respostaBusca = mockMvc.perform(get(BASE_URL + "/{uuid}", UUID_EXISTENTE));

            respostaBusca.andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Dado UUID inexistente, deve retornar 400 sem remover registros")
        void dadoUuidInexistenteNaoDeveExcluir() throws Exception {
            ResultActions resposta = mockMvc.perform(delete(BASE_URL + "/{uuid}", UUID_INEXISTENTE));

            resposta.andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.mensagem").value("Aluno com o ID fornecido não encontrado."));

            assertThat(jdbcTemplate.queryForObject("select count(*) from aluno", Long.class)).isEqualTo(3);
        }
    }
}
