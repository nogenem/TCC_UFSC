package br.ufsc.tcc.common.database.connection;

import org.json.JSONObject;

public class PostgreConnection extends BasicConnection {
	
	public PostgreConnection(String database){
		this(database, "localhost:5432", "postgres", "123");
	}
	
	public PostgreConnection(JSONObject configs){
		this(configs.getString("name"),//name é o unico obrigatório!
				configs.optString("host", "localhost:5432"),
				configs.optString("login", "postgres"),
				configs.optString("password", "123"));
	}
	
	public PostgreConnection(String database, String host, String login, String password){
		this.database = database;
		this.dbms = "postgresql";
		this.host = host;
		this.login = login;
		this.password = password;
	}
}
