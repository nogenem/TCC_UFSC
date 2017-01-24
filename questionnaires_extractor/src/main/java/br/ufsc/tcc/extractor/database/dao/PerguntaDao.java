package br.ufsc.tcc.extractor.database.dao;

import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.BasicDao;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class PerguntaDao extends BasicDao {
	
	public PerguntaDao(BasicConnection c) {
		super(c, "Pergunta");
	}
	
	public void save(Pergunta p) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		Questionario q = p.getQuestionario();
		Pergunta pai = p.getPai();
		
		if(pai != null)
			data.put("PerguntaPai_idPergunta", pai.getId());	

		data.put("Questionario_idQuestionario", q.getId());

		if(p.getForma() != null)
			data.put("FormaDaPergunta_idFormaDaPergunta", p.getForma().getId());
		else
			CommonLogger.info("PerguntaDao::save()> Pergunta sem FormaDaPergunta!\n\t [{} / {}]", 
					q.getLink_doc(), p.getDescricao());
		
		if(p.getGrupo() != null)
			data.put("Grupo_idGrupo", p.getGrupo().getId());
		
		data.put("TipoPergunta", p.getTipo());
		data.put("DESCRICAO", p.getDescricao());
		
		this.insert(data);
		p.setId(getLastUID());
	}
}
