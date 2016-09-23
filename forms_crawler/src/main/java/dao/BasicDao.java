package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

import dao.connection.BasicConnection;

public class BasicDao {
	
	private ResultSet resultSet = null;
	protected PreparedStatement psmt = null;
	protected Connection conn = null;
	
	protected String table = "";
	
	public BasicDao(BasicConnection c, String table) {
		this.conn = c.getConnection();
		this.table = table;
	}
	
	protected void setTable(String table){
		this.table = table;
	}
	
	protected void query(String sql) throws Exception {
		// Para arrumar bug: 'ERROR: syntax error at or near "RETURNING"'
		if(sql.matches("SELECT.*FROM.*"))
			this.psmt = this.conn.prepareStatement(sql);
		else
			this.psmt = this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	}
	
	protected long getLastUID() throws Exception {
		ResultSet rs = psmt.getGeneratedKeys();
		if(rs.next())
			return rs.getLong(1);
		return -1L;
	}
	
	final protected void exec() throws Exception {
		this.resultSet = this.psmt.executeQuery();
	}
	
	final protected ResultSet getResultSet() {
		return this.resultSet;
	}
	
	protected void insert(HashMap<String, Object> data) throws Exception {
		StringBuilder SQL = new StringBuilder("INSERT INTO "+this.table+" (");
		Set<String> fields = data.keySet();
		
		for(String field : fields){
			SQL.append(field + ",");
		}
		SQL.setLength(SQL.length()-1);
		SQL.append(") VALUES (");

		for(int i = 0; i < data.size(); i++){
			SQL.append("?,");
		}
		SQL.setLength(SQL.length()-1);
		SQL.append(")");

		this.query(SQL.toString());
		
		int count = 1;
		for(String field : fields){
			this.psmt.setObject(count, data.get(field));
			count++;
		}
		this.psmt.execute();
	}
	
	protected void update(HashMap<String, Object> data, HashMap<String, Object> where) throws Exception {
		StringBuilder SQL = new StringBuilder("UPDATE "+this.table+" SET ");
		Set<String> fields = data.keySet();
		
		for(String field : fields){
			SQL.append(field + "=?,");
		}
		SQL.setLength(SQL.length()-1);
		
		SQL.append(" WHERE ");
		Set<String> conditions = where.keySet();
		
		for(String condition : conditions){
			SQL.append(condition + "=? AND ");
		}
		SQL.setLength(SQL.length()-5);
		
		this.query(SQL.toString());
		
		int count = 1;
		for(String field : fields){
			this.psmt.setObject(count, data.get(field));
			count++;
		}

		for (String condition : conditions){
			this.psmt.setObject(count, where.get(condition));
			count++;
		}
		this.psmt.execute();
	}
	
	protected void select(HashMap<String, Object> where) throws Exception {
		String SQL = "SELECT * FROM "+this.table+" WHERE ";

		Set<String> conditions = where.keySet();
		for (String condition : conditions){
			SQL += condition + "=? AND ";
		}
		SQL = SQL.substring(0, SQL.length()-5);

		this.query(SQL);
		
		int count = 1;
		for (String condition : conditions){
			this.psmt.setObject(count, where.get(condition));
			count++;
		}
		this.exec();
	}
	
	protected void select(String s) throws Exception {
		String SQL = "";
		if(s.equals(""))
			SQL = "SELECT * FROM "+this.table+";";
		else
			SQL = "SELECT "+s+" FROM "+this.table+";";
		
		this.query(SQL);
		this.exec();
	}
	
	protected void delete(HashMap<String, Object> where) throws Exception {
		String SQL = "DELETE FROM "+this.table+" WHERE ";
		Set<String> conditions = where.keySet();
		
		for (String condition : conditions){
			SQL += condition += "=? AND ";
		}
		SQL = SQL.substring(0, SQL.length()-5);
		
		this.query(SQL);
		
		int count = 1;
		for (String condition : conditions){
			this.psmt.setObject( count, where.get(condition));
			count++;
		}
		this.psmt.execute();
	}
}
