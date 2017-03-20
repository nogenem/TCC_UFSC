package br.ufsc.tcc.extractor.crawler.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.json.JSONObject;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.common.util.DistanceMatrix;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;

public class RulesChecker {
	
	private static JSONObject CONFIGS = null;
	
	private DistanceMatrix distMatrix;
	
	public RulesChecker() {
		this.distMatrix = new DistanceMatrix();
	}
	
	// Getters e Setters
	public DistanceMatrix getDistMatrix(){
		return this.distMatrix;
	}
	
	public static JSONObject getConfigs(){
		return CONFIGS;
	}
	
	// Demais métodos
	
	// Métodos usados pela classe QuestionarioBuilder
	public boolean shouldStartNewQuestionario(Cluster lastDesc, MyNode newNode) {
		if(lastDesc == null || lastDesc.isEmpty() || newNode == null) 
			return false;
		JSONObject obj = CONFIGS.getJSONObject("distBetweenTextsInsideQuestionnaire");
		Dewey dist = this.distMatrix.getDist(lastDesc.last(), newNode);	
		if(dist.getHeight() > obj.getInt("height"))
			return true;
		
		//Verifica se o 1* container é diferente
		Dewey d1 = lastDesc.last().getDewey(), d2 = newNode.getDewey();
		return d1.getNumbers().get(1) != d2.getNumbers().get(1);
	}
	
	
	public boolean shouldCreateNewCluster(Cluster lastCluster, MyNode newNode, List<MyNode> nodes, int i) {
		if(lastCluster == null || lastCluster.isEmpty() || newNode == null || 
				i+1 >= nodes.size()) 
			return false;
		
		if(!this.distMatrix.areNear(lastCluster.last(), newNode))
			return true;
		
		//Todos os elementos do cluster devem ter o mesmo prefixo
		//comum ao proximo nodo encontrado
		String tTmp1 = lastCluster.last().getDewey().getCommonPrefix(nodes.get(i+1).getDewey()), 
				tTmp2 = newNode.getDewey().getCommonPrefix(nodes.get(i+1).getDewey());
		return !tTmp1.equals(tTmp2);		
	}

	// Métodos usados pela classe PerguntaBuilder
	public boolean isOnlyOneImg(Cluster c) {
		return c.size() == 1 && c.first().isImage();
	}

	public boolean areCompAndTextNear(MyNode comp, MyNode text) {
		if(comp == null || text == null) 
			return false;
		JSONObject obj = CONFIGS.getJSONObject("distBetweenCompAndText");
		Dewey dist = this.distMatrix.getDist(comp, text);
		return dist.getHeight() <= obj.getInt("height") && 
				dist.getMaxHeight() <= obj.getInt("maxHeight");
	}

	public Cluster getCorrectDescription(Cluster desc, ArrayList<Alternativa> tmpAlts, MyNode firstNode,
			Stack<Cluster> cStack) {
		if(desc == null || desc.isEmpty()) return desc;
		
		boolean flag = true;
		String altsTxt = "";
		
		//Pega o texto de todas as alternativas da perguta
		for(Alternativa alt : tmpAlts){
			altsTxt += alt.getDescricao()+"\n";
		}
		
		//Ex: https://www.surveymonkey.com/r/General-Event-Feedback-Template [questão 1]
		while(true){
			for(MyNode node : desc.getGroup()){
				String txt = node.getText();
				flag = flag && !txt.isEmpty() && altsTxt.contains(txt);
			}
			if(flag && !cStack.isEmpty()){
				desc = cStack.pop();
			}else
				break;
		}
		return desc;
	}
	
	public Cluster checkIfDescIsComplete(Cluster desc, Stack<Cluster> cStack, List<MyNode> nodes, int i){
		if(desc == null || desc.isEmpty() || cStack.isEmpty() || i+1 >= nodes.size())
			return desc;
		
		Cluster tmp = cStack.peek();
		if(!tmp.getText().isEmpty() && this.isDescriptionsNear(tmp, desc) &&
				!this.distMatrix.areNear(tmp.last(), nodes.get(i+1))){
			tmp = cStack.pop();
			tmp = tmp.join(desc);
			return tmp;
		}
		return desc;
	}
	
	public boolean isDescriptionsNear(Cluster firstDesc, Cluster secondDesc){
		JSONObject obj = CONFIGS.getJSONObject("distBetweenDescriptions");
		Dewey dist = this.distMatrix.getDist(firstDesc.last(), secondDesc.first());
		return dist.getHeight() <= obj.getInt("height") && 
				dist.getWidth() <= obj.getInt("width");
	}

