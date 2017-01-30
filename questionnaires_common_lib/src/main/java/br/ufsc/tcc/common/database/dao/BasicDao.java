package br.ufsc.tcc.common.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

import br.ufsc.tcc.common.database.connection.BasicConnection;

public class BasicDao {
	
	private ResultSet resultSet = null;
	protected PreparedStatement psmt = null;
	protected Connection conn = null;
	
	protected String table;
	
	public BasicDao(BasicConnection c, String table) {
		this.conn = c.getConnection();
		this.table = table;
	}
	
	protected void query(String sql) throws Exception {
		// Para arrumar bug: 'ERROR: syntax error at or near "RETURNING"'
		if(sql.matches("(?i)^select.*"))
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
		if(data == null || data.isEmpty())
			throw new SQLException("Parametro data nao especificado.");
		
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
		if(data == null || data.isEmpty() || where == null || where.isEmpty())
			throw new SQLException("Parametro data ou where nao especificado.");
		
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

		for(String condition : conditions){
			this.psmt.setObject(count, where.get(condition));
			count++;
		}
		this.psmt.execute();
	}
	
	protected void select(String fields, HashMap<String, Object> where) throws Exception {
		if(where == null || where.isEmpty()){
			this.select(fields);
			return;
		}
		
		String SQL = "SELECT "+fields+" FROM "+this.table+" WHERE ";

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
	
	protected void select(String fields) throws Exception {
		fields = fields.isEmpty() ? "*" : fields;
		String SQL = "SELECT "+fields+" FROM "+this.table+";";
		
		this.query(SQL);
		this.exec();
	}
	
	protected void delete(HashMap<String, Object> where) throws Exception {
		if(where == null || where.isEmpty()){
			this.delete();
			return;
		}
		
		String SQL = "DELETE FROM "+this.table+" WHERE ";
		Set<String> conditions = where.keySet();
		
		for(String condition : conditions){
			SQL += condition += "=? AND ";
		}
		SQL = SQL.substring(0, SQL.length()-5);
		
		this.query(SQL);
		
		int count = 1;
		for(String condition : conditions){
			this.psmt.setObject(count, where.get(condition));
			count++;
		}
		this.psmt.execute();
	}
	
	protected void delete() throws Exception {
		String SQL = "DELETE FROM "+this.table;
		
		this.query(SQL);
		this.psmt.execute();
	}
}
