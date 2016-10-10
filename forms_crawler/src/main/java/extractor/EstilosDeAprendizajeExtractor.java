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

public class EstilosDeAprendizajeExtractor implements Extractor {
	
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
		
		// Perguntas sobre aprendizagem
		this.getPerguntasSobreAprendizagem(doc);
		
		// Perguntas sobre dados sócio-acadêmicos
		this.getPerguntasSobreDados(doc);
		
		questionarios.add(currentQ);
		return questionarios;
	}

	private void getPerguntasSobreAprendizagem(Document doc) {
		Elements trs = doc.select("body > table > tbody > tr"), 
				perg = null;
		Element tr = null;
		String tmpTxt = "";
		
		// Alternativas
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();

		tmpAlt = new Alternativa("Más");
		altList.add(tmpAlt);
		tmpAlt = new Alternativa("Menos");
		altList.add(tmpAlt);
		
		// Perguntas
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
		for(int i = 1; i<trs.size(); i++){//ignora 1* tr (cabeçalho)
			tr = trs.get(i);
			perg = tr.select("> td:nth-child(3) > font");
			
			tmpTxt = perg.get(0).ownText().trim();
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
	}

	private void getPerguntasSobreDados(Document doc) {
		Elements trs = doc.select("#IDFORMULARIO > table > tbody > tr > td > table > tbody > tr"),
				tds = null;
		Element tr = null, td = null;
		String tmpTxt = "";
		
		for(int i = 3; i<trs.size()-1; i++){//ignora as 3 primeiras e a ultima tr
			tr = trs.get(i);
			tds = tr.select("td");
			
			// Pergunta
			currentP = new Pergunta();
			td = tds.get(0);

			tmpTxt = this.getDescricaoPergunta(td);
			currentP.setDescricao(tmpTxt);
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
						
			// Alternativas
			td = tds.get(1);
			if(!this.getAlternativas(td))
				System.err.println("ALTERNATIVA DESCONHECIDA");
			else
				currentQ.addPergunta(currentP);
		}
	}
	
	private boolean getAlternativas(Element field) {
		return this.isSelect(field) ||
				this.isRadioInput(field) ||
				this.isTextInput(field);
	}

	private boolean isSelect(Element field) {
		Elements options = field.select("select > option");
		String tmpTxt = "";
		Alternativa tmpAlt = null;
		
		if(options.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\t\t\t\tSelect:");
		
		for(Element op : options){
			tmpTxt = op.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\tOption: " +tmpTxt);
		}
		return true;
	}

	private boolean isRadioInput(Element field) {
		Elements tds = field.select("table > tbody > tr > td"),
				tmp = tds.select("input[type=radio]");
		String tmpTxt = "";
		Alternativa tmpAlt = null;

		if(tmp.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		tmp = tds.select("font");
		for(Element alt : tmp){
			tmpTxt = alt.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\t" +tmpTxt);
		}
		return true;
	}
	
	private boolean isTextInput(Element field) {
		Elements input = field.select("input[type=text]");

		if(input.isEmpty()) return false;
		
		currentP.setTipo("ABERTO");
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
		
		System.out.println("\t\t\t\tInput [text].");
		return true;
	}
	
	private String getDescricaoPergunta(Element field) {
		Elements tmp = field.select("font");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("body > center:nth-child(4) > p > font > b");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
