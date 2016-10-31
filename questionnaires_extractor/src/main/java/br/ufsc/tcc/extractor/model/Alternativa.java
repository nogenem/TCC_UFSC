package br.ufsc.tcc.extractor.model;

public class Alternativa {
	
	// idAlternativa
	private long id;
	// DESCRICAO
	private String descricao;
	
	// Pergunta a qual esta alternativa faz parte
	private Pergunta pergunta;
	
	// Construtores
	public Alternativa(){}
	
	public Alternativa(String descricao){
		this.descricao = descricao;
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

	public Pergunta getPergunta() {
		return pergunta;
	}

	public void setPergunta(Pergunta pergunta) {
		this.pergunta = pergunta;
	}
	
	/**
	 * Cria um clone simplificado deste objeto. Apenas copia o {@link Alternativa#id} 
	 * e {@link Alternativa#descricao} sem se preocupar com a Pergunta e Figura que 
	 * esta Alternativa possa fazer parte/possuir.
	 */
	public Alternativa clone() {
		Alternativa c = new Alternativa();
		c.setDescricao(this.descricao);
		c.setId(this.id);
		return c;
	}
}
