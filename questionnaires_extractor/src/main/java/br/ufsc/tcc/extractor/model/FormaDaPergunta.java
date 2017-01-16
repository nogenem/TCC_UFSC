package br.ufsc.tcc.extractor.model;

public class FormaDaPergunta {
	
	// idFormaDaPergunta
	private long id;
	// descricao
	private String descricao;
	
	// Construtores
	public FormaDaPergunta(){
		this(-1, "");
	}
	
	public FormaDaPergunta(long id, String descricao){
		this.id = id;
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
	
	// Demais métodos
	@Override
	public String toString() {
		return this.getDescricao();
	}
}
