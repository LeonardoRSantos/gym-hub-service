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
| Banco na configuração padrão | PostgreSQL, JPA e Flyway habilitados |
| Integração com PostgreSQL nos testes | Configurada via Docker Compose e perfil `integration` |

A API pode ser iniciada junto com o PostgreSQL usando `docker compose up -d --build`. O Flyway cria a estrutura na primeira inicialização. O banco da aplicação possui volume persistente e fica separado do banco usado nos testes.

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
│   │   ├── controlador/
│   │   │   ├── AlunoController.java
│   │   │   ├── requisicao/AlunoFiltro.java
│   │   │   └── resposta/AlunoSalvo.java
│   │   ├── dominio/
│   │   │   ├── entidade/AlunoEntidade.java
│   │   │   ├── repositorio/AlunoRepository.java
│   │   │   └── especificacao/AlunoSpecs.java
│   │   ├── mapeador/AlunoMapper.java
│   │   └── servico/
│   │       ├── AlunoService.java
│   │       └── AlunoServiceImpl.java
│   └── resources/
│       ├── application.yml
│       └── db/migration/V1__FIRST_TABLES.sql
└── test/
    ├── java/br/com/gymhub/
    │   ├── GymHubApplicationTest.java
    │   ├── controlador/AlunoControllerIT.java
    │   └── servico/AlunoServiceImplTest.java
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

O Flyway aplica a migração real, `@Sql` insere a massa de alunos antes de cada cenário e `@Transactional` desfaz os inserts ao final. Não são usados mocks de servico/repository, autenticação, WireMock ou Testcontainers.

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

## Executar com Docker

Com Docker Desktop em execução, na raiz do projeto:

```shell
docker compose up -d --build
```

O Compose constrói a imagem da aplicação, inicia o PostgreSQL e aguarda o healthcheck do banco antes de iniciar a API. O Flyway aplica as migrações, e o Hibernate valida o esquema. A primeira construção baixa as imagens e dependências Maven; não exige Java ou Maven instalados na máquina.

A API fica disponível em `http://localhost:8080`. Consulte os alunos:

```shell
curl "http://localhost:8080/api/v1/aluno?nome=ana&page=0&size=10&sort=nome,asc"
```

A migração não insere alunos de exemplo. Em um banco novo, a consulta retorna uma página vazia.

Comandos úteis:

```shell
docker compose ps
docker compose logs -f aplicacao
docker compose down
```

`docker compose down` preserva os dados no volume `dados-postgres`. Para apagar o banco de desenvolvimento e recriá-lo do zero, utilize explicitamente `docker compose down -v`.

### Configuração dos containers

| Variável | Padrão | Finalidade |
| --- | --- | --- |
| `APP_PORT` | `8080` | Porta da API na máquina |
| `DB_USER` | `gymhub` | Usuário PostgreSQL |
| `DB_PASSWORD` | `gymhub` | Senha de desenvolvimento |

Essas variáveis podem ser definidas no ambiente ou em um arquivo `.env` local. O Compose conecta a aplicação em `jdbc:postgresql://banco:5432/gymhub`; o banco não publica porta na máquina. As credenciais padrão servem para desenvolvimento. Alterar as credenciais no Compose não altera um banco já inicializado no volume.

O `Dockerfile` usa construção em duas etapas: Maven com Java 17 para gerar o JAR e uma imagem JRE 17 para executá-lo. O build da imagem pula os testes. `.dockerignore` exclui Git, IDE, build local e arquivos de ambiente do contexto enviado ao Docker.

## Executar sem Docker para a aplicação

Com um PostgreSQL disponível, configure as variáveis de ambiente e execute:

```shell
mvn spring-boot:run
```

| Variável | Padrão fora do Compose |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/gymhub` |
| `DB_USER` | `gymhub` |
| `DB_PASSWORD` | `gymhub` |

O banco `gymhub` deve existir e estar acessível. JPA e Flyway já estão habilitados em `application.yml`. Para executar via JAR, use `mvn clean package` e `java -jar target/gym-hub-service-0.0.1-SNAPSHOT.jar` com as mesmas variáveis.

## Organização dos pacotes

O pacote raiz continua `br.com.gymhub`. Os subpacotes do projeto estão em português, sem acentos:

| Pacote | Responsabilidade |
| --- | --- |
| `controlador` | Endpoints HTTP |
| `controlador.requisicao` | DTOs recebidos pela API |
| `controlador.resposta` | DTOs retornados pela API |
| `servico` | Regras e coordenação das consultas |
| `dominio.entidade` | Entidades JPA |
| `dominio.repositorio` | Acesso ao banco |
| `dominio.especificacao` | Critérios de busca JPA |
| `mapeador` | Conversão de entidades para respostas |

Os testes seguem os mesmos pacotes. Os nomes das classes existentes e a URL `/api/v1/aluno` foram preservados.

## Problemas comuns

| Sintoma | O que verificar |
| --- | --- |
| Docker não conecta ao mecanismo Linux | Iniciar Docker Desktop e aguardar o serviço ficar disponível |
| Porta `8080` ocupada | Definir outra `APP_PORT` antes de subir o Compose |
| Porta `15432` ocupada nos testes | Ajustar `compose.test.yml` e `TEST_DB_URL` em conjunto |
| Erro de conexão na integração | Subir o Compose de testes com `--wait` e conferir `TEST_DB_*` |
| API não inicia | Consultar `docker compose logs aplicacao banco` e verificar a conexão/migrações |
| Integração não executada no build padrão | Usar `mvn '-Dtest=AlunoControllerIT' test` |
| Busca não encontra nome com letras maiúsculas | Enviar `nome` em minúsculas enquanto o filtro não normaliza o parâmetro |
