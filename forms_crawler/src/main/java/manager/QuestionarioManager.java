package manager;

import dao.QuestionarioDao;
import dao.connection.BasicConnection;
import model.Pergunta;
import model.Questionario;

public class QuestionarioManager {
	
	QuestionarioDao questionarioDao;
	PerguntaManager perguntaManager;
	
	public QuestionarioManager(BasicConnection c) {
		questionarioDao = new QuestionarioDao(c);
		perguntaManager = new PerguntaManager(c);
		FormaDaPerguntaManager.loadFormas(c);//XXX parte da FormaDaPerguntaManager
	}
	
	public void save(Questionario q) throws Exception{
		questionarioDao.save(q);
		for(Pergunta p : q.getPerguntas()){
			perguntaManager.save(p);
		}
	}
	
}
