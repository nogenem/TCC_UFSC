package br.ufsc.tcc.common.model;

import java.sql.Timestamp;
import java.util.Calendar;

import br.ufsc.tcc.common.util.CommonUtil;

public class PossivelQuestionario {
	
	private long id;
	private String link_doc;
	private String title_doc;
	private Timestamp foundAt;
	
	// Construtor
	public PossivelQuestionario(){
		this.updateFoundAt();
	}
	
	public PossivelQuestionario(String link_doc, String title_doc){
		this.link_doc = link_doc;
		this.title_doc = title_doc;
		this.updateFoundAt();
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

	public String getTitle_doc() {
		return title_doc;
	}

	public void setTitle_doc(String title_doc) {
		this.title_doc = title_doc;
	}

	public Timestamp getFoundAt() {
		return foundAt;
	}

	public void setFoundAt(Timestamp foundAt) {
		this.foundAt = foundAt;
	}
	
	public void updateFoundAt(){
		this.foundAt = CommonUtil.getCurrentTime();
	}
}
