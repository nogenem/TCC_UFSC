package dao.connection;

import java.sql.Connection;
import java.sql.DriverManager;

public class BasicConnection {

	protected Connection conn;
	protected String dbms, host, login, password, database;
	
	public BasicConnection(){
		this.database = "tcc_forms";
	}
	
	public BasicConnection(String dbms, String host, String database, 
			String login, String password) {
		this.dbms = dbms;
		this.host = host;
		this.database = database;
		this.login = login;
		this.password = password;
	}
	
	public Connection getConnection() {
		if(this.conn != null)
			return this.conn;
		
		this.newConnection();
		return this.conn;
	}

	private void newConnection() {
		try{
			this.conn = DriverManager.getConnection("jdbc:"+this.dbms+"://"+this.host+"/"+this.database, 
					this.login, this.password);
		}catch(Exception e){
			System.err.println(e);
		}
	}
}
