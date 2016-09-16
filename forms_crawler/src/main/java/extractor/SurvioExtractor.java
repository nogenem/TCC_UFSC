package extractor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import model.Pergunta;
import model.Questionario;

public class SurvioExtractor implements Extractor {
	
	private Questionario currentQ;
	private Pergunta currentP;
	
	public SurvioExtractor(){}
	
	public boolean shouldExtract(WebURL url){
		String href = url.getURL().toLowerCase();
		return !href.startsWith("http://www.survio.com/br/modelos-de-pesquisa");
	}
	
	// TODO: Terminar o extrator
	public Questionario extract(HtmlParseData htmlParseData) {
		String html = htmlParseData.getHtml();
		currentQ = new Questionario();
		currentP = null;
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		Elements fields = doc.select(".last fieldset");
		for(Element field : fields){
			
			// Titulo da pergunta
			tmpTxt = this.getTituloPergunta(field);
			System.out.println("\t\t\tTitulo Pergunta: " + tmpTxt);
			
			// Alternativas da pergunta
			if(!this.getAlternativas(field))
				System.err.println("ALTERNATIVA DESCONHECIDA");
		}
		
		return null;
	}
	
	private boolean getAlternativas(Element field) {
		return this.isTextArea(field) 		||
			this.isInput(field)				||
			this.isNormalRadioBox(field) 	||
			this.isImgRadioBox(field) 		||
			this.isCheckBox(field) 			||
			this.isStars(field) 			||
			this.isSelect(field) 			||
			this.isSlider(field)			||
			this.isGrid(field);
	}

	//https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados
	private boolean isSlider(Element field) {
		Elements sliders = field.select(".row-divide .divide-item"),
				tmp = null;
		
		if(sliders.size() == 0) return false;
		
		// TODO: como representar isso?
		
		System.out.println("\t\t\t\tSliders:");
		for(Element slider : sliders){
			tmp = slider.select(".divide-title");
			System.out.println("\t\t\t\t\tTitulo: " +tmp.get(0).ownText());
			
			tmp = slider.select(".divide-left");
			System.out.println("\t\t\t\t\tValor esquerda: " +tmp.get(0).ownText());
			
			tmp = slider.select(".divide-right");
			System.out.println("\t\t\t\t\tValor esquerda: " +tmp.get(0).ownText());
		}
		return true;
	}

	//https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados
	private boolean isInput(Element field) {
		Elements input = field.select(".input-group-text input.form-control");

		if(input.size() == 0) return false;
		
		System.out.println("\t\t\t\tEh input.");
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-popularidade-de-esportes-radicais
	private boolean isSelect(Element field) {
		Elements select = field.select(".select select.form-control"),
				options = null;
		
		if(select.isEmpty()) return false;

		System.out.println("\t\t\t\tSelect:");
		options = select.select("option");
		for(Element option : options){
			System.out.println("\t\t\t\t\tOption: " +option.ownText());
		}
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto
	private boolean isGrid(Element field) {
		Elements matrix = field.select(".matrix-values"),
				tmp = null, tmp2 = null;
		
		if(matrix.isEmpty()) return false;
			
		System.out.print("\t\t\t\tMatriz");
			
		// Verifica se a matriz usa input text ou radio button
		tmp = matrix.select(".input-group-matrix-text");
		if(!tmp.isEmpty()) System.out.println(" [com input text]:");
		else System.out.println(" [com radio button]:");
		
		tmp = matrix.select(".input-group-matrix");
		for(Element e : tmp){
			tmp2 = e.select(".title-groups .input-group-title-main");
			if(!tmp2.isEmpty()){//Head
				for(Element span : tmp2){
					System.out.println("\t\t\t\t\tHead: " +span.ownText());
				}
			}else{//Body
				tmp2 = e.select(".title");
				System.out.println("\t\t\t\t\tBody: " +tmp2.get(0).ownText());
			}
		}
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/pesquisa-de-percepcao-da-publicidade-e-de-sua-eficiencia
	private boolean isStars(Element field) {
		Elements stars = field.select(".special-padding-row .original-stars input.star");
		
		if(stars.size() == 0) return false;
		
		System.out.println("\t\t\t\t" +stars.size()+ " Stars");
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto
	private boolean isCheckBox(Element field) {
		Elements labels = field.select(".label-cont .input-group-checkbox"),
				tmp = null;
		
		if(labels.size() == 0) return false; 

		System.out.println("\t\t\t\tCheckbox normal:");
		for(Element label : labels){
			tmp = label.select(".input-group-title");
			System.out.println("\t\t\t\t\t" +tmp.get(0).ownText());
			
			// TODO: como representar isso?
			tmp = label.select(".text-addon");
			if(tmp.size() > 0) System.out.println("\t\t\t\t\t\tCom input text");
		}
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/pesquisa-de-percepcao-da-publicidade-e-de-sua-eficiencia
	private boolean isImgRadioBox(Element field) {
		Elements spans = field.select(".input-image-group .input-group-radio .input-group-title");
		
		if(spans.size() == 0) return false; 
		
		System.out.println("\t\t\t\tRadio com img:");
		for(Element span : spans){
			System.out.println("\t\t\t\t\t" +span.ownText());
		}
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados
	private boolean isNormalRadioBox(Element field) {
		Elements labels = field.select(".label-cont .input-group-radio"),
				tmp = null;
		
		if(labels.size() == 0) return false; 
		
		System.out.println("\t\t\t\tRadio normal:");
		for(Element label : labels){
			tmp = label.select(".input-group-title");
			System.out.println("\t\t\t\t\t" +tmp.get(0).ownText());
			
			// TODO: como representar isso?
			tmp = label.select(".text-addon");
			if(tmp.size() > 0) System.out.println("\t\t\t\t\t\tCom input text");
		}
		return true;
	}
	
	//https://www.survio.com/modelo-de-pesquisa/feedback-sobre-servico
	private boolean isTextArea(Element field) {
		Elements tmp = field.select(".input-group-textarea textarea");
		
		if(tmp.size() == 0) return false;
		
		System.out.println("\t\t\t\tEh textarea.");
		return true;
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements h1Header = doc.select(".title div.col-title h1");
		if(h1Header.size() == 0) return "";
		return h1Header.get(0).ownText().trim();
	}

	private String getTituloPergunta(Element field) {
		Elements tmp = field.select(".title-part");
		if(tmp.size() == 0) return "";
		return tmp.get(0).ownText().trim();
	}
}
