package br.ufsc.tcc.common.config;

import org.json.JSONArray;
import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonUtil;

public abstract class ProjectConfigs {
	
	private static String configPath = "./project_configs.json";
	private static JSONObject configs = loadConfigs();
	
	// Getters e Setters
	public static JSONObject getDatabaseConfigs(){
		return configs.optJSONObject("database");
	}
	
	public static boolean loadUrlsFromCrawler(){
		JSONObject tmp = getDatabaseConfigs();
		if(tmp != null)
			return tmp.optBoolean("loadUrlsFromCrawler", false);
		return false;
	}
	
	public static JSONObject getCrawlerDatabaseConfigs(){
		JSONObject tmp = getDatabaseConfigs();
		if(tmp != null)
			return tmp.optJSONObject("crawler");
		return null;
	}
	
	public static JSONObject getExtractorDatabaseConfigs(){
		JSONObject tmp = getDatabaseConfigs();
		if(tmp != null)
			return tmp.optJSONObject("extractor");
		return null;
	}
	
	public static JSONObject getCrawlerConfigs(){
		return configs.optJSONObject("crawler");
	}
	
	public static JSONObject getHeuristics(){
		return configs.optJSONObject("heuristics");
	}
	
	public static JSONArray getSeeds(){
		return configs.optJSONArray("seeds");
	}
	
	// Demais métodos
	private static JSONObject loadConfigs(){
		String configContent = CommonUtil.readFile(configPath);
		return CommonUtil.parseJson(configContent);
	}
}
