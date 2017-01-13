package br.ufsc.tcc.crawler.database.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.BasicDao;
import br.ufsc.tcc.crawler.model.PossivelQuestionario;

public class PossivelQuestionarioDao extends BasicDao {
	
	public PossivelQuestionarioDao(BasicConnection c) {
		super(c, "PossivelQuestionario");
	}
	
	/**
	 * Salva o possivel questionario passado no banco de dados.
	 * 
	 * @param q				PossivelQuestionario que se quer salvar.
	 * @throws Exception
	 */
	public void save(PossivelQuestionario q) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("LINK_DOCUMENTO", q.getLink_doc());
		data.put("TITULO_DOCUMENTO", q.getTitulo_doc());
		data.put("ENCONTRADO_EM", q.getEncontrado_em());
		
		this.insert(data);
		q.setId(getLastUID());
	}
	
	/**
	 * Retorna todos os valores do campo LINK_DOCUMENTO do banco de dados.
	 * 
	 * @return				Uma lista com todos os valores do campo LINK_DOCUMENTO 
	 * 						do banco de dados.
	 * @throws Exception
	 */
	public ArrayList<String> getAllLinks() throws Exception {
		ArrayList<String> resp = new ArrayList<>();
		
		this.select("LINK_DOCUMENTO");
		
		ResultSet result = this.getResultSet();
		while(result != null && result.next()){
			resp.add(result.getString("LINK_DOCUMENTO"));
		}
		return resp;
	}
	
	/**
	 * Retorna todos os Possiveis Questionários do banco de dados, com todos os
	 * seus atributos.
	 * 
	 * @return				Uma lista com todos os Possiveis Questionários do banco de dados.
	 * @throws Exception
	 */
	public ArrayList<PossivelQuestionario> getAll() throws Exception {
		ArrayList<PossivelQuestionario> resp = new ArrayList<>();
		PossivelQuestionario pq = null;
		
		this.select("*");
		ResultSet result = this.getResultSet();
		while(result != null && result.next()){
			pq = new PossivelQuestionario();
			pq.setId(result.getLong("idPossivelQuestionario"));
			pq.setLink_doc(result.getString("LINK_DOCUMENTO"));
			pq.setTitulo_doc(result.getString("TITULO_DOCUMENTO"));
			pq.setEncontrado_em(result.getTimestamp("ENCONTRADO_EM"));
			resp.add(pq);
		}
		return resp;
	}
}	
