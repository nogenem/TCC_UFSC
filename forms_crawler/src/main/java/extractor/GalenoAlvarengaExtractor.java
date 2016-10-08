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

public class GalenoAlvarengaExtractor implements Extractor {
	
	private ArrayList<Questionario> questionarios;
	private Questionario currentQ;
	private Pergunta currentP;
	private ArrayList<Pergunta> pergs;
	
	@Override
	public boolean shouldExtract(WebURL url) {
		String tmp = url.getPath();
		// O primeiro pattern é para excluir as paginas com a lista de testes
		// O segundo pattern é para excluir as paginas que terminam com '/comment-page-1' e '/feed'
		// 	pois elas são simplesmente cópias das paginas normais
		return !tmp.matches("(/|/page/\\d)|(.+(/comment-page-1$|/feed$))");
	}

	@Override
	public ArrayList<Questionario> extract(String html) {
		questionarios = new ArrayList<>();
		currentQ = new Questionario();
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		Elements tmp = null;
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		// Perguntas
		pergs = new ArrayList<>();
		Pergunta tmpPerg = null;
		tmp = doc.select("div.entry-content dl.quizz_dl > dt");
		for(Element perg : tmp){
			tmpPerg = new Pergunta();
			tmpPerg.setDescricao(this.getDescricaoPergunta(perg));
			pergs.add(tmpPerg);
		}
		
		if(pergs.isEmpty()) return null;
		
		// Alternativas
		Element alts = null;
		tmp = doc.select("div.entry-content dl.quizz_dl > dd");
		for(int i = 0; i<pergs.size(); i++){
			currentP = pergs.get(i);
			alts = tmp.get(i);
			
			System.out.println("\t\t\tDescricao Pergunta: " + currentP.getDescricao());
			if(!this.getAlternativas(alts))
				System.err.println("ALTERNATIVA DESCONHECIDA");
			else
				currentQ.addPergunta(currentP);
		}
		pergs.clear();
		questionarios.add(currentQ);
		return questionarios;
	}
	
	private boolean getAlternativas(Element field) {
		return this.isRadioInput(field);
	}

	private boolean isRadioInput(Element field) {
		Elements tmp = field.select("input[type=radio]");
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(tmp.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		System.out.println("\t\t\t\tRadio normal:");
		
		tmp = field.select("label");
		for(Element lbl : tmp){
			tmpTxt = lbl.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\t" +tmpTxt);
		}
		
		return true;
	}

	private String getDescricaoPergunta(Element field) {
		return field.text().trim();
	}
	
	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("div.hentry > h1.entry-title > a");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
