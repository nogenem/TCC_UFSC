package br.ufsc.tcc.extractor.crawler.builder;

import java.util.ArrayList;
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
	private Cluster firstGroupOfQuestionnaire;
	
	private Cluster lastMatrixHead;
	private Pergunta lastMatrix;
	
	private Cluster lastQuestionGroupDesc;
	private Pergunta lastQuestionGroup;
	private String lastQuestionGroupCommonPrefix;
	
	public PerguntaBuilder(RulesChecker checker) {
		this.currentP = null;
		this.currentG = null;
		this.currentI = 0;
		this.checker = checker;
		this.distMatrix = this.checker.getDistMatrix();
		this.firstGroupOfQuestionnaire = null;
		
		this.lastMatrixHead = null;
		this.lastMatrix = null;
		
		this.lastQuestionGroup = null;
		this.lastQuestionGroupDesc = null;
		this.lastQuestionGroupCommonPrefix = null;
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
				if(this.lastMatrixHead != null && 
						this.checker.isAbove(this.lastMatrixHead.last(), cStack.peek().first())){
					this.saveLastMatrix(currentQ);
				}
				if(this.lastMatrixHead != null || this.checker.isSimpleMatrix(nodes, this.currentI, cStack)){
					if(this.lastMatrixHead == null)
						this.lastMatrixHead = cStack.pop();
					this.extractSimpleMatrix(nodes, currentQ);
				}else
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
			if(nodes.get(this.currentI+1).isComponent()){
				if(this.lastMatrixHead != null && 
						this.checker.isAbove(this.lastMatrixHead.last(), cStack.peek().first())){
					this.saveLastMatrix(currentQ);
				}
				if(this.lastMatrixHead != null || this.checker.isSimpleMatrix(nodes, this.currentI, cStack)){
					if(this.lastMatrixHead == null)
						this.lastMatrixHead = cStack.pop();
					this.extractSimpleMatrix(nodes, currentQ);
				}
			}else
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
		
		if(this.currentP.getForma() != null){
			ArrayList<Alternativa> tmpAlts = this.currentP.getAlternativas();
			desc = checker.getCorrectDescription(desc, tmpAlts, firstNode, cStack);
			
			//A distancia da pergunta para a sua descrição não pode ser maior que 2 
			if(checker.checkDistBetweenDescAndPerg(desc, firstNode)){
				return this.currentI;
			}
			
			this.currentP.setDescricao(desc.getText());
			System.out.println("Descrição: " +this.currentP.getDescricao() +"\n\n");
			
			//Verifica matriz
			boolean matrixFlag = false;
			cTmp2 = this.lastMatrixHead;
			
			if(!cStack.isEmpty()){
				if(cTmp2 == null){
					cTmp2 = cStack.peek();
				}else if(this.checker.isAbove(cTmp2.last(), cStack.peek().first())){
					//Se tiver um texto no meio quer dizer que ja termino a matriz
					this.saveLastMatrix(currentQ);
					cTmp2 = cStack.peek();
				}
			}
			
			matrixFlag = this.checker.hasSameTexts(this.currentP, cTmp2);
			if(matrixFlag){
				this.updateLastMatrix(cStack, cTmp2);
			}else if(this.lastMatrix != null){
				this.saveLastMatrix(currentQ);
			}
			
			//Verifica grupo de perguntas
			boolean questionGroupFlag = false;
			nTmp1 = nodes.get(this.currentI);
			nTmp2 = this.lastQuestionGroupDesc != null ? 
						this.lastQuestionGroupDesc.last() : null;
			
			if(!cStack.isEmpty()){
				if(nTmp2 == null){
					nTmp2 = cStack.peek().last();
				}else if(this.checker.isAbove(nTmp2, cStack.peek().first())){
					//Se tiver um texto no meio quer dizer que ja termino o grupo de pergunta
					this.saveLastQuestionGroup(currentQ);
					nTmp2 = cStack.peek().last();
				}
			}
			
			if(nTmp2 != null){
				if(checker.checkDistForQuestionGroup(nTmp1, nTmp2)){
					if(this.lastQuestionGroup == null && this.checker.checkQuestionGroup(nTmp1, nTmp2, nodes, this.currentI)){
						this.updateLastQuestionGroup(currentQ, cStack, nTmp1);
						questionGroupFlag = true;
					}else if(this.checker.checkPrefixForQuestionGroup(nTmp1, nTmp2, this.lastQuestionGroupCommonPrefix)){
						this.lastQuestionGroup.addFilha(this.currentP);
						questionGroupFlag = true;
					}else{
						this.saveLastQuestionGroup(currentQ);
					}
				}else if(this.lastQuestionGroup != null){
					this.saveLastQuestionGroup(currentQ);
				}
			}
			
			if(!cStack.isEmpty()){
				if(currentQ.getAssunto().isEmpty()){
					//Encontra o assunto do questionario
					cTmp1 = cStack.pop();
					cTmp1 = this.checker.checkIfShouldBeInSameCluster(cTmp1, cStack, firstNode);
					this.firstGroupOfQuestionnaire = null;
					
					//Verifica se não apenas uma img
					if(this.checker.isOnlyOneImg(cTmp1)){
						MyNode tmp = cTmp1.last();
						Figura fig = new Figura(tmp.getAttr("src"), tmp.getAttr("alt"));
						fig.setDono(currentQ);
						currentQ.addFigura(fig);
						System.out.println("Figura do questionario: " +fig+ "\n");
						cTmp1 = cStack.pop();
					}
					//Verifica se não é um grupo
					if(!cStack.isEmpty() && this.checker.isGroupText(cTmp1, desc, this.firstGroupOfQuestionnaire)){
						currentG = new Grupo(cTmp1.getText());
						currentQ.addGrupo(currentG);
						this.firstGroupOfQuestionnaire = cTmp1;

						System.out.println("\nGroup: " +cTmp1.getText()+ "\n\n");
						cTmp1 = cStack.pop();
					}
					currentQ.setAssunto(cTmp1.getText());
					System.out.println("Assunto: " +currentQ.getAssunto() +"\n\n");
				}else{
					//Verifica se o texto acima não é um grupo
					cTmp1 = cStack.peek();
					if(this.checker.isGroupText(cTmp1, desc, this.firstGroupOfQuestionnaire)){
						cTmp1 = cStack.pop();
						this.currentG = new Grupo(cTmp1.getText());
						currentQ.addGrupo(currentG);

						System.out.println("\nGroup: " +cTmp1.getText()+ "\n\n");
					}
				}
			}
			
			//Verifica se o texto abaixo, se tiver, não faz parte desta pergunta (Peso: [...] kg)
			if(this.checker.checkNextText(nodes, this.currentI)){
				nTmp1 = nodes.get(++this.currentI);
				this.currentP.setDescricao(
						this.currentP.getDescricao() +"\n"+ nTmp1.getText());
				System.out.println("Descrição atualizada: " +this.currentP.getDescricao() +"\n\n");
			}
			
			if(this.currentG != null){
				this.currentP.setGrupo(this.currentG);
				if(this.lastMatrix != null && this.lastMatrix.getGrupo() == null)
					this.lastMatrix.setGrupo(this.currentG);
				if(this.lastQuestionGroup != null && this.lastQuestionGroup.getGrupo() == null)
					this.lastQuestionGroup.setGrupo(this.currentG);
			}	
			
			//TODO verificar se a desc ta vazia ou se eh login?
			if(!matrixFlag && !questionGroupFlag)
				currentQ.addPergunta(this.currentP);
		}
		
		return this.currentI;
	}

	private void extractSimpleMatrix(List<MyNode> nodes, Questionario currentQ) {
		MyNode input = null;
		MyNodeType lastCompType = null;
		boolean isMix = false;
		int j = 0;
		
		System.out.println("\tSimple Matrix:");
		input = nodes.get(this.currentI);
		while(input != null && j < this.lastMatrixHead.size() && input.isComponent()){
			if(!isMix && lastCompType != null && lastCompType != input.getType())
				isMix = true;
			lastCompType = input.getType();
			
			String text = this.lastMatrixHead.get(j).getText();
			System.out.println("\t\tText: " +text+ " - Comp: " +input.getText());
			
			if(lastCompType == MyNodeType.RADIO_INPUT || lastCompType == MyNodeType.CHECKBOX_INPUT){
				Alternativa alt = new Alternativa(text);
				this.currentP.addAlternativa(alt);
			}else{
				Pergunta tmpPerg = new Pergunta(text);
				if(lastCompType == MyNodeType.TEXT_INPUT){
					tmpPerg.setTipo("ABERTO");
					tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
				}else if(lastCompType == MyNodeType.TEXTAREA){
					tmpPerg.setTipo("ABERTO");
					tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
				}
				tmpPerg.setQuestionario(currentQ);
				this.currentP.addFilha(tmpPerg);
			}
			
			if(this.currentI+1 < nodes.size())
				input = nodes.get(++this.currentI);
			else
				input = null;
			j++;
		}
		if(input != null)
			this.currentI--;
		
		if(isMix){
			this.currentP.setTipo("ABERTO");
			//TODO mudar este nome?
			this.currentP.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_GROUP"));
		}else{
			switch(lastCompType){
			case RADIO_INPUT:
				this.currentP.setTipo("FECHADO");
				this.currentP.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT"));
				break;
			case CHECKBOX_INPUT:
				this.currentP.setTipo("MULTIPLA_ESCOLHA");
				this.currentP.setForma(FormaDaPerguntaManager.getForma("CHECKBOX_INPUT"));
				break;
			case TEXT_INPUT:
				this.currentP.setTipo("ABERTO");
				this.currentP.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
				break;
			case TEXTAREA:
				this.currentP.setTipo("ABERTO");
				this.currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
				break;
			default:
				break;
			}
		}
	}

	private void updateLastQuestionGroup(Questionario currentQ, Stack<Cluster> cStack, MyNode nTmp1) {
		if(cStack.isEmpty()) return;
		
		this.lastQuestionGroupDesc = cStack.pop();
		this.lastQuestionGroupCommonPrefix = nTmp1.getDewey().
				getCommonPrefix(this.lastQuestionGroupDesc.last().getDewey());
		
		this.lastQuestionGroup = new Pergunta();
		//TODO verificar o tipo de input/criar _GROUP para todos?
		if(this.currentP.getForma() == FormaDaPerguntaManager.getForma("TEXT_INPUT")){
			this.lastQuestionGroup.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT_GROUP"));
			this.lastQuestionGroup.setTipo("ABERTO");
		}
		this.lastQuestionGroup.setDescricao(this.lastQuestionGroupDesc.getText());							
		this.lastQuestionGroup.addFilha(this.currentP);
	}

	private void saveLastQuestionGroup(Questionario currentQ) {
		if(this.lastQuestionGroup != null){
			currentQ.addPergunta(this.lastQuestionGroup);
			System.out.println("Group Descricao: " +this.lastQuestionGroup.getDescricao()+ "\n\n");
			
			this.lastQuestionGroup = null;
			this.lastQuestionGroupDesc = null;
		}
	}

	private void updateLastMatrix(Stack<Cluster> cStack, Cluster cTmp2) {
		if(!cStack.isEmpty() && cTmp2 == cStack.peek()){
			this.lastMatrixHead = cStack.pop();
		}
		if(!cStack.isEmpty() && lastMatrix == null){
			this.lastMatrix = new Pergunta();
			if(this.currentP.getForma() == FormaDaPerguntaManager.getForma("RADIO_INPUT")){
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma("RADIO_INPUT_MATRIX"));
				this.lastMatrix.setTipo("MULTIPLA_ESCOLHA");
			}else if(this.currentP.getForma() == FormaDaPerguntaManager.getForma("TEXT_INPUT")){
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT_MATRIX"));
				this.lastMatrix.setTipo("ABERTO");
			}else if(this.currentP.getForma() == FormaDaPerguntaManager.getForma("MIX_COMP_GROUP")){
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_MATRIX"));
				this.lastMatrix.setTipo("ABERTO");
			}
			this.lastMatrix.setDescricao(cStack.pop().getText());
		}
		if(this.lastMatrix != null)
			this.lastMatrix.addFilha(this.currentP);
	}

	private void saveLastMatrix(Questionario currentQ) {
		if(this.lastMatrix != null){
			currentQ.addPergunta(this.lastMatrix);
			System.out.println("Matrix Descricao: " +this.lastMatrix.getDescricao()+ "\n\n");
		
			this.lastMatrix = null;
			this.lastMatrixHead = null;
		}
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
		boolean error = false;
				
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("RATING"));
		System.out.println("\tRating:");
		
		input = nodes.get(this.currentI);
		while(input != null && input.getType() == MyNodeType.RADIO_INPUT){
			if(lastInput != null && 
					!this.checker.areCompAndTextNear(lastInput, input)){
				error = true;
				break;
			}
			
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
		if(error && this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
	}

	private void extractCheckboxOrRadioInput(Questionario currentQ, List<MyNode> nodes) {
		MyNode img = null, input = null, text = null, tmp = null;
		boolean isImgQuestion = false, error = false;
		
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
			if(!this.checker.areCompAndTextNear(input, text)){
				error = true;
				break;
			}
				
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
			if(this.currentP.getAlternativas().size() == 0)
				this.currentI += 1;
		}
		if(error && this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
	}

	private void extractSelect(List<MyNode> nodes) {
		MyNode opt = null, text = null;
		boolean error = false;
		
		currentP.setTipo("FECHADO");
		currentP.setForma(FormaDaPerguntaManager.getForma("SELECT"));
		System.out.println("\tSelect:");
		
		opt = nodes.get(++this.currentI);
		text = nodes.get(++this.currentI);
		while(opt != null && opt.getType() == MyNodeType.OPTION){
			if(!this.checker.areCompAndTextNear(opt, text)){
				error = true;
				break;
			}
			
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
		if(error && this.currentP.getAlternativas().size() == 0)
			this.currentP.setForma(null);
	}

	public void clearData(Questionario currentQ) {
		if(this.lastMatrix != null)
			this.saveLastMatrix(currentQ);
		if(this.lastQuestionGroup != null)
			this.saveLastQuestionGroup(currentQ);
		
		this.currentG = null;
		this.currentP = null;
	}

}
