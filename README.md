# Gym Hub Service

Backend para gerenciamento de academia com Java 17 e Spring Boot 3.3.5.
A aplicação roda na IDE; o Docker é utilizado apenas para os bancos PostgreSQL.

## Início rápido

1. Inicie o Docker Desktop e execute na raiz do projeto:

   ```shell
   docker compose up -d
   ```

2. Execute `src/main/java/br/com/gymhub/GymHubApplication.java` pela IDE,
   sem ativar o perfil `integration`. Alternativamente:

   ```shell
   mvn spring-boot:run
   ```

3. Acesse a API em `http://localhost:8080` ou importe a collection do Postman.

O Compose não inicia a API. Ele sobe somente estes bancos:

| Serviço | Endereço na máquina | Banco | Usuário / senha padrão |
| --- | --- | --- | --- |
| `banco` | `localhost:5433` | `gymhub` | `gymhub` / `gymhub` |
| `banco-teste` | `localhost:15432` | `gymhub_test` | `gymhub_test` / `gymhub_test` |

A porta 5433 evita conflito com instalações locais do PostgreSQL que usam 5432.
A primeira inicialização pode levar alguns segundos; confira `docker compose ps`
e aguarde os serviços ficarem `healthy`.

## Configuração da IDE

A configuração padrão já conecta ao banco Docker:

| Variável | Padrão |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5433/gymhub` |
| `DB_USER` | `gymhub` |
| `DB_PASSWORD` | `gymhub` |

Com os padrões do Compose, não é necessário adicionar variáveis à configuração de
execução da IDE. Se já houver `DB_URL`, `DB_USER`, `DB_PASSWORD` ou
`SPRING_DATASOURCE_*` configuradas, confira seus valores: elas podem sobrescrever
o `application.yml`.

O Compose aceita `DB_USER` e `DB_PASSWORD` pelo ambiente ou por `.env`. Caso altere
as credenciais, configure os mesmos valores na IDE. Um `.env` é lido pelo Compose,
mas não é automaticamente carregado pelo Java executado na IDE. As credenciais
padrão são para desenvolvimento. Alterá-las no Compose não altera a senha de um
banco já inicializado no volume.

A porta HTTP padrão é 8080. Para alterá-la na IDE, configure `SERVER_PORT` e ajuste
`baseUrl` na collection. Não é necessário Dockerfile nem imagem da aplicação.

## Collection do Postman

Arquivo: [postman/GymHub.postman_collection.json](postman/GymHub.postman_collection.json).

No Postman, utilize **Import**, selecione o arquivo e execute as requisições da
pasta **Alunos**. A variável de collection `baseUrl` já aponta para
`http://localhost:8080`; `page` e `size` controlam a paginação. Não há autenticação.

A collection contém listagem, filtro por nome, CPF parcial/completo/com pontuação,
e-mail, nascimento, combinação de filtros, segunda página e nome inexistente.
Cada requisição valida HTTP 200, Content-Type JSON e estrutura paginada, sem
exigir um número fixo de registros.

Os valores são exemplos: o Flyway cria as tabelas, mas não insere alunos no banco
da aplicação. Por isso, respostas vazias são esperadas em um banco novo. A massa
SQL de `src/test/resources` é exclusiva dos testes automatizados.

## Funcionalidades atuais

| Recurso | Situação |
| --- | --- |
| `GET /api/v1/aluno` | Implementado |
| Filtros por nome, CPF, e-mail e nascimento | Implementados |
| Paginação e ordenação | Implementadas |
| Entidade, repository, service e mapper de aluno | Implementados |
| Tabelas `aluno`, `plano` e `matricula` | Criadas por migração Flyway |
| Cadastro, edição e exclusão | Ainda não implementados |
| Endpoints de planos e matrículas | Ainda não implementados |
| Autenticação | Sem Spring Security |

Exemplo:

```shell
curl "http://localhost:8080/api/v1/aluno?nome=ana&page=0&size=10&sort=nome,asc"
```

| Parâmetro | Comportamento |
| --- | --- |
| `nome` | Busca parcial; envie em minúsculas |
| `cpf` | Remove caracteres não numéricos do filtro e faz busca parcial |
| `email` | Busca parcial; envie em minúsculas |
| `dataNascimento` | Data exata; exemplo `2000-01-02` |
| `page` | Página começando em zero; padrão 0 |
| `size` | Tamanho da página; padrão 20 |
| `sort` | Exemplo `nome,asc` ou `nome,desc` |

Os filtros são opcionais e combinados com AND. Nome e e-mail transformam a coluna
em minúsculas, mas ainda não normalizam o parâmetro recebido. O controller não
utiliza `@Valid`; a anotação `@CPF` no DTO não ativa automaticamente a validação.

A resposta é HTTP 200 com `content`, `number`, `size`, `totalElements`,
`totalPages` e outros metadados de `Page`. Sem resultados, `content` é vazio.
Cada aluno expõe `uuid`, `nome`, `cpf`, `email`, `dataNascimento` e `criadoEm`.
O identificador interno `id` não é exposto.

