package br.ufsc.tcc.extractor.crawler.builder;

import java.util.List;
import java.util.Stack;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.DistanceMatrix;
import br.ufsc.tcc.extractor.model.Questionario;

public class PerguntaBuilder {
	
	private DistanceMatrix distMatrix;
	private RulesChecker checker;
	
	public PerguntaBuilder(RulesChecker checker) {
		this.checker = checker;
		this.distMatrix = this.checker.getDistMatrix();
	}

	public int build(Questionario currentQ, List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		// TODO Auto-generated method stub
		return 0;
	}

}
