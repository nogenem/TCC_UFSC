package br.ufsc.tcc.common.config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;

/**
 * Classe responsável por ler o arquivo de configuração e providenciar 
 * uma forma simples e rápida para que outras classes possam acessar as 
 * propriedades do mesmo.
 * 
 * @author Gilney N. Mathias
 */
public abstract class ProjectConfigs {
	
	private static JSONObject configs = new JSONObject();
	
	// Getters e Setters
	public static String getLogLevels() {
		return configs.optString("logLevels");
	}
	
	public static JSONObject getDatabaseConfigs(){
		try{
			return configs.getJSONObject("database");
		}catch(JSONException exp){
			CommonLogger.fatalError(
					new JSONException("Objeto 'database' não encontrado no arquivo de configuração!"));
			return null;
		}
	}
	
	public static boolean loadSeedsFromCrawler(){
		JSONObject tmp = getDatabaseConfigs();
		return tmp.optBoolean("loadSeedsFromCrawler", true);
	}
	
	public static JSONObject getCrawlerDatabaseConfigs(){
		try{
			return getDatabaseConfigs().getJSONObject("crawler");
		}catch(JSONException exp){
			CommonLogger.fatalError(
					new JSONException("Objeto 'database.crawler' não encontrado no arquivo de configuração!"));
			return null;
		}
	}
	
	public static JSONObject getExtractorDatabaseConfigs(){
		try{
			return getDatabaseConfigs().getJSONObject("extractor");
		}catch(JSONException exp){
			CommonLogger.fatalError(
					new JSONException("Objeto 'database.extractor' não encontrado no arquivo de configuração!"));
			return null;
		}
	}
	
	public static JSONObject getCrawlerConfigs(){
		return configs.optJSONObject("crawler");
	}
	
	public static JSONObject getParameters(){
		try{
			return configs.getJSONObject("parameters");
		}catch(JSONException exp){
			CommonLogger.fatalError(
					new JSONException("Objeto 'parameters' não encontrado no arquivo de configuração!"));
			return null;
		}
	}
	
	public static JSONArray getSeeds(){
		try{
			return configs.getJSONArray("seeds");
		}catch(JSONException exp){
			CommonLogger.info("Arranjo 'seeds' não encontrado no arquivo de configuração!");
			return new JSONArray();
		}
	}
	
	// Demais métodos
	public static void loadConfigs(String path){
		try{
			String configContent = CommonUtil.readFile(path);
			configs = CommonUtil.parseJson(configContent);
		}catch(Exception e){
			// É obrigatório fornecer o arquivo de configuração!
			CommonLogger.fatalError(e);
		}
	}
}
