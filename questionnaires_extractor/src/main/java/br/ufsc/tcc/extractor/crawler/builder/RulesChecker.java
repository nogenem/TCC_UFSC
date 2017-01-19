package br.ufsc.tcc.extractor.crawler.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
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
		Cluster cTmp1 = desc;
		String altsTxt = "";
		
		//Pega o texto de todas as alternativas da perguta
		for(Alternativa alt : tmpAlts){
			altsTxt += alt.getDescricao()+"\n";
		}
		
		while(true){
			for(MyNode node : cTmp1.getGroup()){
				flag = flag && altsTxt.contains(node.getText());
			}
			if(flag && !cStack.isEmpty())
				cTmp1 = cStack.pop();
			else
				break;
		}
		cTmp1 = checkIfShouldBeInSameCluster(cTmp1, cStack, firstNode);
		return cTmp1;
	}

	public Cluster checkIfShouldBeInSameCluster(Cluster c, Stack<Cluster> cStack, MyNode firstNode) {
		//TODO devo ativar a proteção abaixo?
		//Da conflito entre os links:
		//	https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto [matriz para de funcionar]
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
							prefix2 = nTmp2.getDewey().getCommonPrefix(nTmp3.getDewey());
					//O prefixo entre nTmp1 e nTmp3 e entre nTmp2 e nTmp3 são iguais?
					if(prefix1.equals(prefix2)){
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
		//Verifica se as alternativas da currentP contêm todos os textos
		//do cTmp2, ou se a descrição da currentP contém o texto de cTmp2
		ArrayList<Alternativa> tmpAlts = currentP.getAlternativas();
		boolean flag = cTmp2 != null && !cTmp2.getText().isEmpty() && 
				(tmpAlts.size() == cTmp2.size() || tmpAlts.size() == 0 && cTmp2.size() == 1);
		if(flag){
			if(tmpAlts.size() == 0)
				flag = currentP.getDescricao().contains(cTmp2.getText());
			else{
				for(int j = 0; j < cTmp2.getGroup().size(); j++){
					flag = flag && tmpAlts.get(j).getDescricao().contains(cTmp2.get(j).getText());
				}
			}
		}
		return flag;
	}

	public boolean checkDistForQuestionGroup(MyNode n1, MyNode n2) {
		Dewey dist = this.distMatrix.getDist(n1, n2);
		return dist.getHeight() == 1 && dist.getWidth() <= 4;
	}

	public boolean checkPrefixForQuestionGroup(MyNode n1, MyNode n2, String prefix) {
		String tmp = n1.getDewey().getCommonPrefix(n2.getDewey());
		return tmp.equals(prefix);
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
	
	
}
