package br.ufsc.tcc.extractor.model;

public class Figura {
	
	// idFigura
	private long id;
	// Legenda
	private String legenda;
	// imagem
	private String image_url;

	/////////////////// Dono da figura \\\\\\\\\\\\\\\\\\
	// Irá guardar uma Pergunta, P, ou uma Alternativa, A,
	// que será usada para setar os campos
	// dono e idDono do banco de dados.
	private Object dono;
	
	// Construtores
	public Figura(){}
	
	public Figura(String image_url, String legenda){
		this.image_url = image_url;
		this.legenda = legenda;
	}
	
	// Getters e Setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLegenda() {
		return legenda;
	}

	public void setLegenda(String legenda) {
		this.legenda = legenda;
	}

	public String getImage_url() {
		return image_url;
	}

	public void setImage_url(String image_url) {
		this.image_url = image_url;
	}

	public Object getDono() {
		return dono;
	}
	
	/**
	 * Seta o dono da Figura. </br>
	 * Este dono deve ser uma Pergunta ou uma Alternativa.
	 * 
	 * @param dono	O dono desta Figura.
	 */
	public void setDono(Object dono) {
		if(dono instanceof Pergunta ||
				dono instanceof Alternativa){
			this.dono = dono;
		}
	}
}
