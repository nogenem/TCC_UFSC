package br.ufsc.tcc.extractor.crawler.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.DistanceMatrix;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;

public class RulesChecker {
	
	private DistanceMatrix distMatrix;
	
	public RulesChecker() {
		this.distMatrix = new DistanceMatrix();
	}
	
	// Getters e Setters
	public DistanceMatrix getDistMatrix(){
		return this.distMatrix;
	}
	
	// Demais métodos
	
	// Métodos usados pela classe QuestionarioBuilder
	public boolean shouldStartNewQuestionario(Cluster lastDesc, MyNode newNode) {
		if(lastDesc == null || lastDesc.isEmpty() || newNode == null) 
			return false;
		Dewey dist = this.distMatrix.getDist(lastDesc.last(), newNode);	
		//Elementos de texto dentro de um questionario não podem estar a 
		//mais de 4 elementos de altura de distancia
		return dist.getHeight() > 4;
	}

	public boolean shouldCreateNewCluster(Cluster lastCluster, MyNode newNode) {
		if(lastCluster == null || lastCluster.isEmpty() || newNode == null) 
			return false;
		//Se o ultimo Nodo do ultimo Cluster não estiver perto do novo Node
		//encontrado, então, provavelmente, está se iniciando um novo
		//'grupo' de informações na pagina
		return !this.distMatrix.areNear(lastCluster.last(), newNode);
	}

	// Métodos usados pela classe PerguntaBuilder
	public boolean isOnlyOneImg(Cluster c) {
		return c.size() == 1 && c.first().isImage();
	}

	public boolean areCompAndTextNear(MyNode comp, MyNode text) {
		if(comp == null || text == null) return false;
		Dewey dist = this.distMatrix.getDist(comp, text);
		//TODO testar esses numeros
		return dist.getHeight() <= 2 && dist.getMaxHeight() <= 3;
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
		
		//Ex: https://www.surveymonkey.com/r/General-Event-Feedback-Template
		while(true){
			for(MyNode node : desc.getGroup()){
				flag = flag && altsTxt.contains(node.getText());
			}
			if(flag && !cStack.isEmpty()){
				desc = cStack.pop();
			}else
				break;
		}
		return desc;
	}

	public Cluster checkIfShouldBeInSameCluster(Cluster c, Stack<Cluster> cStack, MyNode firstNode) {
		//Da conflito entre os links:
		//	https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto [matriz para de funcionar]
		//  http://anpei.tempsite.ws/intranet/mediaempresa [problema com: site da empresa http://]
		//	https://www.surveycrest.com/template_preview/pyof1IFwp9Xa1_x430JdUeVsuHVRKuw [arruma um dos problemas]
		// 	http://anpei.tempsite.ws/intranet/mediaempresa [arruma um dos problemas]
		
		//Todos os textos da descrição devem ter o mesmo prefixo
		//comum ao 1* componente da pergunta
		Cluster cTmp2 = new Cluster();
		String tTmp1 = "", tTmp2 = "";
		for(MyNode node : c.getGroup()){
			if(tTmp1.isEmpty()){
				tTmp1 = node.getDewey().getCommonPrefix(firstNode.getDewey());
			}else{
				tTmp2 = node.getDewey().getCommonPrefix(firstNode.getDewey());
				if(!tTmp1.equals(tTmp2)){
					tTmp1 = tTmp2;
					cStack.add(cTmp2);
					cTmp2 = new Cluster();
				}
			}
			cTmp2.add(node);
		}
		return cTmp2;
	}

	public boolean checkDistBetweenDescAndPerg(Cluster desc, MyNode perg) {
		if(desc == null || desc.isEmpty() || perg == null)
			return false;
		Dewey dist = this.distMatrix.getDist(desc.last(), perg);
		return dist.getHeight() > 2 || dist.getMaxHeight() > 4;
	}

	public boolean isGroupText(Cluster cTmp, Cluster desc, Cluster firstGroupOfQuestionnaire) {
		//TODO Todos os group text tem a mesma tag pai (?)
		if(cTmp == null || cTmp.isEmpty() || desc == null || desc.isEmpty())
			return false;
		
		//Cluster tem tamanho = 1 ou cluster de text e img?
		if(cTmp.size() == 1 || (cTmp.size() == 2 && cTmp.isAllTextOrImg())){
			//A altura da distância entre esse cTmp e a descrição da pergunta é menor ou igual a 1?
			//E a largura é menor ou igual a 7?
			Dewey dist = distMatrix.getDist(cTmp.last(), desc.first());
			if(dist.getHeight() <= 1 && dist.getWidth() <= 7){
				//O texto do grupo deve ter no maximo 5 palavras (?)
				if(cTmp.getText().split(" ").length <= 5){
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
		}
		return false;
	}

	public boolean checkNextText(List<MyNode> nodes, int i) {
		if(i+2 < nodes.size()){
			MyNode nTmp1 = nodes.get(i),
				nTmp2 = nodes.get(i+1),
				nTmp3 = nodes.get(i+2);
			
			if(nTmp2.isText() && nTmp3.isImgOrText()){
				Dewey dist = distMatrix.getDist(nTmp1, nTmp2);
				if(dist.getHeight() <= 1 && dist.getWidth() <= 4){
					String prefix1 = nTmp1.getDewey().getCommonPrefix(nTmp3.getDewey()),
							prefix2 = nTmp2.getDewey().getCommonPrefix(nTmp3.getDewey()),
							prefix3 = nTmp1.getDewey().getCommonPrefix(nTmp2.getDewey());
					//O prefixo entre nTmp1 e nTmp3 e entre nTmp2 e nTmp3 são iguais?
					if(prefix1.equals(prefix2) && prefix3.length() > prefix1.length()){
						return true;
					}
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

	public boolean checkDistForQuestionGroup(MyNode n1, MyNode n2) {
		Dewey dist = this.distMatrix.getDist(n1, n2);
		return dist.getHeight() == 1 && dist.getWidth() <= 5;
	}

	public boolean isSimpleMatrix(List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		int count = 0;
		Cluster cTmp = !cStack.isEmpty() ? cStack.peek() : null;
		MyNode nTmp = nodes.get(i);
		
		while(nTmp != null && nTmp.isComponent()){ 
			count++;
			if(i+count < nodes.size())
				nTmp = nodes.get(i+count);
			else
				nTmp = null;
		}
		
		return cTmp != null && cTmp.isAllText() && cTmp.size() == count;
	}
	
	public boolean checkPrefixForQuestionGroup(MyNode n1, MyNode n2, String prefix) {
		if(n1 == null || n2 == null || prefix.isEmpty()) return false;
		
		String tmp = n1.getDewey().getCommonPrefix(n2.getDewey());
		return tmp.equals(prefix);
	}
	
	// Meio e bottom devem esta perto um do outro
	// o prefixo entre middle e groupDesc e entre bottom e groupDesc deve ser o mesmo
	// o prefixo acima deve ser menor que o prefixo entre middle e bottom
	public boolean checkQuestionGroup(MyNode middle, MyNode groupDesc, List<MyNode> nodes, int currentI) {
		String prefix1 = middle.getDewey().getCommonPrefix(groupDesc.getDewey());
		MyNode bottom = nodes.get(currentI+1);
		
		if(checkDistForQuestionGroup(middle, bottom) && 
				checkPrefixForQuestionGroup(bottom, groupDesc, prefix1)){
			String prefix2 = middle.getDewey().getCommonPrefix(bottom.getDewey());
			return prefix1.length() < prefix2.length();
		}
		return false;
	}
	
	
}
