package clawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class ClawlerController {
	
	private String crawlStorageFolder = "./tmp";
	private int numberOfCrawlers = 5;
	
	private CrawlController controller;
	private CrawlConfig config;
	
	public ClawlerController(String folder, int number, 
			CrawlConfig config) throws Exception {
		this.crawlStorageFolder = folder;
		this.numberOfCrawlers = number;
		this.config = config;
		
		this.init();
	}
	
	public ClawlerController(String folder, int number) throws Exception {
		this(folder, number, null);
	}
	
	public ClawlerController(CrawlConfig config) throws Exception {
		this.config = config;
		
		this.init();
	}
	
	public ClawlerController() throws Exception {
		this.init();
	}
	
	private void init() throws Exception {
		if(this.config == null){
			this.config = new CrawlConfig();
			
			// Configurações default
			this.config.setCrawlStorageFolder(this.crawlStorageFolder);
			this.config.setPolitenessDelay(1000);
			this.config.setMaxDepthOfCrawling(-1);
			this.config.setMaxPagesToFetch(1000);
			this.config.setIncludeBinaryContentInCrawling(false);
			this.config.setResumableCrawling(false);
		}
		
		PageFetcher pageFetcher = new PageFetcher(this.config);
	    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
	    this.controller = new CrawlController(this.config, pageFetcher, robotstxtServer);
	}
	
	public void addSeed(String url){
		this.controller.addSeed(url);
	}
	
	public void start(){
		this.controller.start(Clawler.class, this.numberOfCrawlers);
	}
	
	public void setDomains(String[] domains){
		this.controller.setCustomData(domains);
	}
	
}
