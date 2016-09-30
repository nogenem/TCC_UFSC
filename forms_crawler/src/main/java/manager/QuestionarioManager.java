package manager;

import dao.GrupoDao;
import dao.QuestionarioDao;
import dao.connection.BasicConnection;
import model.Grupo;
import model.Pergunta;
import model.Questionario;

public class QuestionarioManager {
	
	QuestionarioDao questionarioDao;
	PerguntaManager perguntaManager;
	GrupoDao grupoDao;
	
	public QuestionarioManager(BasicConnection c) {
		questionarioDao = new QuestionarioDao(c);
		perguntaManager = new PerguntaManager(c);
		grupoDao = new GrupoDao(c);
		FormaDaPerguntaManager.loadFormas(c);//XXX parte da FormaDaPerguntaManager
	}
	
	public void save(Questionario q) throws Exception {
		questionarioDao.save(q);
		for(Grupo g : q.getGrupos()){
			grupoDao.save(g);
		}
		for(Pergunta p : q.getPerguntas()){
			perguntaManager.save(p);
		}
	}
	
}
