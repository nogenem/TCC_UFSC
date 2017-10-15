package br.ufsc.tcc.extractor.main;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import br.ufsc.tcc.common.crawler.CrawlerController;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.common.util.CommonConfiguration;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.extractor.builder.QuestionarioBuilder;
import br.ufsc.tcc.extractor.crawler.Crawler;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.database.manager.QuestionarioManager;
import br.ufsc.tcc.extractor.util.Configuration;

public class Main {
	
	public static void main(String[] args) {		
		// Carrega as configurações do projeto
		System.out.println("Carregando arquivo de configuracao...");
		CommonConfiguration.setInstance(new Configuration());
		
		// Inicializa a aplicação
		start();
//		simpleTest();
	}
	
	private static void start(){
		try{
			JSONObject configs = CommonConfiguration.getInstance().getCrawlerConfigs();
			// Para o extrator o maxDepth tem que ser igual a 0 pois a
			// biblioteca de Crawler é usada apenas para percorrer as seeds
			// passadas e não para explorar ainda mais as mesmas
			configs.put("maxDepthOfCrawling", 0);
			
			final CrawlerController controller = new CrawlerController(configs);
			
			// Seeds do banco de dados
			System.out.println("Carregando seeds do banco de dados...");
			if(CommonConfiguration.getInstance().loadSeedsFromCrawler()){
				PossivelQuestionarioManager.loadLinksAsASet();
				
				for(String seed : PossivelQuestionarioManager.getLinksAsASet()){
					controller.addSeed(seed);					
				}
			}
			
			// Seeds do arquivo de configuração
			System.out.println("Carregando seeds do arquivo de configuracao...");
			JSONArray seeds = CommonConfiguration.getInstance().getSeeds();
			if(seeds != null){
				seeds.forEach((seed) -> controller.addSeed((String)seed));	
			}
			
			//Limpando banco de dados
			System.out.println("Limpando banco de dados do extrator...");
			BasicConnection conn = new BasicConnection(CommonConfiguration.getInstance().getExtractorDatabaseConfigs());
			QuestionarioManager qManager = new QuestionarioManager(conn);
			if(CommonConfiguration.getInstance().loadSeedsFromCrawler())
				qManager.cleanDatabase();
			else
				qManager.deleteLinks(seeds);
			conn.close();
			qManager = null;
			
			// Inicia o crawling
			System.out.println("Inicializando a aplicacao...");
			controller.start(Crawler.class);
			System.out.println("Encerrando a aplicacao...");
		}catch(Exception e){
			CommonLogger.error(e);
		}
	}
	
	@SuppressWarnings("unused")
	private static void simpleTest(){
		String path = "cache/Survio_1.html";
//		path = "https://www.questionpro.com/survey-templates/auto-purchase-lease-satisfaction/";

		BasicConnection conn = new BasicConnection(CommonConfiguration.getInstance().getExtractorDatabaseConfigs());;
		FormaDaPerguntaManager.loadFormas(conn);
		QuestionarioBuilder qBuilder = new QuestionarioBuilder();
		
		Document doc = null;
		try{
			if(path.startsWith("cache/")){
				String html = CommonUtil.readResource(path);
				doc = Jsoup.parse(html);				
			}else{
//				System.setProperty("javax.net.debug", "all");
				System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
				
				path = path.replaceAll("%20", " ");
				doc = Jsoup.connect(path)
					.validateTLSCertificates(false)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.82 Safari/537.36")
					.get();
			}
			Element root = doc.select("body").get(0);
			qBuilder.setCurrentLink(path);
			qBuilder.build(root, doc.title());
		}catch(Exception e){
			CommonLogger.error(e);
		}finally{
			conn.close();			
		}
	}
}
