DROP TABLE IF EXISTS PossivelQuestionario;
CREATE TABLE PossivelQuestionario(
	idPossivelQuestionario bigserial NOT NULL,
	LINK_DOCUMENTO VARCHAR NULL,
	TITULO_DOCUMENTO VARCHAR NULL,
    ENCONTRADO_EM timestamp without time zone,
	PRIMARY KEY(idPossivelQuestionario)
);

DROP SEQUENCE IF EXISTS PossivelQuestionario_seq ;
CREATE SEQUENCE PossivelQuestionario_seq 
	START 1 
	INCREMENT 1 
	MAXVALUE  9223372036854775807 
	MINVALUE 1  
	CACHE 1 ;
    
ALTER TABLE PossivelQuestionario ALTER COLUMN idPossivelQuestionario SET DEFAULT nextval('PossivelQuestionario_seq'::text);
