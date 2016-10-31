package br.ufsc.tcc.extractor.extractor;

import br.ufsc.tcc.common.util.Util;
import edu.uci.ics.crawler4j.url.WebURL;

public class SurvioExtractor extends BasicExtractor {
	
	public SurvioExtractor(){
		String content = Util.readResource("ExtractorsConfig/SurvioConfig.json");
		this.configs = Util.parseJson(content);
	}
	
	@Override
	public boolean shouldExtract(WebURL url){
		String href = url.getURL().toLowerCase();
		return !href.startsWith("http://www.survio.com/br/modelos-de-pesquisa") && 
				!href.endsWith("?mobile=1");
	}
}
