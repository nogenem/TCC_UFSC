package manager;

import java.util.Set;

import dao.GrupoDao;
import dao.QuestionarioDao;
import dao.connection.BasicConnection;
import model.Grupo;
import model.Pergunta;
import model.Questionario;

public class QuestionarioManager {
	
	private QuestionarioDao questionarioDao;
	private PerguntaManager perguntaManager;
	private GrupoDao grupoDao;
	
	// Pega os links dos Questionarios que ja estão
	// no banco de dados para não extrair esses 
	// links de novo.
	private static Set<String> extractedLinks; 
	
	public QuestionarioManager(BasicConnection c) {
		questionarioDao = new QuestionarioDao(c);
		perguntaManager = new PerguntaManager(c);
		grupoDao = new GrupoDao(c);
		
		// Carrega os dados do banco de dados
		FormaDaPerguntaManager.loadFormas(c);//XXX parte da FormaDaPerguntaManager
		QuestionarioManager.loadQuestionarioLinks(questionarioDao);
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
	
	public static boolean linkWasExtracted(String link){
		return extractedLinks.contains(link);
	}
	
	//TODO repensar isso?
	private static synchronized void loadQuestionarioLinks(QuestionarioDao dao){
		if(extractedLinks != null) return;
		
		System.out.println(Thread.currentThread().getName() + 
				" carregou os links dos questionarios do banco de dados");
		try {
			extractedLinks = dao.getAllLinks();
		} catch (Exception e) {
			// TODO melhor isso?
			e.printStackTrace();
		}
	}
	
}
