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
		String tmpTxt = "";
		
		JSONArray arrQuestionarios = configs.getJSONArray("questionarios");
		for(int i = 0; i<arrQuestionarios.length(); i++){
			configQ = arrQuestionarios.getJSONObject(i);
			currentQ = new Questionario();
			
			// Assunto questionario
			tmpTxt = this.getAssuntoQuestionario(doc);
			currentQ.setAssunto(tmpTxt);
			System.out.println("\t\tAssunto Questionario: " + tmpTxt);
			
			configP = configQ.getJSONObject("perguntas");
			
			Elements fields = doc.select(configP.getString("seletor"));
			for(Element field : fields){
				currentP = new Pergunta();
				
				// Descricao pergunta
				tmpTxt = this.getDescricaoPergunta(field);
				currentP.setDescricao(tmpTxt);
				System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
				
				// Alternativas
				configA = configP.getJSONObject("alternativas");
				if(!this.getAlternativas(field))
					System.out.println("BasicExtractor:extract()> Alternativa desconhecida");
				else
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
				this.isImgRadioInput(field) ||
				this.isRadioInput(field) ||
				this.isSelect(field) ||
				this.isStars(field) || 
				this.isTextArea(field) || 
				this.isTextInput(field) ||
				this.isDateInput(field) ||
				this.isNumberInput(field);
	}
	
	private boolean isTextArea(Element field) {
		JSONObject taObj = configA.optJSONObject("textarea");
		if(taObj == null) return false;
		
		Elements elems = field.select(taObj.getString("seletor"));
		if(elems.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tTextarea.");
		return true;
	}
	
	private boolean isTextInput(Element field) {
		JSONObject tiObj = configA.optJSONObject("text_input");
		if(tiObj == null) return false;
		
		Elements elems = field.select(tiObj.getString("seletor"));
		if(elems.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tText Input.");
		return true;
	}
	
	private boolean isDateInput(Element field) {
		JSONObject diObj = configA.optJSONObject("date_input");
		if(diObj == null) return false;
		
		Elements elems = field.select(diObj.getString("seletor"));
		if(elems.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("DATE_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tDate Input.");
		return true;
	}
	
	private boolean isNumberInput(Element field) {
		JSONObject niObj = configA.optJSONObject("number_input");
		if(niObj == null) return false;
		
		Elements elems = field.select(niObj.getString("seletor"));
		if(elems.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("NUMBER_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tNumber Input.");
		return true;
	}
	
	private boolean isRangeInputGroup(Element field){
		JSONObject rigObj = configA.optJSONObject("range_input_group");
		if(rigObj == null) return false;
		
		Elements elems = field.select(rigObj.getString("seletor")),
				tmpElems1 = null;
		if(elems.isEmpty()) return false;
		
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RANGE_INPUT_GROUP"));
		System.out.println("\t\t\t\tRange Input Group:");
		
		rigObj = rigObj.getJSONObject("range_input");
		for(Element elem : elems){
			tmpElems1 = elem.select(rigObj.getString("seletor"));
			tmpPerg = new Pergunta();
			
			tmpPerg.setTipo("FECHADO");
			tmpPerg.setForma(FormaDaPerguntaManager.getForma("RANGE_INPUT"));
			
			tmpTxt = Util.trim(tmpElems1.get(0).ownText());
			tmpPerg.setDescricao(tmpTxt);
			System.out.print("\t\t\t\t\tRange Input: " +tmpTxt);
			
			tmpElems1 = elem.select(rigObj.getString("valor_min"));
			tmpTxt = "[" +Util.trim(tmpElems1.get(0).ownText())+ ", ";
			
			tmpElems1 = elem.select(rigObj.getString("valor_max"));
			tmpTxt += Util.trim(tmpElems1.get(0).ownText()) +"]";
			
			tmpAlt = new Alternativa(tmpTxt);
			tmpPerg.addAlternativa(tmpAlt);
			currentP.addFilha(tmpPerg);
			System.out.println(tmpTxt);
		}
		return true;
	}
	
	private boolean isSelect(Element field) {
		JSONObject sObj = configA.optJSONObject("select");
		if(sObj == null) return false;
		
		Elements elems = field.select(sObj.getString("seletor")),
				tmpElems1 = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(elems.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\t\t\t\tSelect:");
		
		tmpElems1 = elems.select(sObj.getString("opcoes"));
		for(Element tmpElem1 : tmpElems1){
			tmpTxt = Util.trim(tmpElem1.ownText());
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\tOption: " +tmpTxt);
		}
		return true;
	}
	
	private boolean isStars(Element field) {
		JSONObject sObj = configA.optJSONObject("stars");
		if(sObj == null) return false;
		
		Elements elems = field.select(sObj.getString("seletor"));
		Alternativa tmpAlt = null;
		
		if(elems.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("STARS"));
		
		tmpAlt = new Alternativa("[0, " +elems.size()+"]");
		currentP.addAlternativa(tmpAlt);
		
		System.out.println("\t\t\t\t" +elems.size()+ " Stars");
		return true;
	}
	
	private boolean isCheckboxInput(Element field) {
		JSONObject ciObj = configA.optJSONObject("checkbox_input");
		if(ciObj == null) return false;
		JSONObject tiObj = ciObj.optJSONObject("text_input");
		
		Elements elems = field.select(ciObj.getString("seletor")),
				tmpElems1 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(elems.isEmpty()) return false; 
		
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("CHECKBOX_INPUT"));
		System.out.println("\t\t\t\tCheckbox Input:");
		
		for(Element elem : elems){
			tmpElems1 = elem.select(ciObj.getString("texto"));
			tmpTxt = Util.trim(tmpElems1.get(0).ownText());
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpElems1 = tiObj == null? null: elem.select(tiObj.getString("seletor"));
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

	private boolean isRadioInput(Element field) {
		JSONObject riObj = configA.optJSONObject("radio_input");
		if(riObj == null) return false;
		JSONObject tiObj = riObj.optJSONObject("text_input");
		
		Elements elems = field.select(riObj.getString("seletor")),
				tmpElems1 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(elems.isEmpty()) return false; 
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tRadio Input:");
		for(Element elem : elems){
			tmpElems1 = elem.select(riObj.getString("texto"));
			tmpTxt = Util.trim(tmpElems1.get(0).ownText());
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpElems1 = tiObj == null? null: elem.select(tiObj.getString("seletor"));
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
	
	private boolean isImgRadioInput(Element field) {
		JSONObject iriObj = configA.optJSONObject("image_radio_input");
		if(iriObj == null) return false;
		
		Elements elems = field.select(iriObj.getString("seletor")),
				tmpElems1 = null;
		Alternativa tmpAlt = null;
		Figura tmpFig = null;
		String tmpTxt = "";
		
		if(elems.isEmpty()) return false; 
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tImage Radio Input:");
		for(Element elem : elems){
			tmpElems1 = elem.select(iriObj.getString("texto"));
			
			tmpTxt = Util.trim(tmpElems1.get(0).ownText());
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpElems1 = elem.select(iriObj.getString("imagem"));
			if(!tmpElems1.isEmpty()){
				tmpFig = new Figura();
				System.out.println("\t\t\t\t\tImagem:");
				
				tmpTxt = tmpElems1.get(0).attr("src").replace("//", "");
				tmpFig.setImage_url(tmpTxt);
				System.out.println("\t\t\t\t\t\tSRC: " +tmpTxt);
				
				tmpTxt = Util.trim(tmpElems1.get(0).attr("alt"));
				tmpFig.setLegenda(tmpTxt);
				System.out.println("\t\t\t\t\t\tALT: " +tmpTxt);
				
				tmpFig.setDono(tmpAlt);
			}
		}
		return true;
	}
	
	private boolean isTextInputMatrix(Element field) {
		JSONObject timObj = configA.optJSONObject("text_input_matrix");
		if(timObj == null) return false;
		JSONObject tmpObj = null;
		
		Elements elems = field.select(timObj.getString("teste")),
				tmpElems1 = null;
		
		if(elems.isEmpty()) return false;
		
		elems = field.select(timObj.getString("seletor"));
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();
		String tmpTxt = "", tmpTipo = "";
		FormaDaPergunta tmpForma = null;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
		currentP.setTipo("ABERTO");
		tmpTipo = "ABERTO";
		tmpForma = FormaDaPerguntaManager.getForma("TEXT_INPUT");
		
		System.out.println("\t\t\t\tText Input Matriz:");
		tmpObj = timObj.getJSONObject("thead");
		tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
		for(Element tmpElem1 : tmpElems1){
			tmpTxt = Util.trim(tmpElem1.ownText());
			tmpAlt = new Alternativa(tmpTxt);
			altList.add(tmpAlt);
			System.out.println("\t\t\t\t\tHead: " +tmpTxt);
		}
		
		tmpObj = timObj.getJSONObject("tbody");
		tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
		for(Element tmpElem1 : tmpElems1){
			tmpTxt = Util.trim(tmpElem1.ownText()); 
			tmpPerg = new Pergunta(tmpTxt, tmpTipo, tmpForma);
			for(Alternativa a : altList){
				tmpPerg.addAlternativa(a.clone());
			}
			currentP.addFilha(tmpPerg);
			System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		}
		altList.clear();
		return true;
	}
	
	private boolean isRadioInputMatrix(Element field) {
		JSONObject rimObj = configA.optJSONObject("radio_input_matrix");
		if(rimObj == null) return false;
		JSONObject tmpObj = null;
		
		Elements elems = field.select(rimObj.getString("teste")),
				tmpElems1 = null;
		
		if(elems.isEmpty()) return false;
		
		elems = field.select(rimObj.getString("seletor"));
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();
		String tmpTxt = "", tmpTipo = "";
		FormaDaPergunta tmpForma = null;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
		currentP.setTipo("MULTIPLA_ESCOLHA");
		tmpTipo = "FECHADO";
		tmpForma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
		
		System.out.println("\t\t\t\tRadio Input Matriz:");
		tmpObj = rimObj.getJSONObject("thead");
		tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
		for(Element tmpElem1 : tmpElems1){
			tmpTxt = Util.trim(tmpElem1.ownText());
			tmpAlt = new Alternativa(tmpTxt);
			altList.add(tmpAlt);
			System.out.println("\t\t\t\t\tHead: " +tmpTxt);
		}
		
		tmpObj = rimObj.getJSONObject("tbody");
		tmpElems1 = elems.get(0).select(tmpObj.getString("seletor"));
		for(Element tmpElem1 : tmpElems1){
			tmpTxt = Util.trim(tmpElem1.ownText()); 
			tmpPerg = new Pergunta(tmpTxt, tmpTipo, tmpForma);
			for(Alternativa a : altList){
				tmpPerg.addAlternativa(a.clone());
			}
			currentP.addFilha(tmpPerg);
			System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		}
		altList.clear();
		return true;
	}
	
	private String getDescricaoPergunta(Element field) {
		Elements tmpElems = field.select(configP.getString("descricao")),
				// subdescricao é opcional
				tmpElems2 = field.select(configP.optString("subdescricao", ""));//TODO oq acontece quando n tem subdescricao
		String desc = "";
		if(!tmpElems.isEmpty())
			desc = Util.trim(tmpElems.get(0).ownText());
		if(!tmpElems2.isEmpty())
			desc += (tmpElems.isEmpty()?"":"\n") + 
				Util.trim(tmpElems2.get(0).ownText());
		return desc;
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select(configQ.getString("assunto"));
		if(tmp.isEmpty()) return "";
		return Util.trim(tmp.get(0).ownText());
	}

}
