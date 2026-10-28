package br.com.gymhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import br.com.gymhub.domain.repository.AlunoRepository;

@SpringBootTest
class GymHubApplicationTest {

    // A base ainda desativa JPA: o teste de contexto nao depende de PostgreSQL.
    @MockBean
    private AlunoRepository alunoRepository;

    @Test
    void contextLoads() {
    }
}
