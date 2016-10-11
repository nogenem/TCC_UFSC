package extractor;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import manager.FormaDaPerguntaManager;
import model.Alternativa;
import model.Pergunta;
import model.Questionario;

public class HotelJardinsdAjudaExtractor implements Extractor {
	
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
		
		Elements fields = doc.select("#postform1 > table > tbody > tr > td > table > tbody > tr"),
				elems = null;
		for(int i = 1; i<fields.size()-3; i++){
			elems = fields.get(i).select("td");
			if(elems.size() == 2){
				// Pergunta
				tmpTxt = elems.get(0).ownText().trim();
				System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
				currentP = new Pergunta(tmpTxt);
				
				// Alternativas
				if(!this.getAlternativas(elems.get(1)))
					System.err.println("ALTERNATIVA DESCONHECIDA");
				else
					currentQ.addPergunta(currentP);
			}
		}
		
		questionarios.add(currentQ);
		return questionarios;
	}

	private boolean getAlternativas(Element field) {
		return this.isTextInput(field) ||
				this.isSelect(field) ||
				this.isTextArea(field);
	}

	private boolean isTextInput(Element field) {
		Elements input = field.select("input[type=text]");
		
		if(input.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tInput [text].");
		return true;
	}
	
	private boolean isSelect(Element field) {
		Elements options = field.select("select > option");
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(options.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\t\t\t\tSelect:");
		
		for(Element option : options){
			tmpTxt = option.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\tOption: " +tmpTxt);
		}
		return true;
	}

	private boolean isTextArea(Element field) {
		Elements tArea = field.select("textarea");
		
		if(tArea.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tTextarea.");
		return true;
	}
	
	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("#DwContent > div.T11-B11_text > h1");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
