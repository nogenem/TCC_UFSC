package dao;

import java.util.HashMap;

import dao.connection.BasicConnection;
import model.Grupo;

public class GrupoDao extends BasicDao {
	
	public GrupoDao(BasicConnection c) {
		super(c, "Grupo");
	}
	
	public void save(Grupo g) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("Questionario_idQuestionario", g.getQuestionario().getId());
		data.put("ASSUNTO", g.getAssunto());
		
		this.insert(data);
		g.setId(getLastUID());
	}
}
