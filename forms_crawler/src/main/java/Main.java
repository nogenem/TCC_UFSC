import java.util.Arrays;

import clawler.ClawlerController;

public class Main {
	
	public static void main(String[] args) {
		ClawlerController controller = null;
		/*
		 * Dominios permitidos no Crawler.
		 */
		String[] domains = {
			"www.survio.com/br/modelos-de-pesquisa",
			"www.survio.com/modelo-de-pesquisa",
			"www.faculdadeages.com.br/uniages/questionarios-cpa/",
			"docs.google.com",
			"goo.gl",
			"vark-learn.com/the-vark-questionnaire/",
			"www.onlinepesquisa.com",
			"www.opinionbox.com",
			"plataforma.opinionbox.com/"
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
			//controller.addSeed("http://www.survio.com/br/modelos-de-pesquisa");
			//controller.addSeed("http://www.faculdadeages.com.br/uniages/questionarios-cpa/");
			controller.addSeed("http://vark-learn.com/the-vark-questionnaire/");
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
