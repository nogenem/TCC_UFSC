package br.ufsc.tcc.common.database.connection;

import org.json.JSONObject;

public class PostgreConnection extends BasicConnection {
	
	public PostgreConnection(String database){
		this.database = database;
		this.dbms = "postgresql";
		this.host = "localhost:5432";
		this.login = "postgres";
		this.password = "123";
	}
	
	public PostgreConnection(String database, String host, String login, String password){
		this.database = database;
		this.dbms = "postgresql";
		this.host = host;
		this.login = login;
		this.password = password;
	}
	
	public PostgreConnection(JSONObject configs){
		this.database = configs.getString("name");
		this.dbms = "postgresql";
		this.host = configs.getString("host");
		this.login = configs.getString("login");
		this.password = configs.getString("password");
	}
}
