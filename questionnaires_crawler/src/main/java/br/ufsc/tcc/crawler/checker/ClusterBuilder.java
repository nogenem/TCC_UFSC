package br.ufsc.tcc.crawler.checker;

import java.util.ArrayList;
import java.util.List;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.util.DistanceMatrix;

/**
 * Classe responsável por agrupar Nodos e/ou Clusters para formar 
 * outros Clusters.
 * 
 * @author Gilney N. Mathias
 */
public class ClusterBuilder {
	
	// Construtores
	public ClusterBuilder(){}
	
	// Demais métodos
	/**
	 * Método responsável por 'construir' uma lista de Clusters a partir da
	 * lista de nodos, {@code nodes}, e da matriz de distâncias, {@code distMatrix},
	 * passados. <br>
	 * Os nodos são primeiro agrupados por distância e em seguida são utilizadas algumas 
	 * heurísticas para tentar melhorar os agrupamentros.
	 * 
	 * @param nodes
	 * @param distMatrix
	 * @return					Lista de Clusters criada a partir dos nodos e da 
	 * 							matriz de distância passados.
	 */
	public List<Cluster> build(List<MyNode> nodes, DistanceMatrix distMatrix){
		List<Cluster> clusters = groupNearNodes(nodes, distMatrix);
		groupClustersByHeuristics(clusters);
		return clusters;
	}
	
	/**
	 * Agrupa os nodos que estão próximos uns dos outros.
	 * 
	 * @param nodes
	 * @param distMatrix
	 * @return					Lista de Clusters de nodos próximos uns dos outros.
	 */
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
	
	/**
	 * Agrupa os Clusters passados utilizando algumas heurísticas que podem
	 * ser encontradas no método {@link #shouldGroup(Cluster, Cluster, Cluster) shouldGroup}.
	 * 
	 * @param clusters
	 */
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
	
	/**
	 * Faz uso de algumas heurísticas para verifica se 
	 * os Clusters {@code c1} e {@code c2} devem ser unidos.<br>
	 * As heurísticas utilizadas são:
	 * <ul>
	 * 	<li>Deve-se juntar grupos de input</li>
	 * 	<li>Deve-se juntar grupos de options seguidos</li>
	 * 	<li>Deve-se juntar padrões de clusters de texto seguidos
	 *      de clusters de elementos ou clusters com apenas 1 elemento</li>
	 * </ul>
	 * O Cluster {@code c3} passado é utilizado na verificação da ultima heurística.
	 * 
	 * @param c1
	 * @param c2
	 * @param c3
	 * @return			<b>TRUE</b> caso os clusters {@code c1} e {@code c2} devam
	 * 					ser unidos ou<br>
	 * 					<b>FALSE</b> caso contrario.
	 */
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
