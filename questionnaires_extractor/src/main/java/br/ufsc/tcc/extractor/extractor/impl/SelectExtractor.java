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

public class SelectExtractor implements IPerguntaExtractor {

	private Pergunta currentP; 
	private RulesChecker checker;
	
	public SelectExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentP = currentP;
		this.checker = checker;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		MyNode opt = null, text = null;
		
		if(currentI+2 >= nodes.size()) return currentI;
		
		if(checker.isSelectGroup(nodes, currentI)){
			currentP.setForma(FormaDaPerguntaManager.getForma("SELECT_GROUP"));
			CommonLogger.debug("\tSelect Group:");
			return this.extractSelectGroup(nodes, currentI);
		}
		
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		CommonLogger.debug("\tSelect:");
		
		opt = nodes.get(++currentI);
		text = nodes.get(++currentI);
		while(opt != null && opt.isA("OPTION")){
			if(text.isA("OPTION")){
				if(currentI+1 < nodes.size()){
					opt = text;
					text = nodes.get(++currentI);
					continue;
				}else{
					opt = null;
					break;
				}
			}
				
			if(!this.checker.areCompAndTextNear(opt, text))
				break;
			
			Alternativa tmpAlt = new Alternativa(text.getText());
			this.currentP.addAlternativa(tmpAlt);
			CommonLogger.debug("\t\t{}", tmpAlt.getDescricao());
			
			if(currentI+2 < nodes.size()){
				opt = nodes.get(++currentI);
				text = nodes.get(++currentI);
			}else
				opt = null;
		}
		if(opt != null) {
			//Ex: http://www.123contactform.com/js-form--37229.html [perg 8]
			if(opt.isA("OPTION"))
				currentI -= 1;
			else
				currentI -= 2;
		}
		if(this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}
	
	private int extractSelectGroup(List<MyNode> nodes, int currentI) {
		MyNode opt = null, text = null;
		Pergunta p = new Pergunta((currentP.getFilhas().size()+1) +"");
		p.setPai(currentP);
		
		p.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		CommonLogger.debug("\t\tSelect:");
		
		opt = nodes.get(++currentI);
		text = nodes.get(++currentI);
		while(opt != null && opt.isA("OPTION") &&
				(text.isA("TEXT") || text.isA("OPTION"))){		
			if(text.isA("OPTION")){
				if(currentI+1 < nodes.size()){
					opt = text;
					text = nodes.get(++currentI);
					continue;
				}else{
					opt = null;
					break;
				}
			}
				
			if(!this.checker.areCompAndTextNear(opt, text))
				break;
			
			Alternativa tmpAlt = new Alternativa(text.getText());
			p.addAlternativa(tmpAlt);
			CommonLogger.debug("\t\t\t{}", tmpAlt.getDescricao());
			
			if(currentI+2 < nodes.size()){
				opt = nodes.get(++currentI);
				text = nodes.get(++currentI);
			}else
				opt = null;
		}
		if(opt != null)
			currentI -= 2;
		if(p.getAlternativas().size() > 0)
			this.currentP.addFilha(p);
		
		if(currentI+2 < nodes.size()){
			text = nodes.get(currentI+1);
			if(text.isText()){
				if(text.getText().matches(RulesChecker.DATE_REGEX1))
					currentI++;
				else if(text.getText().matches("(?i)"+RulesChecker.DATE_REGEX2)){
					p.setDescricao(text.getText());
					currentI++;
				}
			}
			opt = nodes.get(currentI+1);
			if(opt.isA("SELECT")){
				currentI++;
				return this.extractSelectGroup(nodes, currentI);
			}
		}
		
		if(this.currentP.getFilhas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

}
