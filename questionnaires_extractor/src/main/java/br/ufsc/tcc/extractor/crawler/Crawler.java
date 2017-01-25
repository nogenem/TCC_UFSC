package br.ufsc.tcc.extractor.crawler;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.crawler.builder.QuestionarioBuilder;
import br.ufsc.tcc.extractor.database.manager.QuestionarioManager;
import br.ufsc.tcc.extractor.model.Questionario;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class Crawler extends WebCrawler {
	
	private BasicConnection conn;
	private QuestionarioManager qManager;
	private PossivelQuestionarioManager pqManager;
	private QuestionarioBuilder qBuilder;
	
	@Override
	public void onStart() {
		this.conn = new PostgreConnection(ProjectConfigs.getExtractorDatabaseConfigs());
		this.qManager = new QuestionarioManager(this.conn);
		this.pqManager = new PossivelQuestionarioManager(this.conn);
		this.qBuilder = new QuestionarioBuilder();
	}
	
	@Override
	public void onBeforeExit() {
		if(this.conn != null)
			this.conn.close();
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
//					this.pqManager.remove(link);
					
					CommonLogger.debug("SAVE DONE!");
				}
				questionarios.clear();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
