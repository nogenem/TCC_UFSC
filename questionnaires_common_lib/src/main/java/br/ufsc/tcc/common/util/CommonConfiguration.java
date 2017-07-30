package br.ufsc.tcc.common.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe responsável por ler e validar o arquivo de configuração e providenciar 
 * uma forma simples para que outras classes possam acessar as 
 * propriedades do mesmo.
 * 
 * @author Gilney N. Mathias
 */
public abstract class CommonConfiguration {
	
	private static CommonConfiguration instance = null;
	
	protected String configsPath;//DEVE ser sobrescrito pelas subclasses
	protected JSONObject configs;
	protected String validatingPath;//usado apenas na parte de validação
	
	// Getters e Setters
	public static CommonConfiguration getInstance() {
		return instance;
	}
	
	public static void setInstance(CommonConfiguration instance) {
		CommonConfiguration.instance = instance;
	}
	
	public String getLogLevels() {
		return configs.optString("logLevels");
	}
	
	public JSONObject getDatabaseConfigs(){
		return configs.getJSONObject("database");
	}
	
	public boolean loadSeedsFromCrawler(){
		JSONObject tmp = getDatabaseConfigs();
		return tmp.optBoolean("loadSeedsFromCrawler", true);
	}
	
	public JSONObject getCrawlerDatabaseConfigs(){
		try{
			return getDatabaseConfigs().getJSONObject("crawler");
		}catch(JSONException exp){
			CommonLogger.fatalError(
					new JSONException("Objeto 'database.crawler' não encontrado no arquivo de configuração!"));
			return null;
		}
	}
	
	public JSONObject getExtractorDatabaseConfigs(){
		try{
			return getDatabaseConfigs().getJSONObject("extractor");
		}catch(JSONException exp){
			CommonLogger.fatalError(
					new JSONException("Objeto 'database.extractor' não encontrado no arquivo de configuração!"));
			return null;
		}
	}
	
	public JSONObject getCrawlerConfigs(){
		return configs.optJSONObject("crawler");
	}
	
	public JSONArray getSeeds(){
		JSONArray arr = configs.optJSONArray("seeds");
		return arr != null ? arr : new JSONArray();
	}
	
	public JSONObject getParameters(){
		return configs.getJSONObject("parameters");
	}
	
	// Demais métodos
	protected void loadConfigs() {
		try{
			String configContent = CommonUtil.readFile(configsPath);
			configs = CommonUtil.parseJson(configContent);
		}catch(Exception e){
			// É obrigatório fornecer o arquivo de configuração!
			CommonLogger.fatalError(e);
		}
	}
	
	protected void validateConfigs() {
		try {
			validatingPath = "";
			configs.getString("logLevels");
			this.validateDatabaseConfig();
			
			validatingPath = "";
			this.validateCrawlerConfig();
			
			validatingPath = "";
			this.validateParameters();
		}catch(JSONException exp){
			String msg = exp.getMessage();
			String value = msg.substring(msg.indexOf('[')+2, msg.lastIndexOf(']')-1);
			validatingPath += !validatingPath.equals("") ? "." : "";
			msg = msg.replace(value, validatingPath+value);
			CommonLogger.fatalError(new JSONException(msg));
		}
	}

	protected void validateDatabaseConfig() {
		// Não faz nada por padrão
		// Subclasses DEVEM sobrescrever este método!
	}
	
	protected void validateCrawlerConfig() {
		// Não faz nada por padrão [objeto opcional]
		// Subclasses PODEM sobrescrever este método para adicionar
		// algumas verificações
	}
	
	protected void validateParameters() {
		// Não faz nada por padrão
		// Subclasses DEVEM sobrescrever este método!
	}
	
	protected void validateIntParameter(JSONObject paramsObj, String param, String ...keys) throws JSONException {
		boolean checkingKeys = false;
		try {
			JSONObject tmp = paramsObj.getJSONObject(param);
			checkingKeys = true;
			for(String key : keys) {
				tmp.getInt(key);
			}
		} catch(JSONException exp) {
			if(checkingKeys)
				validatingPath += "."+param;
			throw exp;
		}
	}
}
