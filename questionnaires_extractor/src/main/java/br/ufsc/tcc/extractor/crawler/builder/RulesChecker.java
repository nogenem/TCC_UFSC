package br.ufsc.tcc.extractor.crawler.builder;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.DistanceMatrix;

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
	
	
}
