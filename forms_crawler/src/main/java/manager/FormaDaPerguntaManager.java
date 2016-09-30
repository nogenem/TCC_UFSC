package manager;

import java.util.HashMap;

import dao.FormaDaPerguntaDao;
import dao.connection.BasicConnection;
import model.FormaDaPergunta;

public class FormaDaPerguntaManager {
	
	//TODO refazer isso do zero! [QuestionarioManager, Extratores]
	private static HashMap<String, FormaDaPergunta> formas;
	private static FormaDaPerguntaDao formaDao;
	
	public static FormaDaPergunta getForma(String forma){
		if(formas == null){
			System.err.println("FormaDaPerguntaManager:> Chame a funcao 'loadFormas' "
					+ "para inicializar as informacoes!");
			return null;
		}
		forma = forma.toUpperCase();
		if(!formas.containsKey(forma)) return null;
		return formas.get(forma);
	}
	
	public static synchronized void loadFormas(BasicConnection c) {	
		if(formaDao != null) return;
		formaDao = new FormaDaPerguntaDao(c);
		
		System.out.println(Thread.currentThread().getName() + 
				" carregou as formas das perguntas do banco de dados.");
		try {
			// Cache as formas de pergunta para serem
			// usadas na extração dos dados
			formas = formaDao.getAll();
		} catch (Exception e) {
			// TODO Melhorar isso?
			e.printStackTrace();
		}
	}
}
