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
	
	public String getAllNodesText(){
		StringBuilder builder = new StringBuilder();
		for(MyNode node : this.group){
			builder.append(node.getText() +"\n");
		}
		if(builder.length() == 0) return "";
		builder.setLength(builder.length()-1);
		return builder.toString();
	}
	
	public String getText(){
		StringBuilder builder = new StringBuilder();
		for(MyNode node : this.group){
			if(node.getType() == MyNodeType.TEXT)
				builder.append(node.getText() +"\n");
		}
		if(builder.length() == 0) return "";
		builder.setLength(builder.length()-1);
		return builder.toString();
	}
	
	// Demais métodos
	public Cluster add(MyNode node){
		if(node == null) return this;
		if(!this.group.contains(node))
			this.group.add(node);
		return this;
	}
	
	public Cluster join(Cluster other){
		if(other == null) return this;
		for(MyNode node : other.group)
			this.add(node);
		return this;
	}
	
	public MyNode first(){
		if(this.group.isEmpty())
			return null;
		return this.group.get(0);
	}
	
	public MyNode middle(){
		if(this.group.isEmpty())
			return null;
		return this.group.get((group.size()-1)/2);
	}
	
	public MyNode last(){
		if(this.group.isEmpty())
			return null;
		return this.group.get(this.group.size()-1);
	}
	
	public boolean isAllText(){
		return this.group.parallelStream()
				.reduce(true, 
						(a,b) -> a && b.getType() == MyNodeType.TEXT, 
						Boolean::logicalAnd);
	}
	
	public boolean isAllTextOrImg(){
		return this.group.parallelStream()
				.reduce(true, 
						(a,b) -> a && (b.getType() == MyNodeType.TEXT || 
										b.getType() == MyNodeType.IMG), 
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
}
