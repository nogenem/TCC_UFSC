package model;

public class Grupo {
	
	// idGrupo
	private long id;
	// ASSUNTO
	private String assunto;
	
	// Questionario ao qual este grupo faz parte
	private Questionario questionario;

	/* Construtores */
	public Grupo(){
		
	}
	
	public Grupo(String assunto, Questionario questionario){
		this.assunto = assunto;
		this.questionario = questionario;
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

	public Questionario getQuestionario() {
		return questionario;
	}

	public void setQuestionario(Questionario questionario) {
		this.questionario = questionario;
	}
}
