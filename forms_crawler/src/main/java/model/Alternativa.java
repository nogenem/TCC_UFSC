package model;

public class Alternativa {
	
	// IdAlternativa
	private long id;
	// DESCRICAO
	private String descricao;
	
	// Pergunta a qual esta alternativa
	// faz parte
	private Pergunta pergunta;
	
	/* Construtores */
	public Alternativa() {}
	
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

	public Pergunta getPergunta() {
		return pergunta;
	}

	public void setPergunta(Pergunta pergunta) {
		this.pergunta = pergunta;
	}
	
	public Alternativa clone() {
		Alternativa c = new Alternativa();
		c.setDescricao(this.descricao);
		c.setId(this.id);
		c.setPergunta(this.pergunta);
		return c;
	}
}
