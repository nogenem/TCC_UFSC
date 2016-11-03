package br.ufsc.tcc.extractor.database.manager;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.extractor.database.dao.AlternativaDao;
import br.ufsc.tcc.extractor.database.dao.PerguntaDao;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;

public class PerguntaManager {
	
	private PerguntaDao perguntaDao;
	private AlternativaDao alternativaDao;
	
	public PerguntaManager(BasicConnection c){
		perguntaDao = new PerguntaDao(c);
		alternativaDao = new AlternativaDao(c);
	}
	
	/**
	 * Salva a pergunta passada no banco de dados. 
	 * Método mais geral que salva também as Alternativas e as
	 * Perguntas filhas desta Pergunta.
	 * 
	 * @param p				Pergunta que se quer salvar.
	 * @throws Exception
	 */
	public void save(Pergunta p) throws Exception {
		perguntaDao.save(p);
		for(Alternativa a : p.getAlternativas()){
			alternativaDao.save(a);
		}
		for(Pergunta child : p.getFilhas()){
			this.save(child);
		}
	}
}
