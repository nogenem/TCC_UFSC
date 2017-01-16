package br.ufsc.tcc.extractor.crawler.builder;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.DistanceMatrix;

public class RulesChecker {
	
	private DistanceMatrix distMatrix;
	
	public RulesChecker(DistanceMatrix distMatrix) {
		this.distMatrix = distMatrix;
	}

	public boolean shouldCreateNewQuestionario(Cluster lastDesc, MyNode nTmp) {
		if(lastDesc == null || lastDesc.isEmpty()) return false;
		Dewey dist = this.distMatrix.getDist(lastDesc.last(), nTmp);	
		//Elementos de texto dentro de um questionario não podem estar a 
		//mais de 4 elementos de altura de distancia
		return dist.getHeight() > 4;
	}

}
