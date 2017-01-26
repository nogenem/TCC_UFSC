package br.ufsc.tcc.extractor.crawler.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.jsoup.nodes.Node;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.extractor.model.Questionario;

public class QuestionarioBuilder {
	
	private Questionario currentQ;
	private PerguntaBuilder pBuilder;
	private RulesChecker checker;
	
	public QuestionarioBuilder(){
		this.currentQ = null;
		this.checker = new RulesChecker();
		this.pBuilder = new PerguntaBuilder(this.checker);
	}
	
	public ArrayList<Questionario> build(Node root) {
		ArrayList<Questionario> ret = new ArrayList<>();
		List<MyNode> nodes = CommonUtil.findCompsImgsAndTexts(root);
		
		CommonLogger.debug(nodes);
		
		this.currentQ = new Questionario();
		Stack<Cluster> cStack = new Stack<>();
		Cluster cTmp = new Cluster(), lastDesc = null;
		
		for(int i = 0; i<nodes.size(); i++){
			MyNode nTmp = nodes.get(i);
			
			cTmp = this.checker.checkIfNodesShouldBeInSameCluster(cTmp, cStack, nTmp);
			if(nTmp.isImgOrText()){
				//Verifica se tem que criar um novo cluster
				if(this.checker.shouldCreateNewCluster(cTmp, nTmp)){	
//					CommonLogger.debug("{}\n\t{}", cTmp, nTmp);
					cStack.add(cTmp);
					cTmp = new Cluster();
				}
				
				//Verifica se esta começando um novo questinario
				if(!this.currentQ.getPerguntas().isEmpty()){ 
					//Se ja tiver uma pergunta, mas não tiver um assunto do questionario,
					//provavelmente este componente encontrado não faz parte de um questionario
					//ou deu alguma coisa errada
					if(this.currentQ.getAssunto().isEmpty()){
						this.pBuilder.clearData(this.currentQ);
						CommonLogger.debug("1================================================\n");
						this.currentQ = new Questionario();
					}else if(checker.shouldStartNewQuestionario(lastDesc, nTmp)){
						this.pBuilder.clearData(this.currentQ);
						//Um questionario deve ter no minimo 2 perguntas
						if(this.currentQ.getPerguntas().size() >= 2)
							ret.add(this.currentQ);
						CommonLogger.debug("2================================================\n");
						this.currentQ = new Questionario();
					}
				}
				
				cTmp.add(nTmp);
			}else{
				cStack.add(cTmp);				
				i = pBuilder.build(this.currentQ, nodes, i, cStack);
				lastDesc = cTmp;
				cTmp = new Cluster();
			}
		}
		this.pBuilder.clearData(this.currentQ);
		//Um questionario deve ter no minimo 2 perguntas
		if(this.currentQ.getPerguntas().size() >= 2)
			ret.add(this.currentQ);
		
//		CommonLogger.debug("\nClusters:");
//		CommonLogger.debug(cStack);
		
		CommonLogger.debug("\t\t\t========Questionarios========");
		CommonLogger.debug(ret);
		return ret;
	}

}
