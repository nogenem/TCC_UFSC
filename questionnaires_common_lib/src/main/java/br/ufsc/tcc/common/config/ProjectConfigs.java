package br.ufsc.tcc.common.config;

import org.json.JSONArray;
import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonUtil;

public class ProjectConfigs {
	
	//TODO dar uma repensada nisso...
	private static String configPath = "./project_configs.json";
	private static JSONObject configs = loadConfigs();
	
	// Getters e Setters
	public static JSONObject getCrawlerConfigs(){
		return configs.optJSONObject("crawler");
	}
	
	public static JSONObject getDatabaseConfigs(){
		return configs.optJSONObject("database");
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
	
	public static JSONObject getDataConfigs(){
		return configs.optJSONObject("data");
	}
	
	public static boolean loadUrlsFromCrawler(){
		JSONObject tmp = getDataConfigs();
		if(tmp != null)
			return tmp.optBoolean("loadUrlsFromCrawler", false);
		return false;
	}
	
	public static JSONArray getExtraUrls(){
		JSONObject tmp = getDataConfigs();
		if(tmp != null)
			return tmp.optJSONArray("extra_urls");
		return null;
	}
	
	// Demais métodos
	private static JSONObject loadConfigs(){
		String configContent = CommonUtil.readFile(configPath);
		return CommonUtil.parseJson(configContent);
	}
}
