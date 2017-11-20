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
	private CrawlConfig configs;
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
	 * Inicializa o Crawler utilizando as configurações do usuário, ou usando
	 * alguns valores padrão.  </br>
	 * Os valores padrão utilizados são: </br>
	 * <ul>
	 * 	<li>CrawlerStorageFolder = ./tmp</li>
	 * 	<li>PolitenessDelay = 1000</li>
	 * 	<li>MaxDepthOfCrawling = -1 (sem limite)</li>
	 * 	<li>MaxPagesToFetch = 10000</li>
	 * 	<li>IncludeBinaryContentInCrawling = false</li>
	 * 	<li>ResumableCrawling = false</li>
	 * 	<li>NumberOfCrawlers = 5</li>
	 * </ul>
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		configs = new CrawlConfig();
		// Usa as configurações vindas do arquivo json ou usa alguns valores default
		this.configs.setCrawlStorageFolder(userConfigs.optString("crawlStorageFolder", "./tmp"));
		this.configs.setPolitenessDelay(userConfigs.optInt("politenessDelay", 500));
		this.configs.setMaxDepthOfCrawling(userConfigs.optInt("maxDepthOfCrawling", -1));
		this.configs.setMaxPagesToFetch(userConfigs.optInt("maxPagesToFetch", 10000));
		this.configs.setIncludeBinaryContentInCrawling(userConfigs.optBoolean("includeBinaryContentInCrawling", false));
		this.configs.setResumableCrawling(userConfigs.optBoolean("resumableCrawling", false));
		
		PageFetcher pageFetcher = new PageFetcher(this.configs);
	    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	    
	    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
	    this.controller = new CrawlController(this.configs, pageFetcher, robotstxtServer);
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
