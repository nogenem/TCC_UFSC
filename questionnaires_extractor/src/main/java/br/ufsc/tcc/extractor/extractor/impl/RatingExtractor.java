package br.ufsc.tcc.extractor.extractor.impl;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.PerguntaExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class RatingExtractor implements PerguntaExtractor {

	private Pergunta currentP; 
	private RulesChecker checker;
	
	public RatingExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentP = currentP;
		this.checker = checker;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		MyNode input = null, lastInput = null;
		int i = 1;
				
		currentP.setForma(FormaDaPerguntaManager.getForma("RATING"));
		CommonLogger.debug("\tRating:");
		
		input = nodes.get(currentI);
		while(input != null && input.getType() == MyNodeType.RADIO_INPUT){
			if(lastInput != null && 
					!this.checker.areCompAndTextNear(lastInput, input))
				break;
			
			Alternativa tmpAlt = new Alternativa(""+ (i++));
			this.currentP.addAlternativa(tmpAlt);
			CommonLogger.debug("\t\t{}", tmpAlt.getDescricao());
			
			lastInput = input;
			if(currentI+1 < nodes.size())
				input = nodes.get(++currentI);
			else
				input = null;
		}
		if(input != null)
			--currentI;
		if(this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

}
