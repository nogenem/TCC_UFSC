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
		data.put("TITLE_DOCUMENTO", q.getTitle_doc());
		data.put("FOUND_AT", q.getFoundAt());
		
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
			pq.setTitle_doc(result.getString("TITLE_DOCUMENTO"));
			pq.setFoundAt(result.getTimestamp("FOUND_AT"));
			resp.add(pq);
		}
		return resp;
	}
}
