package br.ufsc.tcc.extractor.database.manager;

import java.util.ArrayList;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.extractor.database.dao.FiguraDao;
import br.ufsc.tcc.extractor.database.dao.GrupoDao;
import br.ufsc.tcc.extractor.database.dao.QuestionarioDao;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.Grupo;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class QuestionarioManager {
	
	private QuestionarioDao questionarioDao;
	private GrupoDao grupoDao;
	private PerguntaManager perguntaManager;
	private FiguraDao figuraDao;
	
	// Pega os links dos Questionarios que ja estão
	// no banco de dados para não extrair esses 
	// links de novo.
	private static ArrayList<String> extractedLinks;
	
	public QuestionarioManager(BasicConnection c) {
		questionarioDao = new QuestionarioDao(c);
		grupoDao = new GrupoDao(c);
		perguntaManager = new PerguntaManager(c);
		figuraDao = new FiguraDao(c);
		
		// Carrega os dados do banco de dados
		FormaDaPerguntaManager.loadFormas(c);
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
		for(Figura f : q.getFiguras()){
			figuraDao.save(f);
		}
	}
	
	public static boolean linkWasExtracted(String link){
		return extractedLinks.contains(link);
	}
	
	private static synchronized void loadQuestionarioLinks(QuestionarioDao dao) {
		if(extractedLinks != null) return;
		
		try {
			extractedLinks = dao.getAllLinks();
			
			System.out.println(Thread.currentThread().getName() + 
					" carregou os links dos questionarios do banco de dados");
		} catch (Exception e) {
			// Database não deve ta funcionado, então mata a aplicação
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
