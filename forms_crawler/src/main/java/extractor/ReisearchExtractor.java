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

public class ReisearchExtractor implements Extractor {
	
	private Questionario currentQ;
	private Pergunta currentP;
	
	@Override
	public boolean shouldExtract(WebURL url) {
		return true;
	}

	@Override
	public Questionario extract(String html) {
		currentQ = new Questionario();
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		Elements fields = doc.select("div.question");
		for(Element field : fields){
			currentP = new Pergunta();
			
			// Titulo da pergunta
			tmpTxt = this.getDescricaoPergunta(field);
			currentP.setDescricao(tmpTxt);
			System.out.println("\t\t\tTitulo Pergunta: " + tmpTxt);
			
			// Alternativas da pergunta
			if(!this.getAlternativas(field))
				System.err.println("ALTERNATIVA DESCONHECIDA");
			else
				currentQ.addPergunta(currentP);
		}
		
		return currentQ;
	}

	private boolean getAlternativas(Element field) {
		return this.isRadioInput(field) || 
				this.isCheckboxInput(field) ||
				this.isMatrix(field);
	}

	private boolean isMatrix(Element field) {
		Elements table = field.select("table.subquestions-list"),
				tmp = null;
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();
		Pergunta tmpPerg = null;
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
		String tmpTxt = "";
		
		if(table.isEmpty()) return false;
		
		System.out.println("\t\t\t\tMatriz");
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
		
		tmp = table.select("thead > tr > th");
		for(Element head : tmp){//head
			tmpTxt = head.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			altList.add(tmpAlt);
			System.out.println("\t\t\t\t\tHead: " +tmpTxt);
		}
		
		tmp = table.select("tbody > tr > th");
		for(Element body : tmp){//body
			tmpTxt = body.ownText().trim();
			tmpPerg = new Pergunta();
			tmpPerg.setDescricao(tmpTxt);
			tmpPerg.setTipo("FECHADO");
			tmpPerg.setForma(forma);
			for(Alternativa a : altList){
				tmpPerg.addAlternativa(a.clone());
			}
			tmpPerg.setQuestionario(currentQ);
			currentP.addPergunta(tmpPerg);
			System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		}
		altList.clear();
		return true;
	}

	private boolean isCheckboxInput(Element field) {
		Elements ul = field.select("ul.checkbox-list"),
				labels = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(ul.isEmpty()) return false;
		
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("CHECKBOX_INPUT"));
		System.out.println("\t\t\t\tCheckbox normal:");
		
		labels = ul.select("li.checkbox-item > label.answertext");
		for(Element lbl : labels){
			tmpTxt = lbl.ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
		}
		return true;
	}

	private boolean isRadioInput(Element field) {
		Elements ul = field.select("ul.radio-list"),
				labels = null;;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(ul.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tRadio normal:");
		labels = ul.select("li.radio-item > label.answertext");
		for(Element lbl : labels){
			tmpTxt = lbl.ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
		}
		return true;
	}

	private String getDescricaoPergunta(Element field) {
		Elements tmp = field.select("h3.panel-title > span.question-text"),
				tmp2 = field.select("div.panel-body div.question-help");
		String desc = "";
		if(!tmp.isEmpty())
			desc = tmp.get(0).ownText().trim();
		if(!tmp2.isEmpty())
			desc += (tmp.isEmpty()?"":"\n") + 
				tmp2.get(0).ownText().trim();
		return desc;
	}

	private String getAssuntoQuestionario(Document doc) {
		return "Dê a sua opnião...";
	}

}
