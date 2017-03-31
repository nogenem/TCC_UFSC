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
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.extractor.builder.QuestionarioBuilder;
import br.ufsc.tcc.extractor.crawler.Crawler;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.database.manager.QuestionarioManager;

public class Main {
	
	/*
		Devo remover os links dos possiveis questionarios apos a extração?
		Botar data de extração nos questionarios?
		Criar ferramenta para mostrar os questionarios extraidos? 
			Ou salvar os logs de extração?
	*/
	
	private static String configsPath = "./extractor_configs.json";
	
	public static void main(String[] args) {		
		// Carrega as configurações do projeto
		System.out.println("Carregando arquivo de configuracao...");
		ProjectConfigs.loadConfigs(configsPath);
		
//		Dewey d1 = new Dewey("001.009.001.002.001 "),
//				d2 = new Dewey("001.010.001.002.001"),
//				d3 = d1.distanceOf(d2);
//		System.out.println(d3);
//		System.out.println(d3.getWeight());
		
		// Inicializa a aplicação
//		start();
		test();
	}
	
	//TODO remover isso ao final do desenvolvimento!
	//TODO melhorar a parte de erros!
	private static void test(){
		String path = "cache/Survio_1.html";
		path = "https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto";
		//XXX distBetweenTextsInQuestionWithSubQuestions.width = 2 arruma o problema de grouping
//		path = "https://www.survio.com/modelo-de-pesquisa/feedback-sobre-servico";//TODO dar jeito no problema de grouping
//		path = "https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop";
//		path = "https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados";
		
//		path = "cache/SurveyMonkey_1.html";
//		path = "cache/SurveyMonkey_2.html";
//		path = "https://www.surveymonkey.com/r/General-Event-Feedback-Template";
//		path = "https://www.surveymonkey.com/r/online-social-networking-template";
//		path = "https://www.surveymonkey.com/r/logo_testing_template"; 
//		path= "https://www.surveymonkey.com/r/CAHPS-Health-Plan-Survey-40-Template";
		
//		path = "cache/Vark-Learn_1.html";
		//TODO tratar problema de não criar 2 questionarios / titulo ficando como group
//		path = "http://vark-learn.com/the-vark-questionnaire/";
		
//		path = "cache/Bioinfo_1.html";
		//TODO tratar texto embaixo do Email / name sem grupo (?)
		//XXX distBetweenTextsInQuestionWithSubQuestions.width = 4 arruma problema do 'Name'
//		path = "https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b";
		
//		path = "cache/Anpei_1.html";
//		path = "http://anpei.tempsite.ws/intranet/mediaempresa";
		
		//TODO tratar do Personal+general information
//		path = "cache/SurveyCrest_1.html";
		//XXX distBetweenPartsOfDescription.width = 2 arruma o problema do 1* grupo (mas estraga o assunto do Survio)
//		path = "https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw";//TODO tem login junto
//		path = "https://www.surveycrest.com/template_preview/pufLBGbsEEBvdJvPPxIe9hYJx0Q";
//		path = "https://www.surveycrest.com/template_preview/pcTMgau0DnNMqRJGbCqSAknAAjJA";
		
		//TODO fazer mais testes com esse site
		//XXX distBetweenTextsInQuestionWithSubQuestions.width = 2 arruma os problemas
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page2.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page3.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page8.html";//TODO COISA BUGADA!!!
		
		//TODO arrumar Not at all likely / novo questionario
		//XXX distBetweenPartsOfDescription.width = 4 e .maxHeight = 2 arruma problema do 'Not at...'
//		path = "https://www.telstra.com.au/webforms/consumer-survey/index.cfm";
		
//		path = "https://docs.google.com/forms/d/e/1FAIpQLSdKNoTd6y08to45zgcXlWxCtzVEJg3irc1FbQikSS6fnyMdtQ/viewform?c=0&w=1";
		
//		path = "cache/exemplo.html";
		
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
				System.setProperty("https.protocols", "TLSv1");//,TLSv1.1,TLSv1.2
				doc = Jsoup.connect(path)
					.validateTLSCertificates(false)
					.get();
			}
			Element root = doc.select("body").get(0);
			qBuilder.setCurrentLink(path);
			qBuilder.build(root);
		}catch(Exception e){
			CommonLogger.error(e);
		}
	}
	
	private static void start(){
		try{
			JSONObject configs = ProjectConfigs.getCrawlerConfigs();
			// Para o extrator o maxDepth tem que ser igual a 0 pois a
			// biblioteca de Crawler é usada apenas para percorrer as seeds
			// passadas e não para explorar ainda mais as mesmas
			configs.put("maxDepthOfCrawling", 0);
			
			final CrawlerController controller = new CrawlerController(configs);
			
			// Seeds do banco de dados
			System.out.println("Carregando seeds do banco de dados...");
			if(ProjectConfigs.loadSeedsFromCrawler()){
				//TODO rever isso...
				BasicConnection conn = new PostgreConnection(ProjectConfigs.getCrawlerDatabaseConfigs());
				PossivelQuestionarioManager.loadPossivelQuestionarioLinks(conn);
				conn.close();
				
				for(String seed : PossivelQuestionarioManager.getSavedLinks()){
					controller.addSeed(seed);					
				}
			}
			
			// Seeds do arquivo de configuração
			System.out.println("Carregando seeds do arquivo de configuracao...");
			JSONArray seeds = ProjectConfigs.getSeeds();
			if(seeds != null){
				seeds.forEach((seed) -> controller.addSeed((String)seed));	
			}
			
			//Limpando banco de dados
			System.out.println("Limpando banco de dados do extrator...");
			BasicConnection conn = new PostgreConnection(ProjectConfigs.getExtractorDatabaseConfigs());
			QuestionarioManager qManager = new QuestionarioManager(conn);
			if(ProjectConfigs.loadSeedsFromCrawler())
				qManager.cleanDatabase();
			else
				qManager.deleteLinks(seeds);
			conn.close();
			
			// Inicia o crawling
			System.out.println("Inicializando a aplicacao...");
			controller.start(Crawler.class);
			System.out.println("Encerrando a aplicacao...");
		}catch(Exception e){
			CommonLogger.error(e);
		}
	}
}
