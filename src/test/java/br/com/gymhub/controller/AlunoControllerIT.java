package br.com.gymhub.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@Transactional
@DisplayName("AlunoController - integracao com PostgreSQL")
class AlunoControllerIT {

    private static final String BASE_URL = "/api/v1/aluno";

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Filtrar alunos")
    @Sql("/db/aluno/insert_alunos.sql")
    class FiltrarAlunos {

        @Test
        @DisplayName("Dado nome existente, deve retornar alunos filtrados e paginados")
        void dadoNomeExistenteDeveRetornarAlunosPaginados() throws Exception {
            mockMvc.perform(get(BASE_URL)
                            .param("nome", "ana")
                            .param("page", "0")
                            .param("size", "1")
                            .param("sort", "nome,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(2))
                    .andExpect(jsonPath("$.totalPages").value(2))
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.content[0].uuid").value("123e4567-e89b-12d3-a456-426614174001"))
                    .andExpect(jsonPath("$.content[0].nome").value("Ana Silva"))
                    .andExpect(jsonPath("$.content[0].dataNascimento").value("2000-01-02"))
                    .andExpect(jsonPath("$.content[0].criadoEm").value("2026-10-28T10:00:00"));
        }

        @Test
        @DisplayName("Dado nome inexistente, deve retornar pagina vazia")
        void dadoNomeInexistenteDeveRetornarPaginaVazia() throws Exception {
            mockMvc.perform(get(BASE_URL).param("nome", "Inexistente"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Sem filtro, deve retornar todos os alunos ordenados")
        void semFiltroDeveRetornarTodosOsAlunos() throws Exception {
            mockMvc.perform(get(BASE_URL).param("sort", "nome,asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(3))
                    .andExpect(jsonPath("$.content[2].nome").value("Bruno Lima"));
        }
    }
}
