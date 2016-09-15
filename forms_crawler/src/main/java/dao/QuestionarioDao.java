package dao;

import java.util.HashMap;

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

}
