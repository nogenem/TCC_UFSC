package br.ufsc.tcc.common.database.connection;

import java.sql.SQLException;

import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonLogger;

public class PostgreConnection extends BasicConnection {
	
	public PostgreConnection(String database){
		this(database, "localhost:5432", "postgres", "123");
	}
	
	public PostgreConnection(JSONObject configs){
		this(configs.optString("name", ""),
				configs.optString("host", "localhost:5432"),
				configs.optString("login", "postgres"),
				configs.optString("password", "123"));
	}
	
	public PostgreConnection(String database, String host, String login, String password){
		if(database.equals(""))
			CommonLogger.fatalError(new SQLException("Nome do banco de dados nao especificado!"));
		
		this.database = database;
		this.dbms = "postgresql";
		this.host = host;
		this.login = login;
		this.password = password;
	}
}