## Banco e migrações

O Flyway cria e evolui as tabelas por scripts em
`src/main/resources/db/migration`. O Hibernate apenas valida o mapeamento:

| Configuração | Valor |
| --- | --- |
| `spring.jpa.hibernate.ddl-auto` | `validate` |
| `spring.jpa.generate-ddl` | `false` |
| `spring.sql.init.mode` | `never` |
| `spring.flyway.enabled` | `true` |
| `spring.flyway.validate-on-migrate` | `true` |
| `spring.flyway.baseline-on-migrate` | `false` |

`V1__FIRST_TABLES.sql` cria `aluno`, `plano`, `matricula`, suas sequences,
relacionamentos e índice de nome. Somente aluno possui implementação Java.
Há diferenças entre restrições da entidade e da migração, como a unicidade da
data de nascimento declarada na entidade; o Hibernate não atualiza essas regras.

Para mudar a estrutura, adicione uma nova migração, por exemplo
`V2__ADICIONA_CAMPO_ALUNO.sql`, e ajuste a entidade. Não edite migrações já aplicadas
nem utilize `ddl-auto: update`, `create` ou `create-drop`.

O banco da aplicação usa o volume persistente `dados-postgres`. Para parar os
containers preservando os dados, execute `docker compose down`. O comando
`docker compose down -v` apaga o volume do banco: use apenas para reiniciar os
dados de desenvolvimento do zero.

## Testes

Com os bancos ativos, execute todos os testes:

```shell
docker compose up -d
mvn clean install
```

| Classe | Escopo |
| --- | --- |
| `AlunoServiceImplTest` | Unitário com Mockito, sem Spring ou banco |
| `GymHubApplicationTest` | Contexto Spring com JPA, Flyway e PostgreSQL reais |
| `AlunoControllerIT` | Integração via MockMvc, service, repository e banco reais |

Os testes com contexto usam o perfil `integration` e o banco `gymhub_test`,
separado do banco da aplicação. Não exigem a API iniciada na IDE nem servidor
HTTP externo. `@Sql` prepara os alunos dos cenários do controller e
`@Transactional` desfaz os inserts ao final de cada teste.

Surefire executa `*Test`; Failsafe executa `*IT` durante `verify`/`install`.
`mvn clean install` falha se qualquer suíte falhar. Relatórios:
`target/surefire-reports` e `target/failsafe-reports`.

Para testar somente o service: `mvn '-Dtest=AlunoServiceImplTest' test`.
Para executar o controller isoladamente: `mvn '-Dtest=AlunoControllerIT' test`.
`mvn test` isolado não seleciona `*IT` automaticamente.

O Maven usa os bancos já iniciados; não gerencia os containers. Substitua `mvn`
por `./mvnw` ou, no PowerShell, `./mvnw.cmd` se preferir o Maven Wrapper.

| Variável dos testes | Padrão |
| --- | --- |
| `TEST_DB_URL` | `jdbc:postgresql://localhost:15432/gymhub_test` |
| `TEST_DB_USER` | `gymhub_test` |
| `TEST_DB_PASSWORD` | `gymhub_test` |

O perfil fica em `src/test/resources/application-integration.yml`, fora do JAR.
Use somente banco dedicado aos testes. As variáveis alteram a conexão dos testes,
não as credenciais do serviço `banco-teste`.

## Estrutura e tecnologias

O pacote raiz é `br.com.gymhub`, com subpacotes em português:

| Pacote | Responsabilidade |
| --- | --- |
| `controlador` | Endpoints HTTP |
| `controlador.requisicao` / `controlador.resposta` | DTOs de entrada e saída |
| `servico` | Coordenação e regras das consultas |
| `dominio.entidade` | Entidades JPA |
| `dominio.repositorio` | Acesso ao banco |
| `dominio.especificacao` | Critérios de pesquisa |
| `mapeador` | Conversão de entidades para respostas |

O fluxo é `AlunoController → AlunoServiceImpl → AlunoRepository → PostgreSQL`.
A aplicação usa Spring Web, Spring Data JPA/Hibernate, Validation, Flyway,
PostgreSQL e Lombok. Testes usam JUnit 5, Mockito, AssertJ e MockMvc.
Requisitos: JDK 17 ou superior, Maven/Wrapper e Docker Desktop com containers Linux.

## Problemas comuns

| Sintoma | Verificação |
| --- | --- |
| Erro de senha para `gymhub` | Conferir URL `localhost:5433` e variáveis da IDE; não usar o PostgreSQL local da porta 5432 |
| Porta 8080 ocupada | Encerrar outra execução da API ou alterar `SERVER_PORT` |
| Porta 15432 ocupada | Parar o container antigo de testes ou ajustar Compose e `TEST_DB_URL` juntos |
| Banco indisponível | Conferir `docker compose ps` e `docker compose logs banco banco-teste` |
| Migração falha | Conferir o erro no console da IDE e preservar scripts já aplicados |
| Collection retorna página vazia | Banco da aplicação ainda não possui alunos correspondentes aos filtros |
