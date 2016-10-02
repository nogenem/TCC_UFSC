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

public class GoogleExtractor implements Extractor {
	
	private Questionario currentQ;
	private Pergunta currentP;
	
	@Override
	public boolean shouldExtract(WebURL url) {
		String href = url.getURL().toLowerCase();
		return href.endsWith("/viewform");
	}

	@Override
	public Questionario extract(String html) {
		currentQ = new Questionario();
		
		//TODO fazer parte dos grupos
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		// div[role=listitem] ou div.ss-form-question
		Elements fields = doc.select("div.ss-form form ol > div.ss-form-question");
		for(Element field : fields){
			currentP = new Pergunta();
			
			// Titulo da pergunta
			tmpTxt = this.getTituloPergunta(field);
			currentP.setDescricao(tmpTxt);
			System.out.println("\t\t\tTitulo Pergunta: " + tmpTxt);
			
			// Alternativas da pergunta
			if(!this.getAlternativas(field))
				System.err.println("ALTERNATIVA DESCONHECIDA");
			else
				currentQ.addPergunta(currentP);
		}
		return null;//TODO retornar o questionario
	}

	private boolean getAlternativas(Element field) {
		return this.isTextarea(field)	||
			this.isInputText(field) 	||
			this.isRadioInput(field)	||
			this.isSelect(field)		||
			this.isMatrix(field);
	}

	private boolean isMatrix(Element field) {
		Elements tmp = field.select("div.ss-grid table"),
				tmp2 = null;
		
		if(tmp.isEmpty()) return false;
		
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		ArrayList<Alternativa> altList = new ArrayList<>();
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");;
		
		System.out.print("\t\t\t\tMatriz");
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
		
		tmp2 = tmp.select("thead tr td > label");
		for(Element lbl : tmp2){//head
			tmpTxt = lbl.ownText().trim();
			tmpAlt = new Alternativa();
			tmpAlt.setDescricao(tmpTxt);
			altList.add(tmpAlt);
			System.out.println("\t\t\t\t\tHead: " +tmpTxt);
		}
		
		tmp2 = tmp.select("tbody tr td.ss-gridrow-leftlabel");
		for(Element td : tmp2){//body
			tmpTxt = td.ownText().trim();
			tmpPerg = new Pergunta();
			tmpPerg.setDescricao(tmpTxt);
			tmpPerg.setTipo("FECHADO");
			tmpPerg.setForma(forma);
			for(Alternativa a : altList){
				tmpPerg.addAlternativa(a);
			}
			tmpPerg.setQuestionario(currentQ);
			currentP.addPergunta(tmpPerg);
			System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		}
		altList.clear();
		return true;
	}

	private boolean isSelect(Element field) {
		Elements options = field.select("div.ss-select select > option");
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(options.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\t\t\t\tSelect:");
		
		for(Element opt : options){
			tmpTxt = opt.ownText().trim();
			if(!tmpTxt.equals("")){
				tmpAlt = new Alternativa();
				tmpAlt.setDescricao(tmpTxt);
				currentP.addAlternativa(tmpAlt);
				System.out.println("\t\t\t\t\tOption: " +tmpTxt);
			}
		}
		return true;
	}

	private boolean isRadioInput(Element field) {
		Elements items = field.select("div.ss-radio ul.ss-choices li.ss-choise-item"),
				tmp = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(items.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tRadio normal:");
		for(Element item : items){
			tmp = item.select("label > .ss-choice-label");
			tmpTxt = tmp.get(0).ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpAlt = new Alternativa();
			tmpAlt.setDescricao(tmpTxt);
			currentP.addAlternativa(tmpAlt);
		}
		return true;
	}

	private boolean isTextarea(Element field) {
		Elements tmp = field.select("div.ss-paragraph-text textarea.ss-q-long");
		
		if(tmp.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tTextarea.");
		return true;
	}

	private boolean isInputText(Element field) {
		Elements tmp = field.select("div.ss-text input.ss-q-short[type=text]");
		
		if(tmp.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tInput [text].");
		return true;
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select(".ss-top-of-page .ss-form-title");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}
	
	private String getTituloPergunta(Element field) {
		Elements tmp = field.select(".ss-q-item-label .ss-q-title");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
