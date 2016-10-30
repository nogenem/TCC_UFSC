import java.util.Arrays;

import clawler.ClawlerController;

public class Main {
	
	public static void main(String[] args) {
		ClawlerController controller = null;
		
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
		String regex = "^(";
		for(String dom : domains){
			regex += dom + "|";
		}
		regex = regex.substring(0, regex.length()-1) + ").*";
		
		try{
			if (args.length == 2){ 
				controller = new ClawlerController(args[0], Integer.parseInt(args[1]));
				System.out.println("Inicializando com os parametros: " +Arrays.toString(args)+ ".");
			}else{
		    	controller = new ClawlerController();
		    	System.out.println("Inicializando com os parametros padrao.");
			}
			
			controller.setDomains(new String[]{regex});
			// Ja terminados
			/*controller.addSeed("http://www.survio.com/br/modelos-de-pesquisa");
			controller.addSeed("http://www.faculdadeages.com.br/uniages/questionarios-cpa/");
			controller.addSeed("http://vark-learn.com/the-vark-questionnaire/");
			controller.addSeed("https://www.saiadoescuro.pt/questionario/7.htm");
			controller.addSeed("http://reisearch.eu/survey/index.php/388449?lang=pt");	
			controller.addSeed("http://www.galenoalvarenga.com.br/?s=Teste+para&submit=Pesquisar");	
			controller.addSeed("http://www.galenoalvarenga.com.br/page/2?s=Teste+para&submit=Pesquisar");	
			controller.addSeed("http://www.galenoalvarenga.com.br/page/3?s=Teste+para&submit=Pesquisar");	
			controller.addSeed("http://www.agendor.com.br/blog/questionario-de-pesquisa-de-clima-organizacional/");	
			controller.addSeed("http://www.estilosdeaprendizaje.es/chaea/chaeagrafp2.htm");	
			controller.addSeed("http://www.almaderma.com.br/produtos_floral_questionario.php");				
			controller.addSeed("http://www.hoteljardinsdajuda.com/question%C3%A1rio.aspx?ID=17");							
			controller.addSeed("https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b");	*/
			
			// Ainda em desenvolvimento
			//controller.addSeed("http://www.institutoverweb.com.br/limesurvey/index.php?sid=39941");	
			//controller.addSeed("http://anpei.tempsite.ws/intranet/mediaempresa/");	
			
			controller.start();
		}catch(NumberFormatException | IndexOutOfBoundsException e){
			System.err.println("Parametros necessarios: ");
			System.err.println("\t rootFolder (ira conter os dados intermediarios do crawler)");
		    System.err.println("\t numberOfCralwers (numero de threads concorrentes)");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

}
