package br.ufsc.tcc.extractor.model;

import java.util.ArrayList;

import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;

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
			CommonLogger.info("Pergunta:setTipo()> Tipo nao permitido ({}).", tipo);
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
	public boolean isA(String forma){
		return this.getForma() ==
				FormaDaPerguntaManager.getForma(forma.toUpperCase());
	}
	
	private String convertFormaToTipo(){
		if(this.getForma() == null) return "";
		
		switch(this.getForma().toString()){
		case "SELECT":
		case "SELECT_GROUP":
		case "RADIO_INPUT":
		case "RADIO_INPUT_GROUP":
		case "RANGE_INPUT":
		case "RANGE_INPUT_GROUP":
		case "RATING":
			return "FECHADO";
		case "CHECKBOX_INPUT":
		case "CHECKBOX_INPUT_GROUP":
		case "CHECKBOX_INPUT_MATRIX":
		case "RADIO_INPUT_MATRIX":
			return "MULTIPLA_ESCOLHA";
		case "TEXT_INPUT":
		case "TEXT_INPUT_GROUP":
		case "TEXT_INPUT_MATRIX":
		case "NUMBER_INPUT":
		case "NUMBER_INPUT_GROUP":
		case "EMAIL_INPUT":
		case "EMAIL_INPUT_GROUP":
		case "DATE_INPUT":
		case "DATE_INPUT_GROUP":
		case "TEL_INPUT":
		case "TEL_INPUT_GROUP":
		case "TIME_INPUT":
		case "TIME_INPUT_GROUP":
		case "URL_INPUT":
		case "URL_INPUT_GROUP":
		case "TEXTAREA":
		case "TEXTAREA_GROUP":
		case "TEXTAREA_MATRIX":
		case "MIX_COMP_GROUP"://TODO deixar assim mesmo?
		case "MIX_COMP_MATRIX":
		case "MULTI_COMP":
			return "ABERTO";
		default:
			return "";
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(">> " +this.getDescricao() +" ["+ this.getForma() + 
				" / "+ this.getTipo()+"]\n");
		builder.append("\tGrupo: " +this.getGrupo()+ "\n");
		builder.append("\tAlternativas:\n");
		for(Alternativa a : this.getAlternativas()){
			builder.append("\t\t" +a+ "\n");
		}
		builder.append("\tFilhas:\n");
		for(Pergunta filha : this.getFilhas()){
			builder.append("\t\t" +filha.getDescricao() +" [" +filha.getForma()+ "]\n");
		}
		builder.append("\n");
		return builder.toString();
	}
}
