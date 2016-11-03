package br.ufsc.tcc.extractor.database.manager;

import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.extractor.database.dao.FormaDaPerguntaDao;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;

public class FormaDaPerguntaManager {
	
	private static HashMap<String, FormaDaPergunta> formas;
	private static FormaDaPerguntaDao dao;
	
	/**
	 * Retorna uma FormaDaPergunta que possue a mesma descrição da forma passada.
	 * 
	 * @param forma		Descrição da FormaDaPergunta que se quer.
	 * @return			Um objeto FormaDaPergunta que possue a mesma descrição da forma passada.
	 */
	public static FormaDaPergunta getForma(String forma){
		if(formas == null){//TODO mudar isso?
			System.err.println("FormaDaPerguntaManager:getForma()> Chame a funcao 'loadFormas' "
					+ "para inicializar as informacoes!");
			return null;
		}
		forma = forma.toUpperCase();
		if(!formas.containsKey(forma)) return null;
		return formas.get(forma);
	}
	
	/**
	 * Carrega todas as FormaDaPergunta do banco de dados.
	 * 
	 * @param c		Conexão com o banco de dados.
	 */
	public static synchronized void loadFormas(BasicConnection c){
		if(formas != null) return;
		dao = new FormaDaPerguntaDao(c);
		
		try {
			// Cache as formas de pergunta para serem
			// usadas na extração dos dados
			formas = dao.getAll();
			
			System.out.println(Thread.currentThread().getName() + 
					" carregou as formas das perguntas do banco de dados.");
		} catch (Exception e) {
			// Database não deve ta funcionado, então mata a aplicação
			e.printStackTrace();
			System.exit(-1);
		}
	}

}
