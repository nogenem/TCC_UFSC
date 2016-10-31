package br.ufsc.tcc.extractor.main;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.crawler.CrawlerController;
import br.ufsc.tcc.extractor.crawler.Crawler;

public class Main {

	public static void main(String[] args) {
		CrawlerController controller = null;
		
		// Dominios permitidos no Crawler [sem http].
		String[] domains = {
			"www.survio.com/br/modelos-de-pesquisa",
			"www.survio.com/modelo-de-pesquisa",
			"www.faculdadeages.com.br/uniages/questionarios-cpa/",
			"docs.google.com",
			"goo.gl",
			"vark-learn.com/the-vark-questionnaire/",
			"www.saiadoescuro.pt/questionario/7.htm",
			"reisearch.eu/survey/index.php/388449",
			"www.galenoalvarenga.com.br/testes-psicologicos/",
			"www.agendor.com.br/blog/questionario-de-pesquisa-de-clima-organizacional/",
			"www.estilosdeaprendizaje.es/chaea/chaeagrafp2.htm",
			"anpei.tempsite.ws/intranet/mediaempresa/",
			"www.institutoverweb.com.br/limesurvey/index.php?sid=39941",
			"www.almaderma.com.br/produtos_floral_questionario.php",
			"www.almaderma.com.br/formulario/florais/",
			"www.hoteljardinsdajuda.com/question%C3%A1rio.aspx?ID=17",
			"www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp"
		};
		
		// Cria um regex a partir dos dominios
		// [acredito que regex seja mais rapido que percorrer a lista 
		//  a cada url encontrada pelo crawler]
		String regex = "^(";
		for(String dom : domains){
			regex += dom + "|";
		}
		regex = regex.substring(0, regex.length()-1) + ").*";
		
		// Cria o Controller do Crawler, adiciona as Seeds e inicia o Crawler
		try{
			controller = new CrawlerController(ProjectConfigs.getCrawlerConfigs());
			
			controller.setCustomData(new String[]{regex});
			
			// Seeds ja terminadas
			controller.addSeed("http://www.survio.com/br/modelos-de-pesquisa");
			
			// Seeds ainda em desenvolvimento
			
			// Inicia o crawling
			controller.start(Crawler.class);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
