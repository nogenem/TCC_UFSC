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
		this("", null);
	}
	
	public Pergunta(String descricao){
		this(descricao, null);
	}
	
	public Pergunta(String descricao, FormaDaPergunta forma){
		this.id = -1;
		this.descricao = descricao;
		this.forma = forma;
		this.tipo = this.convertFormaToTipo();
		this.pai = null;
		this.questionario = null;
		this.grupo = null;
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
		this.tipo = this.convertFormaToTipo();
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

	public Grupo getGrupo() {
		if(this.grupo == null && this.pai != null)
			return this.getPai().getGrupo();
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
	
	// Demais métodos
	private String convertFormaToTipo(){
		if(this.getForma() == null) return "";
		
		switch(this.getForma().toString()){
		case "SELECT":
		case "RADIO_INPUT":
		case "RANGE_INPUT":
		case "RANGE_INPUT_GROUP":
		case "RATING":
			return "FECHADO";
		case "CHECKBOX_INPUT":
		case "CHECKBOX_INPUT_MATRIX":
		case "RADIO_INPUT_MATRIX"://TODO é mesmo multipla?
			return "MULTIPLA_ESCOLHA";
		case "TEXT_INPUT":
		case "TEXT_INPUT_MATRIX":
		case "TEXT_INPUT_GROUP":
		case "NUMBER_INPUT":
		case "EMAIL_INPUT":
		case "DATE_INPUT":
		case "TEL_INPUT":
		case "TIME_INPUT":
		case "URL_INPUT":
		case "TEXTAREA":
		case "TEXTAREA_MATRIX":
			return "ABERTO";
		default:
			return "";
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(">> " +this.getDescricao() +" ["+ this.getForma() + "]\n");
		builder.append("\tGrupo: " +this.getGrupo()+ "\n");
		builder.append("\tAlternativas:\n");
		for(Alternativa a : this.getAlternativas()){
			builder.append("\t\t" +a+ "\n");
		}
		builder.append("\tFilhas:\n");
		for(Pergunta filha : this.getFilhas()){
			builder.append("\t\t" +filha.getDescricao() +" [" +filha.getForma()+ "]\n");
		}
		return builder.toString();
	}
}
