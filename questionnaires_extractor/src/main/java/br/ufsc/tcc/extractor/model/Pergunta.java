package br.ufsc.tcc.extractor.model;

import java.util.ArrayList;

public class Pergunta {
	
	// idPergunta
	private long id;
	// DESCRICAO
	private String descricao;
	// TipoPergunta
	// 	ABERTO, FECHADO ou MULTIPLA_ESCOLHA
	private String tipo;
	
	// Forma desta pergunta
	private FormaDaPergunta forma;
	// Pergunta pai, se tiver uma
	private Pergunta pai;
	// Questionario ao qual esta pergunta faz parte
	private Questionario questionario;
	// Grupo que esta pergunta faz parte, se tiver um
	private Grupo grupo;
	
	// Se esta pergunta for uma pergunta pai,
	// ela terá uma ou mais perguntas filhas
	private ArrayList<Pergunta> filhas;
	// Lista de alternativas desta pergunta
	private ArrayList<Alternativa> alternativas;
	
	// Construtores
	public Pergunta(){
		this("", "", null);
	}
	
	public Pergunta(String descricao, String tipo, FormaDaPergunta forma){
		this.descricao = descricao;
		this.tipo = tipo;
		this.forma = forma;
		this.filhas = new ArrayList<>();
		this.alternativas = new ArrayList<>();
	}
	
	// Getters e Setters
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
		if(this.pai != null)
			return this.pai.getQuestionario();
		return questionario;
	}

	public void setQuestionario(Questionario questionario) {
		this.questionario = questionario;
	}

	public Grupo getGrupo() {//TODO pegar grupo do pai?
		return grupo;
	}

	public void setGrupo(Grupo grupo) {
		this.grupo = grupo;
	}

	public ArrayList<Pergunta> getFilhas() {
		return filhas;
	}

	public void setFilhas(ArrayList<Pergunta> filhas) {
		this.filhas = filhas;
	}
	
	public void addFilha(Pergunta p){
		p.setPai(this);
		this.filhas.add(p);
	}

	public ArrayList<Alternativa> getAlternativas() {
		return alternativas;
	}

	public void setAlternativas(ArrayList<Alternativa> alternativas) {
		this.alternativas = alternativas;
	}
	
	public void addAlternativa(Alternativa a){
		a.setPergunta(this);
		this.alternativas.add(a);
	}
}
