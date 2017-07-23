package br.ufsc.tcc.extractor.model;

public class Figura {
	
	// idFigura
	private long id;
	// imagem
	private String image_url;
	// Legenda
	private String legenda;
	

	/////////////////// Dono da figura \\\\\\\\\\\\\\\\\\
	// Irá guardar uma Pergunta, P, uma Alternativa, A, ou
	// um Questionario, Q, que será usado para setar os campos
	// dono e idDono do banco de dados.
	private Object dono;
	
	// Construtores
	public Figura(){
		this("", "");
	}
	
	public Figura(String image_url, String legenda){
		this.id = -1;
		this.image_url = image_url.replaceAll("(\r)?\n", "");
		this.legenda = legenda;
		this.dono = null;
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
	 * Este dono pode ser uma Pergunta, uma Alternativa ou um Questionário.
	 * 
	 * @param dono	O dono desta Figura.
	 */
	public void setDono(Object dono) {
		if(dono instanceof Questionario || 
				dono instanceof Pergunta ||
				dono instanceof Alternativa){
			this.dono = dono;
		}else
			this.dono = null;
	}
	
	// Demais métodos
	@Override
	public String toString() {
		return this.getLegenda() +" - "+ this.getImage_url();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;
		Figura other = (Figura) obj;
		return this.toString().equals(other.toString());
	}
}
