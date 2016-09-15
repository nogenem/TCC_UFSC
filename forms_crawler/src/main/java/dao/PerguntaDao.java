package dao;

import java.util.HashMap;

import dao.connection.BasicConnection;
import model.Pergunta;

public class PerguntaDao extends BasicDao {
	
	public PerguntaDao(BasicConnection c) {
		super(c, "Pergunta");
	}
	
	public void save(Pergunta p) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("Questionario_idQuestionario", p.getQuestionario().getId());
		
		Pergunta pai = p.getPai();
		if(pai != null){
			data.put("PerguntaPai_idPergunta", pai.getId());
			data.put("Pergunta_Questionario_idQuestionario", pai.getQuestionario().getId());
		}
		data.put("FormaDaPergunta_idFormaDaPergunta", p.getForma().getId());
		// grupo
		
		data.put("TipoPergunta", p.getTipo());
		data.put("DESCRICAO", p.getDescricao());
		
		this.insert(data);
		p.setId(getLastUID());
	}
}
