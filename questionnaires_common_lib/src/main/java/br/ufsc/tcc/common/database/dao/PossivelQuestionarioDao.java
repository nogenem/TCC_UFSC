package br.ufsc.tcc.common.database.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.model.PossivelQuestionario;

public class PossivelQuestionarioDao extends BasicDao {
	
	public PossivelQuestionarioDao(BasicConnection c) {
		super(c, "PossivelQuestionario");
	}
	
	public void save(PossivelQuestionario q) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("LINK_DOCUMENTO", q.getLink_doc());
		data.put("TITULO_DOCUMENTO", q.getTitulo_doc());
		data.put("ENCONTRADO_EM", q.getEncontradoEm());
		
		this.insert(data);
		q.setId(getLastUID());
	}
	
	public void remove(String link) throws Exception {
		HashMap<String, Object> where = new HashMap<>();
		
		where.put("LINK_DOCUMENTO", link);
		
		this.delete(where);
	}
	
	public ArrayList<String> getAllLinks() throws Exception {
		ArrayList<String> resp = new ArrayList<>();
		
		this.select("LINK_DOCUMENTO");
		
		ResultSet result = this.getResultSet();
		while(result != null && result.next()){
			resp.add(result.getString("LINK_DOCUMENTO"));
		}
		return resp;
	}
	
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
			pq.setEncontradoEm(result.getTimestamp("ENCONTRADO_EM"));
			resp.add(pq);
		}
		return resp;
	}
}
