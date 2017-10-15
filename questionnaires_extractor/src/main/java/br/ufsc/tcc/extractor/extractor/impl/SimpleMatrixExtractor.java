package br.ufsc.tcc.extractor.extractor.impl;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.IPerguntaExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

/**
 * Classe responsável pela extração de 'simple matrices'. </br>
 * Uma 'simple matrix' é uma matriz que possui simplesmente componentes em sequencia. </br>
 * A quantidade de componentes em sequencia deve bater com a quantidade de textos no header. </br>
 * Exemplo: </br>
 * <pre>Descrição da pergunta
 *   header
 * 	
 *   descrição da primeira subpergunta
 *      sequencia de componentes (alternativas) desta subpergunta
 *   descrição da segunda subpergunta
 *      sequencia de componentes (alternativas) desta subpergunta
 *   etc...</pre> 
 *   
 * @author Gilney N. Mathias
 *
 */
public class SimpleMatrixExtractor implements IPerguntaExtractor {

	private Questionario currentQ;
	private Pergunta currentP; 
	
	public SimpleMatrixExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentQ = currentQ;
		this.currentP = currentP;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		MyNode input = null;
		MyNodeType lastCompType = null;
		Cluster lastMatrixHead = desc; 
		//Esta matriz possui componentes mistos?
		boolean isMix = false;
		int j = 0;
		
		CommonLogger.debug("\tSimple Matrix:");
		input = nodes.get(currentI);
		while(input != null && j < lastMatrixHead.size() && input.isComponent()){
			if(!isMix && lastCompType != null && lastCompType != input.getType())
				isMix = true;
			lastCompType = input.getType();
			
			String text = lastMatrixHead.get(j).getText();
			CommonLogger.debug("\t\tText: {} - Comp: {}", text, input.getText());
			
			if(lastCompType == MyNodeType.RADIO_INPUT || lastCompType == MyNodeType.CHECKBOX_INPUT){
				Alternativa alt = new Alternativa(text);
				this.currentP.addAlternativa(alt);
			}else{
				Pergunta tmpPerg = new Pergunta(text);
				tmpPerg.setForma(FormaDaPerguntaManager.getForma(lastCompType.toString()));
				tmpPerg.setQuestionario(currentQ);
				this.currentP.addFilha(tmpPerg);
			}
			
			if(currentI+1 < nodes.size())
				input = nodes.get(++currentI);
			else
				input = null;
			j++;
		}
		if(input != null)
			currentI--;
		
		if(isMix)
			this.currentP.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_GROUP"));
		else
			this.currentP.setForma(FormaDaPerguntaManager.getForma(lastCompType.toString()));
		
		return currentI;
	}

}
