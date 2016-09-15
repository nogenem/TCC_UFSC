package extractor;

import edu.uci.ics.crawler4j.parser.HtmlParseData;
import model.Questionario;

public interface Extractor {
	
	public Questionario extract(HtmlParseData htmlParseData);
	
}
