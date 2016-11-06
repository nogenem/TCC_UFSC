package br.ufsc.tcc.extractor.extractor;

import br.ufsc.tcc.common.util.Util;
import edu.uci.ics.crawler4j.url.WebURL;

public class SurveyMonkeyExtractor extends BasicExtractor {
	
	//TODO como tratar sub pergunta?
	//TODO como tratar perguntas de logo?
	public SurveyMonkeyExtractor() {
		String content = Util.readResource("ExtractorsConfig/SurveyMonkeyConfig.json");
		this.configs = Util.parseJson(content);
	}
	
	@Override
	public boolean shouldExtract(WebURL url){
		String href = url.getURL().toLowerCase();
		return !href.contains("?sm=") && 
				(href.startsWith("https://www.surveymonkey.com/r/") ||
				href.startsWith("www.surveymonkey.com/s/"));
	}
}
