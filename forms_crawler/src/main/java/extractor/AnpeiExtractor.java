package extractor;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import manager.FormaDaPerguntaManager;
import model.Alternativa;
import model.FormaDaPergunta;
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
		
		Elements trs = doc.select("#confirmacao > table > tbody > tr > td > table > tbody > tr");
		
		// Primeiro grupo de perguntas
		extractFormat1(trs.get(7), 
				trs.get(8).select("td > table > tbody > tr"));
		System.out.println("\n");
		
		// Segundo grupo de perguntas
		extractFormat1(trs.get(11), 
				trs.get(12).select("td > table > tbody > tr"));
		System.out.println("\n");
		
		// Terceiro grupo de perguntas
		extractFormat1(trs.get(14), 
				trs.get(15).select("td > table > tbody > tr"));
		System.out.println("\n");
		
		
		questionarios.add(currentQ);
		return questionarios;
	}

	private void extractFormat1(Element trGrupo, Elements trsPergs){
		updateGrupo(trGrupo);
		
		Elements tds = null;
		Element perg = null, alts = null;
		String tmpTxt = "";
		Pergunta tmpPerg = null;
		
		for(Element tr : trsPergs){
			tds = tr.select("td");
			for(int i = 0; i<tds.size(); i+=2){
				perg = tds.get(i);
				alts = ((i+1)<tds.size()) ? tds.get(i+1) : null;
				if(alts == null){
					alts = perg;
					tmpPerg = currentP;
				}
				
				tmpTxt = perg.text().trim();
				if(tmpTxt.startsWith("Se sim,"))//eh uma sub pergunta
					tmpPerg = currentP;
					
				if(!tmpTxt.equals("")){
					// Pergunta
					currentP = new Pergunta(tmpTxt);
					System.out.println("\t\t\tDescricao Pergunta: " +tmpTxt);
					
					// Alternativas da pergunta
					if(!this.getAlternativas(alts))
						System.err.println("ALTERNATIVA DESCONHECIDA");
					else{
						if(tmpPerg == null)
							currentQ.addPergunta(currentP);
						else{//Eh uma sub pergunta
							currentP.setQuestionario(currentQ);
							tmpPerg.addPergunta(currentP);
						}
					}
				}
				tmpPerg = null;
			}
		}
	}
	
	private boolean getAlternativas(Element field) {
		return this.isTextInput(field) ||
				this.isRadioInput(field) ||
				this.isTextArea(field) || 
				this.isSimpleMatrix(field);
	}

	private boolean isSimpleMatrix(Element field) {
		Elements tmp = field.select("input[type=text]");
		
		//TODO TERMINAR ISSO AKI [esperando resposta]
		if(tmp.size() < 3) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tMatriz Simples [com input text]:");
		
		Alternativa tmpAlt = null;
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("TEXT_INPUT");
		String tmpTxt = "";
		List<TextNode> tnList = field.textNodes();
		
		for(TextNode node : tnList){
			if(!node.isBlank()){
				tmpTxt = node.text().trim();
				tmpAlt = new Alternativa();
			}
		}
		
		return true;
	}

	private boolean isTextInput(Element field) {
		Elements input = field.select("input[type=text]");
		
		if(input.isEmpty()) return false;
		else if(input.size() > 2) return false;//eh matrix
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tInput [text].");
		
		if(input.size() == 2){
			String tmpTxt = field.text();
			tmpTxt = tmpTxt.replace("\u00a0", "").trim();//TODO testar isso de novo
			
			Pergunta tmpPerg = new Pergunta(tmpTxt);
			currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));	
			currentP.setTipo("ABERTO");
			currentQ.addPergunta(tmpPerg);
			
			System.out.println("\t\t\tDescricao Pergunta: " +tmpTxt);
			System.out.println("\t\t\t\tInput [text].");
		}
		return true;
	}
	
	private boolean isRadioInput(Element field) {
		Elements tmp = field.select("input[type=radio]");
		String tmpTxt = "";
		Alternativa tmpAlt = null;
		
		if(tmp.isEmpty()) return false;
		
		tmp = field.select("label");
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tRadio normal:");
		for(Element lbl : tmp){
			tmpTxt = lbl.ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
		}
		return true;
	}
	
	private boolean isTextArea(Element field) {
		Elements tmp = field.select("textarea");
		
		if(tmp.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tTextarea.");
		return true;
	}

	private void updateGrupo(Element tr){
		Elements field = tr.select("td > font > strong");
		if(!field.isEmpty()){
			String tmpTxt = field.get(0).ownText().trim();
			currentG = new Grupo();
			currentG.setAssunto(tmpTxt);
			currentQ.addGrupo(currentG);
			System.out.println("\t\t\tTitulo Secao: " + tmpTxt +"\n");
		}
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("#confirmacao > center > table > tbody > tr:nth-child(1) > td > p > font > strong");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
