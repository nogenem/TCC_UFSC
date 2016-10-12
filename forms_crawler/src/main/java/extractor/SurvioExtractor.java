package extractor;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import manager.FormaDaPerguntaManager;
import model.Alternativa;
import model.FormaDaPergunta;
import model.Pergunta;
import model.Questionario;

public class SurvioExtractor implements Extractor {
	
	private ArrayList<Questionario> questionarios;
	private Questionario currentQ;
	private Pergunta currentP;
	
	public boolean shouldExtract(WebURL url){
		String href = url.getURL().toLowerCase();
		return !href.startsWith("http://www.survio.com/br/modelos-de-pesquisa") && 
				!href.endsWith("?mobile=1");
	}
	
	public ArrayList<Questionario> extract(String html) {
		questionarios = new ArrayList<>();
		currentQ = new Questionario();
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		Elements fields = doc.select("section.last fieldset");
		for(Element field : fields){
			currentP = new Pergunta();
			
			// Titulo da pergunta
			tmpTxt = this.getDescricaoPergunta(field);
			currentP.setDescricao(tmpTxt);
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
			
			// Alternativas da pergunta
			if(!this.getAlternativas(field))
				System.err.println("ALTERNATIVA DESCONHECIDA");
			else
				currentQ.addPergunta(currentP);
		}
		questionarios.add(currentQ);
		return questionarios;
	}
	
	private boolean getAlternativas(Element field) {
		return this.isTextArea(field) 		||
			this.isGenericInput(field)		||
			this.isNormalRadioInput(field) 	||
			this.isImgRadioInput(field) 	||
			this.isCheckBoxInput(field) 	||
			this.isStars(field) 			||
			this.isSelect(field) 			||
			this.isRangeInput(field)		||
			this.isMatrix(field);
	}
	
