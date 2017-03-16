package br.ufsc.tcc.extractor.crawler;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.crawler.builder.QuestionarioBuilder;
import br.ufsc.tcc.extractor.database.manager.QuestionarioManager;
import br.ufsc.tcc.extractor.model.Questionario;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {

	private BasicConnection qConn;
	private QuestionarioManager qManager;
	private QuestionarioBuilder qBuilder;
	
	@Override
	public void onStart() {
		this.qConn = new PostgreConnection(ProjectConfigs.getExtractorDatabaseConfigs());
		this.qManager = new QuestionarioManager(this.qConn);
		this.qBuilder = new QuestionarioBuilder();
	}
	
	@Override
	public void onBeforeExit() {
		if(this.qConn != null)
			this.qConn.close();
	}
	
	@Override
	public boolean shouldVisit(Page referringPage, WebURL url) {
		return true;
	}
	
	@Override
	public void visit(Page page) {
		if(page.getParseData() instanceof HtmlParseData){
			HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
			String link = page.getWebURL().getURL();
			CommonLogger.debug("Link: {}", link);
			
			Document doc = null;
			try{
				doc = Jsoup.parse(htmlParseData.getHtml());
				Element root = doc.select("body").get(0);
				
				ArrayList<Questionario> questionarios = qBuilder.build(root);
				for(Questionario q : questionarios){
					q.setLink_doc(link);
//					this.qManager.save(q);
					
					CommonLogger.debug("SAVE DONE!");
				}
				questionarios.clear();
			}catch (Exception e) {
				CommonLogger.error(e);
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
}
