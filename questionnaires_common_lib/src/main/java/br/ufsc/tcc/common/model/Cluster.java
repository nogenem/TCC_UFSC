package br.ufsc.tcc.common.model;

import java.util.ArrayList;

public class Cluster {
	
	private ArrayList<MyNode> group;
	
	// Construtores
	public Cluster(){
		this.group = new ArrayList<>();
	}
	
	// Getters e Setters
	public MyNode get(int index){
		if(index < 0 || index >= this.group.size())
			return null;
		return this.group.get(index);
	}
	
	public ArrayList<MyNode> getGroup(){
		return this.group;
	}
	
	public int size(){
		return this.group.size();
	}
	
	public boolean isEmpty(){
		return this.group.isEmpty();
	}
	
	/**
	 * Retorna uma String contendo o texto de todos os nodos deste Cluster 
	 * concatenadas com '\n', independente se o nodo é de texto ou não.
	 * 
	 * @return		String contendo o texto de todos os nodos deste Cluster.
	 */
	public String getAllNodesText(){
		StringBuilder builder = new StringBuilder();
		for(MyNode node : this.group){
			builder.append(node.getText() +"\n");
		}
		if(builder.length() == 0) return "";
		builder.setLength(builder.length()-1);
		return builder.toString();
	}
	
	/**
	 * Retorna uma String contendo o texto de todos os nodos de texto 
	 * deste Cluster concatenadas com '\n'.
	 * 
	 * @return		String contendo o texto de todos os nodos deste Cluster.
	 */
	public String getText(){
		return getText(false);
	}
	
	/**
	 * Retorna uma String contendo o texto de todos os nodos de texto e, 
	 * caso especificado, os atributos 'value' de input_text desabilitados  
	 * deste Cluster concatenadas com '\n'.
	 * 
	 * @param useValueOfInputDesabled	Usar os atributos 'value' de input_text desabilitados?
	 * @return		String contendo o texto de todos os nodos deste Cluster.
	 */
	public String getText(boolean useValueOfInputDesabled) {
		StringBuilder builder = new StringBuilder();
		for(MyNode node : this.group){
			if(node.isText())
				builder.append(node.getText() +"\n");
			else if(useValueOfInputDesabled && node.isATextInputDisabledWithValue())
				builder.append(node.getAttr("value") +"\n");
		}
		if(builder.length() == 0) return "";
		builder.setLength(builder.length()-1);
		return builder.toString();
	}
	
	// Demais métodos
	public Cluster add(MyNode node){
		if(node != null && !this.group.contains(node))
			this.group.add(node);
		return this;
	}
	
	/**
	 * Adiciona todos os nodos do parâmetro {@code other} a este Cluster.
	 * 
	 * @param other		Cluster que se quer pegar os nodos.
	 * @return			Este Cluster.
	 */
	public Cluster join(Cluster other){
		if(other != null){
			for(MyNode node : other.group)
				this.add(node);
		}
		return this;
	}
	
	/**
	 * Retorna o primeiro nodo deste Cluster.
	 * 
	 * @return		O primeiro nodo deste Cluster.
	 */
	public MyNode first(){
		if(!this.group.isEmpty())
			return this.group.get(0);
		return null;
	}
	
	/**
	 * Retorna o nodo do meio deste Cluster.
	 * 
	 * @return		O nodo do meio deste Cluster.
	 */
	public MyNode middle(){
		if(!this.group.isEmpty())
			return this.group.get((group.size()-1)/2);
		return null;
	}
	
	/**
	 * Retorna o ultimo nodo deste Cluster.
	 * 
	 * @return		O ultimo nodo deste Cluster.
	 */
	public MyNode last(){
		if(!this.group.isEmpty())
			return this.group.get(this.group.size()-1);
		return null;
	}
	
	/**
	 * Verifica se todos os nodos deste Cluster são nodos de texto.
	 * 
	 * @return		<b>True</b> caso todos os nodos sejam de texto ou<br>
	 * 				<b>False</b> caso contrario.
	 */
	public boolean isAllText(){
		return this.group.parallelStream()
				.reduce(true, 
						(a,b) -> a && b.isText(), 
						Boolean::logicalAnd);
	}
	
	/**
	 * Verifica se todos os nodos deste Cluster são nodos de texto ou imagem.
	 * 
	 * @return		<b>True</b> caso todos os nodos sejam de texto ou imagem ou<br>
	 * 				<b>False</b> caso contrario.
	 */
	public boolean isAllTextOrImg(){
		return this.group.parallelStream()
				.reduce(true, 
						(a,b) -> a && b.isImgOrText(), 
						Boolean::logicalAnd);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Cluster:\n");
		for(MyNode node : this.group){
			builder.append("\t"+ node.toString() +"\n");
		}
		return builder.toString();
	}
	
	@Override
	public Cluster clone() {
		Cluster result = new Cluster();
		for(MyNode n : this.group)
			result.add(n);
		return result;
	}
}
