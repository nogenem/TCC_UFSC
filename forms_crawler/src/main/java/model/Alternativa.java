package model;

public class Alternativa {
	
	// IdAlternativa
	private long id;
	// DESCRICAO
	private String descricao;
	// LINK_IMAGEM
	private String link_img;

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
	
	public String getLink_img() {
		return link_img;
	}

	public void setLink_img(String link_img) {
		this.link_img = link_img;
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
		c.setLink_img(this.link_img);
		c.setId(this.id);
		c.setPergunta(this.pergunta);
		return c;
	}
}
