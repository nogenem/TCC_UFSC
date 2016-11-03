package br.ufsc.tcc.extractor.database.dao;

import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.BasicDao;
import br.ufsc.tcc.extractor.model.Grupo;

public class GrupoDao extends BasicDao {
	
	public GrupoDao(BasicConnection c) {
		super(c, "Grupo");
	}
	
	/**
	 * Salva o grupo passado no banco de dados.
	 * 
	 * @param g				Grupo que se quer salvar.
	 * @throws Exception
	 */
	public void save(Grupo g) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("Questionario_idQuestionario", g.getQuestionario().getId());
		data.put("ASSUNTO", g.getAssunto());
		
		this.insert(data);
		g.setId(getLastUID());
	}
}
