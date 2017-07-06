package br.ufsc.tcc.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.DeweyExt;
import br.ufsc.tcc.common.model.MyNode;

/**
 * Classe responsável por guardar as distâncias entre os Nodos, 
 * serve como uma 'cache' para os valores.
 * 
 * @author Gilney N. Mathias
 */
public class DistanceMatrix {
	
	private static int MAX_WIDTH = 0;
	private static int MAX_HEIGHT = 0;
	private static int MAX_MAXHEIGHT = 0;
	
	private HashMap<String, HashMap<String, DeweyExt>> distances;

	// Construtores
	public DistanceMatrix(){
		this.distances = new HashMap<>();
	}
	
	// Getters e Setters
	public DeweyExt getDist(MyNode n1, MyNode n2){
		if(n1 == null || n2 == null) return null;
		
		String v1 = n1.getDewey().getValue(), 
				v2 = n2.getDewey().getValue();
		
		if(!this.distances.containsKey(v1))
			this.distances.put(v1, new HashMap<>());
		if(!this.distances.get(v1).containsKey(v2))
			this.calculateDist(n1.getDewey(), n2.getDewey());
		
		return this.distances.get(v1).get(v2);
	}
	
	public Set<String> keySet(){
		return this.distances.keySet();
	}
	
	public ArrayList<String> keyList(){
		return new ArrayList<String>(distances.keySet());
	}
	
	// Demais métodos
	/**
	 * Verifica se os Clusters estão perto um do outro olhando 
	 * a distância do ultimo Nodo do Cluster {@code c1} e do
	 * primeiro Nodo do Cluster {@code c2}.
	 * 
	 * @param c1		Primeiro Cluster.
	 * @param c2		Segundo Cluster.
	 * @return			<b>True</b> caso {@code c1} seja considerado 'perto' de {@code c2} ou<br>
	 * 					<False> caso contrario.
	 */
	public boolean areNear(Cluster c1, Cluster c2){
		if(c1 != null && c2 != null) 
			return this.areNear(c1.last(), c2.first());
		return false;
	}
	
	/**
	 * Verifica se os Nodos {@code n1} e {@code n2} estão 
	 * perto um do outro.
	 * 
	 * @param c1		Primeiro Nodo.
	 * @param c2		Segundo Nodo.
	 * @return			<b>True</b> caso {@code n1} seja considerado 'perto' de {@code n2} ou<br>
	 * 					<False> caso contrario.
	 */
	public boolean areNear(MyNode n1, MyNode n2){
		if(n1 != null && n2 != null){
			DeweyExt dist = this.getDist(n1, n2);
			return dist.getHeight() <= MAX_HEIGHT &&
					dist.getMaxHeight() <= MAX_MAXHEIGHT &&
					dist.getWidth() <= MAX_WIDTH;
		}
		return false;
	}
	
	public void clear(){
		this.distances.clear();
	}

	private void calculateDist(DeweyExt d1, DeweyExt d2) {
		DeweyExt dist = d1.distanceOf(d2);
		this.distances.get(d1.getValue()).put(d2.getValue(), dist);
	}
	
	// Métodos/Blocos estáticos
	static {
		//Load parameters
		JSONObject p = ProjectConfigs.getParameters(), tmp = null;
		
		try{
			tmp = p.getJSONObject("distBetweenNearNodes");	
			MAX_WIDTH = tmp.getInt("width");
			if(MAX_WIDTH <= 0) MAX_WIDTH = 3;
			
			MAX_HEIGHT = tmp.getInt("height");
			if(MAX_HEIGHT <= 0) MAX_HEIGHT = 3;
			
			MAX_MAXHEIGHT = tmp.getInt("maxHeight");
			if(MAX_MAXHEIGHT <= 0) MAX_MAXHEIGHT = 3;
		}catch(JSONException exp){
			CommonLogger.fatalError(exp);
		}
		CommonLogger.debug("DistMatrix:> Static block executed!");
	}
}
