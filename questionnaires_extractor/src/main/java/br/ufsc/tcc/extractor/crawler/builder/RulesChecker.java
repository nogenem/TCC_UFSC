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
		//cTmp1 = checkIfShouldBeInSameCluster(cTmp1, cStack, firstNode);
		return cTmp1;
	}

	public boolean checkDistBetweenDescAndPerg(Cluster desc, MyNode perg) {
		if(desc == null || desc.isEmpty() || perg == null)
			return false;
		Dewey dist = this.distMatrix.getDist(desc.last(), perg);
		return dist.getHeight() > 2 || dist.getMaxHeight() > 4;
	}

	public boolean isGroupText(Cluster cTmp, Cluster desc, Cluster firstGroupOfQuestionnaire) {
		//TODO Todos os group text tem a mesma tag pai (?)
		
		//Cluster tem tamanho = 1?
		if(cTmp.size() == 1){
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
					}else
						return true;
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
	
	
}
