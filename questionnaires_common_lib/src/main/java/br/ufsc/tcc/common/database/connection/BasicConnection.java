package br.ufsc.tcc.common.database.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.json.JSONObject;

import br.ufsc.tcc.common.util.CommonLogger;

public class BasicConnection {
	
	protected Connection conn;
	protected String database, dbms, host, login, password;
	
	public BasicConnection(JSONObject configs) {
		this(configs.optString("dbms"),
				configs.optString("name"),
				configs.optString("host"),
				configs.optString("login"),
				configs.optString("password"));
	}
	
	public BasicConnection(String dbms, String database, String host, String login, String password){
		if(dbms.equals(""))
			CommonLogger.fatalError(new SQLException("DBMS nao especificado!"));
		else if(database.equals(""))
			CommonLogger.fatalError(new SQLException("Nome do banco de dados nao especificado!"));
		
		this.dbms = dbms;
		this.database = database;
		this.host = host;
		this.login = login;
		this.password = password;
	}

	public Connection getConnection() {
		if(this.conn != null)
			return this.conn;
		return this.newConnection();
	}

	private Connection newConnection() {
		try{
			this.conn = DriverManager.getConnection("jdbc:"+this.dbms+"://"+this.host+"/"+this.database, 
					this.login, this.password);
		}catch(Exception e){
			CommonLogger.fatalError(e);
		}
		return this.conn;
	}
	
	public void close(){
		try {
			this.conn.close();
		} catch (SQLException e) {
			CommonLogger.error(e);
		}
	}
}
