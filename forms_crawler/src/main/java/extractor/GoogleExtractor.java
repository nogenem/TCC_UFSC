package extractor;

import edu.uci.ics.crawler4j.url.WebURL;
import model.Questionario;

public class GoogleExtractor implements Extractor {

	@Override
	public boolean shouldExtract(WebURL url) {
		String href = url.getURL().toLowerCase();
		return href.endsWith("/viewform");
	}

	@Override
	public Questionario extract(String html) {
		// TODO Auto-generated method stub
		return null;
	}

}
