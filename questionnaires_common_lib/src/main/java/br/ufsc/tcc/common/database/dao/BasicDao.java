package br.ufsc.tcc.common.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Set;

import br.ufsc.tcc.common.database.connection.BasicConnection;

/**
 * Classe básica para manipulação do banco de dados.
 * 
 * @author Gilney N. Mathias
 */
public class BasicDao {
	
	private ResultSet resultSet = null;
	protected PreparedStatement psmt = null;
	protected Connection conn = null;
	
	protected String table;
	
	public BasicDao(BasicConnection c, String table) {
		this.conn = c.getConnection();
		this.table = table;
	}
	
	/**
	 * Método responsável por preparar a query antes de ser executada.
	 * 
	 * @param sql				Sql que se quer preparar.
	 * @throws Exception
	 */
	protected void query(String sql) throws Exception {
		// Para arrumar bug: 'ERROR: syntax error at or near "RETURNING"'
		if(sql.matches("(?i)^select.*"))
			this.psmt = this.conn.prepareStatement(sql);
		else
			this.psmt = this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	}
	
	/**
	 * Retorna o ultimo UID gerado pela ultima query executada.
	 * 
	 * @return				Ultimo UID gerado.
	 * @throws Exception
	 */
	protected long getLastUID() throws Exception {
		if(psmt != null) {
			ResultSet rs = psmt.getGeneratedKeys();
			if(rs.next())
				return rs.getLong(1);
		}
		return -1L;
	}
	
	/**
	 * Executa a ultima query que foi preparada pelo método {@link #query(String) query}.
	 * 
	 * @throws Exception
	 */
	final protected void exec() throws Exception {
		if(psmt != null)
			this.resultSet = this.psmt.executeQuery();
	}
	
	/**
	 * Retorna o resultado da ultima query executada pelo método {@link #exec() exec}.
	 * 
	 * @return	Resultado da ultima query executada.
	 */
	final protected ResultSet getResultSet() {
		return this.resultSet;
	}
	
	/**
	 * Executa um sql Insert utilizando os dados passados pelo parâmetro {@code data}.
	 * 
	 * @param data			HashMap com os dados a serem inseridos utilizando o formato: (campo, valor).
	 * @throws Exception
	 */
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
	
	/**
	 * Executa um sql Update utilizando os dados passados pelo parâmetro {@code data} e as restrições
	 * passadas pelo parâmetro {@code where}.<br>
	 * Todas as restrições neste caso são da forma 'igual', ou seja, 'where campo1 = valor1'.
	 * 
	 * @param data			HashMap com os dados a serem atualizados utilizando o formato: (campo, valor).
	 * @param where			HashMap com as restrições ao Update utilizando o formato: (campo, valor).
	 * @throws Exception
	 */
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
	
	/**
	 * Executa um sql Select utilizando os campos passados pelo parâmetro {@code fields} e as restrições
	 * passadas pelo parâmetro {@code where}.<br>
	 * Todas as restrições neste caso são da forma 'igual', ou seja, 'where campo1 = valor1'.
	 * 
	 * @param fields		String com os campos a serem retornados utilizando o formato: campo1, campo2, etc. ou
	 * 						apenas com o caracter '*' para retornar todos os campos.
	 * @param where			HashMap com as restrições ao Select utilizando o formato: (campo, valor).
	 * @throws Exception
	 */
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
	
	/**
	 * Executa um sql Select utilizando os campos passados pelo parâmetro {@code fields}.
	 * 
	 * @param fields		String com os campos a serem retornados utilizando o formato: campo1, campo2, etc. ou
	 * 						apenas com o caracter '*' para retornar todos os campos.
	 * @throws Exception
	 */
	protected void select(String fields) throws Exception {
		fields = fields.isEmpty() ? "*" : fields;
		String SQL = "SELECT "+fields+" FROM "+this.table+";";
		
		this.query(SQL);
		this.exec();
	}
	
	/**
	 * Executa um sql Delete utilizando as restrições passadas pelo parâmetro {@code where}.<br>
	 * Todas as restrições neste caso são da forma 'igual', ou seja, 'where campo1 = valor1'.
	 * 
	 * @param where			HashMap com as restrições ao Delete utilizando o formato: (campo, valor).
	 * @throws Exception
	 */
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
	
	/**
	 * Executa um sql Delete utilizando nenhuma restrição.
	 * 
	 * @throws Exception
	 */
	protected void delete() throws Exception {
		String SQL = "DELETE FROM "+this.table;
		
		this.query(SQL);
		this.psmt.execute();
	}
}
