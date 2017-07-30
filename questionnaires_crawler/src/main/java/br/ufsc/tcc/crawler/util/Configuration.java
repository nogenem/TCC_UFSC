package br.ufsc.tcc.crawler.util;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonConfiguration;

public class Configuration extends CommonConfiguration {

protected static Configuration instance;
	
	//Construtores
	public Configuration() {
		configsPath = "./crawler_configs.json";
		this.loadConfigs();
		this.validateConfigs();
	}
	
	// Demais métodos
	@Override
	protected void validateParameters() throws JSONException {
		JSONObject p = configs.getJSONObject("parameters");
		validatingPath = "parameters";
		
		validateIntParameter(p, "distBetweenNearNodes", 
				"height", "maxHeight", "width");
		
		validateIntParameter(p, "distBetweenNearQuestions", 
				"height");
		
		p.getString("surveyWordsRegex");
		p.getString("phrasesToIgnoreRegex");
		p.getInt("minCompsInOneCluster");
		p.getInt("minClustersWithComp");
		p.getInt("maxClustersBetweenClustersWithComp");
	}
	
	@Override
	protected void validateCrawlerConfig() {
		JSONObject crawlerObj = configs.getJSONObject("crawler");
		validatingPath = "crawler";

		crawlerObj.getString("excludedFilesExtensions");
		crawlerObj.getString("excludedDomains");
		crawlerObj.getString("excludedLanguages");
	}
	
	@Override
	protected void validateDatabaseConfig() {
		JSONObject dbObj = configs.getJSONObject("database");
		validatingPath = "database";

		JSONObject tmp = dbObj.getJSONObject("crawler");
		validatingPath += ".crawler";
		
		tmp.getString("dbms");
		tmp.getString("name");
		tmp.getString("host");
		tmp.getString("login");
		tmp.getString("password");
	}
}
