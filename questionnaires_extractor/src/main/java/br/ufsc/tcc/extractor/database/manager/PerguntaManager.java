package br.ufsc.tcc.extractor.database.manager;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.extractor.database.dao.AlternativaDao;
import br.ufsc.tcc.extractor.database.dao.PerguntaDao;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;

/**
 * Classe de mais alto nível responsável por lidar com operações do banco de dados
 * relacionadas a classe/tabela Pergunta.
 * 
 * @author Gilney N. Mathias
 */
public class PerguntaManager {
	
	private PerguntaDao perguntaDao;
	private AlternativaDao alternativaDao;
	
	public PerguntaManager(BasicConnection c){
		perguntaDao = new PerguntaDao(c);
		alternativaDao = new AlternativaDao(c);
	}

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
