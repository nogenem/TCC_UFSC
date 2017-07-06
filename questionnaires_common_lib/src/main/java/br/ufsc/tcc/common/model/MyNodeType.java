package br.ufsc.tcc.common.model;

/**
 * Enum que possui os tipos de Nodos interessantes para esta aplicação.
 * 
 * @author Gilney N. Mathias
 */
public enum MyNodeType {
	UNKNOWN("unknown"),
	TEXT("#text"),
	IMG("img"),
	TEXT_INPUT("input[type=text]"),
	NUMBER_INPUT("input[type=number]"),
	DATE_INPUT("input[type=date]"),
	EMAIL_INPUT("input[type=email]"),
	TEL_INPUT("input[type=tel]"),
	TIME_INPUT("input[type=time]"),
	URL_INPUT("input[type=url]"),
	CHECKBOX_INPUT("input[type=checkbox]"),
	RADIO_INPUT("input[type=radio]"),
	RANGE_INPUT("input[type=range]"),
	TEXTAREA("textarea"),
	SELECT("select"),
	OPTION("option");
	
	private final String text;
	
	MyNodeType(String text){
		this.text = text;
	}
	
	public static MyNodeType get(String text, String name){
		text = text.toLowerCase();
		for(MyNodeType t : MyNodeType.values()){
			if(t.text.equals(text) || t.text.equals(name))
				return t;
		}
		return MyNodeType.UNKNOWN;
	}
}
