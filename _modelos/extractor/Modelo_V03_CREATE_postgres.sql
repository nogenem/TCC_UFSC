DROP TABLE IF EXISTS Alternativa;
CREATE TABLE Alternativa(
	IdAlternativa bigserial NOT NULL,
	Pergunta_idPergunta BIGINT NOT NULL,
	DESCRICAO VARCHAR NULL,
	PRIMARY KEY(IdAlternativa)
);

DROP TABLE IF EXISTS Pergunta;
CREATE TABLE Pergunta(
	idPergunta bigserial NOT NULL,
	Questionario_idQuestionario BIGINT NOT NULL,
	FormaDaPergunta_idFormaDaPergunta BIGINT NOT NULL,
	PerguntaPai_idPergunta BIGINT NULL,
	Grupo_idGrupo BIGINT NULL,
	DESCRICAO VARCHAR NULL,
	TipoPergunta VARCHAR NULL,
	PRIMARY KEY(idPergunta)
);

DROP TABLE IF EXISTS Figura;
CREATE TABLE Figura(
	idFigura BIGINT NOT NULL,
	Legenda VARCHAR NULL,
	Imagem_URL VARCHAR NULL,
	Dono CHAR NULL,
	idDono BIGINT NULL,
	PRIMARY KEY(idFigura)
);

DROP TABLE IF EXISTS FormaDaPergunta;
CREATE TABLE FormaDaPergunta(
	idFormaDaPergunta bigserial NOT NULL,
	DESCRICAO VARCHAR NULL,
	PRIMARY KEY(idFormaDaPergunta)
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

ALTER TABLE Grupo
	ADD CONSTRAINT Rel_01 FOREIGN KEY (Questionario_idQuestionario) 
	REFERENCES Questionario(idQuestionario)
	ON DELETE CASCADE
	ON UPDATE CASCADE;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_04 FOREIGN KEY (Grupo_idGrupo) 
	REFERENCES Grupo(idGrupo)
	ON DELETE CASCADE
	ON UPDATE CASCADE;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_05 FOREIGN KEY (PerguntaPai_idPergunta) 
	REFERENCES Pergunta(idPergunta)
	ON DELETE CASCADE
	ON UPDATE CASCADE;

ALTER TABLE Alternativa
	ADD CONSTRAINT Rel_07 FOREIGN KEY (Pergunta_idPergunta) 
	REFERENCES Pergunta(idPergunta)
	ON DELETE CASCADE
	ON UPDATE CASCADE;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_14 FOREIGN KEY (FormaDaPergunta_idFormaDaPergunta) 
	REFERENCES FormaDaPergunta(idFormaDaPergunta)
	ON DELETE CASCADE
	ON UPDATE CASCADE;

ALTER TABLE Pergunta
	ADD CONSTRAINT Rel_07 FOREIGN KEY (Questionario_idQuestionario) 
	REFERENCES Questionario(idQuestionario)
	ON DELETE CASCADE
	ON UPDATE CASCADE;

DROP INDEX IF EXISTS Grupo_FKIndex1;
CREATE INDEX Grupo_FKIndex1 ON Grupo(Questionario_idQuestionario);

DROP INDEX IF EXISTS Pergunta_FKIndex1;
CREATE INDEX Pergunta_FKIndex1 ON Pergunta(Grupo_idGrupo);

DROP INDEX IF EXISTS Pergunta_FKIndex2;
CREATE INDEX Pergunta_FKIndex2 ON Pergunta(PerguntaPai_idPergunta);

DROP INDEX IF EXISTS Pergunta_FKIndex4;
CREATE INDEX Pergunta_FKIndex4 ON Pergunta(FormaDaPergunta_idFormaDaPergunta);

DROP INDEX IF EXISTS Pergunta_FKIndex4;
CREATE INDEX Pergunta_FKIndex4 ON Pergunta(Questionario_idQuestionario);

DROP INDEX IF EXISTS Alternativa_FKIndex1;
CREATE INDEX Alternativa_FKIndex1 ON Alternativa(Pergunta_idPergunta);

DROP SEQUENCE IF EXISTS Questionario_seq ;
CREATE SEQUENCE Questionario_seq 
	START 1 
	INCREMENT 1 
	MAXVALUE  9223372036854775807 
	MINVALUE 1  
	CACHE 1 ;

DROP SEQUENCE IF EXISTS Grupo_seq ;
CREATE SEQUENCE Grupo_seq 
	START 1 
	INCREMENT 1 
	MAXVALUE  9223372036854775807 
	MINVALUE 1  
	CACHE 1 ;

DROP SEQUENCE IF EXISTS Pergunta_seq ;
CREATE SEQUENCE Pergunta_seq 
	START 1 
	INCREMENT 1 
	MAXVALUE  9223372036854775807 
	MINVALUE 1  
	CACHE 1 ;

DROP SEQUENCE IF EXISTS Alternativa_seq ;
CREATE SEQUENCE Alternativa_seq 
	START 1 
	INCREMENT 1 
	MAXVALUE  9223372036854775807 
	MINVALUE 1  
	CACHE 1 ;

DROP SEQUENCE IF EXISTS FormaDaPergunta_seq ;
CREATE SEQUENCE FormaDaPergunta_seq 
	START 1 
	INCREMENT 1 
	MAXVALUE  9223372036854775807 
	MINVALUE 1  
	CACHE 1 ;

DROP SEQUENCE IF EXISTS Figura_seq ;
CREATE SEQUENCE Figura_seq 
	START 1 
	INCREMENT 1 
	MAXVALUE  9223372036854775807 
	MINVALUE 1  
	CACHE 1 ;


ALTER TABLE Questionario ALTER COLUMN idQuestionario SET DEFAULT nextval('Questionario_seq'::text);
ALTER TABLE Grupo ALTER COLUMN idGrupo SET DEFAULT nextval('Grupo_seq'::text);
ALTER TABLE Pergunta ALTER COLUMN idPergunta SET DEFAULT nextval('Pergunta_seq'::text);
ALTER TABLE Alternativa ALTER COLUMN IdAlternativa SET DEFAULT nextval('Alternativa_seq'::text);
ALTER TABLE FormaDaPergunta ALTER COLUMN idFormaDaPergunta SET DEFAULT nextval('FormaDaPergunta_seq'::text);
ALTER TABLE Figura ALTER COLUMN idFigura SET DEFAULT nextval('Figura_seq'::text);