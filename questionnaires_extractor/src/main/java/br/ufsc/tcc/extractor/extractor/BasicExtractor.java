package br.ufsc.tcc.extractor.extractor;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.ufsc.tcc.common.util.Util;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;
import edu.uci.ics.crawler4j.url.WebURL;

public abstract class BasicExtractor implements IExtractor {
	
	/**
	 * Objeto que guarda as configurações gerais do extrator.
	 */
	protected JSONObject configs;
	/**
	 * Lista de questionários encontrados pelo extrator.
	 */
	private ArrayList<Questionario> questionarios;
	
	/**
	 * Questionário atual que o extrator esta trabalhando.
	 */
	private Questionario currentQ;
	/**
	 * Objeto que guarda as configurações do questionário atual
	 * que o extrator esta trabalhando.
	 */
	private JSONObject configQ;
	
	/**
	 * Pergunta atual que o extrator esta trabalhando.
	 */
	private Pergunta currentP;
	/**
	 * Objeto que guarda as configurações das perguntas do 
	 * questionário atual que o extrator esta trabalhando.
	 */
	private JSONObject configP;
	
	/**
	 * Objeto que guarda as configurações das alternativas do 
	 * questionário atual que o extrator esta trabalhando.
	 */
	private JSONObject configA;
	
	// Construtor
	public BasicExtractor(){
	}
	
	@Override
	public boolean shouldExtract(WebURL url) {
		return true;
	}
	
	/**
	 * Tenta extrair os questionários do html passado.
	 * 
	 * @param html		HTML que, provavelmente, possui um ou mais questionários.
	 * @return 			Lista de objetos Questionario que possuem os dados dos questionários
	 * 					encontrados no HTML passado.
	 */
	public ArrayList<Questionario> extract(String html) {
		if(configs == null || !configs.has("questionarios")){
			System.err.println("\nBasicExtractor:extract()> Nao foi possivel encontrar as configuracoes necessarias "
					+ "para fazer a extracao!");
			System.exit(-1);
		}
		
		questionarios = new ArrayList<>();
		Document doc = Jsoup.parse(html);
		Elements questionario = null;
		String tmpTxt = "";
		Figura tmpFig = null;
		
		//TODO adicionar verificação de IMAGE em todos os tipos de alternativas
		//TODO adicionar verificação de teste em todos os tipos de alternativas
		
		JSONArray arrQuestionarios = configs.getJSONArray("questionarios");
		for(int i = 0; i<arrQuestionarios.length(); i++){
			configQ = arrQuestionarios.getJSONObject(i);
			currentQ = new Questionario();
			
			// Pega o seletor do questionario, se tiver um
			tmpTxt = configQ.optString("seletor", "");
			if(tmpTxt.equals(""))
				questionario = doc.getElementsByTag("body");
			else
				questionario = doc.select(tmpTxt);
			
			// Assunto questionario
			tmpTxt = this.getAssuntoQuestionario(questionario);
			currentQ.setAssunto(tmpTxt);
			System.out.println("\t\tAssunto Questionario: " + tmpTxt);
			
			configP = configQ.getJSONObject("perguntas");
			
			Elements fields = questionario.select(configP.getString("seletor"));
			for(Element field : fields){
				currentP = new Pergunta();
				
				// Descricao pergunta
				tmpTxt = this.getDescricaoPergunta(field);
				currentP.setDescricao(tmpTxt);
				System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
				
				// Figura
				tmpFig = getImage(field, configP);
				if(tmpFig != null)
					tmpFig.setDono(currentP);
				
				// Alternativas
				configA = configP.getJSONObject("alternativas");
				if(!this.getAlternativas(field)){
					currentP.setForma(FormaDaPerguntaManager.getForma("UNKNOWN"));
					System.out.println("BasicExtractor:extract()> Alternativa desconhecida");
				}
				currentQ.addPergunta(currentP);
			}
			questionarios.add(currentQ);
		}
		
		return questionarios;
	}

