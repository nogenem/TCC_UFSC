package br.ufsc.tcc.extractor.crawler.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
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
	
	// last question with subquestions
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
		
		this.lastMatrixHead = null;
		this.lastMatrix = null;
		
		this.lastQWithSubQs = null;
		this.lastQWithSubQsDesc = null;
		this.lastQWithSubQsCommonPrefix = null;
	}

	public int build(Questionario currentQ, List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		if(cStack.isEmpty())
			return i;
		
		this.currentP = new Pergunta();
		this.currentI = i;
		
		MyNode firstNode = nodes.get(this.currentI);
		MyNode nTmp1 = null, nTmp2 = null;
		Cluster desc = cStack.pop();
		Cluster cTmp1 = null, cTmp2 = null;
		
		//Se a desc for apenas uma imagem então ela é provavelmente a imagem
		//da 1* alternativa da pergunta
		//		Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop
		if(this.checker.isOnlyOneImg(desc) && !cStack.isEmpty())
			desc = cStack.pop();
		
		//Verifica se tem componentes em sequência que podem fazer parte de uma
		//matriz ou uma pergunta de RATING
		nTmp1 = (this.currentI+1) < nodes.size() ? nodes.get(this.currentI+1) : null;
		if(nTmp1 != null && firstNode.getType() != MyNodeType.SELECT && nTmp1.isComponent() &&
				this.distMatrix.areNear(firstNode, nTmp1)){
			
			if(this.lastMatrixHead != null && !cStack.isEmpty() &&
					this.checker.isAbove(this.lastMatrixHead.last(), cStack.peek().first())){
				this.saveLastMatrix(currentQ);
			}
			if(this.lastMatrixHead != null || this.checker.isSimpleMatrix(nodes, this.currentI, cStack)){
				if(this.lastMatrixHead == null)
					this.lastMatrixHead = cStack.pop();
				this.extractSimpleMatrix(nodes, currentQ);
			}else if(firstNode.getType() == MyNodeType.RADIO_INPUT && 
					nTmp1.getType() == MyNodeType.RADIO_INPUT){
				//Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop [questão 9]
				this.extractSimpleRating(nodes);
			}else if(firstNode.getType() == nTmp1.getType()){
				//Ex: http://lap.umd.edu/surveys/census/files/surveya1pagesbytopic/page1.html [questão 2a]
				this.extractMultiCompQuestion(nodes, currentQ);
			}
		}else{
			switch (firstNode.getType()) {
			case SELECT:{
				this.extractSelect(nodes);
				break;
			}case CHECKBOX_INPUT:{
				this.extractCheckboxOrRadioInput(currentQ, nodes);
				break;
			}case RADIO_INPUT:{
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
		
		//Verifica se foi possivel extrair a pergunta
		if(this.currentP.getForma() != null){
			ArrayList<Alternativa> tmpAlts = this.currentP.getAlternativas();
			
			//Atualiza a desc da pergunta
			desc = this.checker.getCorrectDescription(desc, tmpAlts, firstNode, cStack);
			desc = this.checker.checkIfDescIsComplete(desc, cStack, nodes, this.currentI);
			
			//Verifica se a desc e a pergunta estão perto uma da outra
			if(!checker.areDescAndPergNear(desc, firstNode))
				return this.currentI;
			
			this.currentP.setDescricao(desc.getText());
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
				cTmp1 = cStack.peek();
				if(this.checker.isOnlyOneImg(cTmp1) && this.distMatrix.areNear(cTmp1, desc)){
					cTmp1 = cStack.pop();
					MyNode tmp = cTmp1.last();
					Figura fig = new Figura(tmp.getAttr("src"), tmp.getAttr("alt"));
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
					if(this.checker.isOnlyOneImg(cTmp1)){
						MyNode tmp = cTmp1.last();
						Figura fig = new Figura(tmp.getAttr("src"), tmp.getAttr("alt"));
						fig.setDono(currentQ);
						currentQ.addFigura(fig);
						CommonLogger.debug("Figura do questionario: {}\n", fig);
						if(!cStack.isEmpty())
							cTmp1 = cStack.pop();
						else
							cTmp1 = null;
					}
					//Verifica se não é o texto de um grupo
					if(cTmp1 != null && this.checker.isGroupText(cTmp1, desc, this.firstGroupOfQuestionnaire) &&
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
					if(this.checker.isGroupText(cTmp1, desc, this.firstGroupOfQuestionnaire)){
						cTmp1 = cStack.pop();
						this.currentG = new Grupo(cTmp1.getText());
						currentQ.addGrupo(currentG);
						
						CommonLogger.debug("\nGroup2: {}\n\n", cTmp1.getText());
					}
				}
			}
			
			//Verifica se o texto abaixo, se tiver, não faz parte desta pergunta (Ex: Peso: [ ] kg)
			if(this.checker.checkNextText(nodes, this.currentI)){
				nTmp1 = nodes.get(++this.currentI);
				this.currentP.setDescricao(
						this.currentP.getDescricao() +"\n"+ nTmp1.getText());
				CommonLogger.debug("Descricao atualizada: {}\n\n", this.currentP.getDescricao());
			}
			
			if(this.currentG != null){
				this.currentP.setGrupo(this.currentG);
				if(this.lastMatrix != null && this.lastMatrix.getGrupo() == null)
					this.lastMatrix.setGrupo(this.currentG);
				if(this.lastQWithSubQs != null && this.lastQWithSubQs.getGrupo() == null)
					this.lastQWithSubQs.setGrupo(this.currentG);
			}	
			
			//TODO verificar se eh login?
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
			
			this.lastQWithSubQs = null;
			this.lastQWithSubQsDesc = null;
			this.lastQWithSubQsCommonPrefix = "";
		}
	}

	private void updateLastMatrix(List<MyNode> nodes, Stack<Cluster> cStack, Cluster cTmp2) {
		if(!cStack.isEmpty() && cTmp2 == cStack.peek())
			this.lastMatrixHead = cStack.pop();
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
			CommonLogger.debug("Matrix descricao: {}\n\n", this.lastMatrix.getDescricao());
		
			this.lastMatrix = null;
			this.lastMatrixHead = null;
		}
	}

	private void extractTextarea(List<MyNode> nodes) {
		CommonLogger.debug("\tTextarea");
		
		currentP.setForma(FormaDaPerguntaManager.getForma("TEXTAREA"));
	}

	private void extractGenericInput(List<MyNode> nodes) {
		String type = nodes.get(this.currentI).getType().toString();
		CommonLogger.debug("\tInput [{}].", type);
		currentP.setForma(FormaDaPerguntaManager.getForma(type));
		
		//Checagem para coisas do genêro: Hora: [ ] : [ ]
		//	Ex: https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b
		if(this.currentI+2 < nodes.size() && type.matches("(TEXT|NUMBER)_INPUT")){
			MyNode tmp1 = nodes.get(this.currentI+1), 
					tmp2 = nodes.get(this.currentI+2);
			if(tmp1.getText().equals(":") && 
					tmp2.isComponent() && tmp2.getType().toString().equals(type)){
				this.currentI += 2;
			}
		}
	}
	
	private void extractMultiCompQuestion(List<MyNode> nodes, Questionario currentQ) {
		MyNode input = null, lastInput = null;
		MyNodeType multiCompType = null;
		int i = 1;
		boolean error = false;
		
		currentP.setForma(FormaDaPerguntaManager.getForma("MULTI_COMP"));
		CommonLogger.debug("\tMulti Comp:");
		
		input = nodes.get(this.currentI);
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
			if(this.currentI+1 < nodes.size())
				input = nodes.get(++this.currentI);
			else
				input = null;
		}
		//Se input != null então o loop passo da pergunta e entro na proxima e por isso,
		//deve-se voltar o index para o final da pergunta
		if(input != null)
			--this.currentI;
		//Se deu erro e não tem nenhuma filha/alternativa quer dizer que o loop não
		//completo nenhuma vez
		if(error && this.currentP.getFilhas().size() == 0)
			this.currentP.setForma(null);
	}

	private void extractSimpleRating(List<MyNode> nodes) {
		MyNode input = null, lastInput = null;
		int i = 1;//TODO adicionar 0 tb?
		boolean error = false;
				
		currentP.setForma(FormaDaPerguntaManager.getForma("RATING"));
		CommonLogger.debug("\tRating:");
		
		input = nodes.get(this.currentI);
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
		String txt = "";
		
		input = nodes.get(this.currentI);
		
		this.currentP.setForma(FormaDaPerguntaManager.getForma(input.getType().toString()));
		if(input.getType() == MyNodeType.CHECKBOX_INPUT)
			CommonLogger.debug("\tCheckbox Input:");
		else
			CommonLogger.debug("\tRadio Input:");
		
		img = nodes.get(this.currentI-1);
		text = nodes.get(++this.currentI);
		tmp = nodes.get(this.currentI+1);
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
			CommonLogger.debug("\t\t{}", txt);
			Object dono = null;
			
			if(tmp != null){
				//Ex: https://www.surveymonkey.com/r/CAHPS-Health-Plan-Survey-40-Template [pergunta 8]
				if(text.getType() == MyNodeType.TEXT && tmp.getType() == MyNodeType.TEXT && 
						this.checker.checkDistForTextsOfAlternative(text, tmp)){
					txt = txt + tmp.getText();
					++this.currentI;
					tmp = this.currentI+1 < nodes.size() ? nodes.get(this.currentI+1) : null;
				}
				
				if(tmp != null && tmp.getType() == MyNodeType.TEXT_INPUT && 
						this.checker.areCompAndTextNear(tmp, text)){
					Pergunta tmpPerg = new Pergunta(txt);
					dono = tmpPerg;
					
					tmpPerg.setForma(FormaDaPerguntaManager.getForma("TEXT_INPUT"));
					CommonLogger.debug("\t\t\tCom Text Input.");
					
					tmpPerg.setQuestionario(currentQ);
					this.currentP.addFilha(tmpPerg);
				} 
			}
			
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
		CommonLogger.debug("\tSelect:");
		
		opt = nodes.get(++this.currentI);
		text = nodes.get(++this.currentI);
		while(opt != null && opt.getType() == MyNodeType.OPTION){
			if(!this.checker.areCompAndTextNear(opt, text)){
				error = true;
				break;
			}
			
			Alternativa tmpAlt = new Alternativa(text.getText());
			this.currentP.addAlternativa(tmpAlt);
			CommonLogger.debug("\t\t{}", tmpAlt.getDescricao());
			
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
		//Esta matriz possui componentes mistos?
		boolean isMix = false;
		int j = 0;
		
		CommonLogger.debug("\tSimple Matrix:");
		input = nodes.get(this.currentI);
		while(input != null && j < this.lastMatrixHead.size() && input.isComponent()){
			if(!isMix && lastCompType != null && lastCompType != input.getType())
				isMix = true;
			lastCompType = input.getType();
			
			String text = this.lastMatrixHead.get(j).getText();
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
		this.saveLastMatrix(currentQ);
		this.saveLastQWithSubQs(currentQ);
		
		this.currentG = null;
		this.currentP = null;
	}

}
