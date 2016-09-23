package model;

import java.util.ArrayList;

public class Pergunta {
	
	// idPergunta
	private long id;
	// DESCRICAO
	private String descricao;
	// TipoDaPergunta
	// 	ABERTO ou FECHADO
	private String tipo;
	
	// Forma desta pergunta
	private FormaDaPergunta forma;
	// Pergunta pai, se tiver uma
	private Pergunta pai;
	// Questionario ao qual esta pergunta faz parte
	private Questionario questionario;
	// Grupo
	
	// Se esta pergunta for uma pergunta pai,
	// ela tera uma ou mais perguntas filhas
	private ArrayList<Pergunta> perguntas;
	// Lista de alternativas desta pergunta
	private ArrayList<Alternativa> alternativas;
	
	/* Construtores */
	public Pergunta() {
		this.perguntas = new ArrayList<>();
		this.alternativas = new ArrayList<>();
	}
	
	/* Getters e Setters */
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		tipo = tipo.toUpperCase();
		if(!tipo.matches("(ABERTO|FECHADO|MULTIPLA_ESCOLHA)"))
			System.err.println("Pergunta:setTipo()> Tipo nao permitido ("+tipo+").");
		else
			this.tipo = tipo;
	}

	public FormaDaPergunta getForma() {
		return forma;
	}

	public void setForma(FormaDaPergunta forma) {
		this.forma = forma;
	}
	
	public Pergunta getPai() {
		return pai;
	}

	public void setPai(Pergunta pai) {
		this.pai = pai;
	}

	public Questionario getQuestionario() {
		return questionario;
	}

	public void setQuestionario(Questionario questionario) {
		this.questionario = questionario;
	}

	public ArrayList<Pergunta> getPerguntas() {
		return perguntas;
	}

	public void setPerguntas(ArrayList<Pergunta> perguntas) {
		this.perguntas = perguntas;
	}
	
	public void addPergunta(Pergunta p){
		p.setPai(this);
		this.perguntas.add(p);
	}

	public ArrayList<Alternativa> getAlternativas() {
		return alternativas;
	}

	public void setAlternativas(ArrayList<Alternativa> alternativas) {
		this.alternativas = alternativas;
	}
	
	public void addAlternativa(Alternativa a){
		this.alternativas.add(a);
		a.setPergunta(this);
	}
}
