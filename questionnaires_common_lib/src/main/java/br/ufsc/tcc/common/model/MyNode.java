package br.ufsc.tcc.common.model;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Node;

import br.ufsc.tcc.common.util.CommonUtil;

/**
 * Classe que possui os atribuitos necessários para representar
 * um nodo da árvore HTML.
 * 
 * @author Gilney N. Mathias
 */
public class MyNode implements Comparable<MyNode> {
	
	private String text;
	private MyNodeType type;
	private Attributes attrs;
	private DeweyExt dewey;
	
	// Construtores
	public MyNode() {
		this(null, null);
	}
	
	public MyNode(Node node, DeweyExt dewey) {
		if(node != null){
			this.text = CommonUtil.getNodeRepresentation(node);
			this.attrs = node.attributes();
			this.type = MyNodeType.get(this.text, node.nodeName());
		}else{
			this.text = "";
			this.attrs = null;
			this.type = MyNodeType.UNKNOWN;
		}
		this.dewey = dewey;
	}
	
	// Getters e Setters
	public String getText() {
		return this.text;
	}
	
	public void setText(String text){
		this.text = text;
	}
	
	public MyNodeType getType(){
		return this.type;
	}
	
	public Attributes getAttributes(){
		return this.attrs;
	}
	
	public String getAttr(String key){
		if(this.attrs == null) return "";
		String attr = this.attrs.get(key.toLowerCase());
		if(attr.isEmpty()) 
			attr = this.attrs.get(key.toUpperCase());
		return attr;
	}
	
	public DeweyExt getDewey() {
		return dewey;
	}
	
	public void setDewey(DeweyExt dewey){
		this.dewey = dewey;
	}
	
	// Demais métodos
	/**
	 * Verifica se este Nodo é do tipo passado pelo parâmetro {@code type}.<br>
	 * Os tipos de um Nodo podem ser vistos na classe {@link MyNodeType}.
	 * 
	 * @param type		Tipo para verificação.
	 * @return			<b>True</b> caso este Nodo seja do tipo {@code type} ou<br>
	 * 					<b>False</b> caso contrario.
	 */
	public boolean isA(String type){
		return this.getType().toString()
				.equals(type.toUpperCase());
	}
	
	public boolean isImage(){
		return this.type == MyNodeType.IMG;
	}
	
	public boolean isText(){
		return this.type == MyNodeType.TEXT;
	}
	
	public boolean isImgOrText(){
		return this.isImage() || this.isText();
	}
	
	public boolean isComponent(){
		return !this.isImgOrText() &&
				this.type != MyNodeType.UNKNOWN;
	}
	
	public boolean isATextInputDisabledWithValue(){
		return this.isA("TEXT_INPUT") && !this.getAttr("disabled").isEmpty() && 
				!this.getAttr("value").isEmpty();
	}
	
	@Override
	public String toString() {
		return this.dewey +" - "+ this.text;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || this.getClass() != obj.getClass())
			return false;
		MyNode other = (MyNode) obj;
		return this.dewey.equals(other.dewey);
	}

	@Override
	public int compareTo(MyNode o) {
		if(this.dewey == null){ 
			if(o.dewey == null) return 0;
			return 1;
		}
		if(o.dewey == null) return -1;
		return this.dewey.toString()
				.compareTo(o.dewey.toString());
	}
}
