package extractor;

import java.util.ArrayList;
import java.util.HashMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import manager.FormaDaPerguntaManager;
import model.Alternativa;
import model.FormaDaPergunta;
import model.Grupo;
import model.Pergunta;
import model.Questionario;

public class GoogleExtractor implements Extractor {
	
	private Questionario currentQ;
	private Pergunta currentP;
	private Grupo currentG;
	
	private HashMap<String, Grupo> grupos;
	
	@Override
	public boolean shouldExtract(WebURL url) {
		String href = url.getURL().toLowerCase();
		return href.endsWith("/viewform");
	}

	@Override
	public Questionario extract(String html) {
		currentQ = new Questionario();
		initGrupos();
		currentG = null;
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		// div[role=listitem] ou div.ss-form-question
		Elements fields = doc.select("div.ss-form form ol > div[role=listitem]");
		for(Element field : fields){
			if(!field.hasClass("ss-form-question")){//se não é uma pergunta, então é uma seção
				tmpTxt = this.getTituloSecao(field);
				System.out.println("\t\t\tTitulo Secao: " + tmpTxt +"\n");
				updateGrupo(tmpTxt, true);
			}else{
				currentP = new Pergunta();
				
				// Titulo da pergunta
				tmpTxt = this.getDescricaoPergunta(field);
				currentP.setDescricao(tmpTxt);
				System.out.println("\t\t\tTitulo Pergunta: " + tmpTxt);
				
				// Atualiza o Grupo atual
				updateGrupo(tmpTxt, false);
				if(currentG != null)
					currentP.setGrupo(currentG);
				
				// Alternativas da pergunta
				if(!this.getAlternativas(field))
					System.out.println("ALTERNATIVA DESCONHECIDA");
				else
					currentQ.addPergunta(currentP);
			}
		}
		return currentQ;
	}

	private boolean getAlternativas(Element field) {
		return this.isTextarea(field)	||
			this.isInputText(field) 	||
			this.isRadioInput(field)	||
			this.isSelect(field)		||
			this.isMatrix(field);
	}

	private boolean isMatrix(Element field) {
		Elements tmp = field.select("div.ss-grid table"),
				tmp2 = null;
		
		if(tmp.isEmpty()) return false;
		
		Pergunta tmpPerg = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		ArrayList<Alternativa> altList = new ArrayList<>();
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");;
		
		System.out.println("\t\t\t\tMatriz");
		currentP.setTipo("MULTIPLA_ESCOLHA");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
		
		tmp2 = tmp.select("thead tr td > label");
		for(Element lbl : tmp2){//head
			tmpTxt = lbl.ownText().trim();
			tmpAlt = new Alternativa(tmpTxt);
			altList.add(tmpAlt);
			System.out.println("\t\t\t\t\tHead: " +tmpTxt);
		}
		
		tmp2 = tmp.select("tbody tr td.ss-gridrow-leftlabel");
		for(Element td : tmp2){//body
			tmpTxt = td.ownText().trim();
			tmpPerg = new Pergunta();
			tmpPerg.setDescricao(tmpTxt);
			tmpPerg.setTipo("FECHADO");
			tmpPerg.setForma(forma);
			tmpPerg.setGrupo(currentG);
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

	private boolean isSelect(Element field) {
		Elements options = field.select("div.ss-select select > option");
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(options.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\t\t\t\tSelect:");
		
		for(Element opt : options){
			tmpTxt = opt.ownText().trim();
			if(!tmpTxt.equals("")){
				tmpAlt = new Alternativa(tmpTxt);
				currentP.addAlternativa(tmpAlt);
				System.out.println("\t\t\t\t\tOption: " +tmpTxt);
			}
		}
		return true;
	}

	private boolean isRadioInput(Element field) {
		Elements items = field.select("div.ss-radio ul.ss-choices li.ss-choice-item"),
				tmp = null;
		Alternativa tmpAlt = null;
		String tmpTxt = "";
		
		if(items.isEmpty()) return false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
		
		System.out.println("\t\t\t\tRadio normal:");
		for(Element item : items){
			tmp = item.select("label > .ss-choice-label");
			tmpTxt = tmp.get(0).ownText().trim();
			System.out.println("\t\t\t\t\t" +tmpTxt);
			
			tmpAlt = new Alternativa(tmpTxt);
			currentP.addAlternativa(tmpAlt);
		}
		return true;
	}

	private boolean isTextarea(Element field) {
		Elements tmp = field.select("div.ss-paragraph-text textarea.ss-q-long");
		
		if(tmp.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tTextarea.");
		return true;
	}

	private boolean isInputText(Element field) {
		Elements tmp = field.select("div.ss-text input.ss-q-short[type=text]");
		
		if(tmp.isEmpty()) return false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
		currentP.setTipo("ABERTO");
		System.out.println("\t\t\t\tInput [text].");
		return true;
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select(".ss-top-of-page .ss-form-title");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}
	
	private String getDescricaoPergunta(Element field) {
		Elements tmp = field.select(".ss-q-item-label .ss-q-title"),
				tmp2 = field.select(".ss-q-item-label .ss-secondary-text");
		String desc = "", tmpTxt = "";
		if(!tmp.isEmpty())
			desc = tmp.get(0).ownText().trim();
		if(!tmp2.isEmpty()){
			tmpTxt = tmp2.get(0).ownText().trim();
			if(!tmpTxt.equals(""))
				desc += (tmp.isEmpty()?"":"\n") + 
					tmpTxt;
		}
		return desc;
	}
	
	private String getTituloSecao(Element field) {
		Elements tmp = field.select(".ss-section-header .ss-section-title");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}
	
	private void initGrupos(){
		grupos = null;
		grupos = new HashMap<>();
		grupos.put("instituição", new Grupo("Instituição", currentQ));
		grupos.put("professor", new Grupo("Professor", currentQ));
		grupos.put("coordenação", new Grupo("Coordenação", currentQ));
		grupos.put("curso", new Grupo("Curso", currentQ));
		grupos.put("organização e gestão institucional", new Grupo("Organização e Gestão Institucional", currentQ));
		grupos.put("infraestrutura", new Grupo("Infraestrutura", currentQ));
	}
	
	private void updateGrupo(String perg, boolean isSection){
		perg = perg.toLowerCase();
		Grupo tmp = currentG;
		
		if(isSection){
			currentG = grupos.get(perg);
			if(currentG == null && perg.contains("coordenador"))
				currentG = grupos.get("coordenação");
		}else if(perg.startsWith("1.")){
			currentG = grupos.get("instituição");
		}else if(perg.contains("- nome do professor")){
			currentG = grupos.get("professor");
		}else if(perg.contains(". nome do coordenador")){
			currentG = grupos.get("coordenação");
		}else if(perg.contains("infraestrutura")){
			currentG = grupos.get("infraestrutura");
		}
		
		if(tmp != currentG){//update			
			currentQ.addGrupo(currentG);
			System.out.println("Grupo Atual: " + currentG.getAssunto());
		}
	}

}
