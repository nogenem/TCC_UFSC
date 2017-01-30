package br.ufsc.tcc.extractor.main;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.crawler.CrawlerController;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.extractor.crawler.Crawler;
import br.ufsc.tcc.extractor.crawler.builder.QuestionarioBuilder;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;

public class Main {
	
	private static String configsPath = "./extractor_configs.json";
	
	public static void main(String[] args) {
		// Carrega as configurações do projeto
		ProjectConfigs.loadConfigs(configsPath);
		
		// Inicializa a aplicação
		test();
	}
	
	//TODO remover isso ao final do desenvolvimento!
	//TODO melhorar a parte de erros!
	private static void test(){
		String path = "cache/Survio_1.html";
		path = "https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto";
//		path = "https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop";
//		path = "https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados";
		
//		path = "cache/SurveyMonkey_1.html";
//		path = "cache/SurveyMonkey_2.html";
//		path = "https://www.surveymonkey.com/r/General-Event-Feedback-Template";
//		path = "https://www.surveymonkey.com/r/online-social-networking-template";
//		path = "https://www.surveymonkey.com/r/logo_testing_template"; 
		
//		path = "cache/Vark-Learn_1.html";
		//TODO tratar problema de não criar 2 questionarios / titulo ficando como group
//		path = "http://vark-learn.com/the-vark-questionnaire/";
		
//		path = "cache/Bioinfo_1.html";
		//TODO tratar texto embaixo do Email (?)
//		path = "https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b";
		
//		path = "cache/Anpei_1.html";
//		path = "http://anpei.tempsite.ws/intranet/mediaempresa";
		
		//TODO tratar do Personal+general information
//		path = "cache/SurveyCrest_1.html";
//		path = "https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw";
//		path = "https://www.surveycrest.com/template_preview/pufLBGbsEEBvdJvPPxIe9hYJx0Q";
//		path = "https://www.surveycrest.com/template_preview/pcTMgau0DnNMqRJGbCqSAknAAjJA";
		
		//TODO fazer mais testes com esse site
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page2.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page3.html";
		
//		path = "https://www.surveygizmo.com/survey-examples/";
//		path = "http://www.createsurvey.com/demo/templates.htm";
		
		BasicConnection conn = new PostgreConnection(ProjectConfigs.getExtractorDatabaseConfigs());;
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
				doc = Jsoup.connect(path)
					.validateTLSCertificates(false)
					.get();
			}
			Element root = doc.select("body").get(0);
			qBuilder.build(root);
		}catch(Exception e){
			CommonLogger.error(e);
		}
	}
	
	private static void start(){
		// Cria o Controller do Crawler, adiciona as Seeds e inicia o Crawler
		try{
			JSONObject configs = ProjectConfigs.getCrawlerConfigs();
			// Para o extrator o maxDepth tem que ser igual a 0 pois a
			// biblioteca de Crawler é usada apenas para percorrer as seeds
			// passadas e não para explorar ainda mais as mesmas
			configs.put("maxDepthOfCrawling", 0);
			
			final CrawlerController controller = new CrawlerController(configs);
			
			// Seeds do banco de dados
			if(ProjectConfigs.loadUrlsFromCrawler()){
				//TODO rever isso...
				BasicConnection conn = new PostgreConnection(ProjectConfigs.getCrawlerDatabaseConfigs());
				PossivelQuestionarioManager.loadPossivelQuestionarioLinks(conn);
				conn.close();
				conn = null;
				
				for(String seed : PossivelQuestionarioManager.getSavedLinks()){
					controller.addSeed(seed);					
				}
			}
			
			// Seeds do arquivo de configuração
			JSONArray seeds = ProjectConfigs.getSeeds();
			if(seeds != null){
				seeds.forEach((seed) -> controller.addSeed((String)seed));	
			}
			
			// Inicia o crawling
			controller.start(Crawler.class);
			
			// Fecha a conexão com o banco de dados do Crawler
			Crawler.closePqConnection();
		}catch(Exception e){
			CommonLogger.error(e);
		}
	}
}
