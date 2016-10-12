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

public class BioinfoExtractor implements Extractor {
	
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
		
		Elements fields = doc.select("form[name=mctq] > font.section_header, form[name=mctq] > table.input_form_table");
		
		// Extrair perguntas do formato 1
		extractFormat1(fields.get(0), fields.get(1).select("> tbody > tr"));
		
		// Extrair perguntas do formato 2
		extractFormat2(fields.get(2), fields.get(3).select("> tbody > tr"));
		extractFormat2(fields.get(4), fields.get(5).select("> tbody > tr"));
		
		// Extrair perguntas do formato 3
		extractFormat3(fields.get(6), fields.get(7).select("> tbody > tr"));
		extractFormat3(fields.get(8), fields.get(9).select("> tbody > tr"));
		
		// Extrair perguntas do formato 4
		extractFormat4(fields.get(10), fields.get(11).select("> tbody > tr"));
		
		questionarios.add(currentQ);
		return questionarios;
	}
	
	/**
	 * Extrai as perguntas com suas alternativas seguindo o 'Format1' deste site.
	 * 'Format1' significa que a table possui tr's com duas td's dentro, aonde a primeira
	 * possui o titulo da pergunta e a segunda possui as alternativas da mesma.
	 * 
	 * @param grupo
	 * @param trsPergs
	 */
	private void extractFormat1(Element grupo, Elements trsPergs){
		updateGrupo(grupo);
		
		Elements tds = null;
		String tmpTxt = "";
		for(Element tr : trsPergs){
			tds = tr.select("> td");

			// Titulo da pergunta
			tmpTxt = tds.get(0).text().trim();
			currentP = new Pergunta(tmpTxt);
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
			
			// Alternativas
			if(!this.getAlternativas(tds.get(1)))
				System.err.println("ALTERNATIVA DESCONHECIDA");
			else
				currentQ.addPergunta(currentP);
		}
	}
	
	/**
	 * Extrai as perguntas com suas alternativas seguindo o 'Format2' deste site.
	 * 'Format2' significa que a table possui tr's com uma td dentro, aonde o texto
	 * desta td é o titulo da pergunta e ela possui um elemento interno que possui
	 * as alternativas da mesma.
	 * 
	 * @param grupo
	 * @param trsPergs
	 */
	private void extractFormat2(Element grupo, Elements trsPergs){
		updateGrupo(grupo);
		
		Element td = null;
		String tmpTxt = "";
		for(Element tr : trsPergs){
			td = tr.select("> td").get(0);
			
			// Titulo da pergunta
			tmpTxt = td.ownText().replace("\u00a0", "").trim();
			currentP = new Pergunta(tmpTxt);
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
			
			// Alternativas
			if(!this.getAlternativas(td))
				System.out.println("ALTERNATIVA DESCONHECIDA");
			else
				currentQ.addPergunta(currentP);//FIXME é subpergunta? [Se ?SIM?, ...]
		}
	}
	
	/**
	 * Extrai as perguntas com suas alternativas seguindo o 'Format3' deste site.
	 * 'Format3' significa que a table possui tr's com duas td's dentro, aonde a primeira
	 * possui uma imagem e a segundo possui o titulo da pergunta e as alternativas da mesma.
	 * 
	 * @param grupo
	 * @param trsPergs
	 */
	private void extractFormat3(Element grupo, Elements trsPergs){
		updateGrupo(grupo);
		
		Elements tds = null;
		Element td = null;
		String tmpTxt = "";
		Pergunta tmpPerg = null;
		Elements tmp = null;
		for(Element tr : trsPergs){
			tds = tr.select("> td");
			
			if(tds.size() == 2)
				td = tds.get(1);
			else
				td = tds.get(0);
			
			// Titulo da pergunta
			tmpTxt = td.ownText();
			tmpTxt = adjustPergunta(tmpTxt);
			currentP = new Pergunta(tmpTxt);
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
			
			// Alternativas
			if(!this.getAlternativas(td))//TODO VER O QUE FAZER SOBRE A PERGUNTA "Algumas pessoas..."
				System.out.println("ALTERNATIVA DESCONHECIDA");
			else{
				currentQ.addPergunta(currentP);
				if(!td.select("table input[type=radio]").isEmpty()){//solução para pergunta: "acordo às __:__ horas"
					tmpPerg = currentP;
					tmp = td.select("table > tbody > tr > td:nth-child(2)");
					currentP = new Pergunta("");
					System.out.println("\t\t\tDescricao Pergunta: [sub]");
					this.getAlternativas(tmp.get(0));
					this.getAlternativas(tmp.get(1));
					
					currentP.setQuestionario(currentQ);
					tmpPerg.addPergunta(currentP);
				}
			}
		}
	}
	
	/**
	 * Extrai as perguntas com suas alternativas seguindo o 'Format4' deste site.
	 * 'Format4' é igual ao 'Format2' porem acredito que seja melhor tratar a ultima pergunta
	 * do questionário separadamente, por ser uma matriz.
	 * 
	 * @param grupo
	 * @param trsPergs
	 */
	private void extractFormat4(Element grupo, Elements trsPergs){
		updateGrupo(grupo);
		
		Element td = null;
		String tmpTxt = "";
		td = trsPergs.get(0).select("> td").get(0);
		
		// Titulo da pergunta
		tmpTxt = td.ownText();
		tmpTxt = adjustPergunta(tmpTxt);
		currentP = new Pergunta(tmpTxt);
		System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
		
		// Alternativas
		this.isMatrix(td);
		currentQ.addPergunta(currentP);
	}
	
	private boolean getAlternativas(Element field) {
		return this.isTextInput(field) ||//isTextInput tem que vir antes de isRadioInput
				this.TextArea(field) ||
				this.isDate(field) ||
				this.isRadioInput(field) ||
				this.isSelect(field); 
	}

	private boolean isMatrix(Element field) {//só é usado diretamente pelo Format4		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tMatriz [com input text]:");
		
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("TEXT_INPUT");
		String tmpTxt = "";
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		ArrayList<Alternativa> altList = new ArrayList<>();
		
		Elements tds = field.select("> table > tbody > tr > td:nth-child(3)");
		List<TextNode> tnList = tds.get(0).textNodes();
		
		for(TextNode node : tnList){//Head
			if(!node.isBlank()){
				tmpTxt = node.text().replaceAll("\u00a0", "").trim();
				if(!tmpTxt.equals("")){
					tmpAlt = new Alternativa(tmpTxt);
					altList.add(tmpAlt);
					System.out.println("\t\t\t\t\tHead: " +tmpTxt);
				}
			}
		}
		
		tds = field.select("> table > tbody > tr > td:nth-child(2)");
		for(Element td : tds){//Body
			tmpTxt = td.ownText().trim();
			tmpPerg = new Pergunta(tmpTxt, "ABERTO", forma);
			for(Alternativa a : altList){
				tmpPerg.addAlternativa(a.clone());
			}
			tmpPerg.setQuestionario(currentQ);
			currentP.addPergunta(tmpPerg);
			System.out.println("\t\t\t\t\tBody: " +tmpTxt);
		}
		altList.clear();
		return true;		
	}

	private boolean isTextInput(Element field) {
		Elements input = field.select("input[type=text]");
		
		if(input.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tInput [text].");
		return true;
	}
	
	private boolean TextArea(Element field) {
		Elements tmp = field.select("textarea");
		
		if(tmp.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tTextarea.");
		return true;
	}

	private boolean isDate(Element field) {
		Elements input = field.select("font.user_input_summary");
		
		if(input.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("DATE_INPUT"));	
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tInput [date].");
		return true;
	}
	
	private boolean isRadioInput(Element field) {
		Elements elems = field.select("input[type=radio]");
		
		if(elems.isEmpty()) return false;
		
		elems = field.select("table > tbody > tr > td");//solução para pergunta "Você tem uma atividade..."
		if(!elems.isEmpty()){
			field = elems.get(1);
		}
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		System.out.println("\t\t\t\tRadio normal:");
		
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		List<TextNode> tnList = field.textNodes();
		for(TextNode node : tnList){
			if(!node.isBlank()){
				tmpTxt = node.text().replace("\u00a0", "").trim();
				tmpAlt = new Alternativa(tmpTxt);
				currentP.addAlternativa(tmpAlt);
				System.out.println("\t\t\t\t\t" +tmpTxt);
			}
		}
		return true;
	}
	
	private boolean isSelect(Element field) {
		Elements options = field.select("select > option");
		
		if(options.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\t\t\t\tSelect:");
		
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		for(Element option : options){
			tmpTxt = option.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t\t\t\tOption: " +tmpTxt);
		}
		return true;
	}
	
	private String adjustPergunta(String perg){
		perg = perg.replaceAll("\u00a0", "").trim();
		perg = perg.replaceAll(".:.horas(\\.|,)?", " __:__ horas$1");
		perg = perg.replace("minutos", "__ minutos");
		if(perg.startsWith("\u00e0s_______horas"))
			perg = "\u00e0s __:__ horas, decido dormir.";
		
		return perg;
	}
	
	private void updateGrupo(Element grupo){
		String tmpTxt = grupo.ownText().trim();
		currentG = new Grupo(tmpTxt);
		currentQ.addGrupo(currentG);
		System.out.println("\n\t\t\tTitulo Secao: " + tmpTxt +"\n");
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("table.instructions > tbody font");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).text().trim();
	}

}
