package br.ufsc.tcc.common.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
	
	/**
	 * Prepara o sql passado para ser executado depois.
	 * 
	 * @param sql			Query SQL que se quer preparar.
	 * @throws Exception
	 */
	protected void query(String sql) throws Exception {
		// Para arrumar bug: 'ERROR: syntax error at or near "RETURNING"'
		if(sql.matches("SELECT.*FROM.*"))
			this.psmt = this.conn.prepareStatement(sql);
		else
			this.psmt = this.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	}
	
	/**
	 * Retorna o ultimo UID gerado pelo banco de dados.
	 * 
	 * @return				O ultimo UID gerado pelo banco de dados.
	 * @throws Exception
	 */
	protected long getLastUID() throws Exception {
		ResultSet rs = psmt.getGeneratedKeys();
		if(rs.next())
			return rs.getLong(1);
		return -1L;
	}
	
	/**
	 * Executa a query previamente preparada.
	 * 
	 * @throws Exception
	 */
	final protected void exec() throws Exception {
		this.resultSet = this.psmt.executeQuery();
	}
	
	/**
	 * Retorna o resultado da ultima query executada.
	 * 
	 * @return		O resultado da ultima query executada.
	 */
	final protected ResultSet getResultSet() {
		return this.resultSet;
	}
	
	/**
	 * Monta e executa um INSERT utilizando os dados passados.
	 * 
	 * @param data			Um mapa contendo os dados que se quer inserir no banco de dados.</br>
	 * 						O mapa deve ser da forma: (campo, valor).
	 * @throws Exception
	 */
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
	
	/**
	 * Monta e executa um UPDATE utilizando os dados passados.
	 * 
	 * @param data			Um mapa contendo os dados que se quer atualizar no banco de dados.</br>
	 * 						O mapa deve ser da forma: (campo, valor).
	 * @param where			Um mapa contendo os dados que serão utilizados no WHERE da query.
	 * 						Todos os valores do mapa serão juntados utilizando AND, 
	 * 						por exemplo: WHERE campo1=valor1 AND campo2=valor2.</br>
	 * 						O mapa deve ser da forma: (campo, valor).
	 * @throws Exception
	 */
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

		for(String condition : conditions){
			this.psmt.setObject(count, where.get(condition));
			count++;
		}
		this.psmt.execute();
	}
	
	/**
	 * Monta e executa um SELECT utilizando os dados passados.
	 * 
	 * @param fields		Os campos que se quer selecionar do banco de dados. Por exemplo:</br>
	 * 						"*" para selecionar todos, ou</br>
	 * 						"field1, field2" para selecionar apenas os campos field1 e field2.
	 * @param where			Um mapa contendo os dados que serão utilizados no WHERE da query.
	 * 						Todos os valores do mapa serão juntados utilizando AND, 
	 * 						por exemplo: WHERE campo1=valor1 AND campo2=valor2.</br>
	 * 						O mapa deve ser da forma: (campo, valor).
	 * @throws Exception
	 */
	protected void select(String fields, HashMap<String, Object> where) throws Exception {
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
	 * Monta e executa um SELECT utilizando os dados passados.
	 * 
	 * @param fields		Os campos que se quer selecionar do banco de dados. Por exemplo:</br>
	 * 						"*" para selecionar todos, ou</br>
	 * 						"field1, field2" para selecionar apenas os campos field1 e field2.
	 * @throws Exception
	 */
	protected void select(String fields) throws Exception {
		String SQL = "SELECT "+fields+" FROM "+this.table+";";
		
		this.query(SQL);
		this.exec();
	}
	
	/**
	 * Monta e executa um DELETE utilizando os dados passados.
	 * 
	 * @param where			Um mapa contendo os dados que serão utilizados no WHERE da query.
	 * 						Todos os valores do mapa serão juntados utilizando AND, 
	 * 						por exemplo: WHERE campo1=valor1 AND campo2=valor2.</br>
	 * 						O mapa deve ser da forma: (campo, valor).
	 * @throws Exception
	 */
	protected void delete(HashMap<String, Object> where) throws Exception {
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
}
