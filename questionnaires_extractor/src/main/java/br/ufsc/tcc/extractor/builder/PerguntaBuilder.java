package br.ufsc.tcc.extractor.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.DistanceMatrix;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.PerguntaExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;
import br.ufsc.tcc.extractor.model.Grupo;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class PerguntaBuilder {
	// Current
	private Pergunta currentP;
	private Grupo currentG;
	private int currentI;
	
	// Helpers
	private DistanceMatrix distMatrix;
	private RulesChecker checker;
	private PerguntaExtractor extractor;
	
	// Group
	private Cluster firstGroupOfQuestionnaire;

	// Matrix
	private Cluster lastMatrixDesc;
	private Cluster lastMatrixHead;
	private Cluster lastMatrixEvaluationLevels;
	private Pergunta lastMatrix;
	
	// Question with subquestions
	private Cluster lastQWithSubQsDesc;
	private Pergunta lastQWithSubQs;
	private String lastQWithSubQsCommonPrefix;
	
	public PerguntaBuilder(RulesChecker checker) {
		this.currentP = null;
		this.currentG = null;
		this.currentI = 0;
		
		this.checker = checker;
		this.distMatrix = this.checker.getDistMatrix();
		this.extractor = new PerguntaExtractor(this.checker);
		
		this.firstGroupOfQuestionnaire = null;
		
		this.lastMatrixDesc = null;
		this.lastMatrixHead = null;
		this.lastMatrixEvaluationLevels = null;
		this.lastMatrix = null;
		
		this.lastQWithSubQs = null;
		this.lastQWithSubQsDesc = null;
		this.lastQWithSubQsCommonPrefix = null;
	}

	public int build(Questionario currentQ, List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		if(cStack.isEmpty())
			return i;
		
		this.currentP = new Pergunta();
		this.extractor.setCurrentPergunta(this.currentP);
		this.currentI = i;
		
		MyNode firstNode = nodes.get(this.currentI);
		MyNode nTmp1 = (this.currentI+1) < nodes.size() ? nodes.get(this.currentI+1) : null, 
				nTmp2 = null;
		Cluster desc = cStack.pop();
		Cluster cTmp1 = null, cTmp2 = null;
		
		//Se a desc for apenas uma imagem então ela é provavelmente a imagem
		//da 1* alternativa da pergunta; e se a desc for igual a '('e o próximo nodo 
		//for igual a ')' então provavelmente deve ser uma pergunta de telefone: ( [ ] ) [ ]
		//		Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop
		//		Ex: http://www.almaderma.com.br/formulario/florais/infantil/contato.php
		if((this.checker.isOnlyOneImg(desc) || 
				(desc.getText().equals("(") && nTmp1 != null && nTmp1.getText().equals(")"))) && 
				!cStack.isEmpty())
			desc = cStack.pop();
		
		//Verifica se tem componentes em sequência que podem fazer parte de uma
		//matriz ou uma pergunta de RATING
		if(nTmp1 != null && firstNode.getType() != MyNodeType.SELECT && nTmp1.isComponent() &&
				this.distMatrix.areNear(firstNode, nTmp1)){
			
			if(this.lastMatrixHead != null && !cStack.isEmpty() &&
					this.checker.isAbove(this.lastMatrixHead.last(), cStack.peek().first())){
				this.saveLastMatrix(currentQ);
			}
			if(this.lastMatrixHead != null || this.checker.isSimpleMatrix(nodes, this.currentI, cStack)){
				if(this.lastMatrixHead == null)
					this.lastMatrixHead = cStack.pop();
				
				//Ex: http://anpei.tempsite.ws/intranet/mediaempresa/
				//Ex: https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw
				this.currentI = this.extractor.extractSimpleMatrix(nodes, currentQ, 
						this.lastMatrixHead, this.currentI);
			}else if(this.checker.isRadioInputOrCheckboxWithHeader(nodes, cStack, currentI, desc)){
				
				//Ex: http://infopoll.net/live/surveys/s32805.htm
				this.currentI = this.extractor.extractCheckboxOrRadioInputWithHeader(nodes, desc, currentI);
				desc = !cStack.isEmpty() ? cStack.pop() : null;
			}else if(firstNode.getType() == MyNodeType.RADIO_INPUT && 
					nTmp1.getType() == MyNodeType.RADIO_INPUT){
				
				//Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop [questão 9]
				this.currentI = this.extractor.extractSimpleRating(nodes, this.currentI);
			}else if(firstNode.getType() == nTmp1.getType()){
				
				//Ex: http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html [questão 2a]
				this.currentI = this.extractor.extractMultiCompQuestion(nodes, currentQ, this.currentI);
			}
		}else{
			switch (firstNode.getType()) {
			case SELECT:{
				this.currentI = this.extractor.extractSelect(nodes, this.currentI);
				break;
			}case CHECKBOX_INPUT:{
				if(checker.checkIfTextIsAbove(nodes, currentI)){
					desc = cStack.pop();
					this.currentI = this.extractor.extractCheckboxOrRadioInputWithTextAbove(currentQ, nodes, this.currentI);
				}else
					this.currentI = this.extractor.extractCheckboxOrRadioInput(currentQ, nodes, this.currentI);
				break;
			}case RADIO_INPUT:{
				if(checker.checkIfTextIsAbove(nodes, currentI)){
					desc = cStack.pop();
					this.currentI = this.extractor.extractCheckboxOrRadioInputWithTextAbove(currentQ, nodes, this.currentI);
				}else
					this.currentI = this.extractor.extractCheckboxOrRadioInput(currentQ, nodes, this.currentI);
				break;
			}case TEXT_INPUT:
			case NUMBER_INPUT:
			case EMAIL_INPUT:
			case DATE_INPUT:
			case TEL_INPUT:
			case TIME_INPUT:
			case URL_INPUT:{
				this.currentI = this.extractor.extractGenericInput(nodes, this.currentI);
				break;
			}case TEXTAREA:{
				this.extractor.extractTextarea(nodes);
				break;
			}case RANGE_INPUT:{
				//TODO tentar achar um exemplo?
				break;
			}default:
				break;
			}
		}
		
		//Verifica se foi possivel extrair a pergunta
		if(this.currentP.getForma() != null){
			ArrayList<Alternativa> tmpAlts = this.currentP.getAlternativas();
			
			//Atualiza a desc da pergunta
			if(!cStack.isEmpty() && checker.isEvaluationLevels(desc, cStack)){
				this.setEvaluationLevels(this.currentP, desc);
				desc = cStack.pop();
			}
			desc = this.checker.getCorrectDescription(desc, tmpAlts, firstNode, cStack);
			desc = this.checker.checkIfDescIsComplete(desc, cStack, nodes, this.currentI);
			
			//Verifica se a desc e a pergunta estão perto uma da outra
			if(!checker.areDescAndPergNear(desc, firstNode))
				return this.currentI;
			
			//Verifica se o texto abaixo, se tiver, não faz parte desta pergunta (Ex: Peso: [ ] kg)
			while(this.checker.checkComplementaryText(nodes, this.currentI)){
				nTmp1 = nodes.get(++this.currentI);
				//Se for um CHECKBOX ou RADIO INPUT, então o texto complementar deve
				//pertencer a uma opção 'Outro' que usa um TEXT INPUT
				//	Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto [questão 2] 
				if(this.currentP.isA("CHECKBOX_INPUT") || this.currentP.isA("RADIO_INPUT")){
					if(this.currentP.getFilhas().size() > 0){
						ArrayList<Pergunta> filhas = this.currentP.getFilhas();
						Pergunta p = filhas.get(filhas.size()-1);
						p.setDescricao(p.getDescricao()+"\n"+nTmp1.getText());
					}else
						desc.add(nTmp1);
				}else
					desc.add(nTmp1);
			}

			this.currentP.setDescricao(desc.getText());
			//Ex: http://www.zarca.com/Online-Surveys-Non-Profit/association-member-satisfaction-survey.html
			if(checker.isATextInputDisabledWithValue(desc.last()))
				this.currentP.setDescricao(this.currentP.getDescricao() + "\n" + desc.last().getAttr("value"));
			CommonLogger.debug("Descricao: {}\n\n", this.currentP.getDescricao());
			
			//Verifica se é uma matriz
			//	Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto [questão 3]
			boolean matrixFlag = false;
			cTmp2 = this.lastMatrixHead;
			
			if(!cStack.isEmpty()){
				if(cTmp2 == null){
					if(!currentQ.getAssunto().isEmpty() || cStack.size() >= 2)
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
			
			//Verifica se é uma pergunta com subperguntas
			//	Ex: https://www.surveymonkey.com/r/online-social-networking-template [questão 4]
			boolean qWithSubQsFlag = false;
			nTmp1 = nodes.get(this.currentI);
			nTmp2 = this.lastQWithSubQsDesc != null ? 
						this.lastQWithSubQsDesc.last() : null;
			
			if(!cStack.isEmpty()){
				if(nTmp2 == null){
					if(!currentQ.getAssunto().isEmpty() || cStack.size() >= 2)
						nTmp2 = cStack.peek().last();
				}else if(this.checker.isAbove(nTmp2, cStack.peek().first())){
					//Se tiver um texto no meio quer dizer que ja termino as subperguntas
					this.saveLastQWithSubQs(currentQ);
					nTmp2 = cStack.peek().last();
				}
			}

			if(nTmp2 != null && nTmp2.isText()){
				if(checker.checkDistForQWithSubQs(nTmp2, nTmp1)){
					if(this.lastQWithSubQs == null && this.checker.checkQWithSubQs(nTmp1, nTmp2, nodes, this.currentI)){
						this.updateLastQWithSubQs(currentQ, nodes, cStack, nTmp1);
						qWithSubQsFlag = true;
					}else if(this.lastQWithSubQs != null && 
							this.checker.checkPrefixForQuestionGroup(nTmp1, nTmp2, this.lastQWithSubQsCommonPrefix)){
						if(!this.lastQWithSubQs.getForma().toString().
								startsWith(this.currentP.getForma().toString())){
							this.lastQWithSubQs.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_GROUP"));
						}
						this.lastQWithSubQs.addFilha(this.currentP);
						qWithSubQsFlag = true;
					}else{
						this.saveLastQWithSubQs(currentQ);
					}
				}else if(this.lastQWithSubQs != null){
					this.saveLastQWithSubQs(currentQ);
				}
			}
			
			if(!cStack.isEmpty()){
				//Verifica se não é a imagem da pergunta
				MyNode imgTmp = null;
				cTmp1 = cStack.peek();
				if(this.checker.isOnlyOneImg(cTmp1) && this.distMatrix.areNear(cTmp1, desc))
					imgTmp = cStack.pop().last();
				else if(desc.first().isImage())
					imgTmp = desc.first();
				
				if(imgTmp != null){
					Figura fig = new Figura(imgTmp.getAttr("src"), imgTmp.getAttr("alt"));
					fig.setDono(this.currentP);
					currentQ.addFigura(fig);
					CommonLogger.debug("Figura da pergunta: {}\n", fig);
				}
			}
			
			if(!cStack.isEmpty()){	
				if(currentQ.getAssunto().isEmpty()){
					//Encontra o assunto do questionario
					cTmp1 = cStack.pop();
					this.firstGroupOfQuestionnaire = null;
					
					//Verifica se não é a imagem do questionário
					MyNode imgTmp = null;
					if(this.checker.isOnlyOneImg(cTmp1))
						imgTmp = cTmp1.last();
					else if(cTmp1.first().isImage())
						imgTmp = cTmp1.first();
					
					if(imgTmp != null){
						Figura fig = new Figura(imgTmp.getAttr("src"), imgTmp.getAttr("alt"));
						fig.setDono(currentQ);
						currentQ.addFigura(fig);
						CommonLogger.debug("Figura do questionario: {}\n", fig);
						if(this.checker.isOnlyOneImg(cTmp1)){
							if(!cStack.isEmpty())
								cTmp1 = cStack.pop();
							else
								cTmp1 = null;
						}
					}

					//Verifica se não é o texto de um grupo
					cTmp2 = this.lastMatrixDesc != null ? this.lastMatrixDesc : 
						this.lastQWithSubQsDesc != null ? this.lastQWithSubQsDesc :
							desc;
					if(cTmp1 != null && this.checker.isGroupText(cTmp1, cTmp2, this.firstGroupOfQuestionnaire) &&
							!cStack.isEmpty()){
						currentG = new Grupo(cTmp1.getText());
						currentQ.addGrupo(currentG);
						this.firstGroupOfQuestionnaire = cTmp1;
						
						CommonLogger.debug("\nGroup1: {}\n\n", cTmp1.getText());
						if(!cStack.isEmpty())
							cTmp1 = cStack.pop();
						else
							cTmp1 = null;
					}
					if(cTmp1 != null){
						cTmp1 = this.checker.checkIfDescIsComplete(cTmp1, cStack, nodes, this.currentI);
						currentQ.setAssunto(cTmp1.getText());
						CommonLogger.debug("Assunto: {}\n\n", currentQ.getAssunto());
					}
				}else{
					//Verifica se não é o texto de um grupo
					cTmp1 = cStack.peek();
					cTmp2 = this.lastMatrixDesc != null ? this.lastMatrixDesc : 
						this.lastQWithSubQsDesc != null ? this.lastQWithSubQsDesc :
							desc;
					if(this.checker.isGroupText(cTmp1, cTmp2, this.firstGroupOfQuestionnaire)){
						cTmp1 = cStack.pop();
						this.currentG = new Grupo(cTmp1.getText());
						currentQ.addGrupo(currentG);
						
						CommonLogger.debug("\nGroup2: {}\n\n", cTmp1.getText());
					}
				}
			}

			if(this.currentG != null){
				this.currentP.setGrupo(this.currentG);
				if(this.lastMatrix != null && this.lastMatrix.getGrupo() == null)
					this.lastMatrix.setGrupo(this.currentG);
				if(this.lastQWithSubQs != null && this.lastQWithSubQs.getGrupo() == null)
					this.lastQWithSubQs.setGrupo(this.currentG);
			}	
			
			if(!matrixFlag && !qWithSubQsFlag && !this.currentP.getDescricao().isEmpty())
				currentQ.addPergunta(this.currentP);
		}
		
		return this.currentI;
	}

	private void updateLastQWithSubQs(Questionario currentQ, List<MyNode> nodes, Stack<Cluster> cStack, MyNode nTmp1) {
		if(cStack.isEmpty()) return;
		
		this.lastQWithSubQsDesc = cStack.pop();
		this.lastQWithSubQsDesc = this.checker.
				checkIfDescIsComplete(this.lastQWithSubQsDesc, cStack, nodes, this.currentI);
		this.lastQWithSubQsCommonPrefix = nTmp1.getDewey().
				getCommonPrefix(this.lastQWithSubQsDesc.last().getDewey());
		
		this.lastQWithSubQs = new Pergunta();
		String forma = this.currentP.getForma().toString();
		this.lastQWithSubQs.setForma(FormaDaPerguntaManager.getForma(forma+"_GROUP"));
		this.lastQWithSubQs.setDescricao(this.lastQWithSubQsDesc.getText());							
		this.lastQWithSubQs.addFilha(this.currentP);
	}

	private void saveLastQWithSubQs(Questionario currentQ) {
		if(this.lastQWithSubQs != null){
			currentQ.addPergunta(this.lastQWithSubQs);
			CommonLogger.debug("Q with SubQs descricao: {}\n\n", this.lastQWithSubQs.getDescricao());			
		}
		this.lastQWithSubQs = null;
		this.lastQWithSubQsDesc = null;
		this.lastQWithSubQsCommonPrefix = "";
	}

	private void updateLastMatrix(List<MyNode> nodes, Stack<Cluster> cStack, Cluster cTmp2) {
		if(!cStack.isEmpty() && cTmp2 == cStack.peek())
			this.lastMatrixHead = cStack.pop();
		if(!cStack.isEmpty() && lastMatrix == null){
			this.lastMatrix = new Pergunta();
			FormaDaPergunta forma = this.currentP.getForma();
			if(this.currentP.isA("MIX_COMP_GROUP")){
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_MATRIX"));
			}else{
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma(forma.toString()+"_MATRIX"));
			}
			Cluster desc = cStack.pop();
			if(!cStack.isEmpty() && checker.isEvaluationLevels(desc, cStack)){
				lastMatrixEvaluationLevels = desc;
				desc = cStack.pop();
			}
			desc = this.checker.checkIfDescIsComplete(desc, cStack, nodes, this.currentI);
			this.lastMatrixDesc = desc;
			this.lastMatrix.setDescricao(desc.getText());
		}
		if(this.lastMatrix != null)
			this.lastMatrix.addFilha(this.currentP);
	}

	private void saveLastMatrix(Questionario currentQ) {
		if(this.lastMatrix != null){
			if(this.lastMatrixEvaluationLevels != null)
				this.setEvaluationLevels(this.lastMatrix, this.lastMatrixEvaluationLevels);
			
			currentQ.addPergunta(this.lastMatrix);
			CommonLogger.debug("Matrix descricao: {}\n\n", this.lastMatrix.getDescricao());		
		}
		this.lastMatrixDesc = null;
		this.lastMatrixHead = null;
		this.lastMatrixEvaluationLevels = null;
		this.lastMatrix = null;
	}

	private void setEvaluationLevels(Pergunta perg, Cluster evaluationLevels) {
		if(perg.isA("RADIO_INPUT")){
			ArrayList<Alternativa> alts = perg.getAlternativas();
			
			alts.get(0).setDescricao(evaluationLevels.first().getText() + "\n" +alts.get(0).getDescricao());
			alts.get(alts.size()-1).setDescricao(evaluationLevels.first().getText() + "\n" +alts.get(alts.size()-1).getDescricao());
		}else if(perg.isA("RADIO_INPUT_MATRIX")){
			for(Pergunta p : perg.getFilhas())
				setEvaluationLevels(p, evaluationLevels);
		}
	}

	public void clearData(Questionario currentQ) {
		this.saveLastMatrix(currentQ);
		this.saveLastQWithSubQs(currentQ);
		
		this.currentG = null;
		this.currentP = null;
		this.extractor.setCurrentPergunta(null);
		this.distMatrix.clear();
	}

}
