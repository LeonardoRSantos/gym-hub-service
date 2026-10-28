# Gym Hub Service

Backend para gerenciamento de academia, alunos, planos e matrículas, desenvolvido em Java com Spring Boot.

O projeto está em desenvolvimento. Hoje, a API oferece consulta paginada de alunos com filtro por nome. A migração inicial também prepara as tabelas de planos e matrículas, mas esses módulos ainda não possuem implementação Java ou endpoints.

## Estado atual

| Recurso | Situação |
| --- | --- |
| Consulta de alunos | Implementada em `GET /api/v1/aluno` |
| Filtro por nome, paginação e ordenação | Implementados |
| Entidade, repository, service e mapper de aluno | Implementados |
| Tabelas de aluno, plano e matrícula | Definidas na migração inicial |
| Cadastro, alteração e exclusão | Ainda não implementados |
| API de planos e matrículas | Ainda não implementada |
| Autenticação e autorização | Sem Spring Security no POM |
| Banco na configuração padrão | Datasource, JPA e Flyway desativados |
| Integração com PostgreSQL nos testes | Configurada via Docker Compose e perfil `integration` |

**A configuração padrão ainda não permite iniciar a API com o repository real.** É necessário configurar o banco e retirar as exclusões de autoconfiguração, conforme a seção de execução local. Os testes de integração já fazem essa configuração em um perfil próprio.

## Tecnologias e requisitos

| Tecnologia | Uso |
| --- | --- |
| Java 17 | Versão configurada para compilação |
| Spring Boot 3.3.5 | Inicialização e configuração da aplicação |
| Spring Web / MVC | API REST e suporte a MockMvc |
| Spring Data JPA / Hibernate | Persistência, paginação e Specifications |
| PostgreSQL | Banco de dados; imagem `postgres:16-alpine` nos testes |
| Flyway | Migrações SQL versionadas |
| Lombok | Getters, setters, builders e construtores |
| Spring Validation | Dependência disponível para validação |
| JUnit 5, Mockito e AssertJ | Testes |

Para compilar, utilize JDK 17 ou superior e Maven, ou o Maven Wrapper incluído no repositório. Docker Desktop com o mecanismo de containers Linux e Docker Compose são necessários para o PostgreSQL de testes.

## Estrutura do projeto

```text
src/
├── main/
│   ├── java/br/com/gymhub/
│   │   ├── GymHubApplication.java
│   │   ├── controller/
│   │   │   ├── AlunoController.java
│   │   │   ├── request/AlunoFiltro.java
│   │   │   └── response/AlunoSalvo.java
│   │   ├── domain/
│   │   │   ├── entity/AlunoEntidade.java
│   │   │   ├── repository/AlunoRepository.java
│   │   │   └── specs/AlunoSpecs.java
│   │   ├── mapper/AlunoMapper.java
│   │   └── service/
│   │       ├── AlunoService.java
│   │       └── AlunoServiceImpl.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/V1__FIRST_TABLES.sql
└── test/
    ├── java/br/com/gymhub/
    │   ├── GymHubApplicationTest.java
    │   ├── controller/AlunoControllerIT.java
    │   └── service/AlunoServiceImplTest.java
    └── resources/
        ├── application-integration.yml
        └── db/aluno/insert_alunos.sql
```

O fluxo da consulta é `AlunoController → AlunoServiceImpl → AlunoRepository → PostgreSQL`. `AlunoSpecs` constrói os critérios de busca; `AlunoMapper` converte a entidade em `AlunoSalvo`, preservando os metadados de paginação.

## API disponível

### Consultar alunos

```http
GET /api/v1/aluno?nome=ana&page=0&size=10&sort=nome,asc
```

| Parâmetro | Comportamento atual |
| --- | --- |
| `nome` | Busca parcial com `LIKE`, opcional |
| `page` | Índice da página, começando em zero; padrão do resolver: `0` |
| `size` | Quantidade de registros por página; padrão do resolver: `20` |
| `sort` | Propriedade e direção, por exemplo `nome,asc` ou `nome,desc` |

