package br.ufsc.tcc.crawler.checker;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.DistanceMatrix;

/**
 * Classe responsável por agrupar Nodo e/ou Cluster para formar 
 * outros Cluster.
 * 
 * @author Gilney N. Mathias
 */
public class ClusterBuilder {
	
	// Construtores
	public ClusterBuilder(){}
	
	// Demais métodos
	public List<Cluster> build(List<MyNode> nodes, DistanceMatrix distMatrix){
		List<Cluster> clusters = groupNearNodes(nodes, distMatrix);
		groupClustersByHeuristics(clusters);
		return clusters;
	}
	
	private List<Cluster> groupNearNodes(List<MyNode> nodes, DistanceMatrix distMatrix) {
		Cluster cTmp = new Cluster();
		ArrayList<Cluster> ret = new ArrayList<>();
		
		for(MyNode nTmp : nodes){
			if(!cTmp.isEmpty() && !distMatrix.areNear(cTmp.last(), nTmp)){
				ret.add(cTmp);
				cTmp = new Cluster();
			}
			cTmp.add(nTmp);
		}
		if(!cTmp.isEmpty())
			ret.add(cTmp);
		return ret;
	}

	private void groupClustersByHeuristics(List<Cluster> clusters) {
		Cluster tmp1, tmp2, tmp3;
		int i, size;
		
		do{
			size = clusters.size();
			i = 0;
			
			while(i < clusters.size()-1){
				int tmpSize = clusters.size();
				
				tmp1 = clusters.get(i);
				tmp2 = clusters.get(i+1);
				tmp3 = i+2 < tmpSize ? clusters.get(i+2) : null;
				
				if(shouldGroup(tmp1, tmp2, tmp3)){
					tmp1.join(tmp2);
					clusters.remove(i+1);
					i--;
				}
				i++;
			}
		}while(size != clusters.size());
	}

	private boolean shouldGroup(Cluster c1, Cluster c2, Cluster c3) {
		//Deve-se juntar grupos de input
		//PS: pode ter uma img na frente
		final String starterInputRegex = "(?s)^(img\\[alt=.*\\]\\n)?"
				+ "(input\\[type=.+\\]).*";
		String c1Text = c1.getAllNodesText(),
				c2Text = c2.getAllNodesText();
		if(c1Text.matches(starterInputRegex) && c2Text.matches(starterInputRegex))
			return true;
		
		//Deve-se juntar grupos de options seguidos
		if(c1.size() >= 2){
			MyNode opt1 = c1.get(c1.size()-2), opt2 = c2.first();
			if(opt1.isA("OPTION") && opt2.isA("OPTION"))
				return true;
		}
		
		//Deve-se juntar padrões de cluster texto seguidos de cluster elementos OU
		//cluster com 1 elemento apenas (casos raros?)
		//PS: deve-se priorizar a união de elementos a frente desta regra
		//    -por isso o c3-
		if((c3 == null || c3.first().isText()) &&
				((c1.isAllText() && !c2.first().isText()) || 
						(!c2.first().isText() && c2.size() == 1)))
			return true;
		return false;
	}
}