	private boolean getAlternativas(Element field) {
		return this.isTextInputMatrix(field) ||
				this.isRadioInputMatrix(field) ||
				this.isCheckboxInput(field) ||
				this.isRangeInputGroup(field) ||
				this.isRadioInput(field) ||
				this.isSelect(field) ||
				this.isRating(field) || 
				this.isTextArea(field) || 
				this.isTextInput(field) ||
				this.isDateInput(field) ||
				this.isNumberInput(field);
	}
	
	private boolean isTextArea(Element field) {
		JSONArray taArr = configA.optJSONArray("textarea");
		if(taArr == null) return false;
		
		JSONObject taObj = null;
		Elements elems = null;
		
		for(int i = 0; i < taArr.length(); i++){
			taObj = taArr.getJSONObject(i);
			
			elems = field.select(taObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
				currentP.setTipo("ABERTO");
				System.out.println("\t\t\t\tTextarea.");
				return true;
			}
		}
		return false;
	}
	
	private boolean isTextInput(Element field) {
		JSONArray tiArr = configA.optJSONArray("text_input");
		if(tiArr == null) return false;
		
		JSONObject tiObj = null;
		Elements elems = null;
		
		for(int i = 0; i < tiArr.length(); i++){
			tiObj = tiArr.getJSONObject(i);
			
			elems = field.select(tiObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));	
				currentP.setTipo("ABERTO");
				System.out.println("\t\t\t\tText Input.");
				return true;
			}
		}
		return false;
	}
	
	private boolean isDateInput(Element field) {
		JSONArray diArr = configA.optJSONArray("date_input");
		if(diArr == null) return false;
		
		JSONObject diObj = null;
		Elements elems = null;
		
		for(int i = 0; i < diArr.length(); i++){
			diObj = diArr.getJSONObject(i);
			
			elems = field.select(diObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setForma(FormaDaPerguntaManager.getForma("DATE_INPUT"));	
				currentP.setTipo("ABERTO");
				System.out.println("\t\t\t\tDate Input.");
				return true;
			}
		}
		return false;
	}
	
	private boolean isNumberInput(Element field) {
		JSONArray niArr = configA.optJSONArray("number_input");
		if(niArr == null) return false;
		
		JSONObject niObj = null;
		Elements elems = null;
		
		for(int i = 0; i < niArr.length(); i++){
			niObj = niArr.getJSONObject(i);
			
			elems = field.select(niObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setForma(FormaDaPerguntaManager.getForma("NUMBER_INPUT"));	
				currentP.setTipo("ABERTO");
				System.out.println("\t\t\t\tNumber Input.");
				return true;
			}
		}
		return false;
	}
	
	private boolean isRangeInputGroup(Element field){
		JSONArray riArr = configA.optJSONArray("range_input_group");
		if(riArr == null) return false;
		
		JSONObject riObj = null,
				descObj = null;
		Elements elems = null,
				tmpElems1 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		for(int i = 0; i < riArr.length(); i++){
			riObj = riArr.getJSONObject(i);
			
			elems = field.select(riObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setTipo("FECHADO");
				currentP.setForma(FormaDaPerguntaManager.getForma("RANGE_INPUT_GROUP"));
				System.out.println("\t\t\t\tRange Input Group:");
				
				riObj = riObj.getJSONObject("range_input");
				descObj = riObj.getJSONObject("descricao");
				elems = elems.select(riObj.getString("seletor"));
				for(Element elem : elems){
					tmpElems1 = elem.select(descObj.getString("seletor"));
					tmpPerg = new Pergunta();
					
					tmpPerg.setTipo("FECHADO");
					tmpPerg.setForma(FormaDaPerguntaManager.getForma("RANGE_INPUT"));
					
					tmpTxt = getText(tmpElems1, descObj);
					tmpPerg.setDescricao(tmpTxt);
					System.out.print("\t\t\t\t\tRange Input: " +tmpTxt);
					
					tmpElems1 = elem.select(riObj.getString("valor_min"));
					tmpTxt = "[" +Util.trim(tmpElems1.get(0).ownText())+ ", ";
					
					tmpElems1 = elem.select(riObj.getString("valor_max"));
					tmpTxt += Util.trim(tmpElems1.get(0).ownText()) +"]";
					
					tmpAlt = new Alternativa(tmpTxt);
					tmpPerg.addAlternativa(tmpAlt);
					currentP.addFilha(tmpPerg);
					System.out.println(tmpTxt);
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean isSelect(Element field) {
		JSONArray sArr = configA.optJSONArray("select");
		if(sArr == null) return false;
		
		JSONObject sObj = null,
				descObj = null;
		Elements elems = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		for(int i = 0; i < sArr.length(); i++){
			sObj = sArr.getJSONObject(i);
			
			elems = field.select(sObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setTipo("FECHADO");
				currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
				System.out.println("\t\t\t\tSelect:");
				
				descObj = sObj.getJSONObject("descricao");
				for(Element tmpElem1 : elems){
					tmpTxt = getText(tmpElem1, descObj);
					tmpAlt = new Alternativa(tmpTxt);
					currentP.addAlternativa(tmpAlt);
					System.out.println("\t\t\t\t\tOption: " +tmpTxt);
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean isRating(Element field) {
		JSONArray sArr = configA.optJSONArray("rating");
		if(sArr == null) return false;
		
		JSONObject sObj = null,
				descObj = null;
		Elements elems = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		for(int i = 0; i < sArr.length(); i++){
			sObj = sArr.getJSONObject(i);
			
			elems = field.select(sObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setTipo("FECHADO");
				currentP.setForma(FormaDaPerguntaManager.getForma("RATING"));
				System.out.println("\t\t\t\tRating:");
				
				descObj = sObj.getJSONObject("descricao");
				for(Element tmpElem1 : elems){
					tmpTxt = getText(tmpElem1, descObj);
					tmpAlt = new Alternativa(tmpTxt);
					currentP.addAlternativa(tmpAlt);
					System.out.println("\t\t\t\t\tOption: " +tmpTxt);
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean isCheckboxInput(Element field) {
		JSONArray ciArr = configA.optJSONArray("checkbox_input");
		if(ciArr == null) return false;
		
		JSONObject ciObj = null,
				descObj = null,
				tiObj = null;
		Elements elems = null,
				tmpElems1 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		for(int i = 0; i < ciArr.length(); i++){
			ciObj = ciArr.getJSONObject(i);
			tiObj = ciObj.optJSONObject("text_input");
			
			elems = field.select(ciObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setTipo("MULTIPLA_ESCOLHA");
				currentP.setForma(FormaDaPerguntaManager.getForma("CHECKBOX_INPUT"));
				System.out.println("\t\t\t\tCheckbox Input:");
				
				descObj = ciObj.getJSONObject("descricao");
				for(Element tmpElem1 : elems){
					tmpTxt = getText(tmpElem1, descObj);
					System.out.println("\t\t\t\t\t" +tmpTxt);
					
					tmpElems1 = tiObj == null? null: tmpElem1.select(tiObj.getString("seletor"));
					if(tmpElems1 != null && !tmpElems1.isEmpty()){
						tmpPerg = new Pergunta();
						tmpPerg.setDescricao(tmpTxt);
						
						tmpPerg.setTipo("ABERTO");
						tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
						currentP.addFilha(tmpPerg);
						System.out.println("\t\t\t\t\t\tCom input text");
					}else{
						tmpAlt = new Alternativa(tmpTxt);
						currentP.addAlternativa(tmpAlt);
					}
				}
				return true;
			}
		}
		return false;
	}

	private boolean isRadioInput(Element field) {
		JSONArray riArr = configA.optJSONArray("radio_input");
		if(riArr == null) return false;
		
		JSONObject riObj = null,
				descObj = null,
				tiObj = null;
		Elements elems = null,
				tmpElems1 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		Figura tmpFig = null;
		String tmpTxt = "";
		
		for(int i = 0; i < riArr.length(); i++){
			riObj = riArr.getJSONObject(i);
			tiObj = riObj.optJSONObject("text_input");
			
			elems = field.select(riObj.getString("seletor"));
			if(!elems.isEmpty()){
				currentP.setTipo("FECHADO");
				currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
				System.out.println("\t\t\t\tRadio Input:");
				
				descObj = riObj.getJSONObject("descricao");
				for(Element tmpElem1 : elems){
					tmpTxt = getText(tmpElem1, descObj);
					System.out.println("\t\t\t\t\t" +tmpTxt);
					
					tmpElems1 = tiObj == null? null: tmpElem1.select(tiObj.getString("seletor"));
					if(tmpElems1 != null && !tmpElems1.isEmpty()){
						tmpPerg = new Pergunta();
						tmpPerg.setDescricao(tmpTxt);
						
						tmpPerg.setTipo("ABERTO");
						tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
						currentP.addFilha(tmpPerg);
						System.out.println("\t\t\t\t\t\tCom input text");
						
						tmpFig = getImage(tmpElem1, riObj);
						if(tmpFig != null)
							tmpFig.setDono(tmpPerg);
					}else{
						tmpAlt = new Alternativa(tmpTxt);
						currentP.addAlternativa(tmpAlt);
						
						tmpFig = getImage(tmpElem1, riObj);
						if(tmpFig != null)
							tmpFig.setDono(tmpAlt);
					}
				}
				return true;
			}
		}
		return false;
	}
	
	private boolean isTextInputMatrix(Element field) {
		JSONArray timArr = configA.optJSONArray("text_input_matrix");
		if(timArr == null) return false;
		
		JSONObject timObj = null,
				descObj = null,
				tmpObj = null;
		Elements elems = null,
				tmpElems1 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();
		String tmpTxt = "";
		FormaDaPergunta tmpForma = FormaDaPerguntaManager.getForma("TEXT_INPUT");
		
		for(int i = 0; i < timArr.length(); i++){
			timObj = timArr.getJSONObject(i);
			
			elems = field.select(timObj.getString("teste"));
			if(!elems.isEmpty()){
				elems = field.select(timObj.getString("seletor"));
				
				currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
				currentP.setTipo("ABERTO");
				System.out.println("\t\t\t\tText Input Matriz:");
				
				tmpObj = timObj.getJSONObject("thead");
				descObj = tmpObj.getJSONObject("descricao");
				tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
				for(Element tmpElem1 : tmpElems1){
					tmpTxt = getText(tmpElem1, descObj);
					tmpAlt = new Alternativa(tmpTxt);
					altList.add(tmpAlt);
					System.out.println("\t\t\t\t\tHead: " +tmpTxt);
				}
				
				tmpObj = timObj.getJSONObject("tbody");
				descObj = tmpObj.getJSONObject("descricao");
				tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
				for(Element tmpElem1 : tmpElems1){
					tmpTxt = getText(tmpElem1, descObj); 
					tmpPerg = new Pergunta(tmpTxt, "ABERTO", tmpForma);
					for(Alternativa a : altList){
						tmpPerg.addAlternativa(a.clone());
					}
					currentP.addFilha(tmpPerg);
					System.out.println("\t\t\t\t\tBody: " +tmpTxt);
				}
				altList.clear();
				return true;
			}
		}
		return false;
	}
	
	private boolean isRadioInputMatrix(Element field) {
		JSONArray rimArr = configA.optJSONArray("radio_input_matrix");
		if(rimArr == null) return false;
		
		JSONObject rimObj = null,
				descObj = null,
				tmpObj = null;
		Elements elems = null,
				tmpElems1 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();
		String tmpTxt = "";
		FormaDaPergunta tmpForma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
		
		for(int i = 0; i < rimArr.length(); i++){
			rimObj = rimArr.getJSONObject(i);
			
			elems = field.select(rimObj.getString("teste"));
			if(!elems.isEmpty()){
				elems = field.select(rimObj.getString("seletor"));
				
				currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
				currentP.setTipo("MULTIPLA_ESCOLHA");
				System.out.println("\t\t\t\tRadio Input Matriz:");
				
				tmpObj = rimObj.getJSONObject("thead");
				descObj = tmpObj.getJSONObject("descricao");
				tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
				for(Element tmpElem1 : tmpElems1){
					tmpTxt = getText(tmpElem1, descObj);
					tmpAlt = new Alternativa(tmpTxt);
					altList.add(tmpAlt);
					System.out.println("\t\t\t\t\tHead: " +tmpTxt);
				}
				
				tmpObj = rimObj.getJSONObject("tbody");
				descObj = tmpObj.getJSONObject("descricao");
				tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
				for(Element tmpElem1 : tmpElems1){
					tmpTxt = getText(tmpElem1, descObj); 
					tmpPerg = new Pergunta(tmpTxt, "FECHADO", tmpForma);
					for(Alternativa a : altList){
						tmpPerg.addAlternativa(a.clone());
					}
					currentP.addFilha(tmpPerg);
					System.out.println("\t\t\t\t\tBody: " +tmpTxt);
				}
				altList.clear();
				return true;
			}
		}
		return false;
	}
	
	private String getDescricaoPergunta(Element field) {
		JSONObject descObj = configP.optJSONObject("descricao"),
				subDescObj = configP.optJSONObject("subdescricao");
		Elements tmpElems = field.select(descObj.getString("seletor"));
		String desc = "";
		
		if(!tmpElems.isEmpty()){
			desc = getText(tmpElems, descObj);
			if(subDescObj != null){
				tmpElems = field.select(subDescObj.getString("seletor"));
				if(!tmpElems.isEmpty())
					desc += "\n" + getText(tmpElems, subDescObj);
			}
		}
		return desc;
	}

	private String getAssuntoQuestionario(Elements field) {
		Elements tmp = field.select(configQ.getString("assunto"));
		if(tmp.isEmpty()) return "";
		return Util.trim(tmp.get(0).ownText());
	}
	
	final private Figura getImage(Element elem, JSONObject configObj){
		return getImage(new Elements(elem), configObj);
	}
	
	final private Figura getImage(Elements elems, JSONObject configObj){
		Elements tmpElems1 = optSelect(elems, configObj.optString("imagem", ""), null);
		Figura tmpFig = null;
		String tmpTxt = "";
		if(tmpElems1 != null && !tmpElems1.isEmpty()){
			tmpFig = new Figura();
			System.out.println("\t\t\t\t\tImagem:");
			
			tmpTxt = tmpElems1.get(0).attr("src")
					.replaceAll("^((http|https)://)", "").replace("//", "");
			tmpFig.setImage_url(tmpTxt);
			System.out.println("\t\t\t\t\t\tSRC: " +tmpTxt);
			
			tmpTxt = Util.trim(tmpElems1.get(0).attr("alt"));
			tmpFig.setLegenda(tmpTxt);
			System.out.println("\t\t\t\t\t\tALT: " +tmpTxt);
			
			currentQ.addFigura(tmpFig);
		}
		return tmpFig;
	}
	
	//TODO jogar estas funções para a classe Util?
	private String getText(Element elem, JSONObject descObj){
		return getText(new Elements(elem), descObj);
	}
	
	private String getText(Elements elems, JSONObject descObj){
		String text = "", metodo = "";
		Elements tmpElems = optSelect(elems, 
				descObj.optString("seletor", ""), elems);
		metodo = descObj.optString("metodo", "").toLowerCase();
		
		if(metodo.equals("owntext") || metodo.equals("")){
			text = tmpElems.get(0).ownText();
		}else if(metodo.equals("text")){
			text = tmpElems.text();
		}else if(metodo.equals("value")){
			text = tmpElems.attr("value");
		}else if(metodo.equals("textnodes")){
			//TODO fazer isso...
		}
		
		return Util.trim(text);
	}
	
	final private Elements optSelect(Element elem, String selector, Elements defaultValue){
		return optSelect(new Elements(elem), selector, defaultValue);
	}
	
	final private Elements optSelect(Elements elems, String selector, Elements defaultValue){
		Elements tmpElems = null;
		if(selector.equals(""))
			tmpElems = defaultValue;
		else{
			tmpElems = elems.select(selector);
			if(tmpElems.isEmpty())
				tmpElems = defaultValue;
		}
		return tmpElems;
	}
	
}
