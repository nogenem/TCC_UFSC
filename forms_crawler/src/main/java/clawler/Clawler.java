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
			".*(\\.(css|js|gif|jpg|png|mp3|mp4|zip|gz|pdf))$");
	
	private Extractor extractor;
	private BasicConnection conn;
	private QuestionarioManager questionarioManager;
	private String myDomainsRegex;
	
	@Override
	public void onStart() {
		this.conn = new PostgreConnection();
		this.questionarioManager = new QuestionarioManager(this.conn);
		this.myDomainsRegex = ((String[]) myController.getCustomData())[0];
	}
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		
		// Verifica se o link ja foi extraido
		if(QuestionarioManager.linkWasExtracted(href)) return false;
		
		// Retira o http e https
		href = href.replaceAll("^((http|https)://)", "");
		
		// Retira opções de requisições GET
		int index = href.indexOf("?");
	    if(index > 0)
	    	href = href.substring(0, index);
	    
	    // Verifica o filtro e os dominios
		return !FILTERS.matcher(href).matches() && href.matches(myDomainsRegex);
	}
	
	@Override
	public void visit(Page page) {
		WebURL url = page.getWebURL();
		System.out.println("\tURL: " + url.getURL());
		
		if (chooseExtractor(url) != null && 
				this.extractor.shouldExtract(url) &&
				page.getParseData() instanceof HtmlParseData) {
			
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			
			Questionario q = this.extractor.extract(htmlParseData.getHtml());
			if(q != null){
				q.setLink_doc(url.getURL());
				try {
					this.questionarioManager.save(q);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private Extractor chooseExtractor(WebURL url){
		String dom = url.getDomain().toLowerCase();
		dom = dom.substring(0, dom.indexOf('.'));
		if(dom.matches("(docs|goo)")) 
			dom = "google";
		
		this.extractor = ExtractorFactory.getInstanceFor(dom);
		return this.extractor;
	}
	
}
