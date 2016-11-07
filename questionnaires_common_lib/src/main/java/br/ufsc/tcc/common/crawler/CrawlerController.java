package br.ufsc.tcc.common.crawler;

import org.json.JSONObject;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlerController {
	
	private JSONObject userConfigs;
	private CrawlConfig crawlerConfig;
	private CrawlController controller;
	
	public CrawlerController() throws Exception {
		this(null);
	}
	
	public CrawlerController(JSONObject userConfigs) throws Exception {
		this.userConfigs = userConfigs==null?
				new JSONObject() : userConfigs;
		
		this.init();
	}
	
	/**
	 * Inicializa o Crawler utilizando as configurações do usuario, ou usando
	 * alguns valores padrão.  </br>
	 * Os valores padão utilizados são:
	 * 
	 * <pre>	CrawlerStorageFolder = ./tmp
	 * 	PolitenessDelay = 1000
	 * 	MaxDepthOfCrawling = -1 (sem limite)
	 * 	MaxPagesToFetch = 10000
	 * 	IncludeBinaryContentInCrawling = false
	 * 	ResumableCrawling = false
	 * 	NumberOfCrawlers = 5
	 * 	UserAgentString = "crawler4j (https://github.com/yasserg/crawler4j/)"
	 * 	Proxy.port = 8080</pre>
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		crawlerConfig = new CrawlConfig();
		// Usa as configurações vindas do arquivo json ou usa alguns valores default
		this.crawlerConfig.setCrawlStorageFolder(userConfigs.optString("crawlStorageFolder", "./tmp"));
		this.crawlerConfig.setPolitenessDelay(userConfigs.optInt("politenessDelay", 1000));
		this.crawlerConfig.setMaxDepthOfCrawling(userConfigs.optInt("maxDepthOfCrawling", -1));
		this.crawlerConfig.setMaxPagesToFetch(userConfigs.optInt("maxPagesToFetch", 10000));
		this.crawlerConfig.setIncludeBinaryContentInCrawling(userConfigs.optBoolean("includeBinaryContentInCrawling", false));
		this.crawlerConfig.setResumableCrawling(userConfigs.optBoolean("resumableCrawling", false));
		this.crawlerConfig.setUserAgentString(userConfigs.optString("useragent", "crawler4j (https://github.com/yasserg/crawler4j/)"));
		
		JSONObject proxy = userConfigs.optJSONObject("proxy");
		String tmpTxt = "";
		if(proxy != null){
			this.crawlerConfig.setProxyHost(proxy.getString("host"));
			this.crawlerConfig.setProxyPort(proxy.optInt("port", 8080));
			
			tmpTxt = proxy.optString("username");
			if(!tmpTxt.equals(""))
				this.crawlerConfig.setProxyUsername(tmpTxt);
			
			tmpTxt = proxy.optString("password");
			if(!tmpTxt.equals(""))
				this.crawlerConfig.setProxyPassword(tmpTxt);
		}
		
		PageFetcher pageFetcher = new PageFetcher(this.crawlerConfig);
	    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
	    this.controller = new CrawlController(this.crawlerConfig, pageFetcher, robotstxtServer);
	}
	
	/**
	 * Adiciona a url passada a lista de seeds, urls iniciais, do Crawler.
	 * 
	 * @param url		Url que se quer adicionar a lista de seeds do Crawler.
	 */
	public void addSeed(String url){
		this.controller.addSeed(url);
	}
	
	/**
	 * Inicia o Clawler utilizando a classe passada.
	 * 
	 * @param crawlerClass		Sua classe extendendo a classe WebCrawler.
	 */
	public void start(Class<? extends WebCrawler> crawlerClass){
		this.controller.start(crawlerClass, userConfigs.optInt("numberOfCrawlers", 5));
	}
	
	/**
	 * Seta os dados que serão passados para todas as threads que farão o Crawling.
	 * 
	 * @param data		Dado que será passado as threads.
	 */
	public void setCustomData(String[] data){
		this.controller.setCustomData(data);
	}
}
