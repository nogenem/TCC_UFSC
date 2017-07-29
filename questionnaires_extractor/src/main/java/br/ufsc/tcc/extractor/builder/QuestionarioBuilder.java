package br.ufsc.tcc.extractor.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Node;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.common.util.DistanceMatrix;
import br.ufsc.tcc.extractor.model.Questionario;

/**
 * Classe responsável por 'montar'/'construir' os questionários de 
 * uma pagina Web.
 * 
 * @author Gilney N. Mathias
 *
 */
public class QuestionarioBuilder {
	
	private static int MAX_TEXT_CLUSTERS_BETWEEN_QUESTIONS = 0;
	
	private Questionario currentQ;
	private String currentLink;
	private DistanceMatrix distMatrix;
	private PerguntaBuilder pBuilder;
	private RulesChecker checker;
	
	// Construtores
	public QuestionarioBuilder(){
		this.currentQ = null;
		this.currentLink = "";
		this.distMatrix = new DistanceMatrix();
		
		this.checker = new RulesChecker(this.distMatrix);
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
	public ArrayList<Questionario> build(Node root, String docTitle) {
		ArrayList<Questionario> ret = new ArrayList<>();
		List<MyNode> nodes = CommonUtil.findCompsImgsAndTexts(root);
		
		CommonLogger.debug(nodes);
		
		this.currentQ = new Questionario(this.currentLink);
		Stack<Cluster> cStack = new Stack<>();
		Cluster cTmp = new Cluster(), lastDesc = null;
		int clustersWithTextSinceLastQuestion = 0;
		
		for(int i = 0; i<nodes.size(); i++){
			MyNode nTmp = nodes.get(i);
			
			if(nTmp.isImgOrText()){
				//Verifica se tem que criar um novo cluster
				if(this.checker.shouldCreateNewCluster(cTmp, nTmp, nodes, i)){	
//					CommonLogger.debug("\n{}\n\t{}\n", cTmp, nTmp);
					cStack.add(cTmp);
					cTmp = new Cluster();
					clustersWithTextSinceLastQuestion++;
				}
				
				//Verifica se esta começando um novo questinario
				if(!this.currentQ.getPerguntas().isEmpty() || pBuilder.hasBuildBegun()){ 
					if(clustersWithTextSinceLastQuestion > MAX_TEXT_CLUSTERS_BETWEEN_QUESTIONS || 
							checker.shouldStartNewQuestionario(lastDesc, nTmp)){
						this.pBuilder.clearData(this.currentQ);
						if(this.checker.isValidQuestionnaire(this.currentQ)){
							//Ex: https://polldaddy.com/s/d5564eb1c42db4d1
							if(this.currentQ.getAssunto().isEmpty() || CommonUtil.isOnlyOneWord(this.currentQ.getAssunto())){
								this.currentQ.setAssunto(docTitle);
								CommonLogger.debug("Assunto: {}\n\n", currentQ.getAssunto());
							}
							ret.add(this.currentQ);
						}else
							CommonLogger.debug("================== Questionario invalido! ==================");
						CommonLogger.debug("================== shouldStartNewQuestionario() ==================\n");
						this.currentQ = new Questionario(this.currentLink);
					}
				}
				
				cTmp.add(nTmp);
			}else{
				if(!cTmp.isEmpty()){
					cStack.add(cTmp);				
					lastDesc = cTmp;
					clustersWithTextSinceLastQuestion = 0;
				}
				i = pBuilder.build(this.currentQ, nodes, i, cStack);
				cTmp = new Cluster();
			}
		}
		this.pBuilder.clearData(this.currentQ);
		if(this.checker.isValidQuestionnaire(this.currentQ)){	
			//Ex: https://polldaddy.com/s/d5564eb1c42db4d1
			if(this.currentQ.getAssunto().isEmpty() || CommonUtil.isOnlyOneWord(this.currentQ.getAssunto())){
				this.currentQ.setAssunto(docTitle);
				CommonLogger.debug("Assunto: {}\n\n", currentQ.getAssunto());
			}
			ret.add(this.currentQ);
		}else if(!this.currentQ.getPerguntas().isEmpty())
			CommonLogger.debug("================== Questionario invalido! ==================");
		
//		cStack.add(cTmp);
//		CommonLogger.debug("\nClusters:");
//		CommonLogger.debug(cStack);
		
		CommonLogger.debug("\t\t\t================> Questionarios <================");
		CommonLogger.debug(ret);
		return ret;
	}
	
	// Métodos/Blocos estáticos
	static {
		//Load parameters
		JSONObject p = ProjectConfigs.getParameters();
		try{
			//Ex: http://www.sciencebuddies.org/science-fair-projects/project_ideas/Soc_survey_sample1.shtml
			MAX_TEXT_CLUSTERS_BETWEEN_QUESTIONS = p.getInt("maxTextClustersBetweenQuestions");
			if(MAX_TEXT_CLUSTERS_BETWEEN_QUESTIONS <= 0) MAX_TEXT_CLUSTERS_BETWEEN_QUESTIONS = 4;
		}catch(JSONException exp){
			CommonLogger.fatalError(exp);
		}
		CommonLogger.debug("QuestionarioBuilder:> Static block executed!");
	}
}
