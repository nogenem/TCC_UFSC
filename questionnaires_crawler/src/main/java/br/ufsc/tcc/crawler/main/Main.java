package br.ufsc.tcc.crawler.main;

import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import br.ufsc.tcc.common.crawler.CrawlerController;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.common.model.PossivelQuestionario;
import br.ufsc.tcc.common.util.CommonConfiguration;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.crawler.checker.RulesChecker;
import br.ufsc.tcc.crawler.crawler.Crawler;
import br.ufsc.tcc.crawler.util.Configuration;

public class Main {
	
	private static String outputJsonPath = "./possiveis_questionarios.json";
	
	public static void main(String[] args) {
		// Carrega as configurações do projeto
		System.out.println("Carregando arquivo de configuracao...");
		CommonConfiguration.setInstance(new Configuration());
			
		// Inicializa a aplicação
		if(args.length >= 1){
			if(args[0].matches("--show-links|-sl")){
				showLinks();	
			}else if(args[0].matches("--help|-h")){
				System.out.println("Esta aplicacao pode ser inicializada com os seguintes parametros:\n"
						+ "\t--run-clawler|-rc\t\t Inicia o Crawler.\n"
						+ "\t--show-links|-sl\t\t Mostra os links encontrados pelo Crawler ate agora.\n"
						+ "\t--help|-h\t\t Mostra esta mensagem de ajuda.\n"
						+ "Soh eh possivel executar esta aplicacao com um parametro por vez.\n"
						+ "Quando passado mais de um parametro por vez, apenas o primeiro sera executado.\n"
						+ "Quando inicializado sem nenhum parametro, a aplicacao iniciara o Crawler.");
			}else if(args[0].matches("--run-crawler|-rc")){
				runCrawler();
			}else{
				System.out.println("Parametro desconhecido: " +args[0]+ 
						"\nUse o parametro --help para obter informacoes sobre esta aplicacao.");
			}
		}else{
//			showLinks();
			runCrawler();
//			test();
		}
	}
	
