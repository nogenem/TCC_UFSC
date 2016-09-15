package dao.connection;

public class MysqlConnection extends BasicConnection {
	
	public MysqlConnection(){
		super();
		this.dbms = "mysql";
		this.host = "localhost";
		this.login = "root";
		this.password = "";
	}
	
	public MysqlConnection(String host, String login, String password){
		super();
		this.dbms = "mysql";
		this.host = host;
		this.login = login;
		this.password = password;
	}
	
	public MysqlConnection(String database, String host, String login, String password){
		super();
		this.dbms = "mysql";
		this.database = database;
		this.host = host;
		this.login = login;
		this.password = password;
	}
	
}
