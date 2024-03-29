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
		this.setForma(forma);
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
		if(tipo.matches("(ABERTO|FECHADO|MULTIPLA_ESCOLHA)"))
			this.tipo = tipo;
		else
			CommonLogger.info("Pergunta:setTipo()> Tipo nao permitido ({}).", tipo);
	}

	public FormaDaPergunta getForma() {
		return forma;
	}
	
	/**
	 * Seta a forma desta pergunta.<br>
	 * <b>Side effect</b>: seta o tipo desta pergunta utilizando o método {@link #convertFormaToTipo()}.
	 * 
	 * @param forma
	 */
	public void setForma(FormaDaPergunta forma) {
		this.forma = forma;
		this.convertFormaToTipo();
	}

	public Pergunta getPai() {
		return pai;
	}

	public void setPai(Pergunta pai) {
		this.pai = pai;
	}
	
	/**
	 * Retorna o questionário que esta pergunta faz parte.<br>
	 * Caso esta pergunta seja filha de outra, é retornado o questionário ao
	 * qual o pai desta pergunta faz parte.
	 * 
	 * @return			Questionário que esta pergunta faz parte.
	 */
	public Questionario getQuestionario() {
		if(this.pai != null)
			return this.pai.getQuestionario();
		return questionario;
	}

	public void setQuestionario(Questionario questionario) {
		this.questionario = questionario;
	}
	
	/**
	 * Retorna o grupo que esta pergunta faz parte.<br>
	 * Caso esta pergunta seja filha de outra, é retornado o grupo ao
	 * qual o pai desta pergunta faz parte.
	 * 
	 * @return			Grupo que esta pergunta faz parte.
	 */
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
	
	/**
	 * Adiciona a pergunta {@code p} a lista de perguntas filhas desta
	 * pergunta.<br>
	 * <b>Side effect</b>: Seta também a pergunta pai da pergunta {@code p} para
	 * apontar para esta pergunta.
	 * 
	 * @param p
	 */
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
	
	/**
	 * Adiciona a alternativa {@code a} a lista de alternativas desta
	 * pergunta.<br>
	 * <b>Side effect</b>: Seta também a pergunta da alternativa {@code a} para
	 * apontar para esta pergunta.
	 * 
	 * @param a
	 */
	public void addAlternativa(Alternativa a){
		a.setPergunta(this);
		this.alternativas.add(a);
	}
	
	// Demais métodos
	/**
	 * Verifica se esta pergunta é da {@code forma} passada.
	 * 
	 * @param forma
	 * @return				<b>TRUE</b> caso esta pergunta seja da {@code forma} 
	 * 						passada ou, <br>
	 * 						<b>FALSE</b> caso contrario.
	 */
	public boolean isA(String forma){
		return this.getForma() ==
				FormaDaPerguntaManager.getForma(forma.toUpperCase());
	}
	
	/**
	 * Seta o tipo desta pergunta usando como base a forma dela.
	 */
	public void convertFormaToTipo(){
		if(this.getForma() == null) {
			this.tipo = "";
			return;
		}
		
		switch(this.getForma().toString()){
		case "SELECT":
		case "SELECT_GROUP":
		case "IMAGE_RADIO_INPUT":
		case "IMAGE_RADIO_INPUT_GROUP":
		case "IMAGE_RADIO_INPUT_MATRIX":
		case "RADIO_INPUT":
		case "RADIO_INPUT_GROUP":
		case "RADIO_INPUT_MATRIX":
		case "RANGE_INPUT":
		case "RANGE_INPUT_GROUP":
		case "RATING":
		case "RATING_GROUP":
			this.tipo = "FECHADO";
			break;
		case "IMAGE_CHECKBOX_INPUT":
		case "IMAGE_CHECKBOX_INPUT_GROUP":
		case "IMAGE_CHECKBOX_INPUT_MATRIX":
		case "CHECKBOX_INPUT":
		case "CHECKBOX_INPUT_GROUP":
		case "CHECKBOX_INPUT_MATRIX":
			this.tipo = "MULTIPLA_ESCOLHA";
			break;
		case "TEXT_INPUT":
		case "TEXT_INPUT_GROUP":
		case "TEXT_INPUT_MATRIX":
		case "NUMBER_INPUT":
		case "NUMBER_INPUT_GROUP":
		case "NUMBER_INPUT_MATRIX":
		case "EMAIL_INPUT":
		case "EMAIL_INPUT_GROUP":
		case "EMAIL_INPUT_MATRIX":
		case "DATE_INPUT":
		case "DATE_INPUT_GROUP":
		case "DATE_INPUT_MATRIX":
		case "TEL_INPUT":
		case "TEL_INPUT_GROUP":
		case "TEL_INPUT_MATRIX":
		case "TIME_INPUT":
		case "TIME_INPUT_GROUP":
		case "TIME_INPUT_MATRIX":
		case "URL_INPUT":
		case "URL_INPUT_GROUP":
		case "URL_INPUT_MATRIX":
		case "TEXTAREA":
		case "TEXTAREA_GROUP":
		case "TEXTAREA_MATRIX":
			this.tipo = "ABERTO";
			break;
		case "MIX_COMP_GROUP":
		case "MIX_COMP_MATRIX":
		case "MULTI_COMP":
		case "MULTI_COMP_GROUP":{
			String tipo = "", tmp = "";
			for(Pergunta p : this.filhas) {
				tmp = p.getTipo();
				if(tmp.equals("ABERTO"))
					tipo = "ABERTO";
				else if(tmp.equals("MULTIPLA_ESCOLHA") && !tipo.equals("ABERTO"))
					tipo = "MULTIPLA_ESCOLHA";
				else if(tipo.matches("FECHADO|"))
					tipo = tmp;
			}
			this.tipo = tipo;
			break;
		}default:
			this.tipo = "";
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String tmpTxt = "";
		builder.append(this.getDescricao() +" ["+ this.getForma() + 
				" / "+ this.getTipo()+"]\n");
		builder.append("\tGrupo: " +this.getGrupo()+ "\n");
		builder.append("\tAlternativas [" +this.alternativas.size()+ "]:\n");
		for(Alternativa a : this.getAlternativas()){
			tmpTxt = a.toString().replaceAll("\n", "\n\t\t   ");
			builder.append("\t\t" +tmpTxt+ "\n");
		}
		builder.append("\tFilhas [" +this.filhas.size()+ "]:\n");
		for(Pergunta filha : this.getFilhas()){
			tmpTxt = filha.getDescricao().replaceAll("\n", "\n\t\t   ");
			builder.append("\t\t" +tmpTxt+" [" +filha.getForma()+ "]\n");
			for(Alternativa a : filha.getAlternativas()){
				tmpTxt = a.toString().replaceAll("\n", "\n\t\t\t   ");
				builder.append("\t\t\t"+ tmpTxt +"\n");
			}
		}
		builder.append("\n");
		return builder.toString();
	}
}
