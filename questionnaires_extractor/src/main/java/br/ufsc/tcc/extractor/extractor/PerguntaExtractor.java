package br.ufsc.tcc.extractor.extractor;

import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class PerguntaExtractor {
	
	public Pergunta currentP;
	public RulesChecker checker;

	// Construtores
	public PerguntaExtractor(RulesChecker checker){
		this.checker = checker;
	}
	
	// Getters e Setters
	public void setCurrentPergunta(Pergunta current){
		this.currentP = current;
	}
	
	// Demais métodos	
	public void extractTextarea(List<MyNode> nodes) {
		CommonLogger.debug("\tTextarea");
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
	}

	public int extractGenericInput(Questionario currentQ, List<MyNode> nodes, int currentI) {
		String type = nodes.get(currentI).getType().toString();
		CommonLogger.debug("\tInput [{}].", type);
		currentP.setForma(FormaDaPerguntaManager.getForma(type));
		
		// Verifica se não tem uma imagem entre o input e a sua descrição
		//Ex: https://www.123contactform.com/js-form--37173.html [ultima pergunta]
		int i = currentI-1;
		while(nodes.get(i).isImage()){
			Figura fig = new Figura(nodes.get(i).getAttr("src"), nodes.get(i).getAttr("alt"));
			fig.setDono(this.currentP);
			currentQ.addFigura(fig);
			CommonLogger.debug("\t\tFigura da pergunta: {}", fig);
			i--;
		}
		
		int ret = checker.checkCompositeInput(nodes, type, currentI);
		switch(ret){
		case 0:
			currentI += 2;
			break;
		case 1:
			currentI += 2;
			break;
		case 2:
			currentI += 4;
			break;
		case 3:{
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
		}case 4:{
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
	
	public int extractMultiCompQuestion(List<MyNode> nodes, Questionario currentQ, int currentI) {
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

			Pergunta tmpPerg = new Pergunta(""+ (i++));
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

	public int extractSimpleRating(List<MyNode> nodes, int currentI) {
		MyNode input = null, lastInput = null;
		int i = 1;//TODO adicionar 0 tb?
				
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

	public int extractCheckboxOrRadioInput(Questionario currentQ, List<MyNode> nodes, int currentI) {
		MyNode img = null, input = null, text = null, tmp = null;
		boolean isImgQuestion = false;
		String txt = "";
		
		input = nodes.get(currentI);
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma(input.getType().toString()));
		if(input.getType() == MyNodeType.CHECKBOX_INPUT)
			CommonLogger.debug("\tCheckbox Input:");
		else
			CommonLogger.debug("\tRadio Input:");
		
		img = nodes.get(currentI-1);
		text = nodes.get(++currentI);
		tmp = nodes.get(currentI+1);
		// Perguntas com imagens seguem o padrão: 
		//		img -> input -> text -> img -> input -> text ...
		isImgQuestion = img.isImage() && tmp.isImage();
		while(input != null &&
				(input.isA("CHECKBOX_INPUT") || input.isA("RADIO_INPUT")) &&
				text.getType() == MyNodeType.TEXT &&
				(!isImgQuestion || (img != null && img.isImage()))){
			if(!this.checker.areCompAndTextNear(input, text))
				break;
			
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
					
					tmpPerg.setQuestionario(currentQ);
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
				currentQ.addFigura(fig);
				CommonLogger.debug("\t\t\tLegenda: {}", fig.getLegenda());
			}
			
			if(currentI+1 < nodes.size() && isImgQuestion)
				img = nodes.get(++currentI);
			else
				img = null;
			if(currentI+2 < nodes.size()){
				input = nodes.get(++currentI);
				text = nodes.get(++currentI);
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
	
	public int extractImageCheckboxOrRadioInput(Questionario currentQ, List<MyNode> nodes, int currentI) {
		MyNode img = null, 
			input = nodes.get(currentI);
		int backup_currentI = currentI;
		
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
			
			Figura fig = new Figura(img.getAttr("src"), img.getAttr("alt"));
			fig.setDono(currentP);
			currentQ.addFigura(fig);
			CommonLogger.debug("\t\t{}", fig);
			
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

	public int extractCheckboxOrRadioInputWithTextAbove(Questionario currentQ, List<MyNode> nodes, int currentI) {
		// A principio não se preocupa com input text
		MyNode input = null, text = null, img = null;
		boolean isImgQuestion = false;
		String txt = "";
		
		input = nodes.get(currentI);
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma(input.getType().toString()));
		if(input.getType() == MyNodeType.CHECKBOX_INPUT)
			CommonLogger.debug("\tCheckbox Input [text above]:");
		else
			CommonLogger.debug("\tRadio Input [text above]:");
		
		text = nodes.get(currentI-1);
		img = nodes.get(currentI-2);
		// Perguntas com imagens seguem o padrão: 
		//		img -> text -> input -> img -> text -> input ...
		isImgQuestion = img.isImage() && (nodes.get(currentI+1).isImage());
		while(input != null && 
				(input.getType() == MyNodeType.CHECKBOX_INPUT || input.getType() == MyNodeType.RADIO_INPUT) &&
				text.getType() == MyNodeType.TEXT &&
				(!isImgQuestion || (img != null && img.isImage()))){
			
			if(!this.checker.areCompAndTextNear(input, text))
				break;
			
			txt = text.getText();
			CommonLogger.debug("\t\t{}", txt);
			Alternativa tmpAlt = new Alternativa(txt);
			this.currentP.addAlternativa(tmpAlt);
			
			if(isImgQuestion){
				Figura fig = new Figura(img.getAttr("src"), img.getAttr("alt"));
				fig.setDono(tmpAlt);
				currentQ.addFigura(fig);
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

	public int extractSelect(List<MyNode> nodes, int currentI) {
		MyNode opt = null, text = null;
		
		if(checker.isSelectGroup(nodes, currentI)){
			currentP.setForma(FormaDaPerguntaManager.getForma("SELECT_GROUP"));
			CommonLogger.debug("\tSelect Group:");
			return this.extractSelectGroup(nodes, currentI);
		}
		
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		CommonLogger.debug("\tSelect:");
		
		opt = nodes.get(++currentI);
		text = nodes.get(++currentI);
		while(opt != null && opt.getType() == MyNodeType.OPTION){
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
		if(opt != null)
			currentI -= 2;
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

	public int extractSimpleMatrix(List<MyNode> nodes, Questionario currentQ, Cluster lastMatrixHead, int currentI) {
		MyNode input = null;
		MyNodeType lastCompType = null;
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

	public int extractCheckboxOrRadioInputWithHeader(List<MyNode> nodes, Cluster head, int currentI) {
		MyNode input = nodes.get(currentI);
		String type = input.getType().toString();
		int j = 0;

		this.currentP.setForma(FormaDaPerguntaManager.getForma(type));
		if(input.getType() == MyNodeType.CHECKBOX_INPUT)
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
