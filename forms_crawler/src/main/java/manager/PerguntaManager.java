package manager;

import dao.AlternativaDao;
import dao.PerguntaDao;
import dao.connection.BasicConnection;
import model.Alternativa;
import model.Pergunta;

public class PerguntaManager {
	
	PerguntaDao perguntaDao;
	AlternativaDao alternativaDao;
	
	public PerguntaManager(BasicConnection c){
		perguntaDao = new PerguntaDao(c);
		alternativaDao = new AlternativaDao(c);
	}
	
	public void save(Pergunta p) throws Exception {
		perguntaDao.save(p);
		for(Alternativa a : p.getAlternativas()){
			alternativaDao.save(a);
		}
		for(Pergunta child : p.getPerguntas()){
			this.save(child);
		}
	}
	
}
