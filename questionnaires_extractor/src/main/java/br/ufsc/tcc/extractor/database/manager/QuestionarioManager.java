package br.ufsc.tcc.extractor.database.manager;

import java.util.ArrayList;

import org.json.JSONArray;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.util.CommonLogger;
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
	
	public QuestionarioManager(BasicConnection c) {
		questionarioDao = new QuestionarioDao(c);
		grupoDao = new GrupoDao(c);
		perguntaManager = new PerguntaManager(c);
		figuraDao = new FiguraDao(c);
		
		// Carrega os dados do banco de dados
		FormaDaPerguntaManager.loadFormas(c);
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
	
	public void cleanDatabase() {
		try{
			this.questionarioDao.clean();
		}catch(Exception e){
			CommonLogger.error(e);
		}
	}
	
	public void deleteLinks(JSONArray links){
		try{
			ArrayList<String> dbLinks = this.questionarioDao.getAllLinks();
			for(int i = 0; i<links.length(); i++){
				String link = (String) links.get(i);
				if(dbLinks.contains(link))
					this.questionarioDao.remove(link);
			}
		}catch(Exception e){
			CommonLogger.error(e);
		}
	}
}
