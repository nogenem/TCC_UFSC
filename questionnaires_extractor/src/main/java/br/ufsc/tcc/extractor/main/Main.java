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
//		path = "cache/SurveyMonkey_1.html";
//		path = "cache/SurveyMonkey_2.html";
//		path = "cache/Vark-Learn_1.html";
//		path = "cache/Bioinfo_1.html";
//		path = "cache/Anpei_1.html";
//		path = "cache/SurveyCrest_1.html";
//		path = "cache/exemplo.html";
	
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page2.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page3.html";
//		path = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page8.html";
		
		
		//XXX site esta off... espero q volte... ;x
//		path = "http://surveyonics.com/Survey/motivation-and-buying-experience-survey.aspx";									
//		path = "http://surveyonics.com/SurveyCourseware/Designing-Marketing-Research-Survey.aspx";									
//		path = "http://surveyonics.com/Survey/Post-event-Survey.aspx";									

		
		//TODO pegar mais links da _v6 do link:
			//https://www.proprofs.com/survey/examples.php
		
		path = "https://www.questionpro.com/survey-templates/auto-purchase-lease-satisfaction/";
		path = "https://www.questionpro.com/survey-templates/retail-website-customer-evaluation/";
		path = "https://survey.zoho.com/surveytemplate/human%20resources-peer%20performance%20review%20survey";	
		path = "https://survey.zoho.com/surveytemplate/health%20care%20survey-smoking%20habits%20survey";	
		path = "https://survey.zoho.com/surveytemplate/human%20resources-leadership%20survey%20template";	
		path = "http://www.surveyshare.com/template/3076/OfficeClinic-Survey";										
		path = "http://www.surveyshare.com/template/3084/Emergency-Room-Visit";										
		path = "http://www.surveyshare.com/template/3074/OfficeClinic-Survey";	
		path = "http://www.surveyshare.com/template/1454/Community-Crime-Perception";	
		path = "https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto";							
		path = "https://www.survio.com/modelo-de-pesquisa/feedback-sobre-servico";									
		path = "https://www.surveymonkey.com/r/online-social-networking-template";									
		path = "https://www.surveymonkey.com/r/Personal-Hygiene-Template";									
		path = "https://www.surveymonkey.com/r/General-Internet-Usage-Template";									
		path = "https://www.surveymonkey.com/r/CAHPS-Dental-Plan-Survey-Template";		
		path = "https://www.surveyforbusiness.com/survey-industry/internet.html";									
		path = "http://cs.createsurvey.com/publish/survey?s=17&m=MM1Kro";									
		path = "http://cs.createsurvey.com/publish/survey?s=9&m=bvC7ZD";									
		path = "https://www.surveycrest.com/template_preview/pXT5Nw_AQPn48qqQ57GYpnQRsHA";									
		path = "https://www.surveycrest.com/template_preview/pZ0u5a9Kcnda1_x4Kw1pQuJK14thHQpQ";									
		path = "https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw";						
		path = "https://gallery.wufoo.com/embed/r15y6jxp139gb8f/def/embedKey=r15y6jxp139gb8f455631";									
		path = "https://gallery.wufoo.com/embed/m1v1ukx90mamwj9/def/embedKey=m1v1ukx90mamwj9188801";									
		path = "https://gallery.wufoo.com/embed/zpfozn70yydvip/def/embedKey=zpfozn70yydvip429528";									
		path = "https://gallery.wufoo.com/embed/w1qtrb451rja978/def/embedKey=w1qtrb451rja9786468";	
		path = "http://www.websurveymaster.com/t/16/Samples";									
		path = "http://www.websurveymaster.com/t/14/Samples";									
		path = "https://www.smartsurvey.co.uk/s/customer-service-feedback-template?sample-provider=true";									
		path = "https://www.smartsurvey.co.uk/s/evaluation-of-company-and-supervisor-template?sample-provider=true";									
		path = "https://www.smartsurvey.co.uk/s/evaluation-of-job-template?sample-provider=true";									
		path = "http://www.123contactform.com/js-form--37173.html";									
		path = "http://www.123contactform.com/js-form--1346991.html";									
		path = "http://www.123contactform.com/js-form--37862.html";									
		path = "https://www.proprofs.com/survey/t/?title=okgaw&type=template";
		path = "https://www.proprofs.com/survey/t/?title=xl7oc&type=template";
		path = "https://www.proprofs.com/survey/t/?title=teacher-feedback-survey";
		path = "https://survey.com.br/preview/healthcare-patient-feedback?template=true";
		path = "https://survey.com.br/preview/job-application-web-form-2?template=true";
		path = "https://survey.com.br/preview/market-research-tourism-target-customers?template=true";
		path = "https://www.surveyrock.com/template/sample-nonprofit-membership-survey-template-1956";
		path = "https://www.surveyrock.com/template/sample-campus-issues-survey-template-1843";
		path = "https://www.surveyrock.com/template/sample-employee-performance-evaluation-survey-template-2008";
		path = "http://www.surveyexpression.com/Survey.aspx?id=d3f09b77-831a-47ba-907c-ae34368bfd80";
		path = "http://www.surveyexpression.com/Survey.aspx?id=5bd74ae1-3d2c-49c1-9df0-9e43a8f580c0";
		
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
