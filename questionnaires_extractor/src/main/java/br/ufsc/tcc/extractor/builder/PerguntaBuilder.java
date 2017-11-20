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
import br.ufsc.tcc.extractor.extractor.IPerguntaExtractor;
import br.ufsc.tcc.extractor.extractor.PerguntaExtractorFactory;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.FormaDaPergunta;
import br.ufsc.tcc.extractor.model.Grupo;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

/**
 * Classe responsável por 'montar'/'construir' as perguntas de um questionário 
 * utilizando certos padrões.<br>
 * Ela também é responsável por achar outras características importantes de
 * um questionário, como o seu assunto e imagens relacionadas ao mesmo.
 * 
 * @author Gilney N. Mathias
 */
public class PerguntaBuilder {
	// Current
	private Pergunta currentP;
	private Grupo currentG;
	private int currentI;
	
	// Helpers
	private DistanceMatrix distMatrix;
	private RulesChecker checker;
	
	// Group
	private Cluster firstGroupOfQuestionnaire;

	// Matrix
	private Cluster lastMatrixDesc;
	private Cluster lastMatrixHead;
	private Cluster lastMatrixEvaluationLevels;
	private Pergunta lastMatrix;
	private String lastMatrixCommonPrefix;
	
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
		
		this.firstGroupOfQuestionnaire = null;
		
		this.lastMatrixDesc = null;
		this.lastMatrixHead = null;
		this.lastMatrixEvaluationLevels = null;
		this.lastMatrix = null;
		//Criado por causa deste link [2* perg tem as mesmas alternativas que a head da matriz]:
		//	Ex: https://survey.com.br/preview/hotel-satisfaction-feedback-survey?template=true
		this.lastMatrixCommonPrefix = "";
		
