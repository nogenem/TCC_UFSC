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
import br.ufsc.tcc.extractor.model.FormaDaPergunta;
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
		if(this.checker.isOnlyOneImg(desc) && !cStack.isEmpty())
			desc = cStack.pop();

		if(firstNode.getType() != MyNodeType.SELECT && nodes.get(this.currentI+1).isComponent() &&
				this.distMatrix.areNear(firstNode, nodes.get(this.currentI+1))){
			
			if(this.lastMatrixHead != null && 
					this.checker.isAbove(this.lastMatrixHead.last(), cStack.peek().first())){
				this.saveLastMatrix(currentQ);
			}
			if(this.lastMatrixHead != null || this.checker.isSimpleMatrix(nodes, this.currentI, cStack)){
				if(this.lastMatrixHead == null)
					this.lastMatrixHead = cStack.pop();
				this.extractSimpleMatrix(nodes, currentQ);
			}else if(firstNode.getType() == MyNodeType.RADIO_INPUT && 
					nodes.get(this.currentI+1).getType() == MyNodeType.RADIO_INPUT){
				this.extractSimpleRating(nodes);
			}
		}else{
			switch (firstNode.getType()) {
			case SELECT:{
				this.extractSelect(nodes);
				break;
			}case CHECKBOX_INPUT:
				this.extractCheckboxOrRadioInput(currentQ, nodes);
				break;
			case RADIO_INPUT:{
				this.extractCheckboxOrRadioInput(currentQ, nodes);
				break;
			}case TEXT_INPUT:
			case NUMBER_INPUT:
			case EMAIL_INPUT:
			case DATE_INPUT:
			case TEL_INPUT:
			case TIME_INPUT:
			case URL_INPUT:{
				this.extractGenericInput(nodes);
				break;
			}case TEXTAREA:{
				this.extractTextarea(nodes);
				break;
			}case RANGE_INPUT:{
				//TODO tentar achar um exemplo?
				break;
			}default:
				break;
			}
		}
		
		if(this.currentP.getForma() != null){
			ArrayList<Alternativa> tmpAlts = this.currentP.getAlternativas();
			desc = this.checker.getCorrectDescription(desc, tmpAlts, firstNode, cStack);
			desc = this.checker.checkIfDescIsComplete(desc, cStack, nodes, this.currentI);
			
			//A distancia da pergunta para a sua descrição não pode ser maior que 2 
			if(!checker.areDescAndPergNear(desc, firstNode))
				return this.currentI;
			
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
				this.updateLastMatrix(nodes, cStack, cTmp2);
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

			if(nTmp2 != null && nTmp2.isText()){
				if(checker.checkDistForQuestionGroup(nTmp2, nTmp1)){
					if(this.lastQuestionGroup == null && this.checker.checkQuestionGroup(nTmp1, nTmp2, nodes, this.currentI)){
						this.updateLastQuestionGroup(currentQ, nodes, cStack, nTmp1);
						questionGroupFlag = true;
					}else if(this.lastQuestionGroup != null && 
							this.checker.checkPrefixForQuestionGroup(nTmp1, nTmp2, this.lastQuestionGroupCommonPrefix)){
						if(!this.lastQuestionGroup.getForma().toString().
								startsWith(this.currentP.getForma().toString())){
							this.lastQuestionGroup.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_GROUP"));
						}
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
				//Verifica se não é a imagem da pergunta
				cTmp1 = cStack.peek();
				if(this.checker.isOnlyOneImg(cTmp1) && this.distMatrix.areNear(cTmp1, desc)){
					cTmp1 = cStack.pop();
					MyNode tmp = cTmp1.last();
					Figura fig = new Figura(tmp.getAttr("src"), tmp.getAttr("alt"));
					fig.setDono(this.currentP);
					currentQ.addFigura(fig);
					System.out.println("Figura da pergunta: " +fig+ "\n");
				}
			}
			
			if(!cStack.isEmpty()){	
				if(currentQ.getAssunto().isEmpty()){
					//Encontra o assunto do questionario
					cTmp1 = cStack.pop();
					this.firstGroupOfQuestionnaire = null;
					
					//Verifica se não é a imagem do questionário
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

						System.out.println("\nGroup1: " +cTmp1.getText()+ "\n\n");
						cTmp1 = cStack.pop();
					}
					cTmp1 = this.checker.checkIfDescIsComplete(cTmp1, cStack, nodes, this.currentI);
					currentQ.setAssunto(cTmp1.getText());
					System.out.println("Assunto: " +currentQ.getAssunto() +"\n\n");
				}else{
					cTmp1 = cStack.peek();
					
					//Verifica se o texto acima não é um grupo
					if(this.checker.isGroupText(cTmp1, desc, this.firstGroupOfQuestionnaire)){
						cTmp1 = cStack.pop();
						this.currentG = new Grupo(cTmp1.getText());
						currentQ.addGrupo(currentG);

						System.out.println("\nGroup2: " +cTmp1.getText()+ "\n\n");
					}
				}
			}
			
			//Verifica se o texto abaixo, se tiver, não faz parte desta pergunta (Ex: Peso: [...] kg)
			if(this.checker.checkNextText(nodes, this.currentI)){
				nTmp1 = nodes.get(++this.currentI);
				this.currentP.setDescricao(
						this.currentP.getDescricao() +"\n"+ nTmp1.getText());
				System.out.println("Descrição atualizada1: " +this.currentP.getDescricao() +"\n\n");
			}
			
			if(this.currentG != null){
				this.currentP.setGrupo(this.currentG);
				if(this.lastMatrix != null && this.lastMatrix.getGrupo() == null)
					this.lastMatrix.setGrupo(this.currentG);
				if(this.lastQuestionGroup != null && this.lastQuestionGroup.getGrupo() == null)
					this.lastQuestionGroup.setGrupo(this.currentG);
			}	
			
			//TODO verificar se eh login?
			if(!matrixFlag && !questionGroupFlag && !this.currentP.getDescricao().isEmpty())
				currentQ.addPergunta(this.currentP);
		}
		
		return this.currentI;
	}

	private void updateLastQuestionGroup(Questionario currentQ, List<MyNode> nodes, Stack<Cluster> cStack, MyNode nTmp1) {
		if(cStack.isEmpty()) return;
		
		this.lastQuestionGroupDesc = cStack.pop();
		this.lastQuestionGroupDesc = this.checker.
				checkIfDescIsComplete(this.lastQuestionGroupDesc, cStack, nodes, this.currentI);
		this.lastQuestionGroupCommonPrefix = nTmp1.getDewey().
				getCommonPrefix(this.lastQuestionGroupDesc.last().getDewey());
		
		this.lastQuestionGroup = new Pergunta();
		this.lastQuestionGroup.setForma(FormaDaPerguntaManager.getForma(this.currentP.getForma().toString()+"_GROUP"));
		this.lastQuestionGroup.setDescricao(this.lastQuestionGroupDesc.getText());							
		this.lastQuestionGroup.addFilha(this.currentP);
	}

	private void saveLastQuestionGroup(Questionario currentQ) {
		if(this.lastQuestionGroup != null){
			currentQ.addPergunta(this.lastQuestionGroup);
			System.out.println("Group Descricao: " +this.lastQuestionGroup.getDescricao()+ "\n\n");
			
			this.lastQuestionGroup = null;
			this.lastQuestionGroupDesc = null;
			this.lastQuestionGroupCommonPrefix = "";
		}
	}

	private void updateLastMatrix(List<MyNode> nodes, Stack<Cluster> cStack, Cluster cTmp2) {
		if(!cStack.isEmpty() && cTmp2 == cStack.peek()){
			this.lastMatrixHead = cStack.pop();
		}
		if(!cStack.isEmpty() && lastMatrix == null){
			this.lastMatrix = new Pergunta();
			FormaDaPergunta forma = this.currentP.getForma();
			if(forma == FormaDaPerguntaManager.getForma("MIX_COMP_GROUP")){
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_MATRIX"));
			}else{
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma(forma.toString()+"_MATRIX"));
			}
			Cluster desc = cStack.pop();
			desc = this.checker.checkIfDescIsComplete(desc, cStack, nodes, this.currentI);
			this.lastMatrix.setDescricao(desc.getText());
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
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
	}

	private void extractGenericInput(List<MyNode> nodes) {
		String type = nodes.get(this.currentI).getAttr("type").toUpperCase();

		System.out.println("\tInput [" +type+ "].");
		
		currentP.setForma(FormaDaPerguntaManager.getForma(type + "_INPUT"));
		
		//Checagem para coisas do genêro: Hora: [ ] : [ ]
		//	Ex: https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b
		if(this.currentI+2 < nodes.size() && type.matches("TEXT|NUMBER")){
			MyNode tmp1 = nodes.get(this.currentI+1), 
					tmp2 = nodes.get(this.currentI+2);
			if(tmp1.isText() && tmp1.getText().equals(":") && 
					tmp2.isComponent() && tmp2.getType() == nodes.get(this.currentI).getType()){
				this.currentI += 2;
			}
		}
	}

	private void extractSimpleRating(List<MyNode> nodes) {
		MyNode input = null, lastInput = null;
		int i = 1;
		boolean error = false;
				
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
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma(input.getType().toString()));
		if(input.getType() == MyNodeType.CHECKBOX_INPUT)
			System.out.println("\tCheckbox Input:");
		else
			System.out.println("\tRadio Input:");
		
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
				tmpPerg.setForma(FormaDaPerguntaManager.getForma(lastCompType.toString()));
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
		
		if(isMix)
			this.currentP.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_GROUP"));
		else
			this.currentP.setForma(FormaDaPerguntaManager.getForma(lastCompType.toString()));
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
