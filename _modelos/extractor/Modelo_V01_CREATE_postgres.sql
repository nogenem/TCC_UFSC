DROP TABLE IF EXISTS FormaDaPergunta;
CREATE TABLE FormaDaPergunta(
	idFormaDaPergunta bigserial NOT NULL,
	DESCRICAO VARCHAR NULL,
	PRIMARY KEY(idFormaDaPergunta)
);

DROP TABLE IF EXISTS Alternativa;
CREATE TABLE Alternativa(
	IdAlternativa bigserial NOT NULL,
	Pergunta_Questionario_idQuestionario BIGINT NOT NULL,
	Pergunta_idPergunta BIGINT NOT NULL,
	DESCRICAO VARCHAR NULL,
	PRIMARY KEY(IdAlternativa)
);

DROP TABLE IF EXISTS Pergunta;
CREATE TABLE Pergunta(
	idPergunta bigserial NOT NULL,
	Questionario_idQuestionario BIGINT NOT NULL,
	FormaDaPergunta_idFormaDaPergunta BIGINT NOT NULL,
	Pergunta_Questionario_idQuestionario BIGINT,
	PerguntaPai_idPergunta BIGINT,
	Grupo_idGrupo INTEGER,
	DESCRICAO VARCHAR NULL,
	TipoPergunta VARCHAR NULL,
	PRIMARY KEY(idPergunta,Questionario_idQuestionario)
);

DROP TABLE IF EXISTS Grupo;
CREATE TABLE Grupo(
	idGrupo bigserial NOT NULL,
	Questionario_idQuestionario BIGINT NOT NULL,
	ASSUNTO VARCHAR NULL,
	PRIMARY KEY(idGrupo)
);

DROP TABLE IF EXISTS Questionario;
CREATE TABLE Questionario(
	idQuestionario bigserial NOT NULL,
	ASSUNTO VARCHAR NULL,
	LINK_DOCUMENTO VARCHAR NULL,
	PRIMARY KEY(idQuestionario)
);

DROP VIEW IF EXISTS vi_Questionario;
CREATE VIEW vi_Questionario AS 
SELECT 
	idQuestionario,
	ASSUNTO,
	LINK_DOCUMENTO
FROM Questionario;


DROP VIEW IF EXISTS vi_Grupo;
CREATE VIEW vi_Grupo AS 
SELECT 
	idGrupo,
	Questionario_idQuestionario,
	ASSUNTO
FROM Grupo;


DROP VIEW IF EXISTS vi_Pergunta;
CREATE VIEW vi_Pergunta AS 
SELECT 
	idPergunta,
	Questionario_idQuestionario,
	FormaDaPergunta_idFormaDaPergunta,
	Pergunta_Questionario_idQuestionario,
	PerguntaPai_idPergunta,
	Grupo_idGrupo,
	DESCRICAO,
	TipoPergunta
FROM Pergunta;


DROP VIEW IF EXISTS vi_Alternativa;
CREATE VIEW vi_Alternativa AS 
SELECT 
	IdAlternativa,
	Pergunta_Questionario_idQuestionario,
	Pergunta_idPergunta,
	DESCRICAO
FROM Alternativa;


DROP VIEW IF EXISTS vi_FormaDaPergunta;
CREATE VIEW vi_FormaDaPergunta AS 
SELECT 
	idFormaDaPergunta,
	DESCRICAO
FROM FormaDaPergunta;

ALTER TABLE Grupo
	ADD CONSTRAINT Rel_01 FOREIGN KEY (Questionario_idQuestionario) 
	REFERENCES Questionario(idQuestionario)
	ON DELETE NO ACTION
	ON UPDATE NO ACTION;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_04 FOREIGN KEY (Grupo_idGrupo) 
	REFERENCES Grupo(idGrupo)
	ON DELETE NO ACTION
	ON UPDATE NO ACTION;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_05 FOREIGN KEY (PerguntaPai_idPergunta, Pergunta_Questionario_idQuestionario) 
	REFERENCES Pergunta(idPergunta, Questionario_idQuestionario)
	ON DELETE SET NULL
	ON UPDATE NO ACTION;

ALTER TABLE Alternativa
	ADD CONSTRAINT Rel_07 FOREIGN KEY (Pergunta_idPergunta, Pergunta_Questionario_idQuestionario) 
	REFERENCES Pergunta(idPergunta, Questionario_idQuestionario)
	ON DELETE NO ACTION
	ON UPDATE NO ACTION;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_10 FOREIGN KEY (Questionario_idQuestionario) 
	REFERENCES Questionario(idQuestionario)
	ON DELETE NO ACTION
	ON UPDATE NO ACTION;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_14 FOREIGN KEY (FormaDaPergunta_idFormaDaPergunta) 
	REFERENCES FormaDaPergunta(idFormaDaPergunta)
	ON DELETE NO ACTION
	ON UPDATE NO ACTION;


DROP INDEX IF EXISTS Grupo_FKIndex1;
CREATE INDEX Grupo_FKIndex1 ON Grupo(Questionario_idQuestionario);

DROP INDEX IF EXISTS Pergunta_FKIndex1;
CREATE INDEX Pergunta_FKIndex1 ON Pergunta(Grupo_idGrupo);

DROP INDEX IF EXISTS Pergunta_FKIndex2;
CREATE INDEX Pergunta_FKIndex2 ON Pergunta(PerguntaPai_idPergunta, Pergunta_Questionario_idQuestionario);

DROP INDEX IF EXISTS Pergunta_FKIndex3;
CREATE INDEX Pergunta_FKIndex3 ON Pergunta(Questionario_idQuestionario);

DROP INDEX IF EXISTS Pergunta_FKIndex4;
CREATE INDEX Pergunta_FKIndex4 ON Pergunta(FormaDaPergunta_idFormaDaPergunta);

DROP INDEX IF EXISTS Alternativa_FKIndex1;
CREATE INDEX Alternativa_FKIndex1 ON Alternativa(Pergunta_idPergunta, Pergunta_Questionario_idQuestionario);

