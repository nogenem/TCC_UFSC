package br.ufsc.tcc.common.database.manager;

import java.util.ArrayList;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.PossivelQuestionarioDao;
import br.ufsc.tcc.common.model.PossivelQuestionario;
import br.ufsc.tcc.common.util.CommonLogger;

public class PossivelQuestionarioManager {
	
	private PossivelQuestionarioDao pqDao;
	
	// Pega os links dos Possiveis Questionarios que ja estão
	// no banco de dados
	private static ArrayList<String> savedLinks;
	
	public PossivelQuestionarioManager(BasicConnection c) {
		this.pqDao = new PossivelQuestionarioDao(c);
		
		// Carrega os dados do banco de dados
		PossivelQuestionarioManager.loadPossivelQuestionarioLinks(pqDao);
	}
	
	public ArrayList<PossivelQuestionario> getAll() throws Exception {
		return pqDao.getAll();
	}
	
	public void save(PossivelQuestionario q) throws Exception {
		pqDao.save(q);
		savedLinks.add(q.getLink_doc());//TODO remover isso?
	}
	
	public void remove(String link) throws Exception {
		if(savedLinks.contains(link)){
			pqDao.remove(link);
			savedLinks.remove(link);//TODO remover isso?
		}
	}
	
	// Métodos estáticos
	public static boolean linkWasSaved(String link){
		return savedLinks.contains(link);
	}
	
	public static ArrayList<String> getSavedLinks(){
		return savedLinks;
	}
	
	public static synchronized void loadPossivelQuestionarioLinks(BasicConnection c){
		loadPossivelQuestionarioLinks(new PossivelQuestionarioDao(c));
	}
	
	private static synchronized void loadPossivelQuestionarioLinks(PossivelQuestionarioDao dao) {
		if(savedLinks != null) return;
		
		try {
			savedLinks = dao.getAllLinks();
			
			CommonLogger.debug("{} carregou os links dos possiveis questionarios do banco de dados.", 
					Thread.currentThread().getName());
		} catch (Exception e) {
			// Database não deve esta funcionado, então mata a aplicação
			CommonLogger.fatalError(e);
		}
	}
}
