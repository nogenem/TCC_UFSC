package br.ufsc.tcc.common.model;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Node;

import br.ufsc.tcc.common.util.CommonUtil;

public class MyNode implements Comparable<MyNode> {
	
	private String text;
	private MyNodeType type;
	private Attributes attrs;
	private Dewey dewey;
	
	// Construtores
	public MyNode() {
		this(null, null);
	}
	
	public MyNode(Node node, Dewey dewey) {
		if(node != null){
			this.text = CommonUtil.getNodeRepresentation(node);
			this.attrs = node.attributes();
			this.parseTextToType(node.nodeName());
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
	
	public MyNodeType getType(){
		return this.type;
	}
	
	public Attributes getAttributes(){
		return this.attrs;
	}
	
	public String getAttr(String key){
		if(this.attrs == null) return "";
		return this.attrs.get(key);
	}
	
	public Dewey getDewey() {
		return dewey;
	}
	
	// Demais métodos
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
	
	private void parseTextToType(String name) {
		switch(this.text){
		case "img":
			this.type = MyNodeType.IMG;
			break;
		case "input[type=text]":
			this.type = MyNodeType.TEXT_INPUT;
			break;
		case "input[type=number]":
			this.type = MyNodeType.NUMBER_INPUT;
			break;
		case "input[type=date]":
			this.type = MyNodeType.DATE_INPUT;
			break;
		case "input[type=email]":
			this.type = MyNodeType.EMAIL_INPUT;
			break;
		case "input[type=tel]":
			this.type = MyNodeType.TEL_INPUT;
			break;
		case "input[type=time]":
			this.type = MyNodeType.TIME_INPUT;
			break;
		case "input[type=url]":
			this.type = MyNodeType.URL_INPUT;
			break;
		case "input[type=checkbox]":
			this.type = MyNodeType.CHECKBOX_INPUT;
			break;
		case "input[type=radio]":
			this.type = MyNodeType.RADIO_INPUT;
			break;
		case "input[type=range]":
			this.type = MyNodeType.RANGE_INPUT;
			break;
		case "textarea":
			this.type = MyNodeType.TEXTAREA;
			break;
		case "select":
			this.type = MyNodeType.SELECT;
			break;
		default:
			this.type = MyNodeType.UNKNOWN;
		}
		if(name.equals("#text"))
			this.type = MyNodeType.TEXT;
	}

}
