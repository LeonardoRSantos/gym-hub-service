package br.com.gymhub;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import br.com.gymhub.dominio.repositorio.AlunoRepository;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration")
class GymHubApplicationTest {

    // Apenas este teste de contexto desativa JPA e simula o repositorio.
    @MockBean
    private AlunoRepository alunoRepository;

    @Test
    void contextLoads() {
    }
}
