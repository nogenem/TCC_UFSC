package br.ufsc.tcc.common.database.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class BasicConnection {
	
	protected Connection conn;
	protected String database, dbms, host, login, password;
	
	/**
	 * Retorna a conexão com o banco de dados.
	 * 
	 * @return		A conexão com o banco de dados.
	 */
	public Connection getConnection() {
		if(this.conn != null)
			return this.conn;
		
		this.newConnection();
		return this.conn;
	}
	
	/**
	 * Cria uma nova conexão com o banco de dados.
	 */
	private void newConnection() {
		try{
			this.conn = DriverManager.getConnection("jdbc:"+this.dbms+"://"+this.host+"/"+this.database, 
					this.login, this.password);
		}catch(Exception e){
			System.err.println("BasicConnection::newConnection()> " +e.getMessage());
		}
	}
	
	/**
	 * Fecha a conexão com o banco de dados.
	 */
	public void close(){
		try {
			this.conn.close();
		} catch (SQLException e) {
			System.err.println("BasicConnection::close()> " +e.getMessage());
		}
	}
}
