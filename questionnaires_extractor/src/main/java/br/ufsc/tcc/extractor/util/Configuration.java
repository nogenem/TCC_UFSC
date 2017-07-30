package br.ufsc.tcc.extractor.util;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonConfiguration;

public class Configuration extends CommonConfiguration {

	// Construtores
	public Configuration() {
		configsPath = "./extractor_configs.json";
		this.loadConfigs();
		this.validateConfigs();
	}
	
	// Demais métodos
	@Override
	protected void validateParameters() throws JSONException {
		JSONObject p = configs.getJSONObject("parameters");
		validatingPath = "parameters";
		
		p.getInt("minQuestionsOnQuestionnaire");
		p.getInt("maxWordsInAGroupDescription");
		p.getInt("maxTextClustersBetweenQuestions");
		p.getString("phrasesToIgnoreRegex");
		
		validateIntParameter(p, "distBetweenNearNodes", 
				"height", "maxHeight", "width");
		
		validateIntParameter(p, "distBetweenTextsInsideQuestionnaire", 
				"height");
		
		validateIntParameter(p, "distBetweenCompAndText", 
				"height", "maxHeight");
		
		validateIntParameter(p, "distBetweenDescAndQuestion", 
				"height", "maxHeight");
		
		validateIntParameter(p, "distBetweenGroupAndFirstQuestion", 
				"height", "width");
		
		validateIntParameter(p, "distBetweenDescAndComplementaryText", 
				"height", "maxHeight", "width");
		
		validateIntParameter(p, "distBetweenTextsInQuestionWithSubQuestions", 
				"height", "width");
		
		validateIntParameter(p, "distBetweenPartsOfDescription", 
				"height", "maxHeight", "width");
		
		validateIntParameter(p, "distBetweenTextsOfSameAlternative", 
				"height", "maxHeight", "width");
		
		validateIntParameter(p, "distBetweenHeaderAndFirstAlternative", 
				"height", "width");
		
		validateIntParameter(p, "distBetweenEvaluationLevelsAndDesc", 
				"height");
		
		p.getInt("maxSpacesAndNewLinesInEvaluationLevels");
		p.getString("evaluationLevelsWordsRegex");
	}
	
	@Override
	protected void validateDatabaseConfig() {
		JSONObject dbObj = configs.getJSONObject("database");
		validatingPath = "database";
		
		dbObj.getBoolean("loadSeedsFromCrawler");

		JSONObject tmp = dbObj.getJSONObject("crawler");
		validatingPath += ".crawler";
		
		tmp.getString("dbms");
		tmp.getString("name");
		tmp.getString("host");
		tmp.getString("login");
		tmp.getString("password");
		
		tmp = dbObj.getJSONObject("extractor");
		validatingPath = validatingPath.substring(0, validatingPath.indexOf(".")+1) + "extractor";
		
		tmp.getString("dbms");
		tmp.getString("name");
		tmp.getString("host");
		tmp.getString("login");
		tmp.getString("password");
	}
}