	public boolean areDescAndPergNear(Cluster desc, MyNode perg) {
		if(desc == null || desc.isEmpty() || perg == null)
			return false;
		JSONObject obj = CONFIGS.getJSONObject("distBetweenDescAndQuestion");
		Dewey dist = this.distMatrix.getDist(desc.last(), perg);
		return dist.getHeight() <= obj.getInt("height") && 
				dist.getMaxHeight() <= obj.getInt("maxHeight");
	}

	public boolean isGroupText(Cluster cTmp, Cluster desc, Cluster firstGroupOfQuestionnaire) {
		//TODO Todos os group text tem a mesma tag pai (?)
		if(cTmp == null || cTmp.isEmpty() || desc == null || desc.isEmpty())
			return false;
		
		String txt = cTmp.getText();
		//Cluster deve ter um e APENAS um texto
		if(txt.isEmpty() || txt.contains("\n"))
			return false;
		
		JSONObject obj = CONFIGS.getJSONObject("distBetweenGroupAndFirstQuestion");
		Dewey dist = distMatrix.getDist(cTmp.last(), desc.first());
		
		if(dist.getHeight() <= obj.getInt("height") && dist.getWidth() <= obj.getInt("width")){
			//O texto do grupo deve ter no maximo 5 palavras (?)
			if(txt.split(" ").length <= 5){
				if(firstGroupOfQuestionnaire != null){
					//O tamanho do Dewey dos grupos de um questionario, geralmente,
					//é o mesmo
					if(firstGroupOfQuestionnaire.last().getDewey().toString().length() == 
							cTmp.last().getDewey().toString().length())
						return true;
				}else{
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkComplementaryText(List<MyNode> nodes, int i) {
		if(i+2 < nodes.size()){
			MyNode nTmp1 = nodes.get(i),
				nTmp2 = nodes.get(i+1),
				nTmp3 = nodes.get(i+2);
			JSONObject obj = CONFIGS.getJSONObject("distBetweenDescAndComplementaryText");
			
			if(nTmp2.isText() && nTmp3.isImgOrText()){
				Dewey dist = distMatrix.getDist(nTmp1, nTmp2);
				if(dist.getHeight() <= obj.getInt("height") && dist.getWidth() <= obj.getInt("width")){
					String prefix1 = nTmp1.getDewey().getCommonPrefix(nTmp3.getDewey()),
							prefix2 = nTmp2.getDewey().getCommonPrefix(nTmp3.getDewey()),
							prefix3 = nTmp1.getDewey().getCommonPrefix(nTmp2.getDewey());
					
					//O prefixo3 deve ser maior que o prefix1, pois isso indica que nTmp1 e nTmp2 estão, 
					//pelo menos, um elemento a mais juntos
					if(prefix1.equals(prefix2) && 
							CommonUtil.getPrefixLength(prefix3) > CommonUtil.getPrefixLength(prefix1))
						return true;
				}
			}
		}
		return false;
	}

	public boolean isAbove(MyNode n1, MyNode n2) {
		//Verifica se n1 esta acima de n2
		Dewey dist = this.distMatrix.getDist(n1, n2);
		return dist.isNegative();
	}

	public boolean hasSameTexts(Pergunta currentP, Cluster cTmp2) {
		//Verifica se as alternativas/filhas da currentP contêm todos os textos
		//do cTmp2, ou se a descrição da currentP contém o texto de cTmp2
		if(cTmp2 == null || cTmp2.getText().isEmpty()) return false;
		
		ArrayList<Alternativa> alts = currentP.getAlternativas();
		ArrayList<Pergunta> filhas = currentP.getFilhas();
		String txt = cTmp2.getText(), regex = "(?s).*(\nXXX|XXX\n).*";
		boolean flag = false;
		int count = filhas.size()+alts.size();
		
		if(count == 0 && cTmp2.size() == 1){//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto
			flag = currentP.getDescricao().matches(regex.replaceAll("XXX", cTmp2.getText()));
		}else if(count == cTmp2.size()){
			flag = true;
			for(int j = 0; j < alts.size(); j++){
				flag = flag && txt.matches(regex.replaceAll("XXX", alts.get(j).getDescricao()));
			}
			for(int j = 0; j < filhas.size(); j++){
				flag = flag && txt.matches(regex.replaceAll("XXX", filhas.get(j).getDescricao()));
			}
		}
		
		return flag;
	}

	public boolean isSimpleMatrix(List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		int count = 0;
		Cluster head = !cStack.isEmpty() ? cStack.peek() : null;
		MyNode nTmp = nodes.get(i);
		
		//Conta a quantidade de componentes em sequência
		while(nTmp != null && nTmp.isComponent()){ 
			count++;
			if(i+count < nodes.size())
				nTmp = nodes.get(i+count);
			else
				nTmp = null;
		}
		
		//A quantidade encontrada acima deve ser a mesma de textos no head da matriz
		return head != null && head.isAllText() && head.size() == count;
	}
	
	public boolean checkPrefixForQuestionGroup(MyNode n1, MyNode n2, String prefix) {
		if(n1 == null || n2 == null || prefix.isEmpty()) return false;
		
		String tmp = n1.getDewey().getCommonPrefix(n2.getDewey());
		return tmp.equals(prefix);
	}
	
	public boolean checkDistForQWithSubQs(MyNode n1, MyNode n2) {
		JSONObject obj = CONFIGS.getJSONObject("distBetweenTextsInQuestionWithSubQuestions");
		Dewey dist = this.distMatrix.getDist(n1, n2);
		return dist.getHeight() == obj.getInt("height") && dist.getWidth() <= obj.getInt("width");
	}
	
	// Middle e bottom devem esta perto um do outro
	// o prefixo entre middle e qWithSubQsDesc e entre bottom e qWithSubQsDesc deve ser o mesmo
	// o prefixo acima deve ser menor que o prefixo entre middle e bottom
	public boolean checkQWithSubQs(MyNode middle, MyNode qWithSubQsDesc, List<MyNode> nodes, int currentI) {
		if(currentI+1 >= nodes.size()) return false;
		
		String prefix1 = middle.getDewey().getCommonPrefix(qWithSubQsDesc.getDewey());
		MyNode bottom = nodes.get(currentI+1);
		
		if(checkDistForQWithSubQs(middle, bottom) && 
				checkPrefixForQuestionGroup(bottom, qWithSubQsDesc, prefix1)){
			String prefix2 = middle.getDewey().getCommonPrefix(bottom.getDewey());
			return CommonUtil.getPrefixLength(prefix1) < CommonUtil.getPrefixLength(prefix2);
		}
		return false;
	}
	
	public boolean checkDistForTextsOfAlternative(MyNode n1, MyNode n2){
		JSONObject obj = CONFIGS.getJSONObject("distBetweenTextsOfSameAlternative");
		Dewey dist = this.distMatrix.getDist(n1, n2);
		return dist.getWeight() <= obj.getInt("deweyWeight");
	}
	
	// Métodos/Blocos estáticos
	static {
		CONFIGS = new JSONObject();
		CONFIGS.put("distBetweenTextsInsideQuestionnaire", new JSONObject())
			.put("distBetweenCompAndText", new JSONObject())
			.put("distBetweenDescAndQuestion", new JSONObject())
			.put("distBetweenGroupAndFirstQuestion", new JSONObject())
			.put("distBetweenDescAndComplementaryText", new JSONObject())
			.put("distBetweenTextsInQuestionWithSubQuestions", new JSONObject())
			.put("distBetweenDescriptions", new JSONObject())
			.put("distBetweenTextsOfSameAlternative", new JSONObject());
		
		JSONObject h = ProjectConfigs.getHeuristics(), 
				tmp1 = new JSONObject(), tmp2 = null;
		
		int n = h!=null ? h.optInt("minQuestionsOnQuestionnaire") : 0;
		n = n>0 ? n : 2;
		CONFIGS.put("minQuestionsOnQuestionnaire", n);
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenTextsInsideQuestionnaire") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenTextsInsideQuestionnaire")
			.put("height", tmp2.optInt("height", 4));
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenCompAndText") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenCompAndText")
			.put("height", tmp2.optInt("height", 2))
			.put("maxHeight", tmp2.optInt("maxHeight", 3));
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenDescAndQuestion") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenDescAndQuestion")
			.put("height", tmp2.optInt("height", 2))
			.put("maxHeight", tmp2.optInt("maxHeight", 4));
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenGroupAndFirstQuestion") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenGroupAndFirstQuestion")
			.put("height", tmp2.optInt("height", 1))
			.put("width", tmp2.optInt("width", 7));
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenDescAndComplementaryText") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenDescAndComplementaryText")
			.put("height", tmp2.optInt("height", 1))
			.put("width", tmp2.optInt("width", 4));
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenTextsInQuestionWithSubQuestions") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenTextsInQuestionWithSubQuestions")
			.put("height", tmp2.optInt("height", 1))
			.put("width", tmp2.optInt("width", 5));
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenDescriptions") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenDescriptions")
			.put("height", tmp2.optInt("height", 1))
			.put("width", tmp2.optInt("width", 4));
		
		tmp2 = h!=null ? h.optJSONObject("distBetweenTextsOfSameAlternative") : tmp1;
		tmp2 = tmp2!=null ? tmp2 : tmp1;
		CONFIGS.getJSONObject("distBetweenTextsOfSameAlternative")
			.put("deweyWeight", tmp2.optInt("deweyWeight", 100));
		
		CommonLogger.debug("RULESCHECKER: {}", CONFIGS.getJSONObject("distBetweenTextsInQuestionWithSubQuestions"));
	}
}
