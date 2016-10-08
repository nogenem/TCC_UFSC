package extractor;

import java.util.ArrayList;

import edu.uci.ics.crawler4j.url.WebURL;
import model.Questionario;

public interface Extractor {
	
	public boolean shouldExtract(WebURL url);
	public ArrayList<Questionario> extract(String html);
	
}
