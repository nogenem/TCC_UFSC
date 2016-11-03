package br.ufsc.tcc.extractor.database.dao;

import java.sql.ResultSet;
import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.BasicDao;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;

public class FormaDaPerguntaDao extends BasicDao {
	
	public FormaDaPerguntaDao(BasicConnection c) {
		super(c, "FormaDaPergunta");
	}
	
	/**
	 * Salva a forma da pergunta passada no banco de dados.
	 * 
	 * @param fp				FormaDaPergunta que se quer salvar.
	 * @throws Exception
	 */
	public void save(FormaDaPergunta fp) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("DESCRICAO", fp.getDescricao());
		
		this.insert(data);
		fp.setId(getLastUID());
	}
	
	/**
	 * Retorna um mapa contendo todas as FormaDaPergunta do banco de dados
	 * com seus atributos. O mapa é da forma: (descrição, FormaDaPergunta).
	 * 
	 * @return				Um mapa contendo todas as FormaDaPergunta do banco de dados.
	 * @throws Exception
	 */
	public HashMap<String, FormaDaPergunta> getAll() throws Exception {
		HashMap<String, FormaDaPergunta> resp = new HashMap<>();
		
		this.select("*");// select all
		
		ResultSet result = this.getResultSet();
		FormaDaPergunta tmp = null;
		while(result != null && result.next()){
			tmp = new FormaDaPergunta(result.getLong("idFormaDaPergunta"),
					result.getString("DESCRICAO"));

			resp.put(tmp.getDescricao(), tmp);
		}
		
		return resp;
	}
}	
