package br.ufsc.tcc.common.model;

import java.sql.Timestamp;

import br.ufsc.tcc.common.util.CommonUtil;

public class PossivelQuestionario {
	
	private long id;
	private String link_doc;
	private String titulo_doc;
	private Timestamp encontrado_em;
	
	// Construtor
	public PossivelQuestionario(){
		this.updateEncontradoEm();
	}
	
	public PossivelQuestionario(String link_doc, String titulo_doc){
		this.link_doc = link_doc;
		this.titulo_doc = titulo_doc;
		this.updateEncontradoEm();
	}
	
	// Getters e Setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLink_doc() {
		return link_doc;
	}

	public void setLink_doc(String link_doc) {
		this.link_doc = link_doc;
	}

	public String getTitulo_doc() {
		return titulo_doc;
	}

	public void setTitulo_doc(String titulo_doc) {
		this.titulo_doc = titulo_doc;
	}

	public Timestamp getEncontradoEm() {
		return encontrado_em;
	}

	public void setEncontradoEm(Timestamp encontradoEm) {
		this.encontrado_em = encontradoEm;
	}
	
	public void updateEncontradoEm(){
		this.encontrado_em = CommonUtil.getCurrentTime();
	}
}
