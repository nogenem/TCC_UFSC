package br.ufsc.tcc.extractor.crawler;

import java.util.ArrayList;
import java.util.regex.Pattern;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.extractor.database.manager.QuestionarioManager;
import br.ufsc.tcc.extractor.extractor.ExtractorFactory;
import br.ufsc.tcc.extractor.extractor.IExtractor;
import br.ufsc.tcc.extractor.model.Questionario;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {
	
	private static final Pattern FILTERS = Pattern.compile(
			".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|"
			+ "avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	
	private IExtractor extractor;
	private BasicConnection conn;
	private QuestionarioManager questionarioManager;
	private String myDomainsRegex;
	
	@Override
	public void onStart() {
		this.conn = new PostgreConnection(ProjectConfigs.getDatabaseConfigs());
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

		// Verifica se o link ja foi extraido 
		// [shouldVisit() não é chamada em seeds]
		if(QuestionarioManager.linkWasExtracted(url.getURL())) 
			return;
		
		if(chooseExtractor(url) != null && 
				this.extractor.shouldExtract(url) &&
				page.getParseData() instanceof HtmlParseData) {
			
			System.out.println("\tURL: " + url.getURL());
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			
			//TODO refazer o banco de dados do zero
			ArrayList<Questionario> questionarios = this.extractor.extract(htmlParseData.getHtml());
			for(Questionario q : questionarios){
				q.setLink_doc(url.getURL().toLowerCase());
				try {
					//this.questionarioManager.save(q);
					System.out.println("SAVE DONE!");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private IExtractor chooseExtractor(WebURL url){
		String dom = url.getDomain().toLowerCase();
		dom = dom.substring(0, dom.indexOf('.'));
		if(dom.matches("(docs|goo)")) 
			dom = "google";
		
		this.extractor = ExtractorFactory.getInstanceFor(dom);
		return this.extractor;
	}
}
