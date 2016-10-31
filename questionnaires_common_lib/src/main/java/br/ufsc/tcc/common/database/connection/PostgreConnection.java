package br.ufsc.tcc.common.database.connection;

import org.json.JSONObject;

public class PostgreConnection extends BasicConnection {
	
	public PostgreConnection(String database){
		this(database, 
				"localhost:5432", 
				"postgres", 
				"123");
	}
	
	public PostgreConnection(JSONObject configs){
		this(configs.getString("name"),
				configs.getString("host"),
				configs.getString("login"),
				configs.getString("password"));
	}
	
	public PostgreConnection(String database, String host, String login, String password){
		this.database = database;
		this.dbms = "postgresql";
		this.host = host;
		this.login = login;
		this.password = password;
	}
}
