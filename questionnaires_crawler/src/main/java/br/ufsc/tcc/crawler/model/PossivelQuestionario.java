package br.ufsc.tcc.crawler.model;

import java.sql.Timestamp;
import java.util.Calendar;

public class PossivelQuestionario {
	
	// idPossivelQuestionario
	private long id;
	// LINK_DOCUMENTO
	private String link_doc;
	// TITULO_DOCUMENTO
	private String titulo_doc;
	// ENCONTRADO_EM
	private Timestamp encontrado_em;
	
	// Construtor
	public PossivelQuestionario(){
		this.updateEncontrado_em();
	}
	
	public PossivelQuestionario(String link_doc, String titulo_doc){
		this.link_doc = link_doc;
		this.titulo_doc = titulo_doc;
		this.updateEncontrado_em();
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

	public Timestamp getEncontrado_em() {
		return encontrado_em;
	}

	public void setEncontrado_em(Timestamp encontrado_em) {
		this.encontrado_em = encontrado_em;
	}
	
	/**
	 * Atualiza o TimeStamp do atributo {@link PossivelQuestionario#encontrado_em}.
	 */
	public void updateEncontrado_em(){
		Calendar calendar = Calendar.getInstance();
		this.encontrado_em = new Timestamp(calendar.getTime().getTime());
	}
}
