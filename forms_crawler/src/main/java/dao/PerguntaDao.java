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
		if(pai != null)
			data.put("PerguntaPai_idPergunta", pai.getId());
		
		if(p.getForma() != null)
			data.put("FormaDaPergunta_idFormaDaPergunta", p.getForma().getId());
		else
			System.err.println(String.format("Pergunta sem FormaDaPergunta!\n\t [%s / %s]", 
					p.getQuestionario().getLink_doc(), p.getDescricao()));
		
		if(p.getGrupo() != null)
			data.put("Grupo_idGrupo", p.getGrupo().getId());
		
		data.put("TipoPergunta", p.getTipo());
		data.put("DESCRICAO", p.getDescricao());
		
		this.insert(data);
		p.setId(getLastUID());
	}
}
