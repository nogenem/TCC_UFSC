package br.ufsc.tcc.common.database.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import br.ufsc.tcc.common.util.CommonLogger;

public class BasicConnection {
	
	protected Connection conn;
	protected String database, dbms, host, login, password;
	
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
			CommonLogger.error(e);
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
