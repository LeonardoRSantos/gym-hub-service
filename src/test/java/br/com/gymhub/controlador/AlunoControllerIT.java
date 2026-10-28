package br.com.gymhub.controlador;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
}
