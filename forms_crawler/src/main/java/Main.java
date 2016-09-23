import java.util.Arrays;

import clawler.ClawlerController;

public class Main {
	
	public static void main(String[] args) {
		ClawlerController controller = null;
		/*
		 * Dominios permitidos no Crawler.
		 */
		String[] domains = {
			"http://www.survio.com/br/modelos-de-pesquisa",
			"https://www.survio.com/modelo-de-pesquisa",
			"http://www.survio.com/modelo-de-pesquisa",
			"http://www.faculdadeages.com.br",
			"https://docs.google.com",
			"http://vark-learn.com",
			"https://www.onlinepesquisa.com",
			"http://www.opinionbox.com",
			"http://plataforma.opinionbox.com/"
		};
		
		try{
			if (args.length == 2){ 
				controller = new ClawlerController(args[0], Integer.parseInt(args[1]));
				System.out.println("Inicializando com os parametros: " +Arrays.toString(args)+ ".");
			}else{
		    	controller = new ClawlerController();
		    	System.out.println("Inicializando com os parametros padrao.");
			}
			
			controller.setDomains(domains);
			controller.addSeed("http://www.survio.com/br/modelos-de-pesquisa");
			//controller.addSeed("http://www.faculdadeages.com.br/uniages/questionarios-cpa/");
			//controller.addSeed("http://vark-learn.com/the-vark-questionnaire/");
			//controller.addSeed("https://www.onlinepesquisa.com/s/8b456d4");
			//controller.addSeed("http://www.opinionbox.com/plataforma-de-pesquisa/questionarios/");
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
