package br.ufsc.tcc.extractor.model;

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
	// Lista de grupos do questionario
	private ArrayList<Grupo> grupos;
	// Lista de figuras encontradas no questionario
	private ArrayList<Figura> figuras;
	
	// Construtores
	public Questionario(){
		this.perguntas = new ArrayList<>();
		this.grupos = new ArrayList<>();
		this.figuras = new ArrayList<>();
	}
	
	// Getters e Setters
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
		p.setQuestionario(this); 
		this.perguntas.add(p);
	}

	public ArrayList<Grupo> getGrupos() {
		return grupos;
	}

	public void setGrupos(ArrayList<Grupo> grupos) {
		this.grupos = grupos;
	}
	
	public void addGrupo(Grupo g){
		g.setQuestionario(this);
		this.grupos.add(g);
	}
	
	public ArrayList<Figura> getFiguras() {
		return figuras;
	}

	public void setFiguras(ArrayList<Figura> figuras) {
		this.figuras = figuras;
	}
	
	public void addFigura(Figura f){
		this.figuras.add(f);
	}
}
