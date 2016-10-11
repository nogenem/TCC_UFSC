package extractor;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import model.Pergunta;
import model.Questionario;

public class InstitutoVerWebExtractor implements Extractor {
	
	private ArrayList<Questionario> questionarios;
	private Questionario currentQ;
	private Pergunta currentP;
	
	@Override
	public boolean shouldExtract(WebURL url) {
		return true;
	}

	@Override
	public ArrayList<Questionario> extract(String html) {
		questionarios = new ArrayList<>();
		currentQ = new Questionario();
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		//TODO TERMINAR ISSO AKI
		
		questionarios.add(currentQ);
		return questionarios;
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("#limesurvey > table.survey-header-table > tbody td.survey-description > h2");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