		this.lastQWithSubQs = null;
		this.lastQWithSubQsDesc = null;
		this.lastQWithSubQsCommonPrefix = "";
	}
	
	public boolean hasBuildBegun() {
		return this.lastQWithSubQs != null ||
				this.lastMatrix != null;
	}

	public int build(Questionario currentQ, List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		if(cStack.isEmpty())
			return i;
		
		this.currentP = new Pergunta();
		this.currentI = i;
		
		IPerguntaExtractor extractor = null;
		MyNode firstNode = nodes.get(this.currentI), questionLastNode = null,
				firstImg = null;
		MyNode nTmp1 = (this.currentI+1) < nodes.size() ? nodes.get(this.currentI+1) : null, 
				nTmp2 = null;
		Cluster desc = cStack.pop();
		Cluster cTmp1 = null, cTmp2 = null;
		boolean isSimpleMatrix = false;
		
		//Se a desc for apenas uma imagem então ela é provavelmente a imagem
		//da 1* alternativa da pergunta; e se a desc for igual a '('e o próximo nodo 
		//for igual a ')' então provavelmente deve ser uma pergunta de telefone: ( [ ] ) [ ]
		//		Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop
		//		Ex: http://www.almaderma.com.br/formulario/florais/infantil/contato.php
		if(!cStack.isEmpty() && this.checker.isOnlyOneImg(desc)) {
			firstImg = desc.last();
			desc = cStack.pop();
		}else if(!cStack.isEmpty() && desc.getText().equals("(") && 
				nTmp1 != null && nTmp1.getText().equals(")")){
			desc = cStack.pop();
		}
		
		//Verifica se tem componentes em sequência
		if(nTmp1 != null && firstNode.getType() != MyNodeType.SELECT && nTmp1.isComponent() &&
				!nTmp1.isATextInputDisabledWithValue() &&
				this.distMatrix.areNear(firstNode, nTmp1)){
			
			if(this.lastMatrixHead != null && !cStack.isEmpty() &&
					this.checker.isAbove(this.lastMatrixHead.last(), cStack.peek().first())){
				this.saveLastMatrix(currentQ);
			}
			if(this.checker.isSimpleMatrix(this.lastMatrixHead, nodes, this.currentI, cStack)){
				if(this.lastMatrixHead == null)
					this.lastMatrixHead = cStack.pop();
				
				//Ex: http://anpei.tempsite.ws/intranet/mediaempresa/
				//Ex: https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw
				extractor = PerguntaExtractorFactory.getExtractor("SIMPLE_MATRIX", currentQ, 
						this.currentP, this.checker);
				this.currentI = extractor.extract(this.lastMatrixHead, nodes, this.currentI);
				isSimpleMatrix = true;
			}else if(this.checker.isRadioInputOrCheckboxWithHeader(nodes, currentI, desc)){
				
				//Ex: http://infopoll.net/live/surveys/s32805.htm
				extractor = PerguntaExtractorFactory.getExtractor("CHOICE_INPUT_WITH_HEADER", currentQ, 
						this.currentP, this.checker);
				this.currentI = extractor.extract(desc, nodes, this.currentI);
				desc = !cStack.isEmpty() ? cStack.pop() : null;
			}else if(firstNode.getType() == MyNodeType.RADIO_INPUT && 
					nTmp1.getType() == MyNodeType.RADIO_INPUT){
				
				//Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop [questão 9]
				extractor = PerguntaExtractorFactory.getExtractor("RATING", currentQ, 
						this.currentP, this.checker);
				this.currentI = extractor.extract(null, nodes, this.currentI);
			}else if(firstNode.getType() == nTmp1.getType()){
				
				//Ex: http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html [questão 2a]
				extractor = PerguntaExtractorFactory.getExtractor("MULTI_COMP", currentQ, 
						this.currentP, this.checker);
				this.currentI = extractor.extract(null, nodes, this.currentI);
			}
		}else{
			//Checagem duplicada mas, infelizmente, necessária
			if((firstNode.isA("CHECKBOX_INPUT") || firstNode.isA("RADIO_INPUT")) && 
					checker.checkIfTextIsAbove(nodes, currentI))
				desc = cStack.pop();
			extractor = PerguntaExtractorFactory.getExtractor(firstNode.getType().toString(), currentQ, 
					this.currentP, this.checker);
			this.currentI = extractor.extract(desc, nodes, this.currentI);
		}
		extractor = null;
		
		//Verifica se foi possível extrair a pergunta
		if(this.currentP.getForma() != null){
			this.currentP.convertFormaToTipo();
			
			ArrayList<Alternativa> tmpAlts = this.currentP.getAlternativas();
			
			//Atualiza a desc da pergunta
			if(!cStack.isEmpty() && checker.isEvaluationLevels(desc, cStack, false)){
				this.setEvaluationLevels(this.currentP, desc);
				desc = cStack.pop();
			}
			desc = this.checker.getCorrectDescription(desc, tmpAlts, firstNode, cStack);
			
			//Verifica se a desc e a pergunta estão perto uma da outra
			if(!checker.areDescAndPergNear(desc, firstNode))
				return this.currentI;
			
			// Atualiza a firstImg
			if(firstImg == null || currentQ.hasFigura(firstImg))
				firstImg = desc.last();
			
			questionLastNode = nodes.get(this.currentI);
			
			//Verifica se o texto abaixo, se tiver, não faz parte desta pergunta (Ex: Peso: [ ] kg)
			boolean foundComplementaryText = false;
			while(this.checker.checkComplementaryText(nodes, this.currentI)){
				foundComplementaryText = true;
				nTmp1 = nodes.get(++this.currentI);
				//Se for um CHECKBOX ou RADIO INPUT, então o texto complementar deve
				//pertencer a uma opção 'Outro' que usa um TEXT INPUT
				//	Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto [questão 2] 
				if(this.currentP.isA("CHECKBOX_INPUT") || this.currentP.isA("RADIO_INPUT")){
					if(this.currentP.getFilhas().size() > 0){
						ArrayList<Pergunta> filhas = this.currentP.getFilhas();
						Pergunta p = filhas.get(filhas.size()-1);
						if(nTmp1.isATextInputDisabledWithValue())
							p.setDescricao(p.getDescricao() +"\n"+ nTmp1.getAttr("value"));
						else
							p.setDescricao(p.getDescricao() +"\n"+ nTmp1.getText());
					}else 
						desc.add(nTmp1);
				}else 
					desc.add(nTmp1);
			}
			desc = this.checker.checkIfDescIsComplete(desc, cStack, nodes, this.currentI);
			String descTxt = desc.getText(foundComplementaryText);
			
			//Ex: http://www.createsurvey.com/cgi-bin/pollfrm?s=36960&m=FWIOpt&presurvey_view=1
			if(descTxt.matches("\\d\n\\d"))
				return this.currentI;
			
			//Seta a descrição da pergunta
			this.currentP.setDescricao(descTxt);
			CommonLogger.debug("Descricao: {}\n\n", this.currentP.getDescricao());
			
			//Verifica se é uma matriz
			//	Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto [questão 3]
			boolean matrixFlag = false;
			nTmp1 = desc.first();
			cTmp2 = this.lastMatrixHead;
			
			if(!cStack.isEmpty()){
				if(cTmp2 == null){
					if(!currentQ.getAssunto().isEmpty() || cStack.size() >= 2)
						cTmp2 = cStack.peek();
				}else { 
					//Ex: https://survs.com/survey-templates/teacher-evaluation-survey/
					if(lastQWithSubQsDesc != null && this.checker.isAbove(lastQWithSubQsDesc.last(), cTmp2.first()))
						this.saveLastQWithSubQs(currentQ);
					if(this.checker.isAbove(cTmp2.last(), cStack.peek().first())) {
						//Se tiver um texto no meio quer dizer que ja termino a matriz
						this.saveLastMatrix(currentQ);
						cTmp2 = cStack.peek();
					}
				}
			}
			
			if(this.lastMatrix == null || isSimpleMatrix || 
					this.checker.checkCommonPrefix(cTmp2.last(), nTmp1, this.lastMatrixCommonPrefix)) {
				matrixFlag = this.checker.hasSameTexts(this.currentP, cTmp2);
				if(matrixFlag){
					this.updateLastMatrix(nodes, cStack, currentQ, nTmp1, cTmp2);
				}else if(this.lastMatrix != null){
					this.saveLastMatrix(currentQ);
				}
			}else {
				this.saveLastMatrix(currentQ);
			}
			
			//Verifica se é uma pergunta com subperguntas
			//	Ex: https://www.surveymonkey.com/r/online-social-networking-template [questão 4]
			boolean qWithSubQsFlag = false;
			
			nTmp1 = questionLastNode;//XXX qual será o melhor?
//			nTmp1 = firstNode;
//			nTmp1 = desc.first();
			
			cTmp1 = this.lastQWithSubQsDesc != null ? 
					this.lastQWithSubQsDesc : null;
			nTmp2 = cTmp1==null ? null : cTmp1.first();
			
			if(!cStack.isEmpty()){
				if(cTmp1 == null){
					if(!currentQ.getAssunto().isEmpty() || cStack.size() >= 2)
						cTmp1 = cStack.peek();
				}else if( (lastMatrixDesc != null && this.checker.isAbove(nTmp2, lastMatrixDesc.first())) ||
						this.checker.isAbove(nTmp2, cStack.peek().first())){
					//Se tiver um texto no meio quer dizer que ja termino as subperguntas
					this.saveLastQWithSubQs(currentQ);
					cTmp1 = cStack.peek();
				}
			}
			
			if(!matrixFlag) {
				if(cTmp1 != null && this.lastQWithSubQsDesc == null)
					cTmp1 = this.checker.checkIfDescIsCompleteWithClone(cTmp1, cStack, nodes, this.currentI);
				nTmp2 = cTmp1==null ? null : cTmp1.first();
				
				if(nTmp2 != null && nTmp2.isText()){
					if(checker.checkDistForQWithSubQs(nTmp2, nTmp1)){
						if(this.lastQWithSubQs == null && this.checker.
								isQWithSubQs(nTmp1, nTmp2, nodes, this.currentI)){
							this.updateLastQWithSubQs(currentQ, nodes, cStack, nTmp1);
							qWithSubQsFlag = true;
						}else if(this.lastQWithSubQs != null && 
								this.checker.checkCommonPrefix(nTmp1, nTmp2, this.lastQWithSubQsCommonPrefix)){
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
			}
			
			//Verifica se não é a imagem da pergunta
			if(!firstImg.isImage() || currentQ.hasFigura(firstImg)) {
				if(!cStack.isEmpty()) {
					cTmp1 = cStack.peek();
					if(this.checker.isOnlyOneImg(cTmp1) && this.distMatrix.areNear(cTmp1, desc))
						firstImg = cStack.pop().last();
					else if(desc.first().isImage())
						firstImg = desc.first();
				}
			}
			if(firstImg.isImage() && !currentQ.hasFigura(firstImg)) {
				Figura fig = new Figura(firstImg.getAttr("src"), firstImg.getAttr("alt"));
				fig.setDono(this.currentP);
				currentQ.addFigura(fig);
				CommonLogger.debug("Figura da pergunta: {}\n", fig);
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
					if(cTmp1 != null && this.checker.isAGroupText(cTmp1, cTmp2, this.firstGroupOfQuestionnaire) &&
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
					
					//Seta o assunto do questionário atual
					if(cTmp1 != null) {
						cTmp1 = this.checker.checkIfDescIsComplete(cTmp1, cStack, nodes, this.currentI);
						currentQ.setAssunto(cTmp1.getText());
						CommonLogger.debug("Assunto1: {}\n\n", currentQ.getAssunto());
					}
				}else{
					//Verifica se não é o texto de um grupo
					cTmp1 = cStack.peek();
					cTmp2 = this.lastMatrixDesc != null ? this.lastMatrixDesc : 
						this.lastQWithSubQsDesc != null ? this.lastQWithSubQsDesc :
							desc;
					if(this.checker.isAGroupText(cTmp1, cTmp2, this.firstGroupOfQuestionnaire)){
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
	
	/**
	 * Atualiza os dados da ultima pergunta com subperguntas encontrada.
	 * 
	 * @param currentQ
	 * @param nodes
	 * @param cStack
	 * @param nTmp1
	 */
	private void updateLastQWithSubQs(Questionario currentQ, List<MyNode> nodes, 
			Stack<Cluster> cStack, MyNode nTmp1) {
		if(cStack.isEmpty()) return;
		
		this.lastQWithSubQsDesc = cStack.pop();
		this.lastQWithSubQsDesc = this.checker.
				checkIfDescIsComplete(this.lastQWithSubQsDesc, cStack, nodes, this.currentI);
		this.lastQWithSubQsCommonPrefix = nTmp1.getDewey().
				getCommonPrefix(this.lastQWithSubQsDesc.first().getDewey());
		
		this.lastQWithSubQs = new Pergunta();
		String forma = this.currentP.getForma().toString();
		this.lastQWithSubQs.setForma(FormaDaPerguntaManager.getForma(forma+"_GROUP"));
		this.lastQWithSubQs.setDescricao(this.lastQWithSubQsDesc.getText());							
		this.lastQWithSubQs.addFilha(this.currentP);
		
		if(this.lastQWithSubQsDesc.last().isImage()) {
			Figura fig = new Figura(this.lastQWithSubQsDesc.last().getAttr("src"), 
					this.lastQWithSubQsDesc.last().getAttr("alt"));
			fig.setDono(this.lastMatrix);
			currentQ.addFigura(fig);
			CommonLogger.debug("Figura da qWithSubQ: {}\n", fig);
		}
	}
	
	/**
	 * Adiciona a ultima pergunta com subperguntas encontrada ao questionário atual.
	 * 
	 * @param currentQ		Questionário atual.
	 */
	private void saveLastQWithSubQs(Questionario currentQ) {
		if(this.lastQWithSubQs != null){
			this.lastQWithSubQs.convertFormaToTipo();
			currentQ.addPergunta(this.lastQWithSubQs);
			CommonLogger.debug("Q with SubQs descricao: {}\n\n", this.lastQWithSubQs.getDescricao());			
		}
		this.lastQWithSubQs = null;
		this.lastQWithSubQsDesc = null;
		this.lastQWithSubQsCommonPrefix = "";
	}
	
	
	/**
	 * Atualiza os dados da ultima matriz encontrada.
	 * 
	 * @param nodes
	 * @param cStack
	 * @param descFirstNode 
	 * @param cTmp2
	 */
	private void updateLastMatrix(List<MyNode> nodes, Stack<Cluster> cStack, 
			Questionario currentQ, MyNode descFirstNode, Cluster cTmp2) {
		if(!cStack.isEmpty() && cTmp2 == cStack.peek())
			this.lastMatrixHead = cStack.pop();
		
		if(lastMatrix == null) {
			this.lastMatrix = new Pergunta();
			FormaDaPergunta forma = this.currentP.getForma();
			if(this.currentP.isA("MIX_COMP_GROUP")){
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma("MIX_COMP_MATRIX"));
			}else{
				this.lastMatrix.setForma(FormaDaPerguntaManager.getForma(forma.toString()+"_MATRIX"));
			}
			
			this.lastMatrixCommonPrefix = this.lastMatrixHead.last().getDewey()
					.getCommonPrefix(descFirstNode.getDewey());
			
			Cluster desc = !cStack.isEmpty() ? cStack.peek() : null;
			if(desc != null) {
				if(!cStack.isEmpty() && checker.isEvaluationLevels(desc, cStack, true)){
					desc = cStack.pop();
					lastMatrixEvaluationLevels = desc;
					desc = !cStack.isEmpty() ? cStack.peek() : null;
				}
				
				//Verifica se a desc e o cabeçalho da matriz estão perto um do outro
				if(desc != null && checker.areDescAndPergNear(desc, this.lastMatrixHead.first())) {
					desc = cStack.pop();
					desc = this.checker.checkIfDescIsComplete(desc, cStack, nodes, this.currentI);
					
					if(desc.last().isImage()) {
						Figura fig = new Figura(desc.last().getAttr("src"), desc.last().getAttr("alt"));
						fig.setDono(this.lastMatrix);
						currentQ.addFigura(fig);
						CommonLogger.debug("Figura da matriz: {}\n", fig);
					}
					
					this.lastMatrixDesc = desc;
					this.lastMatrix.setDescricao(desc.getText());		
				}else {
					//Matriz sem descrição
					//	Ex: http://www.questionpro.com/survey-templates/employee-benefits-survey/
					this.lastMatrixDesc = null;
					this.lastMatrix.setDescricao("");
				}
			}else {
				//Caso meio raro...
				//	Ex: http://www.123contactform.com/js-form--1941084.html
				this.lastMatrixDesc = null;
				this.lastMatrix.setDescricao("");
			}
		}
		
		if(this.lastMatrix != null)
			this.lastMatrix.addFilha(this.currentP);
	}
	
	
	/**
	 * Adiciona a ultima matriz encontrada ao questionário atual.
	 * 
	 * @param currentQ		Questionário atual.
	 */
	private void saveLastMatrix(Questionario currentQ) {
		if(this.lastMatrix != null){
			if(this.lastMatrixEvaluationLevels != null)
				this.setEvaluationLevels(this.lastMatrix, this.lastMatrixEvaluationLevels);
			this.removePergDescFromAltDesc(this.lastMatrix);
			this.lastMatrix.convertFormaToTipo();
			
			currentQ.addPergunta(this.lastMatrix);
			CommonLogger.debug("Matrix descricao: {}\n\n", this.lastMatrix.getDescricao());		
		}
		this.lastMatrixDesc = null;
		this.lastMatrixHead = null;
		this.lastMatrixEvaluationLevels = null;
		this.lastMatrix = null;
		this.lastMatrixCommonPrefix = "";
	}
	
	
	/**
	 * Remove a descrição das perguntas filhas da Matriz da 
	 * descrição de suas alternativas.
	 * 
	 * @param matrix
	 */
	private void removePergDescFromAltDesc(Pergunta matrix) {
		//Ex: http://www.surveymoz.com/s/evaluation-of-company-and-supervisor-example
		String pDesc = "", aDesc = "";

		for(Pergunta perg : matrix.getFilhas()) {
			pDesc = perg.getDescricao();
			if(!perg.getAlternativas().isEmpty()) {
				for(Alternativa alt : perg.getAlternativas()) {
					aDesc = alt.getDescricao();
					alt.setDescricao(aDesc.replace(pDesc, "").trim());
				}
			}else {
				for(Pergunta p : perg.getFilhas()) {
					aDesc = p.getDescricao();
					p.setDescricao(aDesc.replace(pDesc, "").trim());
				}
			}
		}
	}
	

	private void setEvaluationLevels(Pergunta perg, Cluster evaluationLevels) {
		if(perg.isA("RADIO_INPUT")){
			ArrayList<Alternativa> alts = perg.getAlternativas();
			
			alts.get(0).setDescricao(evaluationLevels.first().getText() + 
					"\n" +alts.get(0).getDescricao());
			alts.get(alts.size()-1).setDescricao(evaluationLevels.last().getText() + 
					"\n" +alts.get(alts.size()-1).getDescricao());
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
		this.distMatrix.clear();
	}

}