A consulta transforma o nome da coluna em minúsculas, mas ainda não normaliza o parâmetro recebido. Use o filtro em minúsculas, como `nome=ana`, para o comportamento demonstrado nos testes.

O DTO `AlunoFiltro` também declara `cpf`, `email` e `dataNascimento`, mas esses campos ainda não são aplicados pela Specification. A anotação `@CPF` existente no DTO também não implica validação automática do endpoint: o método do controller ainda não utiliza `@Valid`.

A resposta é `200 OK` com uma página. Sem resultados, `content` fica vazio e `totalElements` é zero. Cada aluno retorna:

| Campo | Tipo |
| --- | --- |
| `uuid` | UUID público do aluno |
| `nome` | Texto |
| `cpf` | Texto |
| `email` | Texto |
| `dataNascimento` | Data no formato `yyyy-MM-dd` |
| `criadoEm` | Data e hora sem fuso |

Exemplo ilustrativo dos principais campos da resposta:

```json
{
  "content": [
    {
      "uuid": "123e4567-e89b-12d3-a456-426614174001",
      "nome": "Ana Silva",
      "cpf": "12345678909",
      "email": "ana@example.com",
      "dataNascimento": "2000-01-02",
      "criadoEm": "2026-10-28T10:00:00"
    }
  ],
  "number": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

O identificador interno `id` não é exposto pelo mapper. A resposta atual serializa diretamente `Page`; o exemplo omite os demais metadados dessa estrutura.

## Banco de dados

A migração `src/main/resources/db/migration/V1__FIRST_TABLES.sql` cria:

- `aluno`: identificação, nome, CPF, e-mail, nascimento e data de criação; índice de nome e unicidade de UUID, CPF e e-mail.
- `plano`: identificação, nome, valor, duração em meses, indicador de ativo e data de criação.
- `matricula`: vínculo entre aluno e plano, com datas de início, fim e criação; chaves estrangeiras para as duas tabelas.
- Sequences `aluno_seq`, `plano_seq` e `matricula_seq`.

`AlunoEntidade` está mapeada para `aluno`, com identificador gerado pela sequence. Somente esse módulo possui camada de persistência Java atualmente.

Há diferenças entre as restrições declaradas na entidade e na migração, como a unicidade da data de nascimento na entidade. O perfil de integração usa `ddl-auto: validate`: a estrutura é criada pelo Flyway, sem atualização automática do esquema pelo Hibernate.

## Compilar e executar testes sem Docker

```shell
mvn clean install
```

Para executar apenas os testes da suíte padrão:

```shell
mvn test
```

Sem Maven instalado, substitua `mvn` por `./mvnw` no Linux/Git Bash ou por `./mvnw.cmd` no PowerShell.

| Classe | Tipo e cobertura |
| --- | --- |
| `AlunoServiceImplTest` | Unitário com Mockito: mapeamento dos resultados, página vazia e propagação de exceção do repository |
| `GymHubApplicationTest` | Teste de contexto Spring com repository simulado, sem banco |
| `AlunoControllerIT` | Integração com Spring Boot, MockMvc, service, repository e PostgreSQL reais |

O teste unitário cria o repository com `mock()` no `@BeforeEach`, usa AssertJ para asserções e verifica as chamadas com `verify()`.

O POM atual não configura execução automática de classes `*IT`. Por isso, `mvn test`, `mvn verify` e `mvn clean install` não executam o teste de integração; ele deve ser selecionado explicitamente.

## Executar a integração com Docker

Com Docker Desktop em execução, suba o banco exclusivo de testes:

```shell
docker compose -p gymhub-tests -f compose.test.yml up -d --wait
```

Execute o teste:

```shell
mvn '-Dtest=AlunoControllerIT' test
```

O argumento entre aspas funciona também no PowerShell. O teste usa `@SpringBootTest`, `@AutoConfigureMockMvc` e o perfil `integration`. Não inicia um servidor HTTP externo: as requisições passam pelo MockMvc dentro da aplicação de teste.

O Flyway aplica a migração real, `@Sql` insere a massa de alunos antes de cada cenário e `@Transactional` desfaz os inserts ao final. Não são usados mocks de service/repository, autenticação, WireMock ou Testcontainers.

Os cenários verificam filtro por nome com paginação, busca sem resultados e listagem sem filtro com ordenação.

Ao terminar, remova o ambiente de testes:

```shell
docker compose -p gymhub-tests -f compose.test.yml down -v
```

### Configuração do banco de testes

| Variável | Padrão no perfil de integração |
| --- | --- |
| `TEST_DB_URL` | `jdbc:postgresql://localhost:15432/gymhub_test` |
| `TEST_DB_USER` | `gymhub_test` |
| `TEST_DB_PASSWORD` | `gymhub_test` |

