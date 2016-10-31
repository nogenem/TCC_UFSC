package br.ufsc.tcc.extractor.extractor;

import java.util.ArrayList;

import br.ufsc.tcc.extractor.model.Questionario;
import edu.uci.ics.crawler4j.url.WebURL;

public interface IExtractor {
	public boolean shouldExtract(WebURL url);
	public ArrayList<Questionario> extract(String html);
}
