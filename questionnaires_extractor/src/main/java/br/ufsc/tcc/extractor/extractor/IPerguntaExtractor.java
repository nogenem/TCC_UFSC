package br.ufsc.tcc.extractor.extractor;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;

public interface IPerguntaExtractor {
	
	public int extract(Cluster desc, List<MyNode> nodes, int currentI);
	
}
