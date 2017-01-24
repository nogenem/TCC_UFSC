package br.ufsc.tcc.crawler.crawler;

import java.util.regex.Pattern;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.common.model.PossivelQuestionario;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.crawler.checker.RulesChecker;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {
	
	private static final Pattern FILTERS = Pattern.compile(
			".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4|wav|"
			+ "avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");
	private static final Pattern EXCLUDED_DOMAINS = Pattern.compile(
			"^(.*youtube\\.com.*|.*facebook\\.com.*|"
			+ ".*twitter\\.com.*)");
	
	private BasicConnection conn;
	private PossivelQuestionarioManager pqManager;
	private RulesChecker checker;
	
	@Override
	public void onStart() {
		this.conn = new PostgreConnection(ProjectConfigs.getCrawlerDatabaseConfigs());
		this.pqManager = new PossivelQuestionarioManager(conn);
		this.checker = new RulesChecker();
	}
	
	@Override
	public void onBeforeExit() {
		if(this.conn != null)
			this.conn.close();
	}
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL();
				
		// Retira o http e https
		href = href.replaceAll("^((http|https)://)", "");
		
		// Retira opções de requisições GET
		int index = href.indexOf("?");
	    if(index > 0)
	    	href = href.substring(0, index);
	    
	    // Verifica o filtro e os dominios
		return !FILTERS.matcher(href).matches() &&
				!EXCLUDED_DOMAINS.matcher(href).matches();
	}
	
	@Override
	public void visit(Page page) {
		WebURL url = page.getWebURL();
		
		// Verifica se o link ja foi extraido
		if(PossivelQuestionarioManager.linkWasSaved(url.getURL())) return;
				
		if(page.getContentType().contains("html") &&
				page.getParseData() instanceof HtmlParseData){
			
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();			
			if(checker.shouldSave(htmlParseData)){
				CommonLogger.debug("\tPossivelQuestionario: {}", url.getURL());
				PossivelQuestionario pq = new PossivelQuestionario(
						url.getURL(), htmlParseData.getTitle());
				try {
					//pqManager.save(pq);
				} catch (Exception e) {
					CommonLogger.error(e);
				}
			}else
				CommonLogger.debug("URL: {}", url.getURL());
		}
	}
}
