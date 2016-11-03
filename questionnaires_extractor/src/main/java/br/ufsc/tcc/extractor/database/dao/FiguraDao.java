package br.ufsc.tcc.extractor.database.dao;

import java.util.HashMap;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.dao.BasicDao;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.Pergunta;

public class FiguraDao extends BasicDao {
	
	public FiguraDao(BasicConnection c){
		super(c, "Figura");
	}
	
	/**
	 * Salva a figura passada no banco de dados.
	 * 
	 * @param f				Figura que se quer salvar.
	 * @throws Exception
	 */
	public void save(Figura f) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("Legenda", f.getLegenda());
		data.put("imagem", f.getImage_url());
		
		Object dono = f.getDono();
		if(dono instanceof Pergunta){
			data.put("dono", "P");
			data.put("idDono", ((Pergunta)dono).getId());
		}else if(dono instanceof Alternativa){
			data.put("dono", "A");
			data.put("idDono", ((Alternativa)dono).getId());
		}else{
			System.err.println("FiguraDao:save()> Dono da Figura nao eh uma "
					+ "Alternativa e nem uma Pergunta!\n\t<"+f.getImage_url()+">");
			return;
		}
		
		this.insert(data);
		f.setId(getLastUID());
	}
}
