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
	 * Os valores padão utilizados são: </br>
	 * &nbsp;&nbsp;&nbsp;  CrawlerStorageFolder = ./tmp </br>
	 * &nbsp;&nbsp;&nbsp;  PolitenessDelay = 1000 </br>
	 * &nbsp;&nbsp;&nbsp;  MaxDepthOfCrawling = -1 (sem limite) </br>
	 * &nbsp;&nbsp;&nbsp;  MaxPagesToFetch = 10000 </br>
	 * &nbsp;&nbsp;&nbsp;  IncludeBinaryContentInCrawling = false </br>
	 * &nbsp;&nbsp;&nbsp;  ResumableCrawling = false </br>
	 * &nbsp;&nbsp;&nbsp;  NumberOfCrawlers = 5 </br>
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
		
		PageFetcher pageFetcher = new PageFetcher(this.crawlerConfig);
	    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
	    this.controller = new CrawlController(this.crawlerConfig, pageFetcher, robotstxtServer);
	}
	
	public void addSeed(String url){
		this.controller.addSeed(url);
	}
	
	public void start(Class<? extends WebCrawler> crawlerClass){
		this.controller.start(crawlerClass, userConfigs.optInt("numberOfCrawlers", 5));
	}
	
	public void setCustomData(String[] data){
		this.controller.setCustomData(data);
	}
}
