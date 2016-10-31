package br.ufsc.tcc.crawler.main;

import java.net.URL;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.crawler.CrawlerController;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.util.Util;
import br.ufsc.tcc.crawler.crawler.Crawler;
import br.ufsc.tcc.crawler.database.manager.PossivelQuestionarioManager;
import br.ufsc.tcc.crawler.model.PossivelQuestionario;

public class Main {
	
	private static String outputJsonPath = "./possiveis_questionarios.json";
	
	public static void main(String[] args) {		
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
			runCrawler();
		}
	}
	
	/**
	 * Inicializa e roda o Crawler.
	 */
	private static void runCrawler(){
		CrawlerController controller = null;
		JSONObject configs = ProjectConfigs.getCrawlerConfigs();
		
		try {
			controller = new CrawlerController(configs);
			
			// Adiciona as seeds
			controller.addSeed("http://www.ask.com/web?q=survey+template&qsrc=0&o=0&l=dir&qo=homepageSearchBox");
			controller.addSeed("http://www.ask.com/web?q=questionnaires+template&qsrc=1&o=0&l=dir&qo=serpSearchTopBox");
			controller.addSeed("http://www.yippy.com/search?v%3Aproject=clusty-new&query=survey+template");
			controller.addSeed("http://www.yippy.com/search?v%3Aproject=clusty-new&query=questionnaire+template");

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
	
	/**
	 * Le as informações do banco de dados, cria um JSONObject das mesmas,
	 * o salva em um arquivo em disco e tenta abri-lo utilizando o editor padrão
	 * do sistema.
	 */
	private static void showLinks(){
		//TODO criar uma classe para lidar com isso?
		JSONObject output = new JSONObject();
		
		BasicConnection c = new PostgreConnection(ProjectConfigs.getDatabaseConfigs());
		PossivelQuestionarioManager pqManager = new PossivelQuestionarioManager(c);
		
		try {
			// Carrega os dados do banco de dados
			ArrayList<PossivelQuestionario> pqs = pqManager.getAll();
			String key = "", tmpTxt = "";
			URL tmpUrl = null;
			JSONObject tmpObj = null;
			
			// Transforma os dados em um JSONObject
			for(PossivelQuestionario pq : pqs){
				key = pq.getEncontrado_em().toLocalDateTime().toLocalDate().toString();
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
			e.printStackTrace();
			System.exit(-1);
		}
		
		// Salva o arquivo em disco e abre ele usando o editor default do sistema
		if(Util.writeFile(outputJsonPath, output.toString(4))){
			if(!Util.openFile(outputJsonPath)){
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
