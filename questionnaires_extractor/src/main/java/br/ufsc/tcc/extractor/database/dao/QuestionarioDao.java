package br.ufsc.tcc.extractor.database.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.BasicDao;
import br.ufsc.tcc.extractor.model.Questionario;

public class QuestionarioDao extends BasicDao {
	
	public QuestionarioDao(BasicConnection c) {
		super(c, "Questionario");
	}
	
	public void save(Questionario q) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("ASSUNTO", q.getAssunto());
		data.put("LINK_DOCUMENTO", q.getLink_doc());
		
		this.insert(data);
		q.setId(getLastUID());
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
}	
