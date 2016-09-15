package extractor;

import edu.uci.ics.crawler4j.parser.HtmlParseData;
import model.Questionario;

public class SurvioExtractor implements Extractor {
	
	public SurvioExtractor(){
		
	}
	
	public Questionario extract(HtmlParseData htmlParseData){
		String html = htmlParseData.getHtml();
		Questionario quest = new Questionario();
		
		// TODO: Terminar o extrator
		
		return null;
	}
}
