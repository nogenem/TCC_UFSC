package clawler;

import java.util.regex.Pattern;

import dao.connection.BasicConnection;
import dao.connection.PostgreConnection;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import extractor.Extractor;
import extractor.ExtractorFactory;
import manager.QuestionarioManager;
import model.Questionario;

public class Clawler extends WebCrawler {
	
	private static final Pattern FILTERS = Pattern.compile(
			".*(\\.(css|js|gif|jpg|png|mp3|zip|gz))$");
	
	private Extractor extractor;
	private BasicConnection conn;
	private QuestionarioManager questionarioManager;
	private String[] myDomains;
	
	@Override
	public void onStart() {
		this.conn = new PostgreConnection();
		this.questionarioManager = new QuestionarioManager(this.conn);
		this.myDomains = (String[]) myController.getCustomData();
	}
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		
		// Se não verificar isso os formularios do survio vão
		// ser extraidos 2x
		if(href.endsWith("?mobile=1")) return false;
		
		// Retira opções de requisições GET
		int index = href.indexOf("?");
	    if(index > 0)
	    	href = href.substring(0, index);
	    
	    // Verifica o filtro e os dominios
		if(FILTERS.matcher(href).matches()) return false;
		for(String dom : this.myDomains){
			if(href.startsWith(dom))
				return true;
		}
		return false;
	}
	
	@Override
	public void visit(Page page) {
		WebURL url = page.getWebURL();
		System.out.println("\t" + url.getURL());
		
		if (chooseExtractor(url) != null && 
				page.getParseData() instanceof HtmlParseData) {
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			
			Questionario q = this.extractor.extract(htmlParseData);
			if(q == null) return;
			try {
				this.questionarioManager.save(q);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Extractor chooseExtractor(WebURL url){
		String dom = url.getDomain().toLowerCase();
		if(dom.contains("survio"))
			this.extractor = ExtractorFactory.getInstanceFor("survio");
		else if(dom.contains("google"))
			this.extractor = null;
		else if(dom.contains("vark-learn"))
			this.extractor = null;
		
		return this.extractor;
	}
	
}
