package dao;

import java.util.HashMap;

import dao.connection.BasicConnection;
import model.Alternativa;
import model.Pergunta;

public class AlternativaDao extends BasicDao {
	
	public AlternativaDao(BasicConnection c) {
		super(c, "Alternativa");
	}
	
	public void save(Alternativa a) throws Exception {
		HashMap<String, Object> data = new HashMap<>();
		
		Pergunta p = a.getPergunta();
		if(p != null){
			data.put("Pergunta_Questionario_idQuestionario", p.getQuestionario().getId());
			data.put("Pergunta_idPergunta", p.getId());
		}
		
		data.put("DESCRICAO", a.getDescricao());

		this.insert(data);
		a.setId(getLastUID());
	}
	
}
