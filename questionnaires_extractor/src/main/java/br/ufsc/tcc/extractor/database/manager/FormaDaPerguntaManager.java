package br.ufsc.tcc.extractor.database.manager;

import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.database.dao.FormaDaPerguntaDao;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;

/**
 * Classe de mais alto nível responsável por lidar com operações do banco de dados
 * relacionadas a classe/tabela FormaDaPergunta.
 * 
 * @author Gilney N. Mathias
 */
public class FormaDaPerguntaManager {
	
	private static HashMap<String, FormaDaPergunta> formas;
	private static FormaDaPerguntaDao dao;
	
	public static FormaDaPergunta getForma(String forma){
		if(formas == null){
			CommonLogger.info("FormaDaPerguntaManager:getForma()> Chame a funcao 'loadFormas' "
					+"para inicializar as informacoes!");
			return null;
		}
		forma = forma.toUpperCase();
		if(!formas.containsKey(forma)){ 
			CommonLogger.info("FormaDaPerguntaManager:getForma()> forma não encontrada: " +forma);
			return null;
		}
		return formas.get(forma);
	}
	
	public static synchronized void loadFormas(BasicConnection c){
		if(formas != null) return;
		dao = new FormaDaPerguntaDao(c);
		
		try {
			// Cache as formas de pergunta para serem
			// usadas na extração dos dados
			formas = dao.getAll();
			
			CommonLogger.debug("{} carregou as formas das perguntas do banco de dados.", 
					Thread.currentThread().getName());
		} catch (Exception e) {
			// Database não deve ta funcionando, então mata a aplicação
			CommonLogger.fatalError(e);
		}
	}

}
