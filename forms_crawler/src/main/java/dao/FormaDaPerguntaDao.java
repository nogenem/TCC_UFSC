package dao;

import java.util.HashMap;

import dao.connection.BasicConnection;
import model.FormaDaPergunta;

public class FormaDaPerguntaDao extends BasicDao {
	
	public FormaDaPerguntaDao(BasicConnection c) {
		super(c, "FormaDaPergunta");
	}
	
	public void save(FormaDaPergunta fp) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("DESCRICAO", fp.getDescricao());
		
		this.insert(data);
		fp.setId(getLastUID());
	}
}
