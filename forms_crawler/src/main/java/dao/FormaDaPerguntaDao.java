package dao;

import java.sql.ResultSet;
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
	
	//XXX parte da FormaDaPerguntaManager
	public HashMap<String, FormaDaPergunta> getAll() throws Exception {
		HashMap<String, FormaDaPergunta> resp = new HashMap<>();
		
		this.select("");
		
		ResultSet result = this.getResultSet();
		FormaDaPergunta tmp = null;
		while(result != null && result.next()){
			tmp = new FormaDaPergunta();
			tmp.setDescricao(result.getString("DESCRICAO"));
			tmp.setId(result.getLong("idFormaDaPergunta"));
			resp.put(tmp.getDescricao(), tmp);
		}
		
		return resp;
	}
}
