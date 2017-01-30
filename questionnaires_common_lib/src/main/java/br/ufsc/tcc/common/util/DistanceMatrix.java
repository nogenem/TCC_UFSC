package br.ufsc.tcc.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.json.JSONObject;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;

public class DistanceMatrix {
	
	private static int MAX_WEIGHT = 0;
	private static int MAX_HEIGHT = 0;
	
	private HashMap<String, HashMap<String, Dewey>> distances;

	// Construtores
	public DistanceMatrix(){
		this.distances = new HashMap<>();
	}
	
	// Getters e Setters
	public Dewey getDist(MyNode n1, MyNode n2){
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
	public boolean areNear(Cluster c1, Cluster c2){
		if(c1 != null && c2 != null) 
			return this.areNear(c1.last(), c2.first());
		return false;
	}
	
	public boolean areNear(MyNode n1, MyNode n2){
		if(n1 != null && n2 != null){
			Dewey dist = this.getDist(n1, n2);
			return dist.getDeweyWeight() <= MAX_WEIGHT && 
					dist.getHeight() <= MAX_HEIGHT;
		}
		return false;
	}
	
	public void clear(){
		this.distances.clear();
	}

	private void calculateDist(Dewey d1, Dewey d2) {
		Dewey dist = d1.distanceOf(d2);
		this.distances.get(d1.getValue()).put(d2.getValue(), dist);
	}
	
	// Métodos/Blocos estáticos
	static {
		//Load heuristics
		JSONObject tmp = ProjectConfigs.getHeuristics();
		if(tmp != null){
			tmp = tmp.optJSONObject("distBetweenNearNodes");
			if(tmp != null){
				MAX_WEIGHT = tmp.optInt("deweyWeight");
				MAX_HEIGHT = tmp.optInt("height");
			}
		}
		if(MAX_WEIGHT <= 0) MAX_WEIGHT = 1000;
		if(MAX_HEIGHT <= 0) MAX_HEIGHT = 3;
		
		CommonLogger.debug("DISTMATRIX: {} / {}", MAX_WEIGHT, MAX_HEIGHT);
	}
}
