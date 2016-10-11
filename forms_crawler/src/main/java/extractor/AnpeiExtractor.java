package extractor;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import model.Grupo;
import model.Pergunta;
import model.Questionario;

public class AnpeiExtractor implements Extractor {
	
	private ArrayList<Questionario> questionarios;
	private Questionario currentQ;
	private Pergunta currentP;
	private Grupo currentG;
	
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
		
		Elements trs = doc.select("#confirmacao > table > tbody > tr > td > table > tbody > tr"),
				tmp = null;
		Element tr = null;
		for(int i = 7; i<trs.size(); i++){
			tr = trs.get(i);
			
			// Grupo
			tmp = tr.select("td > font > strong");
			if(!tmp.isEmpty()){
				tmpTxt = tmp.get(0).ownText().trim();
				currentG = new Grupo();
				currentG.setAssunto(tmpTxt);
				currentQ.addGrupo(currentG);
				System.out.println("\t\t\tTitulo Secao: " + tmpTxt +"\n");
			}
			
			// Perguntas
			tmp = tr.select("td > table > tbody > tr");
			if(!tmp.isEmpty()){
				//TODO TERMINAR ISSO AKI
			}
		}
		
		questionarios.add(currentQ);
		return questionarios;
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("#confirmacao > center > table > tbody > tr:nth-child(1) > td > p > font > strong");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
