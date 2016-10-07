package extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import manager.FormaDaPerguntaManager;
import model.Alternativa;
import model.Pergunta;
import model.Questionario;

public class VarkLearnExtractor implements Extractor {
	
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
		
		Elements fields = doc.select("#questionnaireForm div.VARKQuestion");
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
		return this.isCheckboxInput(field);
	}

	private boolean isCheckboxInput(Element field) {
		Elements labels = field.select("div.VARKResponse > label");
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(labels.isEmpty()) return false;
		
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("CHECKBOX_INPUT"));
		System.out.println("\t\t\t\tCheckbox normal:");
		
		for(Element lbl : labels){
			tmpTxt = lbl.ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
		}
		return true;
	}

	private String getDescricaoPergunta(Element field) {
		Elements tmp = field.select("div.VARKQuestionText");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

	private String getAssuntoQuestionario(Document doc) {
		//header.entry-header > h1.entry-title
		Elements tmp = doc.select("div.entry-content > h2:first-of-type");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