	//Ex: https://www.survio.com/modelo-de-pesquisa/feedback-sobre-servico
	private boolean isTextArea(Element field) {
		Elements tmp = field.select(".input-group-textarea textarea");
		
		if(tmp.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tTextarea.");
		return true;
	}
	
	//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados
	private boolean isGenericInput(Element field) {
		Elements input = field.select(".input-group-text input.form-control");

		if(input.isEmpty()) return false;
		
		String type = input.get(0).attr("type").toUpperCase();
		currentP.setForma(FormaDaPerguntaManager.getForma(type + "_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tInput ["+type+"].");
		return true;
	}

	//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados
	private boolean isRangeInput(Element field) {
		Elements ranges = field.select("div.row-divide div.divide-item"),
				tmpElems = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(ranges.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RANGE_INPUT_GROUP"));
		System.out.println("\t\t\t\tRanges:");
		
		for(Element range : ranges){
			tmpElems = range.select("div.divide-title");
			tmpPerg = new Pergunta();
			
			tmpPerg.setTipo("FECHADO");
			tmpPerg.setForma(FormaDaPerguntaManager.getForma("RANGE_INPUT"));
			
			tmpTxt = tmpElems.get(0).ownText().trim();
			tmpPerg.setDescricao(tmpTxt);
			System.out.print("\t\t\t\t\t>> " +tmpTxt);
			
			tmpElems = range.select("div.divide-left");
			tmpTxt = "[" +tmpElems.get(0).ownText().trim()+ ", ";
			
			tmpElems = range.select("div.divide-right");
			tmpTxt += tmpElems.get(0).ownText().trim() +"]";
			
			tmpAlt = new Alternativa(tmpTxt);
			tmpPerg.addAlternativa(tmpAlt);
			tmpPerg.setQuestionario(currentQ);
			currentP.addPergunta(tmpPerg);
			System.out.println(tmpTxt);
		}
		return true;
	}
	
	//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-popularidade-de-esportes-radicais
	private boolean isSelect(Element field) {
		Elements select = field.select("label.select select.form-control"),
				options = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(select.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\t\t\t\tSelect:");
		
		options = select.select("option");
		for(Element option : options){
			tmpTxt = option.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\tOption: " +tmpTxt);
		}
		return true;
	}
	
	//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto
	private boolean isMatrix(Element field) {
		Elements matrix = field.select("div.matrix-values"),
				tmpElems1 = null, tmpElems2 = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();
		String tmpTxt = "", tipo = "";
		FormaDaPergunta forma = null;
		
		if(matrix.isEmpty()) return false;
		
		System.out.print("\t\t\t\tMatriz");
		
		// Verifica se a matriz usa input text ou radio button
		tmpElems1 = matrix.get(0).select("div.input-group-matrix-text");
		if(!tmpElems1.isEmpty()){ 
			currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
			currentP.setTipo("ABERTO");
			tipo = "ABERTO";
			forma = FormaDaPerguntaManager.getForma("TEXT_INPUT");
			System.out.println(" [com input text]:");
		}else{
			currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
			currentP.setTipo("MULTIPLA_ESCOLHA");
			tipo = "FECHADO";
			forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
			System.out.println(" [com radio button]:");
		}
		
		tmpElems1 = matrix.get(0).select("div.input-group-matrix");
		for(Element e : tmpElems1){
			tmpElems2 = e.select("div.title-groups span.input-group-title-main");
			if(!tmpElems2.isEmpty()){//Head
				for(Element span : tmpElems2){
					tmpTxt = span.ownText().trim();
					tmpAlt = new Alternativa(tmpTxt);
					altList.add(tmpAlt);
					System.out.println("\t\t\t\t\tHead: " +tmpTxt);
				}
			}else{//Body
				tmpElems2 = e.select("div.title");
				
				tmpTxt = tmpElems2.get(0).ownText().trim(); 
				tmpPerg = new Pergunta(tmpTxt, tipo, forma);//TODO TESTAR ISSO
				for(Alternativa a : altList){
					tmpPerg.addAlternativa(a.clone());
				}
				tmpPerg.setQuestionario(currentQ);
				currentP.addPergunta(tmpPerg);
				System.out.println("\t\t\t\t\tBody: " +tmpTxt);
			}
		}
		altList.clear();
		return true;
	}
	
	//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-percepcao-da-publicidade-e-de-sua-eficiencia
	private boolean isStars(Element field) {
		Elements stars = field.select("div.special-padding-row div.original-stars input.star");
		Alternativa tmpAlt = null;
		
		if(stars.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("STARS"));
		
		tmpAlt = new Alternativa("[0, " +stars.size()+"]");
		currentP.addAlternativa(tmpAlt);
		
		System.out.println("\t\t\t\t" +stars.size()+ " Stars");
		return true;
	}
	
	//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto
	private boolean isCheckBoxInput(Element field) {
		Elements labels = field.select("div.label-cont label.input-group-checkbox"),
				tmpElems = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(labels.isEmpty()) return false; 
		
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("CHECKBOX_INPUT"));
		System.out.println("\t\t\t\tCheckbox normal:");
		
		for(Element label : labels){
			tmpElems = label.select(".input-group-title");
			tmpTxt = tmpElems.get(0).ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpElems = label.select("div.text-addon");
			if(!tmpElems.isEmpty()){
				tmpPerg = new Pergunta();
				tmpPerg.setDescricao(tmpTxt);
				
				tmpPerg.setTipo("ABERTO");
				tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
				tmpPerg.setQuestionario(currentQ);
				currentP.addPergunta(tmpPerg);
				System.out.println("\t\t\t\t\t\tCom input text");
			}else{
				tmpAlt = new Alternativa(tmpTxt);
				currentP.addAlternativa(tmpAlt);
			}
		}
		return true;
	}
	
	//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-percepcao-da-publicidade-e-de-sua-eficiencia
	private boolean isImgRadioInput(Element field) {
		Elements divs = field.select("div.images div.input-image-group"),
				tmpElems = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(divs.isEmpty()) return false; 
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tRadio com img:");
		for(Element div : divs){
			tmpElems = div.select(".input-group-radio .input-group-title");
			
			tmpTxt = tmpElems.get(0).ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpElems = div.select(".input-image img");
			if(!tmpElems.isEmpty()){
				tmpTxt = tmpElems.get(0).attr("src").replace("//", "");
				tmpAlt.setLink_img(tmpTxt);
				System.out.println(tmpTxt);
			}
			tmpElems = div.select("div.text-addon");
			if(!tmpElems.isEmpty()){
				//TODO testar isso aki
				System.err.println("TEXT-ADDON EM IMGRADIOINPUT!");
			}
		}
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados
	private boolean isNormalRadioInput(Element field) {
		Elements labels = field.select("div.label-cont label.input-group-radio"),
				tmpElems = null;
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(labels.isEmpty()) return false; 
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tRadio normal:");
		for(Element label : labels){
			tmpElems = label.select("span.input-group-title");
			tmpTxt = tmpElems.get(0).ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpElems = label.select("div.text-addon");
			if(!tmpElems.isEmpty()){
				tmpPerg = new Pergunta();
				tmpPerg.setDescricao(tmpTxt);
				
				tmpPerg.setTipo("ABERTO");
				tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
				tmpPerg.setQuestionario(currentQ);
				currentP.addPergunta(tmpPerg);
				System.out.println("\t\t\t\t\t\tCom input text");
			}else{
				tmpAlt = new Alternativa(tmpTxt);
				currentP.addAlternativa(tmpAlt);
			}
		}
		return true;
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("header.title > div.col-title > h1");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

	private String getDescricaoPergunta(Element field) {
		Elements tmp = field.select("div.title-part"),
				tmp2 = field.select("p.title");
		String desc = "";
		if(!tmp.isEmpty())
			desc = tmp.get(0).ownText().trim();
		if(!tmp2.isEmpty())
			desc += (tmp.isEmpty()?"":"\n") + 
				tmp2.get(0).ownText().trim();
		return desc;
	}
}
