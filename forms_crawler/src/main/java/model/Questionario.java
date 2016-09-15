package model;

import java.util.ArrayList;

public class Questionario {
	
	// idQuestionario
	private long id;
	// ASSUNTO
	private String assunto;
	// LINK_DOCUMENTO
	private String link_doc;
	
	// Lista de perguntas do questionario
	private ArrayList<Pergunta> perguntas;
	
	/* Construtores */
	public Questionario() {
		this.perguntas = new ArrayList<>();
	}
	
	/* Getters e Setters */
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	public String getLink_doc() {
		return link_doc;
	}

	public void setLink_doc(String link_doc) {
		this.link_doc = link_doc;
	}

	public ArrayList<Pergunta> getPerguntas() {
		return perguntas;
	}

	public void setPerguntas(ArrayList<Pergunta> perguntas) {
		this.perguntas = perguntas;
	}
	
	public void addPergunta(Pergunta p){
		this.perguntas.add(p);
		p.setQuestionario(this);
	}
}
