package dao;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import dao.connection.BasicConnection;
import model.Questionario;

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
	
	public Set<String> getAllLinks() throws Exception {
		Set<String> resp = new HashSet<>();
		
		this.select("LINK_DOCUMENTO");
		
		ResultSet result = this.getResultSet();
		while(result != null && result.next()){
			resp.add(result.getString("LINK_DOCUMENTO"));
		}
		return resp;
	}

}
