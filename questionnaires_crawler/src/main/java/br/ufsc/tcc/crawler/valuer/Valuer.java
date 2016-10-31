package br.ufsc.tcc.crawler.valuer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.parser.HtmlParseData;

public class Valuer {
	
	/**
	 * Algumas frases que podem aparecer em sites com listas de questionários.
	 */
	private static final Pattern SURVEY_PHRASES_REGEX = Pattern.compile(
			"(.*surveys?\\s+(templates?|samples?|examples?).*|"
			+ ".*(templates?|samples?|examples?)\\s+(of\\s+)?surveys?.*|"
			+ ".*questionnaires?\\s+(templates?|samples?|examples?).*|"
			+ ".*(templates?|samples?|examples?)\\s+(of\\s+)?questionnaires?.*|"
			+ ".*(modelos?|exemplos?|amostras?)\\s+(de|para)?\\s+pesquisas?.*|"
			+ ".*(modelos?|exemplos?|amostras?)\\s+(de|para)?\\s+question(a|á)rios?.*)", 
			Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
	/**
	 * Algumas palavras que podem aparecer em sites com questionários.
	 */
	private static final Pattern SURVEY_WORDS_REGEX = Pattern.compile("(.*surveys?.*|.*questionnaires?.*|"
			+ ".*question(a|á)rios?.*|.*pesquisas?.*|.*testes?\\s+para.*)", 
			Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
	/**
	 * Um regex para tentar identificar perguntas de um questionários. </br>
	 * Este regex apenas identifica enumerações, como por exemplo:
	 * 
	 * <pre>      1. Como você se sente?
	 *      1) Você concorda com:
	 *      1 - Qualidade dos recursos.</pre>
	 * 
	 * Explicação do regex:
	 * 
	 * <pre>      (                     -- Inicio do regex
	 *      (\\d{1,2}             -- 1 ou 2 numeros
	 *      (\\s{1,2})?           -- 1 ou 2 espaços em branco, opcional
	 *      (\\.|\\:|\\)|\\-)?    -- Caracteres . ou : ou ) ou -, opcional
	 *      )                     -- Todo o grupo acima é obrigatório [enumeração]
	 *      [^\\?\\:\r\n]+        -- Qualquer numero de caracteres que não seja ? ou : ou fim de linha
	 *      (\\?|\\:|\\.)?        -- Caracteres ? ou : ou ., opcional
	 *      )                     -- Fim do regex</pre>
	 */
	private static final Pattern QUESTIONS_REGEX_ENUM = Pattern.compile(
			"((\\d{1,2}(\\s{1,2})?(\\.|\\:|\\)|\\-)?)[^\\?\\:\r\n]+(\\?|\\:|\\.)?)"
			);
	/**
	 * Um regex mais geral/genérico para tentar identificar perguntas de um questionário.
	 * Ele será utilizado em FORMs e TABLEs para tentar pegar questionários mais 'frouxos'.</br>
	 * 
	 * Explicação do regex:
	 * 
	 * <pre>      (                 -- Inicio da primeira parte do regex
	 *      ...               -- Esta parte inicial é a mesma explicada em {@link Valuer#QUESTIONS_REGEX_ENUM}
	 *      )                 -- Fim da primeira parte do regex
	 *      |                 -- Atua como o operador booleano OR
	 *      (                 -- Inicio da segunda parte do regex
	 *      [^\\?\\:\r\n]+    -- Qualquer numero de caracteres que não seja ? ou : ou fim de linha
	 *      (\\?|\\:)         -- Caracteres ? ou :
	 *      )                 -- Fim da segunda parte do regex</pre>
	 */
	private static final Pattern QUESTIONS_REGEX_GERAL = Pattern.compile(
			"((\\d{1,2}(\\s{1,2})?(\\.|\\:|\\)|\\-)?)[^\\?\\:\r\n]+(\\?|\\:|\\.)?)|"
			+ "([^\\?\\:\r\n]+(\\?|\\:))"
			);
	
	/**
	 * Numero minimo de elementos, {@link Valuer#elemsToCheck}, 
	 * para dizer que um site possui um questionário.
	 */
	private static final int minNumOfElems = 3;
	/**
	 * Elementos para se procurar em um site para verificar
	 * se possui um questionário.
	 */
	private static final String[] elemsToCheck = {
		"input:not([type=hidden])",
		"textarea",
		"select"
	};
	/**
	 * Numero minimo de perguntas que deve ser encontrado 
	 * em um site para dizer que ele tem um questionário.
	 */
	private static final int minNumOfQuestions = 3;
	
	/**
	 * Verifica se o site com o HTML passado deve ser salvo no banco de dados
	 * como um possivel questionário ou não.
	 * 
	 * @param htmlParseData		HTML que se quer testar.
	 * @return					<b>TRUE</b> caso o HTML passado pareça ser um questionário, 
	 * 							ou lista de questionários, ou</br>
	 * 							<b>FALSE</b> caso contrario.
	 */
	public static boolean shouldSave(HtmlParseData htmlParseData){
		if(htmlParseData.getTitle() == null) return false;

		// Verifica se o titulo da pagina contém o SURVEY_PHRASES_REGEX
		String title = htmlParseData.getTitle().toLowerCase();
		if(SURVEY_PHRASES_REGEX.matcher(title).matches()){
			System.out.println("TITLE MATCHES");
			return true;
		}
		
		String html = htmlParseData.getHtml();
		Document doc = Jsoup.parse(html);
		Elements elems = doc.select("form"),
				tmp = null;
		
		// Primeiro tenta olhar só os FORMs
		if(!elems.isEmpty()){
			for(Element form : elems){
				if(hasMinQuestionsAndElems(form, QUESTIONS_REGEX_GERAL))
					return true;
			}
		}
		
		// Depois tenta verificar TABLEs
		elems = doc.select("table > tbody");
		if(!elems.isEmpty()){
			int count;
			for(Element table : elems){
				count = 0;
				if(hasMinQuestionsAndElems(table, QUESTIONS_REGEX_GERAL))
					return true;
				
				// Verifica as trs para tentar achar o padrão:
				// <td>Pergunta</td><td>Opções</td>
				tmp = table.select("tr");
				for(Element tr : tmp){
					if(tr.textNodes().size() >= 1)
						count += getCountOfElems(tr);
				}
				
				System.out.println("COUNT [TABLE>TRs]: " +count);
				if(count >= minNumOfElems)
					return true;
			}
		}
		
		// E em ultima instancia faz uma busca geral no BODY,
		// faz uma verificação por palavras também para tentar
		// filtrar melhor
		elems = doc.select("body");
		if(hasMinQuestionsAndElems(elems.get(0), QUESTIONS_REGEX_ENUM) &&
				(SURVEY_WORDS_REGEX.matcher(title).matches() || 
				 SURVEY_WORDS_REGEX.matcher(html).matches()))
			return true;
		
		return false;
	}
	
	/**
	 * Verifica se o elemento passado possui o numero minimo de perguntas
	 * e elementos.
	 * 
	 * @param elem		Elemento que se quer fazer a checagem.
	 * @return			<b>TRUE</b> caso o elemento possua no minimo {@link Valuer#minNumOfQuestions} de perguntas e
	 * 					{@link Valuer#minNumOfElems} de elementos,</br>
	 * 					<b>FALSE</b> caso contrario.
	 */
	private static boolean hasMinQuestionsAndElems(Element elem, Pattern patt){
		int count = 0;
		Matcher m = patt.matcher(elem.text());	
		while(m.find()){ count++; }
		
		if(count >= minNumOfQuestions){
			count = getCountOfElems(elem);
			System.out.println("COUNT ["+elem.tagName()+"]: " +count);
			if(count >= minNumOfElems)
				return true;
		}
		return false;
	}
	
	/**
	 * Conta o numero de elementos da lista {@link Valuer#elemsToCheck} que o 
	 * elemento passado possue.
	 * 
	 * @param elem			Elemento que se quer checar.
	 * @return				O numero de elementos que o elemento passado possue.
	 */
	private static int getCountOfElems(Element elem){
		Elements tmp = null;
		int count = 0;
		
		for(String e : elemsToCheck){
			tmp = elem.select(e);
			count += tmp.size();
		}
		
		return count;
	}
}
