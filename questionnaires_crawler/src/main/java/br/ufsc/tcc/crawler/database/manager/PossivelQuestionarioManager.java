package br.ufsc.tcc.crawler.database.manager;

import java.util.ArrayList;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.crawler.database.dao.PossivelQuestionarioDao;
import br.ufsc.tcc.crawler.model.PossivelQuestionario;

public class PossivelQuestionarioManager {
	
	private PossivelQuestionarioDao possivelQuestionarioDao;
	
	// Pega os links dos Possiveis Questionarios que ja estão
	// no banco de dados para não salvar esses 
	// links de novo.
	private static ArrayList<String> savedLinks;
	
	public PossivelQuestionarioManager(BasicConnection c) {
		possivelQuestionarioDao = new PossivelQuestionarioDao(c);
		
		// Carrega os dados do banco de dados
		PossivelQuestionarioManager.loadPossivelQuestionarioLinks(possivelQuestionarioDao);
	}
	
	public void save(PossivelQuestionario q) throws Exception {
		possivelQuestionarioDao.save(q);
	}
	
	public ArrayList<PossivelQuestionario> getAll() throws Exception {
		return possivelQuestionarioDao.getAll();
	}
	
	public static boolean linkWasSaved(String link){
		return savedLinks.contains(link);
	}
	
	private static synchronized void loadPossivelQuestionarioLinks(PossivelQuestionarioDao dao) {
		if(savedLinks != null) return;
		
		try {
			savedLinks = dao.getAllLinks();
			
			System.out.println(Thread.currentThread().getName() + 
					" carregou os links dos questionarios do banco de dados");
		} catch (Exception e) {
			// Database não deve esta funcionado, então mata a aplicação
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
