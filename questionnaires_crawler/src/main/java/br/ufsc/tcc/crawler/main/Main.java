package br.ufsc.tcc.crawler.main;

import java.net.URL;
import java.util.Set;

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
//			simpleTest();
		}
	}

	private static void runCrawler() {
		// Cria o Controller do Crawler, adiciona as Seeds e inicia o Crawler
		try {
			final CrawlerController controller = new CrawlerController(CommonConfiguration.getInstance().getCrawlerConfigs());
			
			// Adiciona as seeds do arquivo de configuração
			System.out.println("Carregando seeds do arquivo de configuracao...");
			JSONArray seeds = CommonConfiguration.getInstance().getSeeds();
			if(seeds != null){
				seeds.forEach((seed) -> controller.addSeed((String)seed));	
			}
			
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
	
	@SuppressWarnings("unused")
	private static void simpleTest() {
		String url = "https://www.businessformtemplate.com/preview/Blank_Survey_Template"; 
		
		RulesChecker checker = new RulesChecker();
		Document doc = null;
		try{
			if(url.startsWith("cache/")){
				String html = CommonUtil.readResource(url);
				doc = Jsoup.parse(html);				
			}else{
//				System.setProperty("javax.net.debug", "all");
				System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");//TLSv1,TLSv1.1,TLSv1.2
				
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
}
