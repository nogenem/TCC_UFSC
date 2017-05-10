package br.ufsc.tcc.extractor.builder;

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
	private String currentLink;
	private PerguntaBuilder pBuilder;
	private RulesChecker checker;
	
	// Construtores
	public QuestionarioBuilder(){
		this.currentQ = null;
		this.currentLink = "";
		this.checker = new RulesChecker();
		this.pBuilder = new PerguntaBuilder(this.checker);
	}
	
	// Getters e Setters
	public String getCurrentLink(){
		return this.currentLink;
	}
	
	public void setCurrentLink(String link){
		this.currentLink = link;
	}
	
	// Demais métodos
	public ArrayList<Questionario> build(Node root) {
		ArrayList<Questionario> ret = new ArrayList<>();
		List<MyNode> nodes = CommonUtil.findCompsImgsAndTexts(root);
		
		CommonLogger.debug(nodes);
		
		this.currentQ = new Questionario(this.currentLink);
		Stack<Cluster> cStack = new Stack<>();
		Cluster cTmp = new Cluster(), lastDesc = null;
		
		for(int i = 0; i<nodes.size(); i++){
			MyNode nTmp = nodes.get(i);
			
			if(nTmp.isImgOrText()){
				//Verifica se tem que criar um novo cluster
				if(this.checker.shouldCreateNewCluster(cTmp, nTmp, nodes, i)){	
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
						CommonLogger.debug("==================getAssunto().isEmpty()==================\n");
						this.currentQ = new Questionario(this.currentLink);
					}else if(checker.shouldStartNewQuestionario(lastDesc, nTmp)){
						this.pBuilder.clearData(this.currentQ);
						if(this.checker.isValidQuestionnaire(this.currentQ))	
							ret.add(this.currentQ);
						CommonLogger.debug("==================shouldStartNewQuestionario()==================\n");
						this.currentQ = new Questionario(this.currentLink);
					}
				}
				
				cTmp.add(nTmp);
			}else{
				if(!cTmp.isEmpty()){
					cStack.add(cTmp);				
					lastDesc = cTmp;
				}
				i = pBuilder.build(this.currentQ, nodes, i, cStack);
				cTmp = new Cluster();
			}
		}
		this.pBuilder.clearData(this.currentQ);
		if(this.checker.isValidQuestionnaire(this.currentQ))	
			ret.add(this.currentQ);
		
//		cStack.add(cTmp);
//		CommonLogger.debug("\nClusters:");
//		CommonLogger.debug(cStack);
		
		CommonLogger.debug("\t\t\t========Questionarios========");
		CommonLogger.debug(ret);
		return ret;
	}
}
