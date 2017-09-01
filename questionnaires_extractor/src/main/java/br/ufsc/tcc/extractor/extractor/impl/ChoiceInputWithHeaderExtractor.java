package br.ufsc.tcc.extractor.extractor.impl;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.IPerguntaExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class ChoiceInputWithHeaderExtractor implements IPerguntaExtractor {

	private Pergunta currentP; 
	
	public ChoiceInputWithHeaderExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentP = currentP;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		MyNode input = nodes.get(currentI);
		String type = input.getType().toString();
		Cluster head = desc;
		int j = 0;

		this.currentP.setForma(FormaDaPerguntaManager.getForma(type));
		if(input.isA("CHECKBOX_INPUT"))
			CommonLogger.debug("\tCheckbox Input [with header]:");
		else
			CommonLogger.debug("\tRadio Input [with header]:");
		
		while(input != null && input.isA(type) && j < head.size()){
			String text = head.get(j).getText();
			CommonLogger.debug("\t\t{}", text);
			
			Alternativa alt = new Alternativa(text);
			this.currentP.addAlternativa(alt);
			
			if(currentI+1 < nodes.size())
				input = nodes.get(++currentI);
			else
				input = null;
			j++;
		}
		if(input != null)
			currentI--;
		return currentI;
	}

}
