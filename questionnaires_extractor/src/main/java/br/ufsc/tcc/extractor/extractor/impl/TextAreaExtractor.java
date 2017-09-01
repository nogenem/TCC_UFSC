package br.ufsc.tcc.extractor.extractor.impl;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.IPerguntaExtractor;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class TextAreaExtractor implements IPerguntaExtractor {

	private Pergunta currentP; 
	
	public TextAreaExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentP = currentP;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		CommonLogger.debug("\tTextarea");
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
		return currentI;
	}

}
