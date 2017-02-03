package br.ufsc.tcc.common.config;

import org.json.JSONArray;
import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;

public abstract class ProjectConfigs {
	
	private static JSONObject configs = new JSONObject();
	
	// Getters e Setters
	public static String getLogLevels() {
		return configs.optString("logLevels");
	}
	
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
	public static void loadConfigs(String path){
		configs = null;
		try{
			String configContent = CommonUtil.readFile(path);
			configs = CommonUtil.parseJson(configContent);
		}catch(Exception e){
			// É obrigatório fornecer o arquivo de configuração!
			CommonLogger.fatalError(e);
		}
	}
}
