package br.ufsc.tcc.common.database.manager;

import java.util.Set;

import com.google.common.hash.BloomFilter;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.PossivelQuestionarioDao;
import br.ufsc.tcc.common.model.PossivelQuestionario;
import br.ufsc.tcc.common.util.CommonLogger;

/**
 * Classe de mais alto nível responsável por lidar com operações do banco de dados
 * relacionadas a classe/tabela PossivelQuestionario.
 * 
 * @author Gilney N. Mathias
 */
public class PossivelQuestionarioManager {
	
	private PossivelQuestionarioDao pqDao;
	
	/**
	 * BloomFilter é uma estrutura de dados rápida e eficiente mas que
	 * possui um problema, ela pode gerar falso possitivos quando se 
	 * verifica a existência de um elemento nela.
	 */
	private static BloomFilter<String> bfSavedLinks = null;
	private static Set<String> setSavedLinks = null;
	
	// Construtores
	/**
	 * Construtor da classe.
	 * 
	 * @param loadLinksAsASet		Deve-se carregar os links do banco de dados
	 * 								em uma estrutura de <b>Set</b>? <br> 
	 * 								Caso seja passado o valor <b>False</b>, os links 
	 * 								serão carregados em uma estrutura de <b>BloomFilter</b>.
	 */
	public PossivelQuestionarioManager(boolean loadLinksAsASet) {
		this(new BasicConnection(ProjectConfigs.getCrawlerDatabaseConfigs()), 
				loadLinksAsASet);
	}
	
	/**
	 * Construtor da classe.
	 * 
	 * @param connection			Uma instancia de BasicConnection.
	 * @param loadLinksAsASet		Deve-se carregar os links do banco de dados
	 * 								em uma estrutura de <b>Set</b>? <br> 
	 * 								Caso seja passado o valor <b>False</b>, os links 
	 * 								serão carregados em uma estrutura de <b>BloomFilter</b>.
	 */
	public PossivelQuestionarioManager(BasicConnection connection, boolean loadLinksAsASet) {
		this.pqDao = new PossivelQuestionarioDao(connection);
		
		if(loadLinksAsASet)
			PossivelQuestionarioManager.loadLinksAsASet(this.pqDao);
		else
			PossivelQuestionarioManager.loadLinksAsABloomFilter(this.pqDao);
	}
	
	// Demais métodos
	public boolean containsLink(String link) throws Exception {
		return this.pqDao.containsLink(link);
	}
	
	public Set<PossivelQuestionario> getAll() throws Exception {
		return pqDao.getAll();
	}
	
	public void save(PossivelQuestionario q) throws Exception {
		pqDao.save(q);
		if(bfSavedLinks != null)
			bfSavedLinks.put(q.getLink_doc());
		else
			setSavedLinks.add(q.getLink_doc());
	}
	
	// Métodos estáticos
	public static boolean linkWasSaved(String link){
		if(bfSavedLinks != null)
			return bfSavedLinks.mightContain(link);
		else if(setSavedLinks != null)
			return setSavedLinks.contains(link);
		else
			return false;
	}
	
	public static Set<String> getLinksAsASet(){
		return setSavedLinks;
	}
	
	public static BloomFilter<String> getLinksAsABloomFilter(){
		return bfSavedLinks;
	}
	
	public static synchronized void loadLinksAsASet() {
		BasicConnection conn = new BasicConnection(ProjectConfigs.getCrawlerDatabaseConfigs());
		loadLinksAsASet(new PossivelQuestionarioDao(conn));
		conn.close();
	}
	
	private static synchronized void loadLinksAsASet(PossivelQuestionarioDao dao) {
		if(setSavedLinks != null) return;
		
		try {
			setSavedLinks = dao.getAllLinksAsASet();
			
			CommonLogger.debug("{} carregou os links dos possiveis questionarios do banco de dados.", 
					Thread.currentThread().getName());
		} catch (Exception e) {
			// Database não deve esta funcionado, então mata a aplicação
			CommonLogger.fatalError(e);
		}
	}
	
	public static synchronized void loadLinksAsABloomFilter() {
		BasicConnection conn = new BasicConnection(ProjectConfigs.getCrawlerDatabaseConfigs());
		loadLinksAsABloomFilter(new PossivelQuestionarioDao(conn));
		conn.close();
	}
	
	private static synchronized void loadLinksAsABloomFilter(PossivelQuestionarioDao dao) {
		if(bfSavedLinks != null) return;
		
		try {
			bfSavedLinks = dao.getAllLinksAsABloomFilter();
			
			CommonLogger.debug("{} carregou os links dos possiveis questionarios do banco de dados.", 
					Thread.currentThread().getName());
		} catch (Exception e) {
			// Database não deve esta funcionado, então mata a aplicação
			CommonLogger.fatalError(e);
		}
	}
}
