package br.ufsc.tcc.common.database.dao;

import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.model.PossivelQuestionario;

public class PossivelQuestionarioDao extends BasicDao {
	
	private static final int BloomFilter_BASE_SIZE = 1000;
	private static final double BloomFilter_BASE_FPP = 0.01;
	
	public PossivelQuestionarioDao(BasicConnection c) {
		super(c, "PossivelQuestionario");
	}
	
	public void save(PossivelQuestionario q) throws Exception{
		HashMap<String, Object> data = new HashMap<>();
		
		data.put("LINK_DOCUMENTO", q.getLink_doc());
		data.put("TITULO_DOCUMENTO", q.getTitulo_doc());
		data.put("ENCONTRADO_EM", q.getEncontradoEm());
		
		this.insert(data);
		q.setId(getLastUID());
	}
	
	public BloomFilter<String> getAllLinksAsABloomFilter() throws Exception {
		this.select("COUNT(*) AS rowcount");
		
		ResultSet result = this.getResultSet();
		int size = BloomFilter_BASE_SIZE;
		if(result.next())
			size += result.getInt("rowcount");
		
		BloomFilter<String> resp = BloomFilter.create(Funnels.stringFunnel(Charset.forName("utf-8")), 
				size, BloomFilter_BASE_FPP);
		
		this.select("LINK_DOCUMENTO");
		
		result = this.getResultSet();
		while(result != null && result.next()){
			resp.put(result.getString("LINK_DOCUMENTO"));
		}
		
		result.close();
		return resp;
	}
	
	public Set<String> getAllLinksAsASet() throws Exception {
		this.select("LINK_DOCUMENTO");
		
		Set<String> resp = new HashSet<>();
		ResultSet result = this.getResultSet();
		while(result != null && result.next()){
			resp.add(result.getString("LINK_DOCUMENTO"));
		}
		
		result.close();
		return resp;
	}
	
	public Set<PossivelQuestionario> getAll() throws Exception {
		Set<PossivelQuestionario> resp = new HashSet<>();
		PossivelQuestionario pq = null;
		
		this.select("*");
		ResultSet result = this.getResultSet();
		while(result != null && result.next()){
			pq = new PossivelQuestionario();
			pq.setId(result.getLong("idPossivelQuestionario"));
			pq.setLink_doc(result.getString("LINK_DOCUMENTO"));
			pq.setTitulo_doc(result.getString("TITULO_DOCUMENTO"));
			pq.setEncontradoEm(result.getTimestamp("ENCONTRADO_EM"));
			resp.add(pq);
		}
		
		result.close();
		return resp;
	}

	public boolean containsLink(String link) throws Exception {
		HashMap<String, Object> where = new HashMap<>();
		where.put("LINK_DOCUMENTO", link);
		
		this.select("COUNT(*) AS rowcount", where);
		
		ResultSet result = this.getResultSet();
		int size = 0;
		if(result.next())
			size = result.getInt("rowcount");
		
		result.close();
		return size > 0;
	}
}