	private static void test() {
		String url = "https://www.businessformtemplate.com/preview/Blank_Survey_Template"; 
//		url = "https://www.surveygizmo.com/survey-examples/";
//		url = "https://www.quotev.com/surveys/Fun?v=users";
//		url = "http://searchdisasterrecovery.techtarget.com/tutorial/Business-impact-analysis-questionnaire-template";
//		url = "https://www.jotform.com/help/158-How-to-create-a-survey-form-and-customize-it";
//		url = "http://evaluationtoolbox.net.au/index.php?Itemid=139&id=29&option=com_rubberdoc&view=category";
//		url = "http://www.howtogeek.com/203892/how-to-create-fillable-forms-with-ms-word-2010/";
//		url = "http://www.webcrawler.com/support/privacypolicy?aid=271b9f5c-942b-4c4b-befc-cb06bc0b1f49&qc=web&ridx=1";
//		url = "https://forums.zoho.com/zoho-books/uncategorised/filter/ideas/mostvoted";
		
		
//		url = "https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto";
//		url = "https://www.surveymonkey.com/r/General-Event-Feedback-Template";
//		url = "https://www.surveymonkey.com/r/logo_testing_template";
//		url = "http://vark-learn.com/the-vark-questionnaire/";
//		url = "https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b";
//		url = "http://anpei.tempsite.ws/intranet/mediaempresa";
//		url = "https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw";
//		url = "http://www.hoteljardinsdajuda.com/questionário.aspx?ID=17";
//		url = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page8.html";
		
		url = "https://www.surveyking.com/preview/employee-satisfaction.php";
		
		RulesChecker checker = new RulesChecker();
		Document doc = null;
		try{
			if(url.startsWith("cache/")){
				String html = CommonUtil.readResource(url);
				doc = Jsoup.parse(html);				
			}else{
//				System.setProperty("javax.net.debug", "all");
//				System.setProperty("http.keepAlive", "true");
				System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");//TLSv1,TLSv1.1,TLSv1.2
				
//				enableSSLSocket();
				doc = Jsoup.connect(url)
					.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36")
					.validateTLSCertificates(false)
					.get();
			}
			checker.shouldSave(doc);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static void runCrawler() {
		// Cria o Controller do Crawler, adiciona as Seeds e inicia o Crawler
		try {
			CommonLogger.setDebugEnabled(false);//TODO remover isso!!
			final CrawlerController controller = new CrawlerController(CommonConfiguration.getInstance().getCrawlerConfigs());
			
			// Adiciona as seeds do arquivo de configuração
			System.out.println("Carregando seeds do arquivo de configuracao...");
			JSONArray seeds = CommonConfiguration.getInstance().getSeeds();
			if(seeds != null){
				seeds.forEach((seed) -> controller.addSeed((String)seed));	
			}
			
			// robots.txt
//			controller.addSeed("http://www.ask.com/web?q=survey+template&qsrc=0&o=0&l=dir&qo=homepageSearchBox");
//			controller.addSeed("http://www.ask.com/web?q=questionnaires+template&qsrc=1&o=0&l=dir&qo=serpSearchTopBox");
//			controller.addSeed("https://www.webcrawler.com/serp?q=survey+template");
			
			// Testar
//			controller.addSeed("https://survey.com.br/examples");
//			controller.addSeed("https://www.surveygizmo.com/survey-examples/");
//			controller.addSeed("https://www.surveyking.com/help/survey-templates.php");
//			controller.addSeed("https://www.surveyrock.com/home/sample-survey-templates.html");
			
			controller.addSeed("http://search.lycos.com/web/?q=survey+online&pageInfo=Keywords%3Dsurvey%2520online%26xargs%3D12KPjg1sVSq5quh831MeKMQeKUgRpd1tm58854T8AuSrYHuidgUODDX5u%255F3pgqGK5q7wrGlE6gzJRada2g3KTXSwOPQVKfQKO9icqaiNYlVZ%252DhT4gTv49vn6C80t8QZztZPze8eNKfrt4%252E%26hData%3D12KPjg1o1gkMH3yLmqAs7ASOSAxl195pCy9MNpCJEPbd1a93BpUpENT5Px");
			controller.addSeed("http://search.lycos.com/web/?q=survey+online+template&pageInfo=Keywords%3Dsurvey%2520online%2520template%26xargs%3D12KPjg1sVSq5quh831MeKMQeKUgRpd1tm58854T8AuSrYHuidgUODDX5u%255F3pgqGK5q7wrGlE6gzJRada2g3KTXSwOPQVKfQKO9icqaiNYlVZ%252DhT4gTv49vn6C80t8QZztZPze8eNKfrt4%252E%26hData%3D12KPjg1o1g48b%252Dyc%252Dsc83OPeGDxigOlJDB88pofpd%252DbtMv8nNpJ%252DJ%255FT5Px");

//			controller.addSeed("http://www.ask.com/web?o=0&l=dir&qo=moreResults&q=survey+online&qsrc=467");
//			controller.addSeed("https://www.webcrawler.com/serp?q=survey+online");
			
			
//			controller.addSeed("http://search.lycos.com/web/?q=questionario+de+pesquisa&keyvol=00f83b25c31b3b36e4ba");
//			controller.addSeed("http://search.lycos.com/web/?q=survey+template&keyvol=00948a2c54764e0e566a");
//			controller.addSeed("http://search.lycos.com/web/?q=questionnaire+template&keyvol=008b07d47969f38be4a4");
//			controller.addSeed("http://search.lycos.com/web/?q=research+questionnaire&keyvol=00f8b07f7d1bb06fa01a");
			
			// off?
			//controller.addSeed("http://www.yippy.com/search?v%3Aproject=clusty-new&query=survey+template");
			//controller.addSeed("http://www.yippy.com/search?v%3Aproject=clusty-new&query=questionnaire+template");
			
			// precisam de simulador
			//controller.addSeed("http://www.contenko.com/?q=survey%20template");
			//controller.addSeed("http://www.blackle.com/results/?cx=partner-pub-8993703457585266%3A4862972284&cof=FORID%3A10&ie=UTF-8&q=survey+template&sa=+#gsc.tab=0&gsc.q=survey%20template&gsc.page=1");
			//controller.addSeed("http://www.goodsearch.com/search-web?utf8=%E2%9C%93&keywords=survey+template");
			
			// Inicia o crawling
			System.out.println("Inicializando a aplicacao...");
			controller.start(Crawler.class);
			System.out.println("Encerrando a aplicacao...");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showLinks() {
		JSONObject output = new JSONObject();
		
		BasicConnection c = new BasicConnection(CommonConfiguration.getInstance().getCrawlerDatabaseConfigs());
		PossivelQuestionarioManager pqManager = new PossivelQuestionarioManager(c, true);
		
		try {
			// Carrega os dados do banco de dados
			System.out.println("Carregando dados do banco de dados...");
			Set<PossivelQuestionario> pqs = pqManager.getAll();
			String key = "", tmpTxt = "";
			URL tmpUrl = null;
			JSONObject tmpObj = null;
			
			// Transforma os dados em um JSONObject
			System.out.println("Transformando os dados do banco de dados para o formato JSON...");
			for(PossivelQuestionario pq : pqs){
				key = pq.getEncontradoEm().toLocalDateTime().toLocalDate().toString();
				if(!output.has(key))
					output.put(key, new JSONObject());
				
				tmpObj = output.getJSONObject(key);
				tmpTxt = pq.getLink_doc();
				tmpUrl = new URL(tmpTxt);
				key = tmpUrl.getHost();
				
				if(!tmpObj.has(key))
					tmpObj.put(key, new JSONArray());
				
				tmpObj.getJSONArray(key).put(pq.getLink_doc());
			}
		} catch (Exception e) {
			//Fecha conexão com banco de dados
			c.close();
			
			CommonLogger.fatalError(e);
		}
		
		//Fecha conexão com banco de dados
		c.close();
		
		// Salva o arquivo em disco e abre ele usando o editor default do sistema
		System.out.println("Escrevendo dados no arquivo de saida...");
		if(CommonUtil.writeFile(outputJsonPath, output.toString(4))){
			if(!CommonUtil.openFile(outputJsonPath)){
				JOptionPane.showMessageDialog(null, 
						"Não foi possivel abrir o arquivo dos links, por favor tente abri-lo manualmente.\n"
						+ "Ele se encontra em: " +outputJsonPath+ ".", 
						"Ocorreu um problema!", 
						JOptionPane.ERROR_MESSAGE);
			}
		}else{
			JOptionPane.showMessageDialog(null, 
					"Não foi possivel salvar o arquivo dos links, "
					+ "por favor verifique os logs para descobrir o problema.", 
					"Ocorreu um problema!", 
					JOptionPane.ERROR_MESSAGE);
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
