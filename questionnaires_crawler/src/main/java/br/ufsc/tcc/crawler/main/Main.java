package br.ufsc.tcc.crawler.main;

import java.net.URL;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.crawler.CrawlerController;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.common.model.PossivelQuestionario;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.crawler.checker.RulesChecker;
import br.ufsc.tcc.crawler.crawler.Crawler;

public class Main {
	
	private static String outputJsonPath = "./possiveis_questionarios.json";
	private static String configsPath = "./crawler_configs.json";
	
	public static void main(String[] args) {
		// Carrega as configurações do projeto
		ProjectConfigs.loadConfigs(configsPath);
			
		// Inicializa a aplicação
		if(args.length >= 1){
			if(args[0].matches("--show-links|-sl")){
				showLinks();	
			}else if(args[0].matches("--help|-h")){
				System.out.println("Esta aplicacao pode ser inicializada com os seguintes parametros:\n"
						+ "\t--run-clawler|-rc\t\t Inicia o Crawler.\n"
						+ "\t--show-links|-sl\t\t Mostra os links encontrados pelo Crawler ate agora.\n"
						+ "\t--help|-h\t\t\t Mostra esta mensagem de ajuda.\n"
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
//			runCrawler();
			test();
		}
	}
	
	//TODO pensar em um jeito de saber que esta começando um novo questionario!!!
	private static void test() {
		String url = "http://www.survio.com/en/blog/tips-and-tricks/how-to-create-a-questionnaire-in-survio";
//		url = "https://www.surveygizmo.com/survey-examples/";
//		url = "https://www.quotev.com/surveys/Fun?v=users";
//		url = "https://www.businessformtemplate.com/preview/Blank_Survey_Template";
//		url = "http://searchdisasterrecovery.techtarget.com/tutorial/Business-impact-analysis-questionnaire-template";
//		url = "https://www.jotform.com/help/158-How-to-create-a-survey-form-and-customize-it";
//		url = "http://evaluationtoolbox.net.au/index.php?Itemid=139&id=29&option=com_rubberdoc&view=category";
//		url = "http://www.howtogeek.com/203892/how-to-create-fillable-forms-with-ms-word-2010/";
		
//		url = "https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop";
//		url = "https://www.surveymonkey.com/r/logo_testing_template";
//		url = "http://vark-learn.com/the-vark-questionnaire/";
//		url = "https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b";
//		url = "http://anpei.tempsite.ws/intranet/mediaempresa";
//		url = "https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw";
//		url = "http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html";
		
		RulesChecker checker = new RulesChecker();
		Document doc = null;
		try{
			if(url.startsWith("cache/")){
				String html = CommonUtil.readResource(url);
				doc = Jsoup.parse(html);				
			}else{
//				System.setProperty("javax.net.debug", "all");
				System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
				doc = Jsoup.connect(url)
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
			final CrawlerController controller = new CrawlerController(ProjectConfigs.getCrawlerConfigs());
			
			// Adiciona as seeds do arquivo de configuração
			JSONArray seeds = ProjectConfigs.getSeeds();
			if(seeds != null){
				seeds.forEach((seed) -> controller.addSeed((String)seed));	
			}

			// precisam de simulador
			//controller.addSeed("http://www.contenko.com/?q=survey%20template");
			//controller.addSeed("http://www.blackle.com/results/?cx=partner-pub-8993703457585266%3A4862972284&cof=FORID%3A10&ie=UTF-8&q=survey+template&sa=+#gsc.tab=0&gsc.q=survey%20template&gsc.page=1");
			//controller.addSeed("http://www.goodsearch.com/search-web?utf8=%E2%9C%93&keywords=survey+template");
			
			// Inicia o crawling
			controller.start(Crawler.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showLinks() {
		JSONObject output = new JSONObject();
		
		BasicConnection c = new PostgreConnection(ProjectConfigs.getCrawlerDatabaseConfigs());
		PossivelQuestionarioManager pqManager = new PossivelQuestionarioManager(c);
		
		try {
			// Carrega os dados do banco de dados
			ArrayList<PossivelQuestionario> pqs = pqManager.getAll();
			String key = "", tmpTxt = "";
			URL tmpUrl = null;
			JSONObject tmpObj = null;
			
			// Transforma os dados em um JSONObject
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
			
			e.printStackTrace();
			System.exit(-1);
		}
		
		//Fecha conexão com banco de dados
		c.close();
		
		// Salva o arquivo em disco e abre ele usando o editor default do sistema
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
}
