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

public class AgendorExtractor implements Extractor {
	
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
		Document doc = Jsoup.parse(html);
		
		this.extractQuestionario1(doc);		
		this.extractQuestionario2(doc);

		return questionarios;
	}
	
	private void extractQuestionario1(Document doc) {
		currentQ = new Questionario();
		
		Elements tmp = doc.select("#post-15741 > div.entry > h4:nth-child(20)");
		String tmpTxt = "";
		
		// Assunto questionario
		tmpTxt = tmp.get(0).text().trim();//TODO pegar a parte secundaria?
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		// Alternativas
		ArrayList<Alternativa> altList = new ArrayList<>();
		Alternativa tmpAlt = null;
		
		tmpAlt = new Alternativa("Sim");
		altList.add(tmpAlt);
		tmpAlt = new Alternativa("Não");
		altList.add(tmpAlt);
		tmpAlt = new Alternativa("Às vezes");
		altList.add(tmpAlt);
		
		// Perguntas
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
		tmp = doc.select("#post-15741 > div.entry > ol:nth-child(22) > li");
		for(Element li : tmp){
			tmpTxt = li.ownText().trim();
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
			System.out.println("\t\t\t\tRadio normal:");
			currentP = new Pergunta();
			currentP.setDescricao(tmpTxt);
			currentP.setTipo("FECHADO");
			currentP.setForma(forma);
			for(Alternativa a : altList){
				currentP.addAlternativa(a.clone());
				System.out.println("\t\t\t\t\t" +a.getDescricao());
			}
			currentQ.addPergunta(currentP);
		}
		
		questionarios.add(currentQ);
	}

	private void extractQuestionario2(Document doc) {
		currentQ = new Questionario();
		
		Elements tmp = doc.select("#post-15741 > div.entry > h4:nth-child(23)");
		Element li = null;
		String tmpTxt = "";
		Pergunta tmpPerg = null;
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
		Alternativa tmpAlt = null;
		
		// Assunto questionario
		tmpTxt = tmp.get(0).text().trim();//TODO pegar a parte secundaria?
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		// Perguntas
		for(int i = 25; i<=27; i+=2){
			tmp = doc.select("#post-15741 > div.entry > p:nth-child("+i+")");
			currentP = new Pergunta();
			
			// Titulo da pergunta
			tmpTxt = tmp.get(0).text().trim();
			currentP.setDescricao(tmpTxt);
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
			
			currentP.setTipo("FECHADO");
			currentP.setForma(forma);
			
			// Alternativas
			tmp = doc.select("#post-15741 > div.entry > ul:nth-child("+(i+1)+") > li");
			for(int j = 0; j<tmp.size()-1; j++){
				li = tmp.get(j);
				tmpTxt = li.ownText().trim();//TODO tirar o ( ) ?
				System.out.println("\t\t\t\t\t" +tmpTxt);
				tmpAlt = new Alternativa(tmpTxt);
				currentP.addAlternativa(tmpAlt);
			}
			// 'Outros fatores' é outra pergunta
			li = tmp.get(tmp.size()-1);
			tmpTxt = li.ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			tmpPerg = new Pergunta();
			tmpPerg.setDescricao(tmpTxt);
			tmpPerg.setTipo("ABERTO");
			tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
			tmpPerg.setQuestionario(currentQ);
			currentP.addPergunta(tmpPerg);
			
			currentQ.addPergunta(currentP);
		}
		
		questionarios.add(currentQ);
	}

}
