DROP TABLE IF EXISTS Alternativa;
DROP TABLE IF EXISTS Pergunta;
DROP TABLE IF EXISTS Grupo;
DROP TABLE IF EXISTS Questionario;
DROP TABLE IF EXISTS FormaDaPergunta;

CREATE TABLE FormaDaPergunta (
  idFormaDaPergunta BIGINT NOT NULL AUTO_INCREMENT,
  DESCRICAO VARCHAR(255),
  PRIMARY KEY(idFormaDaPergunta)
);

CREATE TABLE Questionario (
  idQuestionario BIGINT NOT NULL AUTO_INCREMENT,
  ASSUNTO VARCHAR(255),
  LINK_DOCUMENTO VARCHAR(255),
  PRIMARY KEY(idQuestionario)
);

CREATE TABLE Grupo (
  idGrupo BIGINT NOT NULL AUTO_INCREMENT,
  Questionario_idQuestionario BIGINT NOT NULL,
  ASSUNTO VARCHAR(255),
  PRIMARY KEY(idGrupo),
  INDEX Grupo_FKIndex1(Questionario_idQuestionario)
);

CREATE TABLE Pergunta (
  idPergunta BIGINT NOT NULL AUTO_INCREMENT,
  Questionario_idQuestionario BIGINT NOT NULL,
  FormaDaPergunta_idFormaDaPergunta BIGINT NOT NULL,
  Pergunta_Questionario_idQuestionario BIGINT,
  PerguntaPai_idPergunta BIGINT,
  Grupo_idGrupo BIGINT,
  DESCRICAO VARCHAR(255),
  TipoPergunta VARCHAR(255),
  PRIMARY KEY(idPergunta, Questionario_idQuestionario),
  INDEX Pergunta_FKIndex1(Grupo_idGrupo),
  INDEX Pergunta_FKIndex2(PerguntaPai_idPergunta, Pergunta_Questionario_idQuestionario),
  INDEX Pergunta_FKIndex3(Questionario_idQuestionario),
  INDEX Pergunta_FKIndex4(FormaDaPergunta_idFormaDaPergunta)
);

CREATE TABLE Alternativa (
  IdAlternativa BIGINT NOT NULL AUTO_INCREMENT,
  Pergunta_Questionario_idQuestionario BIGINT NOT NULL,
  Pergunta_idPergunta BIGINT NOT NULL,
  DESCRICAO VARCHAR(255),
  PRIMARY KEY(IdAlternativa),
  INDEX Alternativa_FKIndex1(Pergunta_idPergunta, Pergunta_Questionario_idQuestionario)
);

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