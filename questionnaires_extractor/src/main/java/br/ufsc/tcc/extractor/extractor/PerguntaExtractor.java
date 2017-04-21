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

	public int extractGenericInput(List<MyNode> nodes, int currentI) {
		String type = nodes.get(currentI).getType().toString();
		CommonLogger.debug("\tInput [{}].", type);
		currentP.setForma(FormaDaPerguntaManager.getForma(type));
		
		//Checagem para coisas do genêro: Hora: [ ] : [ ]
		//	Ex: https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b
		if(currentI+2 < nodes.size() && type.matches("(TEXT|NUMBER)_INPUT")){
			MyNode tmp1 = nodes.get(currentI+1), 
					tmp2 = nodes.get(currentI+2);
			if(tmp1.getText().equals(":") && 
					tmp2.isComponent() && tmp2.getType().toString().equals(type)){
				currentI += 2;
			}
		}
		return currentI;
	}
	
	public int extractMultiCompQuestion(List<MyNode> nodes, Questionario currentQ, int currentI) {
		MyNode input = null, lastInput = null;
		MyNodeType multiCompType = null;
		int i = 1;
		boolean error = false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("MULTI_COMP"));
		CommonLogger.debug("\tMulti Comp:");
		
		input = nodes.get(currentI);
		multiCompType = input.getType();
		while(input != null && input.getType() == multiCompType){
			if(lastInput != null && 
					!this.checker.areCompAndTextNear(lastInput, input)){
				error = true;
				break;
			}

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
		//Se deu erro e não tem nenhuma filha/alternativa quer dizer que o loop não
		//completo nenhuma vez
		if(error && this.currentP.getFilhas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

	public int extractSimpleRating(List<MyNode> nodes, int currentI) {
		MyNode input = null, lastInput = null;
		int i = 1;//TODO adicionar 0 tb?
		boolean error = false;
				
		currentP.setForma(FormaDaPerguntaManager.getForma("RATING"));
		CommonLogger.debug("\tRating:");
		
		input = nodes.get(currentI);
		while(input != null && input.getType() == MyNodeType.RADIO_INPUT){
			if(lastInput != null && 
					!this.checker.areCompAndTextNear(lastInput, input)){
				error = true;
				break;
			}
			
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
		if(error && this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

	public int extractCheckboxOrRadioInput(Questionario currentQ, List<MyNode> nodes, int currentI) {
		MyNode img = null, input = null, text = null, tmp = null;
		boolean isImgQuestion = false, error = false;
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
				(input.getType() == MyNodeType.CHECKBOX_INPUT || input.getType() == MyNodeType.RADIO_INPUT) &&
				(!isImgQuestion || (img != null && img.isImage()))){
			if(!this.checker.areCompAndTextNear(input, text)){
				error = true;
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
				
				if(tmp != null && tmp.getType() == MyNodeType.TEXT_INPUT && 
						this.checker.areCompAndTextNear(tmp, text)){
					Pergunta tmpPerg = new Pergunta(txt);
					dono = tmpPerg;
					
					tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
					CommonLogger.debug("\t\t\tCom Text Input.");
					
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
		if(error && this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
		
		return currentI;
	}

	public int extractSelect(List<MyNode> nodes, int currentI) {
		MyNode opt = null, text = null;
		boolean error = false;
		
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
				
			if(!this.checker.areCompAndTextNear(opt, text)){
				error = true;
				break;
			}
			
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
		if(error && this.currentP.getAlternativas().size() == 0)
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
}
