package br.ufsc.tcc.extractor.database.dao;

import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.BasicDao;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;

public class AlternativaDao extends BasicDao {
	
	public AlternativaDao(BasicConnection c) {
		super(c, "Alternativa");
	}
	
	/**
	 * Salva a alternativa passada no banco de dados.
	 * 
	 * @param a				Alternativa que se quer salvar.
	 * @throws Exception
	 */
	public void save(Alternativa a) throws Exception {
		HashMap<String, Object> data = new HashMap<>();
		
		Pergunta p = a.getPergunta();
		if(p != null){
			data.put("Pergunta_idPergunta", p.getId());
		}
		
		data.put("DESCRICAO", a.getDescricao());

		this.insert(data);
		a.setId(getLastUID());
	}
}
