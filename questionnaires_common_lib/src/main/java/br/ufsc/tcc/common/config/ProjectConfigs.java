package br.ufsc.tcc.common.config;

import org.json.JSONObject;

import br.ufsc.tcc.common.util.Util;

public abstract class ProjectConfigs {
	
	private static String configPath = "./project_config.json";
	private static JSONObject configs = loadConfigs();
	
	/**
	 * Carrega as informações do arquivo de configuração do projeto e
	 * as retorna em um JSONObject.
	 * 
	 * @return	Um JSONObject que representa as informações das configurações
	 * 			do projeto.
	 */
	private static JSONObject loadConfigs(){
		String configContent = Util.readFile(configPath);
		return Util.parseJson(configContent);
	}
	
	/**
	 * Retorna um JSONObject referente as configurações do Crawler.
	 * 
	 * @return		Um JSONObject referente as configurações do Crawler, ou</br>
	 * 				null caso não se consiga encontrar tal configuração.
	 */
	public static JSONObject getCrawlerConfigs(){
		return configs.optJSONObject("crawler");
	}
	
	/**
	 * Retorna um JSONObject referente as configurações do Banco de Dados.
	 * 
	 * @return		Um JSONObject referente as configurações do Banco de Dados, ou</br>
	 * 				null caso não se consiga encontrar tal configuração.
	 */
	public static JSONObject getDatabaseConfigs(){
		return configs.optJSONObject("database");
	}
}
