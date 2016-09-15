package dao.connection;

public class PostgreConnection extends BasicConnection {
	
	public PostgreConnection(){
		super();
		this.dbms = "postgresql";
		this.host = "localhost:5432";
		this.login = "postgres";
		this.password = "123";
	}
	
	public PostgreConnection(String host, String login, String password){
		super();
		this.dbms = "postgresql";
		this.host = host;
		this.login = login;
		this.password = password;
	}
	
	public PostgreConnection(String database, String host, String login, String password){
		super();
		this.dbms = "postgresql";
		this.database = database;
		this.host = host;
		this.login = login;
		this.password = password;
	}
	
}