O Compose publica a porta somente em `127.0.0.1:15432` e não configura volume persistente. As credenciais são exclusivas para desenvolvimento/testes. As variáveis acima alteram a conexão usada pelo teste; não alteram a configuração do container no Compose.

O arquivo `application-integration.yml` fica em `src/test/resources` e não é incluído no JAR da aplicação. Use somente um banco dedicado aos testes, pois o Flyway aplica migrações e os testes inserem dados.

## Executar a API localmente

O arquivo `src/main/resources/application.yml` ainda mantém exclusões de datasource, JPA e Flyway da base inicial. Como `AlunoServiceImpl` depende de `AlunoRepository`, executar apenas `mvn spring-boot:run` com essa configuração falha ao criar o contexto.

Para habilitar a execução local, disponibilize um PostgreSQL e substitua as exclusões por uma configuração de conexão. Exemplo de conteúdo para `application.yml`:

```yaml
spring:
  application:
    name: gym-hub-service
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/gymhub}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
```

Esse trecho é uma orientação de configuração; o arquivo atual não foi modificado. Crie o banco e informe `DB_USER` e `DB_PASSWORD` antes de iniciar. Use `DB_URL` para alterar o endereço, porta ou nome do banco.

Após configurar:

```shell
mvn spring-boot:run
```

A porta padrão é `8080`. Exemplo de chamada:

```shell
curl "http://localhost:8080/api/v1/aluno?nome=ana&page=0&size=10&sort=nome,asc"
```

A migração cria a estrutura, mas não cadastra alunos de exemplo na aplicação. A massa em `src/test/resources` é exclusiva dos testes.

## Imagem Docker da aplicação

O `Dockerfile` compila o projeto em uma etapa Maven e executa o JAR em Java 17:

```shell
docker build -t gym-hub-service .
```

A construção da imagem pula os testes e não provisiona PostgreSQL. Para executar o container, a configuração de banco precisa ser habilitada conforme a seção anterior e deve apontar para um PostgreSQL acessível pelo container. O `compose.test.yml` sobe somente o banco de testes.

## Problemas comuns

| Sintoma | O que verificar |
| --- | --- |
| Docker não conecta ao mecanismo Linux | Iniciar Docker Desktop e aguardar o serviço ficar disponível |
| Porta `15432` ocupada | Liberar a porta ou ajustar o Compose e `TEST_DB_URL` em conjunto |
| Erro de conexão na integração | Subir o Compose com `--wait` e conferir as variáveis `TEST_DB_*` |
| `AlunoRepository` não encontrado ao iniciar a API | Retirar as exclusões de banco/JPA e configurar PostgreSQL |
| Integração não executada no build padrão | Usar `mvn '-Dtest=AlunoControllerIT' test` |
| Busca não encontra nome com letras maiúsculas | Enviar `nome` em minúsculas enquanto o filtro não normaliza o parâmetro |
