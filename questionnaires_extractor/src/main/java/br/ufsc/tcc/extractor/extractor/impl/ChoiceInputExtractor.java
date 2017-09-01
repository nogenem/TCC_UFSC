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
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class ChoiceInputExtractor implements IPerguntaExtractor {

	private Questionario currentQ;
	private Pergunta currentP; 
	private RulesChecker checker;
	
	public ChoiceInputExtractor(Questionario currentQ, Pergunta currentP, RulesChecker checker) {
		this.currentQ = currentQ;
		this.currentP = currentP;
		this.checker = checker;
	}
	
	@Override
	public int extract(Cluster desc, List<MyNode> nodes, int currentI) {
		if(checker.checkIfTextIsAbove(nodes, currentI)){
			//Ex: https://www.nbrii.com/customer-survey-questions-template/
			return this.extractWithTextAbove(desc, nodes, currentI);
		}else if(this.checker.isImageCheckboxOrRadioInput(nodes, currentI)){
			//Ex: https://survey.zoho.com/surveytemplate/Events-Entertainment%20Evaluation%20Survey [+/-]
			return this.extractWithImageOnly(nodes, currentI);
		}else
			return this.extractSimplePattern(desc, nodes, currentI);
	}

	private int extractSimplePattern(Cluster desc, List<MyNode> nodes, int currentI) {
		MyNode img = null, input = null, text = null, tmp = null, lastDescNode = desc.last();
		boolean isImgInputQuestion = false, 
				isTextImgQuestion = false,
				isImgQuestion = false;
		String txt = "", commonPrefix = "";
		
		input = nodes.get(currentI);
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma(input.getType().toString()));
		if(input.isA("CHECKBOX_INPUT"))
			CommonLogger.debug("\tCheckbox Input:");
		else
			CommonLogger.debug("\tRadio Input:");
		
		// Faz verificações sobre imagens no meio das alternativas
		img = nodes.get(currentI-1);
		text = nodes.get(++currentI);
		tmp = nodes.get(currentI+1);
		// Perguntas com imagens pode seguir o padrão: 
		//		img -> input -> text -> img -> input -> text ...
		// Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop
		isImgInputQuestion = img.isImage() && tmp.isImage();
		
		if(!isImgInputQuestion) {
			img = tmp;
			tmp = currentI+4 < nodes.size() ? nodes.get(currentI+4) : null;
			// Ou pode seguir o padrão:
			//		input -> text -> img -> input -> text -> img
			// Ex: http://www.objectplanet.com/opinio/s/s?s=259
			isTextImgQuestion = img.isImage() && tmp != null && tmp.isImage();
			if(isTextImgQuestion)
				img = nodes.get(++currentI);
			else
				img = null;
			tmp = nodes.get(currentI+1);
		}
		
		isImgQuestion = isImgInputQuestion || isTextImgQuestion;
		while(input != null &&
				(input.isA("CHECKBOX_INPUT") || input.isA("RADIO_INPUT")) &&
				text.getType() == MyNodeType.TEXT &&
				(!isImgQuestion || (img != null && img.isImage()))){
			
			if(!this.checker.areCompAndTextNear(input, text))
				break;
			
			// Verifica o prefixo comum para ver se terminou esta pergunta
			if(commonPrefix.isEmpty()) {
				commonPrefix = lastDescNode.getDewey().getCommonPrefix(input.getDewey());
			}else {
				String cTmp = lastDescNode.getDewey().getCommonPrefix(input.getDewey());
				if(!cTmp.equals(commonPrefix))
					break;
			}
			
			txt = text.getText();
			Object dono = null;
			
			if(tmp != null){
				//Ex: https://www.surveymonkey.com/r/CAHPS-Health-Plan-Survey-40-Template [pergunta 8]
				if(text.getType() == MyNodeType.TEXT && tmp.getType() == MyNodeType.TEXT && 
						this.checker.checkDistForTextsOfAlternative(text, tmp)){
					txt = txt + tmp.getText();
					++currentI;
					tmp = currentI+1 < nodes.size() ? nodes.get(currentI+1) : null;
				}
				
				CommonLogger.debug("\t\t{}", txt);
				
				if(tmp != null && (tmp.isA("TEXT_INPUT") || tmp.isA("TEXTAREA")) && 
						this.checker.areCompAndTextNear(tmp, text)){
					Pergunta tmpPerg = new Pergunta(txt);
					dono = tmpPerg;
					
					if(tmp.isA("TEXT_INPUT")) {
						tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
						CommonLogger.debug("\t\t\tCom Text Input.");
					}else {
						//Ex: http://www.surveymoz.com/s/course-evaluation-survey-example
						tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
						CommonLogger.debug("\t\t\tCom Textarea.");						
					}
					
					tmpPerg.setQuestionario(this.currentQ);
					this.currentP.addFilha(tmpPerg);
					++currentI;
				} 
			}else
				CommonLogger.debug("\t\t{}", txt);
			
			if(dono == null){
				Alternativa tmpAlt = new Alternativa(txt);
				dono = tmpAlt;
				this.currentP.addAlternativa(tmpAlt);
			}
			
			if(isImgQuestion && img != null && img.isImage()){
				Figura fig = new Figura(img.getAttr("src"), img.getAttr("alt"));
				fig.setDono(dono);
				this.currentQ.addFigura(fig);
				CommonLogger.debug("\t\t\tLegenda: {}", fig.getLegenda());
			}
			
			if(currentI+1 < nodes.size() && isImgInputQuestion)
				img = nodes.get(++currentI);
			else
				img = null;
			if(currentI+2 < nodes.size()){
				input = nodes.get(++currentI);
				text = nodes.get(++currentI);
				if(isTextImgQuestion && currentI+1 < nodes.size())
					img = nodes.get(++currentI);
				if(currentI+1 < nodes.size())
					tmp = nodes.get(currentI+1);
			}else
				input = null;
		}
		if(input != null){
			if(isImgQuestion && img != null) --currentI;
			currentI -= 2;
			if(this.currentP.getAlternativas().size() == 0)
				currentI += 1;
		}
		if(this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

	private int extractWithTextAbove(Cluster desc, List<MyNode> nodes, int currentI) {
		// A principio não se preocupa com input text
		MyNode input = null, text = null, img = null, lastDescNode = desc.last();
		boolean isImgQuestion = false;
		String txt = "", commonPrefix = "";
		
		input = nodes.get(currentI);
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma(input.getType().toString()));
		if(input.isA("CHECKBOX_INPUT"))
			CommonLogger.debug("\tCheckbox Input [text above]:");
		else
			CommonLogger.debug("\tRadio Input [text above]:");
		
		text = nodes.get(currentI-1);
		img = nodes.get(currentI-2);
		// Perguntas com imagens seguem o padrão: 
		//		img -> text -> input -> img -> text -> input ...
		isImgQuestion = img.isImage() && (nodes.get(currentI+1).isImage());
		while(input != null && 
				(input.isA("CHECKBOX_INPUT") || input.isA("RADIO_INPUT")) &&
				text.getType() == MyNodeType.TEXT &&
				(!isImgQuestion || (img != null && img.isImage()))){
			
			if(!this.checker.areCompAndTextNear(input, text))
				break;
			
			// Verifica o prefixo comum para ver se terminou esta pergunta
			if(commonPrefix.isEmpty()) {
				commonPrefix = lastDescNode.getDewey().getCommonPrefix(input.getDewey());
			}else {
				String cTmp = lastDescNode.getDewey().getCommonPrefix(input.getDewey());
				if(!cTmp.equals(commonPrefix))
					break;
			}
			
			txt = text.getText();
			CommonLogger.debug("\t\t{}", txt);
			Alternativa tmpAlt = new Alternativa(txt);
			this.currentP.addAlternativa(tmpAlt);
			
			if(isImgQuestion){
				Figura fig = new Figura(img.getAttr("src"), img.getAttr("alt"));
				fig.setDono(tmpAlt);
				this.currentQ.addFigura(fig);
				CommonLogger.debug("\t\t\tLegenda: {}", fig.getLegenda());
			}
			
			if(currentI+1 < nodes.size() && isImgQuestion)
				img = nodes.get(++currentI);
			else
				img = null;
			if(currentI+2 < nodes.size()){
				text = nodes.get(++currentI);
				input = nodes.get(++currentI);
			}else
				input = null;
		}
		
		if(input != null){
			if(isImgQuestion && img != null) --currentI;
			currentI -= 2;
		}
		if(this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

	private int extractWithImageOnly(List<MyNode> nodes, int currentI) {
		MyNode img = null, 
				input = nodes.get(currentI);
		int backup_currentI = currentI, i = 1;
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma("IMAGE_" +input.getType().toString()));
		if(input.isA("CHECKBOX_INPUT"))
			CommonLogger.debug("\tImage Checkbox Input:");
		else
			CommonLogger.debug("\tImage Radio Input:");
		
		img = nodes.get(++currentI);
		while(input != null && 
				(input.isA("CHECKBOX_INPUT") || input.isA("RADIO_INPUT")) &&
				img.isImage()){
			if(!this.checker.areCompAndTextNear(input, img))
				break;
			
			Alternativa tmpAlt = new Alternativa();
			Figura fig = new Figura(img.getAttr("src"), img.getAttr("alt"));
			
			tmpAlt.setDescricao(fig.getLegenda().isEmpty() ? i+"" : fig.getLegenda());
			currentP.addAlternativa(tmpAlt);
			
			fig.setDono(tmpAlt);
			this.currentQ.addFigura(fig);
			CommonLogger.debug("\t\t{}", fig);
			
			i++;
			if(currentI+2 < nodes.size()){
				input = nodes.get(++currentI);
				img = nodes.get(++currentI);
			}else
				input = null;
		}
		if(input != null)
			currentI -= 2;
		if(currentI == backup_currentI)//Não salvo nenhuma imagem
			this.currentP.setForma(null);
		return currentI;
	}

}
