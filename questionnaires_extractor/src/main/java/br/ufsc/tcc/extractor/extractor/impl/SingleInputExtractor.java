package br.ufsc.tcc.extractor.extractor.impl;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.IPerguntaExtractor;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class SingleInputExtractor implements IPerguntaExtractor {

	@SuppressWarnings("unused")
	private Questionario currentQ;
	private Pergunta currentP; 
	private RulesChecker checker;
	
	public SingleInputExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentQ = currentQ;
		this.currentP = currentP;
		this.checker = checker;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		String type = nodes.get(currentI).getType().toString();
		CommonLogger.debug("\tInput [{}].", type);
		currentP.setForma(FormaDaPerguntaManager.getForma(type));
		
		int ret = checker.checkCompositeInput(nodes, type, currentI);
		switch(ret){
		case 0://Check [ ] : [ ] (: [ ])?
			currentI += 2;
			break;
		case 1://Check ( [ ] ) [ ]
			currentI += 2;
			break;
		case 2://Check [ ] (/|-) [ ] (/|-) [ ]
			currentI += 4;
			break;
		case 3:{//Check [ ] (/|-) Month [ ] (/|-) Day [ ] Year
			FormaDaPergunta forma = FormaDaPerguntaManager.getForma(type);
			CommonLogger.debug("\tInput [{}].", type+"_GROUP");
			currentP.setForma(FormaDaPerguntaManager.getForma(type+"_GROUP"));
			
			String txt = nodes.get(currentI+2).getText();
			Pergunta p = new Pergunta(txt, forma);
			currentP.addFilha(p);
			CommonLogger.debug("\t\t{}", txt);
			
			txt = nodes.get(currentI+5).getText();
			p = new Pergunta(txt, forma);
			currentP.addFilha(p);
			CommonLogger.debug("\t\t{}", txt);
			
			txt = nodes.get(currentI+7).getText();
			p = new Pergunta(txt, forma);
			currentP.addFilha(p);
			CommonLogger.debug("\t\t{}", txt);
			
			currentI += 7;
			break;
		}case 4:{//Check [ ] Dollars . [ ] Cents
			FormaDaPergunta forma = FormaDaPerguntaManager.getForma(type);
			CommonLogger.debug("\tInput [{}].", type+"_GROUP");
			currentP.setForma(FormaDaPerguntaManager.getForma(type+"_GROUP"));
			
			String txt = nodes.get(currentI+1).getText();
			Pergunta p = new Pergunta(txt, forma);
			currentP.addFilha(p);
			CommonLogger.debug("\t\t{}", txt);
			
			txt = nodes.get(currentI+4).getText();
			p = new Pergunta(txt, forma);
			currentP.addFilha(p);
			CommonLogger.debug("\t\t{}", txt);
			
			currentI += 4;
			break;
		}
		default:
			break;
		}
		return currentI;
	}

}
