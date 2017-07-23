package br.ufsc.tcc.extractor.extractor.impl;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.PerguntaExtractor;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class MultiCompExtractor implements PerguntaExtractor {

	private Questionario currentQ;
	private Pergunta currentP; 
	private RulesChecker checker;
	
	public MultiCompExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentQ = currentQ;
		this.currentP = currentP;
		this.checker = checker;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		MyNode input = null, lastInput = null;
		MyNodeType multiCompType = null;
		int i = 1;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("MULTI_COMP"));
		CommonLogger.debug("\tMulti Comp:");
		
		input = nodes.get(currentI);
		multiCompType = input.getType();
		while(input != null && input.getType() == multiCompType){
			if(lastInput != null && 
					!this.checker.areCompAndTextNear(lastInput, input))
				break;
			
			String description = input.getAttr("placeholder");
			if(description.isEmpty())
				description = ""+ (i++);
			
			Pergunta tmpPerg = new Pergunta(description);
			tmpPerg.setForma(FormaDaPerguntaManager.getForma(multiCompType.toString()));
			tmpPerg.setQuestionario(currentQ);
			this.currentP.addFilha(tmpPerg);
			CommonLogger.debug("\t\tText: {} - Comp: {}", tmpPerg.getDescricao(), input.getText());
			
			lastInput = input;
			if(currentI+1 < nodes.size())
				input = nodes.get(++currentI);
			else
				input = null;
		}
		//Se input != null então o loop passo da pergunta e entro na proxima e por isso,
		//deve-se voltar o index para o final da pergunta
		if(input != null)
			--currentI;
		//Se não tem nenhuma filha/alternativa quer dizer que o loop não
		//completo nenhuma vez
		if(this.currentP.getFilhas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

}
