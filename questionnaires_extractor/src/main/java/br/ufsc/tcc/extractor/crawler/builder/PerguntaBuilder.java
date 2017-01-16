package br.ufsc.tcc.extractor.crawler.builder;

import java.util.List;
import java.util.Stack;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.DistanceMatrix;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.Grupo;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class PerguntaBuilder {
	
	private Pergunta currentP;
	private Grupo currentG;
	private int currentI;
	private DistanceMatrix distMatrix;
	private RulesChecker checker;
	
	public PerguntaBuilder(RulesChecker checker) {
		this.currentP = null;
		this.currentG = null;
		this.currentI = 0;
		this.checker = checker;
		this.distMatrix = this.checker.getDistMatrix();
	}

	public int build(Questionario currentQ, List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		this.currentP = new Pergunta();
		this.currentI = i;
		
		MyNode firstNode = nodes.get(this.currentI);
		MyNode nTmp1 = null, nTmp2 = null;
		Cluster desc = cStack.pop();
		Cluster cTmp1 = null, cTmp2 = null;
		
		//Verifica se o cluster de desc é apenas uma img
		//		Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop
		if(this.checker.isOnlyOneImg(desc) && !cStack.isEmpty()){
			desc = cStack.pop();
		}
		
		
		switch (firstNode.getType()) {
		case SELECT:
			this.extractSelect(nodes);
			break;
		case CHECKBOX_INPUT:
			this.extractCheckboxOrRadioInput(currentQ, nodes);
			break;
		case RADIO_INPUT:{
			if(nodes.get(this.currentI+1).getType() == MyNodeType.RADIO_INPUT){
				//TODO tratar matriz
				this.extractSimpleRating(nodes);
			}else
				this.extractCheckboxOrRadioInput(currentQ, nodes);
			break;
		}case TEXT_INPUT:
		case NUMBER_INPUT:
		case EMAIL_INPUT:
		case DATE_INPUT:
		case TEL_INPUT:
		case TIME_INPUT:
		case URL_INPUT:{
			//TODO tratar matriz http://anpei.tempsite.ws/intranet/mediaempresa
			this.extractGenericInput(nodes);
			break;
		}case TEXTAREA:
			this.extractTextarea(nodes);
			break;
		case RANGE_INPUT:
			//TODO tentar achar um exemplo?
			break;
		default:
			break;
		}
		
		return this.currentI;
	}

	private void extractTextarea(List<MyNode> nodes) {
		System.out.println("\tTextarea.");
		
		currentP.setTipo("ABERTO");
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
	}

	private void extractGenericInput(List<MyNode> nodes) {
		String type = nodes.get(this.currentI).getAttr("type").toUpperCase();

		System.out.println("\tInput [" +type+ "].");
		
		currentP.setTipo("ABERTO");
		currentP.setForma(FormaDaPerguntaManager.getForma(type + "_INPUT"));
	}

	private void extractSimpleRating(List<MyNode> nodes) {
		MyNode input = null, lastInput = null;
		int i = 1;
				
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RATING"));
		System.out.println("\tRating:");
		
		input = nodes.get(this.currentI);
		while(input != null && input.getType() == MyNodeType.RADIO_INPUT){
			if(lastInput != null && 
					!this.checker.areCompAndTextNear(lastInput, input))
				break;
			
			Alternativa tmpAlt = new Alternativa(""+ (i++));
			this.currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t" +tmpAlt.getDescricao());
			
			lastInput = input;
			if(this.currentI+1 < nodes.size())
				input = nodes.get(++this.currentI);
			else
				input = null;
		}
		if(input != null)
			--this.currentI;
	}

	private void extractCheckboxOrRadioInput(Questionario currentQ, List<MyNode> nodes) {
		MyNode img = null, input = null, text = null, tmp = null;
		boolean isImgQuestion = false;
		
		input = nodes.get(this.currentI);
		
		if(input.getType() == MyNodeType.CHECKBOX_INPUT){
			this.currentP.setTipo("MULTIPLA_ESCOLHA");
			this.currentP.setForma(FormaDaPerguntaManager.getForma("CHECKBOX_INPUT"));
			System.out.println("\tCheckbox Input:");
		}else if(input.getType() == MyNodeType.RADIO_INPUT){
			this.currentP.setTipo("FECHADO");
			this.currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
			System.out.println("\tRadio Input:");
		}else
			return;
		
		img = nodes.get(this.currentI-1);
		text = nodes.get(++this.currentI);
		tmp = nodes.get(this.currentI+1);
		// Perguntas com imagens seguem o padrão: 
		//		img -> input -> text -> img -> input -> text ...
		isImgQuestion = img.isImage() && tmp.isImage();
		while(input != null && 
				(input.getType() == MyNodeType.CHECKBOX_INPUT || input.getType() == MyNodeType.RADIO_INPUT)){
			if(!this.checker.areCompAndTextNear(input, text))
				break;
			
			System.out.println("\t\t" +text.getText());
			Object dono = null;
			
			if(tmp != null && tmp.getType() == MyNodeType.TEXT_INPUT &&
					this.checker.areCompAndTextNear(tmp, text)){
				Pergunta tmpPerg = new Pergunta(text.getText());
				dono = tmpPerg;
				
				tmpPerg.setTipo("ABERTO");
				tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
				System.out.println("\t\t\tCom Text Input.");
				
				tmpPerg.setQuestionario(currentQ);
				this.currentP.addFilha(tmpPerg);
			}else{
				Alternativa tmpAlt = new Alternativa(text.getText());
				dono = tmpAlt;
				this.currentP.addAlternativa(tmpAlt);
			}
			
			if(isImgQuestion && img != null && img.isImage()){
				Figura fig = new Figura(img.getAttr("src"), img.getAttr("alt"));
				fig.setDono(dono);
				currentQ.addFigura(fig);
				System.out.println("\t\t\tLegenda: " +fig.getLegenda());
			}
			
			if(this.currentI+1 < nodes.size() && isImgQuestion)
				img = nodes.get(++this.currentI);
			else
				img = null;
			if(this.currentI+2 < nodes.size()){
				input = nodes.get(++this.currentI);
				text = nodes.get(++this.currentI);
				if(this.currentI+1 < nodes.size())
					tmp = nodes.get(this.currentI+1);
			}else
				input = null;
		}
		if(input != null){
			if(isImgQuestion && img != null) --this.currentI;
			this.currentI -= 2;
			if(input.getType() == MyNodeType.TEXT_INPUT)
				this.currentI += 1;
		}
	}

	private void extractSelect(List<MyNode> nodes) {
		MyNode opt = null, text = null;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\tSelect:");
		
		opt = nodes.get(++this.currentI);
		text = nodes.get(++this.currentI);
		while(opt != null && opt.getType() == MyNodeType.OPTION){
			if(!this.checker.areCompAndTextNear(opt, text))
				break;
			
			Alternativa tmpAlt = new Alternativa(text.getText());
			this.currentP.addAlternativa(tmpAlt);
			System.out.println("\t\t" +tmpAlt.getDescricao());
			
			if(this.currentI+2 < nodes.size()){
				opt = nodes.get(++this.currentI);
				text = nodes.get(++this.currentI);
			}else
				opt = null;
		}
		if(opt != null)
			this.currentI -= 2;
	}

}
