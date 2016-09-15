package model;

public class FormaDaPergunta {
	
	// idTipoPergunta
	private long id;
	// DESCRICAO
	private String descricao;
	
	/* Construtores */
	public FormaDaPergunta() {}
	
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
}
