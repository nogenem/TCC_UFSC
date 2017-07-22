package br.ufsc.tcc.extractor.main;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.crawler.CrawlerController;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.extractor.builder.QuestionarioBuilder;
import br.ufsc.tcc.extractor.crawler.Crawler;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.database.manager.QuestionarioManager;

public class Main {
	private static String configsPath = "./extractor_configs.json";
	
	public static void main(String[] args) {		
		// Carrega as configurações do projeto
		System.out.println("Carregando arquivo de configuracao...");
		ProjectConfigs.loadConfigs(configsPath);
		
//		DeweyExt d1 = new DeweyExt("001.001.015.002.004.001.002.001.003.001.001.001.001.001.001.001.001.002.001.001"),
//				d2 = new DeweyExt("001.001.015.002.004.001.002.001.003.001.001.001.001.001.001.001.002.002.001.001.001.001.001.001"),
//				d3 = d1.distanceOf(d2);
//		System.out.println(d3);
		
		// Inicializa a aplicação
//		start();
		test();
	}
	
	//TODO remover isso ao final do desenvolvimento!
	private static void test(){
		String path = "cache/Survio_1.html";
//		path = "https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto";
//		path = "https://www.survio.com/modelo-de-pesquisa/feedback-sobre-servico";
//		path = "https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop";
//		path = "https://www.survio.com/modelo-de-pesquisa/pesquisa-sobre-empregados-sobrecarregados-e-esgotados";
		
//		path = "cache/SurveyMonkey_1.html";
//		path = "cache/SurveyMonkey_2.html";
//		path = "https://www.surveymonkey.com/r/General-Event-Feedback-Template";
//		path = "https://www.surveymonkey.com/r/online-social-networking-template";
//		path = "https://www.surveymonkey.com/r/logo_testing_template"; 
//		path= "https://www.surveymonkey.com/r/CAHPS-Health-Plan-Survey-40-Template";
		
//		path = "cache/Vark-Learn_1.html";
//		path = "http://vark-learn.com/the-vark-questionnaire/";
		
//		path = "cache/Bioinfo_1.html";
//		path = "https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b";
		
//		path = "cache/Anpei_1.html";
//		path = "http://anpei.tempsite.ws/intranet/mediaempresa";
		
//		path = "cache/SurveyCrest_1.html";
//		path = "https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw";
//		path = "https://www.surveycrest.com/template_preview/pufLBGbsEEBvdJvPPxIe9hYJx0Q";
//		path = "https://www.surveycrest.com/template_preview/pcTMgau0DnNMqRJGbCqSAknAAjJA";
	
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page2.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page3.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page8.html";
		
//		path = "https://www.telstra.com.au/webforms/consumer-survey/index.cfm";
//		path = "https://docs.google.com/forms/d/e/1FAIpQLSdKNoTd6y08to45zgcXlWxCtzVEJg3irc1FbQikSS6fnyMdtQ/viewform?c=0&w=1";
//		path = "cache/exemplo.html";
		
//		path = "https://www.telstra.com.au/webforms/consumer-survey/index.cfm";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page2.html";
//		path = "cache/SurveyForBusiness1.html";
//		path = "https://www.surveyshare.com/template/380/ELearning-Student-Tracking";

		path = "https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop";
		
		BasicConnection conn = new BasicConnection(ProjectConfigs.getExtractorDatabaseConfigs());;
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
//				enableSSLSocket();
				
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
				PossivelQuestionarioManager.loadLinksAsASet();
				
				for(String seed : PossivelQuestionarioManager.getLinksAsASet()){
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
			BasicConnection conn = new BasicConnection(ProjectConfigs.getExtractorDatabaseConfigs());
			QuestionarioManager qManager = new QuestionarioManager(conn);
			if(ProjectConfigs.loadSeedsFromCrawler())
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
	
	public static void enableSSLSocket() throws KeyManagementException, NoSuchAlgorithmException {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });
 
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, new X509TrustManager[]{new X509TrustManager() {
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
 
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
 
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }}, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }
}
