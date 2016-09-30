package extractor;

import edu.uci.ics.crawler4j.url.WebURL;
import model.Questionario;

public interface Extractor {
	
	public boolean shouldExtract(WebURL url);
	public Questionario extract(String html);
	
}
