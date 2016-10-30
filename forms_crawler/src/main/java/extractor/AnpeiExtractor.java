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
	
	private ArrayList<Alternativa> altList;
	
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
		
		// Quarto grupo de perguntas
		extractFormat2(trs.get(17), 
				trs.get(18).select("td > table > tbody > tr"));
		System.out.println("\n");
		
		// Quinto grupo de perguntas
		extractFormat3(trs.get(20), 
				trs.get(21).select("td > table > tbody > tr"));
		System.out.println("\n");

		questionarios.add(currentQ);
		return questionarios;
	}

	/**
	 * Extrai as perguntas com suas alternativas seguindo o 'Format1' deste site.
	 * 'Format1' significa que a table possui tr's com duas td's dentro, aonde a primeira
	 * possui o titulo da pergunta e a segunda possui as alternativas da mesma, podendo as vezes,
	 * possuir uma td a mais, geralmente uma subpergunta, que tiveram que ser tratadas.
	 * 
	 * @param grupo
	 * @param trsPergs
	 */
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
	
	/**
	 * Extrai as perguntas com suas alternativas seguindo o 'Format2' deste site.
	 * 'Format2' é usado para um grupo que possui uma pergunta normal e uma matriz
	 * que teve que ser tratada separadamente por não ter um padrão que facilitasse
	 * a extração.
	 * 
	 * @param grupo
	 * @param trsPergs
	 */
	private void extractFormat2(Element trGrupo, Elements trsPergs){
		updateGrupo(trGrupo);
		altList = new ArrayList<>();
		
		Elements tds = null;
		String tmpTxt = "";
		
		for(Element tr : trsPergs){
			tds = tr.select("> td");
			switch(tds.size()){
			case 1://Titulo da pergunta da matriz
				tmpTxt = tds.get(0).ownText().replaceAll("\u00a0", "").trim();
				currentP = new Pergunta(tmpTxt, "ABERTO", FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
				System.out.println("\t\t\tDescricao Pergunta: " +tmpTxt);
				System.out.println("\t\t\t\tMatriz [com input text]:");
				currentQ.addPergunta(currentP);
				break;
			case 2://1* pergunta do grupo
				tmpTxt = tds.get(0).ownText().replaceAll("\u00a0", "").trim();
				currentP = new Pergunta(tmpTxt);
				System.out.println("\t\t\tDescricao Pergunta: " +tmpTxt);
				this.getAlternativas(tds.get(1));
				currentQ.addPergunta(currentP);
				break;
			case 3://Resto da pergunta da matriz
				extractMatrizOfFormat2(tds);				
				break;
			default:
				break;
			}
		}
	}
	
	private void extractMatrizOfFormat2(Elements tds){
		String tmpTxt = "";
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		
		if(tds.get(0).select("input[type=text]").isEmpty()){//São as alternativas
			for(int i = 0; i<3; i++){
				tmpTxt = tds.get(i).text().replaceAll("\u00a0", "").trim();
				tmpAlt = new Alternativa(tmpTxt);
				altList.add(tmpAlt);
				System.out.println("\t\t\t\t\tHead: " +tmpTxt);
			}
		}else{//São as perguntas
			tmpTxt = tds.get(0).text().replaceAll("\u00a0", "").trim();
			tmpPerg = new Pergunta(tmpTxt, "ABERTO", FormaDaPerguntaManager.getForma("TEXT_INPUT"));
			for(Alternativa a : altList){
				tmpPerg.addAlternativa(a.clone());
			}
			tmpPerg.setQuestionario(currentQ);
			currentP.addPergunta(tmpPerg);
			System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		}
	}
	
	private void extractFormat3(Element trGrupo, Elements trsPergs){
		updateGrupo(trGrupo);
		
		Element tr = null;
		Elements tds = null;
		String tmpTxt = "";
		Alternativa tmpAlt = null;
		
		// Primeira pergunta pai
		tr = trsPergs.get(0);
		tds = tr.select("> td");
		tmpTxt = tds.get(0).ownText().trim();
		currentP = new Pergunta(tmpTxt, "ABERTO", FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
		System.out.println("\t\t\tDescricao Pergunta: " +tmpTxt);
		System.out.println("\t\t\t\tMatriz [com input text]:");
		
		// Alternativas
		altList = new ArrayList<>();
		for(int i = 1; i<tds.size(); i++){
			tmpTxt = tds.get(i).ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			altList.add(tmpAlt);
			System.out.println("\t\t\t\t\tHead: " +tmpTxt);
		}
		
		// Subperguntas
		extractPerguntasFormat3(trsPergs.get(2), trsPergs.get(3));
		currentQ.addPergunta(currentP);
		
		// Segunda pergunta pai
		tr = trsPergs.get(1);
		tds = tr.select("> td");
		tmpTxt = tds.get(0).ownText().trim();
		currentP = new Pergunta(tmpTxt, "ABERTO", FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
		System.out.println("\t\t\tDescricao Pergunta: " +tmpTxt);
		System.out.println("\t\t\t\tMatriz [com input text]:");
		
		// Alternativas
		altList = new ArrayList<>();
		for(int i = 1; i<tds.size(); i++){
			tmpTxt = tds.get(i).ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			altList.add(tmpAlt);
			System.out.println("\t\t\t\t\tHead: " +tmpTxt);
		}
		
		// Subperguntas
		extractPerguntasFormat3(trsPergs.get(2), trsPergs.get(3));
		currentQ.addPergunta(currentP);
		
	}
	
	private void extractPerguntasFormat3(Element trPerg1, Element trPerg2) {
		String tmpTxt = "";
		Elements tds = null;
		Pergunta tmpPerg = null;
		
		// Subpergunta 1
		tds = trPerg1.select("> td");
		tmpTxt = tds.get(0).ownText().replaceAll("\u00a0", "").trim();
		tmpPerg = new Pergunta(tmpTxt, "ABERTO", FormaDaPerguntaManager.getForma("TEXT_INPUT"));
		for(Alternativa a : altList){
			tmpPerg.addAlternativa(a.clone());
		}
		tmpPerg.setQuestionario(currentQ);
		currentP.addPergunta(tmpPerg);
		System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		
		// Subpergunta 2
		tds = trPerg2.select("> td");
		tmpTxt = tds.get(0).ownText().replaceAll("\u00a0", "").trim();
		tmpPerg = new Pergunta(tmpTxt, "ABERTO", FormaDaPerguntaManager.getForma("TEXT_INPUT"));
		for(Alternativa a : altList){
			tmpPerg.addAlternativa(a.clone());
		}
		tmpPerg.setQuestionario(currentQ);
		currentP.addPergunta(tmpPerg);
		System.out.println("\t\t\t\t\tBody: " +tmpTxt);
	}

	private boolean getAlternativas(Element field) {
		return this.isTextInput(field) ||
				this.isRadioInput(field) ||
				this.isTextArea(field) || 
				this.isGroupOfTextarea(field);
	}

	private boolean isGroupOfTextarea(Element field) {
		Elements tmp = field.select("input[type=text]");
		
		if(tmp.size() < 3) return false;

		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("TEXT_INPUT");
		
		currentP.setForma(forma);	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tGrupo de textareas:");
		
		Pergunta tmpPerg = null;
		String tmpTxt = "";
		List<TextNode> tnList = field.textNodes();
		
		for(TextNode node : tnList){
			if(!node.isBlank()){
				tmpTxt = node.text().replaceAll("\u00a0", "").trim();
				tmpPerg = new Pergunta(tmpTxt, "ABERTO", forma);
				tmpPerg.setQuestionario(currentQ);
				currentP.addPergunta(tmpPerg);
				System.out.println("\t\t\t\tDescricao Pergunta [sub]: " +tmpTxt);
				System.out.println("\t\t\t\t\tTextarea.");
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
			
			System.out.println("\t\t\t\tDescricao Pergunta: " +tmpTxt);
			System.out.println("\t\t\t\t\tInput [text].");
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
