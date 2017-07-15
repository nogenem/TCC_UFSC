package br.ufsc.tcc.crawler.crawler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

import org.json.JSONObject;

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
	
	private static Pattern EXCLUDED_EXTENSIONS_REGEX;
	private static Pattern EXCLUDED_DOMAINS_REGEX;
	private static Pattern EXCLUDED_LANGUAGES_REGEX;
	
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
	protected WebURL handleUrlBeforeProcess(WebURL curURL) {
		String href = curURL.getURL();
		//TODO remover isso??
		//Pequena gambiarra para esse site especifico...
		if(href.matches(".*search\\.lycos\\.com/b(njs)?\\.php.+")) {
			href = href.substring(href.indexOf("&as=")+4, href.length()) +"/";
			try {
				href = URLDecoder.decode(href, "utf-8");
				curURL.setURL(href);
			} catch (UnsupportedEncodingException e) {}
		}
        return curURL;
    }
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL();
		
		if(!EXCLUDED_LANGUAGES_REGEX.toString().isEmpty() && 
				this.isOfExcludedLanguage(url))
			return false;
				
		// Retira o http e https
		href = href.replaceAll("^((http|https)://)", "");
		
		// Retira opções de requisições GET
		int index = href.indexOf("?");
	    if(index > 0)
	    	href = href.substring(0, index);
	    
	    // Verifica o filtro e os dominios
		return !EXCLUDED_EXTENSIONS_REGEX.matcher(href).matches() &&
				!EXCLUDED_DOMAINS_REGEX.matcher(href).matches();
	}
	
	private boolean isOfExcludedLanguage(WebURL url) {
		//Link útil: 
		//	https://en.wikipedia.org/wiki/List_of_Internet_top-level_domains
		
		//Ex: http://www.surveymonkey.de
		String tmp = url.getDomain();
		int idx = tmp.lastIndexOf('.');
		String language = tmp.substring(idx+1, tmp.length());
		
		if(EXCLUDED_LANGUAGES_REGEX.matcher(language).matches())
			return true;
		
		//Ex: http://sv.surveymonkey.com
		tmp = url.getSubDomain();
		idx = tmp.indexOf('.');
		idx = idx < 1 ? tmp.length() : idx;
		language = tmp.substring(0, idx);
		
		if(EXCLUDED_LANGUAGES_REGEX.matcher(language).matches())
			return true;
		
		//Ex: https://surveynuts.com/en
		tmp = url.getPath();
		tmp = !tmp.isEmpty() ? tmp.substring(1) : tmp;//retira o 1* '/'
		idx = tmp.indexOf('/');
		idx = idx < 1 ? tmp.length() : idx;
		language = tmp.substring(0, idx);
		
		if(EXCLUDED_LANGUAGES_REGEX.matcher(language).matches())
			return true;
		else
			return false;
	}
	
	@Override
	public void visit(Page page) {
		WebURL url = page.getWebURL();
		
		// Verifica se o link ja foi extraido
		if(PossivelQuestionarioManager.linkWasSaved(url.getURL())) return;
		
		String contentType = page.getContentType();
		if(contentType != null && contentType.contains("html") &&
				page.getParseData() instanceof HtmlParseData){
			
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();			
			if(checker.shouldSave(htmlParseData)){
				System.out.println("\t<"+Thread.currentThread().getName()+">PossivelQuestionario: " +url.getURL());
				
				PossivelQuestionario pq = new PossivelQuestionario(
						url.getURL(), htmlParseData.getTitle());
				try {
					pqManager.save(pq);
				} catch (Exception e) {
					CommonLogger.error(e);
				}
			}else{
				System.out.println("<"+Thread.currentThread().getName()+">URL: " +url.getURL());
			}
		}
	}
	
	@Override
	protected void onUnhandledException(WebURL webUrl, Throwable e){
		String urlStr = (webUrl == null ? "NULL" : webUrl.getURL());
		CommonLogger.info("Unhandled exception while fetching {}: {}", urlStr, e.getMessage());
		CommonLogger.error(e);
	}
	
	@Override
	protected void onPageBiggerThanMaxSize(String urlStr, long pageSize) {
		CommonLogger.info("Skipping a URL: {} which was bigger ( {} ) than max allowed size", urlStr, pageSize);
	}
	
	@Override
	protected void onUnexpectedStatusCode(String urlStr, int statusCode, String contentType, String description) {
		CommonLogger.info("Skipping URL: {}, StatusCode: {}, {}, {}", urlStr, statusCode, contentType, description);
	}
	
	@Override
	protected void onContentFetchError(WebURL webUrl) {
		CommonLogger.info("Can't fetch content of: {}", webUrl.getURL());
	}
	
	@Override
	protected void onParseError(WebURL webUrl) {
	    CommonLogger.info("Parsing error of: {}", webUrl.getURL());
	}
	
	@Override
	protected void onRedirectedStatusCode(Page page) {
        //Subclasses can override this to add their custom functionality
		//CommonLogger.info("Redirected to: " +page.getWebURL()); 
    }
	
	// Métodos/Blocos estáticos
	static {
		JSONObject tmp = ProjectConfigs.getCrawlerConfigs();
		EXCLUDED_EXTENSIONS_REGEX = Pattern.compile(tmp.optString("excludedFilesExtensions"));
		EXCLUDED_DOMAINS_REGEX = Pattern.compile(tmp.optString("excludedDomains"));
		EXCLUDED_LANGUAGES_REGEX = Pattern.compile(tmp.optString("excludedLanguages"));
		CommonLogger.debug("Crawler:> Static block executed!");
	}
}
