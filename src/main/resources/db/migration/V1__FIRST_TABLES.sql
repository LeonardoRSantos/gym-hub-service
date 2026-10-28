CREATE TABLE aluno
(
	id BIGINT		PRIMARY KEY,
	uuid 			UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(), -- Identificador 128 Bits aleatório.
	nome			VARCHAR(120),
	cpf				VARCHAR(14) UNIQUE,
	email			VARCHAR(150) UNIQUE,
	data_nascimento DATE,
	criado_em       TIMESTAMP       NOT NULL DEFAULT NOW()

);

CREATE INDEX idx_aluno_nome ON aluno (nome);
CREATE SEQUENCE aluno_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE plano
(
	id BIGINT		PRIMARY KEY,
	uuid 			UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(), -- Identificador 128 Bits aleatório.
	nome			VARCHAR(80),
	valor			NUMERIC(10,2),
	duracao_mes		INTEGER,
	ativo			BOOLEAN,
	criado_em 		TIMESTAMP    DEFAULT NOW()	
);

CREATE SEQUENCE plano_seq START WITH 1 INCREMENT BY 1;

-- Entidade associativa entre Aluno e Plano.
-- Um Aluno pode possuir várias mátriculas ao longo do tempo.
-- E um plano pode estar Associado a vários alunos
CREATE TABLE matricula
(
	id 				BIGINT PRIMARY KEY,
	uuid 			UUID NOT NULL UNIQUE DEFAULT gen_random_uuid(), -- Identificador 128 Bits aleatório.
	aluno_id		BIGINT NOT NULL,
	plano_id		BIGINT NOT NULL,
	data_inicio		TIMESTAMP NOT NULL,
	data_fim		TIMESTAMP,
	criado_em 		TIMESTAMP    DEFAULT NOW()	
);

CREATE SEQUENCE matricula_seq START WITH 1 INCREMENT BY 1;

-- Relacionamentos e criação das constraints
ALTER TABLE matricula ADD CONSTRAINT fk_matricula_aluno FOREIGN KEY (aluno_id) REFERENCES aluno (id);
ALTER TABLE matricula ADD CONSTRAINT fk_matricula_plano FOREIGN KEY (plano_id) REFERENCES plano (id);

-- Comentários na tabela de aluno

COMMENT ON COLUMN aluno.id IS 'Identificador único do aluno';
COMMENT ON COLUMN aluno.uuid IS 'Identificador que será exposto na API.';
COMMENT ON COLUMN aluno.nome IS 'Nome do Aluno';
COMMENT ON COLUMN aluno.cpf IS 'CPF do Aluno';
COMMENT ON COLUMN aluno.email IS 'E-mail do aluno';
COMMENT ON COLUMN aluno.data_nascimento IS 'Data de Nascimento do Aluno';
COMMENT ON COLUMN aluno.criado_em IS 'Data que o aluno foi criado';

-- Comentários na tabela de plano

COMMENT ON COLUMN plano.id IS 'Identificador único do plano';
COMMENT ON COLUMN plano.uuid IS 'Identificador que será exposto na API.';
COMMENT ON COLUMN plano.nome IS 'Nome do Plano(Básico, Intermediário, Premium)';
COMMENT ON COLUMN plano.valor IS 'Valor do plano';
COMMENT ON COLUMN plano.duracao_mes IS 'Duração do plano';
COMMENT ON COLUMN plano.ativo IS 'Matrícula Ativa ou Não';
COMMENT ON COLUMN plano.criado_em IS 'Data que o Plano foi criado';

-- Comentários na tabela de matricula

COMMENT ON COLUMN matricula.id IS 'Identificador único da empresa';
COMMENT ON COLUMN matricula.uuid IS 'Identificador que será exposto na API.';
COMMENT ON COLUMN matricula.aluno_id IS 'Chave estrangeira que irá servir a tabela aluno';
COMMENT ON COLUMN matricula.plano_id IS 'Chave estrangeira que irá servir a tabela plano';
COMMENT ON COLUMN matricula.data_inicio IS 'Data de Início Obrigatória que a matrícula irá iniciar.';
COMMENT ON COLUMN matricula.data_fim IS 'Data Fim não Obrigatória que a matrícula irá finalizar';
COMMENT ON COLUMN matricula.criado_em IS 'Data que a matrícula foi criado';