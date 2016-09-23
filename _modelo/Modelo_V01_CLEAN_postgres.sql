DELETE FROM alternativa;
DELETE FROM pergunta;
DELETE FROM grupo;
DELETE FROM questionario;

ALTER SEQUENCE alternativa_idalternativa_seq RESTART WITH 1;
ALTER SEQUENCE pergunta_idpergunta_seq RESTART WITH 1;
ALTER SEQUENCE grupo_idgrupo_seq RESTART WITH 1;
ALTER SEQUENCE questionario_idquestionario_seq RESTART WITH 1;