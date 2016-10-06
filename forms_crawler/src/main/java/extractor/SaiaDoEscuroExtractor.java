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

public class SaiaDoEscuroExtractor implements Extractor {
	
	private Questionario currentQ;
	private Pergunta currentP;
	
	@Override
	public boolean shouldExtract(WebURL url) {
		return url.getURL().endsWith("questionario/7.htm");
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
		
		Elements form = doc.select("#multiForm > form"), 
				tmp = null, tmp2 = null;
		currentP = new Pergunta();
		
		// Titulo da primeira pergunta
		tmpTxt = this.getTituloPergunta(form.get(0));
		currentP.setDescricao(tmpTxt);
		System.out.println("\t\t\tTitulo Pergunta: " + tmpTxt);
		
		// Tipo e Forma da primeira pergunta
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
		System.out.println("\t\t\t\tMatriz:");
		
		// Alternativas da primeira pergunta
		tmp = form.get(0).select("div.area_g");
		for(Element field : tmp){
			// Alternativas da primeira pergunta
			if(!this.getAlternativas(field))
				System.err.println("ALTERNATIVA DESCONHECIDA");
		}
		currentQ.addPergunta(currentP);
		
		// Outras perguntas
		for(int i = 12; i<=13; i++){
			tmp = form.get(0).select("> div:nth-child("+i+")");
			currentP = new Pergunta();
			
			// Titulo da pergunta
			tmpTxt = this.getTituloPergunta(tmp.get(0));
			currentP.setDescricao(tmpTxt);
			System.out.println("\t\t\tTitulo Pergunta: " + tmpTxt);
			
			System.out.println("\t\t\t\tMatriz:");
			// Tipo e Forma
			currentP.setTipo("MULTIPLA_ESCOLHA");
			currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
			
			tmp2 = tmp.get(0).select("div.area_p");
			for(Element field : tmp2){
				// Alternativas da pergunta
				if(!this.getAlternativas(field))
					System.err.println("ALTERNATIVA DESCONHECIDA");
			}
			currentQ.addPergunta(currentP);
		}
		return currentQ;
	}

	private boolean getAlternativas(Element field) {
		return this.isMatrixRow(field);
	}

	private boolean isMatrixRow(Element field) {
		Elements title = field.select("div.label_inq"), 
				values = field.select("div.value_off > span");
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(title.isEmpty() || values.isEmpty()) return false;
		
		// Titulo da pergunta
		tmpPerg = new Pergunta();
		tmpTxt = title.get(0).text().trim();
		tmpPerg.setDescricao(tmpTxt);
		System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		
		// Tipo e Forma da pergunta
		tmpPerg.setTipo("FECHADO");
		tmpPerg.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.print("\t\t\t\t\t\t\tHead: ");			
		for(Element value : values){
			tmpTxt = value.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			tmpPerg.addAlternativa(tmpAlt);
			System.out.print(tmpTxt +" ");
		}
		System.out.println();
		tmpPerg.setQuestionario(currentQ);
		currentP.addPergunta(tmpPerg);
		
		return true;
	}

	private String getTituloPergunta(Element field) {
		Elements tmp = field.select("span.title_area");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("#details_div > h1:first-of-type");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}
}
