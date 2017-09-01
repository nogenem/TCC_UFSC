package br.ufsc.tcc.extractor.model;

public class Alternativa {
	
	// idAlternativa
	private long id;
	// DESCRICAO
	private String descricao;
	
	// Pergunta a qual esta alternativa faz parte
	private Pergunta pergunta;
	
	// Construtores
	public Alternativa(){
		this("");
	}
	
	public Alternativa(String descricao){
		this.id = -1;
		this.descricao = descricao;
		this.pergunta = null;
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
	
	// Demais métodos
	/**
	 * Cria um clone simplificado deste objeto. Apenas copia o {@link Alternativa#id} 
	 * e {@link Alternativa#descricao} sem se preocupar com a Pergunta e Figura que 
	 * esta Alternativa possa fazer parte/possuir.
	 * 
	 * @return		Um clone simplificado desta alternativa.
	 */
	public Alternativa clone() {
		Alternativa c = new Alternativa();
		c.setDescricao(this.descricao);
		c.setId(this.id);
		return c;
	}
	
	@Override
	public String toString() {
		return this.getDescricao();
	}
}
