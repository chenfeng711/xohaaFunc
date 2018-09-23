package com.xohaa.SQLDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import com.xohaa.SQLDB.Pool;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

/**
 * 
 * SQL Server 操作类库
 * @author Created 2016-5-15 by cjx
 * 
 */
public class HandlePool{
	private Connection con;
	private PreparedStatement pst;
	private Database initDb = null;
	private String configname = "ZXFX";
	private HandleSQLCon F = null;


	/**
	 * 构造函数
	 * @param session
	 * @param conAutoCommit 手动提交事务（false）
	 * @param isInit true 连接sql数据库
	 * @throws Exception 
	 */
	public HandlePool(HandleSQLCon F,boolean isInit,boolean conAutoCommit,String configname) throws Exception {
		this.initDb = F.getInitDb();
		this.configname = configname;
		this.F = F;
		initPoolCon(isInit,conAutoCommit);
	}
	public HandlePool(HandleSQLCon F,boolean isInit,boolean conAutoCommit) throws Exception {
		this.F = F;
		this.initDb = F.getInitDb();
		initPoolCon(isInit,conAutoCommit);
	}	
	/**
	 * 通过连接池连接sql
	 * @param session
	 * @param isInit
	 * @param conAutoCommit
	 * @throws Exception 
	 */
	private void initPoolCon(boolean isInit,boolean conAutoCommit) throws Exception{
		if(isInit){
			Pool pool = new Pool();
			StringBuffer buf = new StringBuffer();
			buf.append(F.getCurPATH().toUpperCase()).append("_").append(configname).append("Pool");
			con = pool.getConnectionByPoolName(initDb,buf.toString());
			this.setAutoCommit(conAutoCommit);
		}
	}
	
	/**
	 * 获取数据库类型
	 * @return String
	 */
	public String getDBType(){
		String dbtype = null;
		try{
			Document appdoc = F.getDocumentBykey(initDb, "vw_fmLinkList_ConnectionName",this.configname, true);
			dbtype = appdoc.getItemValueString("DBType").toLowerCase();
			appdoc.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
		return dbtype;
	}
	/**
	 * 直接连接sql
	 * @throws NotesException 
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 */
	public void initCon(boolean conAutoCommit) throws NotesException, ClassNotFoundException, SQLException{
		Document appdoc = F.getDocumentBykey(initDb, "vw_fmLinkList_ConnectionName", configname, true);
		con = Pool.CreateConn(appdoc);
		this.setAutoCommit(conAutoCommit);
		appdoc.recycle();
	}

	/**
	 * 设置提交事务
	 * @param conAutoCommit 手动提交事务(false) 自动提交事务(true)
	 * @throws SQLException
	 */
	public void setAutoCommit(boolean conAutoCommit) throws SQLException{
		con.setAutoCommit(conAutoCommit);
	}

	/**
	 * 连接数据库
	 * @param conAutoCommit 手动提交事务(false) 自动提交事务(true)
	 * @throws Exception 
	 */
	public void SQLConnection(boolean conAutoCommit) throws Exception{
		initPoolCon(true,conAutoCommit);
	}

	/**
	 * 连接数据库－自动提交事务(true)
	 * @throws Exception 
	 */
	public void SQLConnection() throws Exception{
		this.SQLConnection(true);
	}

	/**
	 * 关闭连接。
	 * @throws SQLException
	 */
	public void closeConnection() throws SQLException{
		if (pst != null ) pst.close();
		if(con != null) con.close();
	}
	
	/**
	 * 事务提交
	 * @throws SQLException 
	 */
	public void Commit(){
		try {
			con.commit();
		} catch (SQLException e1) {
			// TODO 自动生成 catch 块
			e1.printStackTrace();
		}
	}

	/**
	 * 事务回滚
	 * @throws SQLException
	 */
	public void rollback(){
		try {
			con.rollback();//事务回滚
		} catch (SQLException e1) {
			// TODO 自动生成 catch 块
			e1.printStackTrace();
		}
	}

	/**
	 * 删除数据
	 * @param tablename
	 * @param key
	 * @param keyvalue
	 * @return boolean
	 * @throws SQLException
	 */
	public boolean deleteData(String tablename, String key, String keyvalue) throws SQLException {
		String deleStr = "delete from " + tablename + " where " + key + "='"+ keyvalue + "'";
		return this.execute(deleStr);
	}

	/**
	 * 执行sql
	 * @param sql
	 * @return boolean
	 * @throws SQLException 
	 */
	public boolean execute(String sql) throws SQLException{
		pst = con.prepareStatement(sql);
		return pst.execute();
	}

	/**
	 * 预编更新记录SQL语句
	 * @param tableName
	 * @param fields
	 * @param key
	 * @throws SQLException
	 */
	public void setUpdateFields(String tableName,Vector<String> fields,String key) throws SQLException{
		pst = con.prepareStatement(this.updateDataStr(tableName, fields,key));
	}

	/**
	 * 预编SQL语句:select,update,insert;
	 * @param sql sql语句
	 * @throws SQLException 
	 */
	public void setSQLCodePrepare(String sql) throws SQLException{
		pst = con.prepareStatement(sql);
	}

	/**
	 * 预编SQL语句:select,update,insert;
	 * @param sql sql语句
	 * @throws SQLException 
	 */
	public void setSQLCodePrepare(StringBuffer sql) throws SQLException{
		pst = con.prepareStatement(sql.toString());
	}
	/**
	 * 预编SQL语句:update
	 * @param updateDataStr sql语句
	 * @throws SQLException 
	 */
	public void setUpdateFields(String updateDataStr) throws SQLException{
		pst = con.prepareStatement(updateDataStr);
	}

	/**
	 * 预编SQL语句:insert
	 * @param insertDataStr sql语句
	 * @throws SQLException
	 */
	public void setInsertFields(String insertDataStr) throws SQLException{
		pst = con.prepareStatement(insertDataStr);
	}

	/**
	 * 预编SQL语句:insert
	 * @param tableName 表名
	 * @param fields 字段数据组
	 * @throws SQLException
	 */
	public void  setInsertFields(String tableName,Vector<String> fields) throws SQLException{
		pst = con.prepareStatement(this.insertDataStr(tableName, fields));
		
	}

	/**
	 * 
	 * @param index
	 * @param v
	 * @throws SQLException
	 */
	public void setPstvalue(int index,String v) throws SQLException{
		pst.setString(index, v);
	}
	/**
	 * 
	 * @param index
	 * @param v
	 * @throws SQLException
	 */
	public void setPstvalue(int index,int v) throws SQLException{
		pst.setInt(index, v);
	}
	/**
	 * 
	 * @param index
	 * @param v
	 * @throws SQLException
	 */
	public void setPstvalue(int index,Object v) throws SQLException{
		pst.setObject(index, v);
	}
	/**
	 * 
	 * @param index
	 * @param v
	 * @throws SQLException
	 */
	public void setPstvalue(int index,Timestamp v) throws SQLException{
		pst.setTimestamp(index, v);
	}
	/**
	 * 
	 * @param index
	 * @param date
	 * @param isTimestamp
	 * @throws SQLException
	 */
	public void setPstvalue(int index,Date date,boolean isTimestamp) throws SQLException{
		if(isTimestamp){
			pst.setTimestamp(index, new Timestamp(date.getTime()));
		}else{
			pst.setDate(index, (java.sql.Date) date);
		}
	}
	/**
	 * 
	 * @param index
	 * @param date
	 * @throws SQLException
	 */
	public void setPstvalue(int index,Date date) throws SQLException{
		this.setPstvalue(index, date, true);
	}

	/**
	 * 
	 * @param index
	 * @param v
	 * @throws SQLException
	 */
	public void setPstvalue(int index,double v) throws SQLException{
		pst.setDouble(index, v);
	}
	/**
	 * 
	 * @param index
	 * @param v
	 * @throws SQLException
	 */
	public void setPstvalue(int index,long v) throws SQLException{
		pst.setLong(index, v);
	}
	/**
	 * 
	 * @param index
	 * @param v
	 * @throws SQLException
	 */
	public void setPstvalue(int index,float v) throws SQLException{
		pst.setFloat(index, v);
	}
	/**
	 * 
	 * @throws SQLException
	 */
	public void addBatch() throws SQLException{
		pst.addBatch();
	}
	/**
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public void addBatch(String sql) throws SQLException{
		pst.addBatch(sql);
	}
	/**
	 * 
	 * @return int
	 * @throws SQLException
	 */
	public int executeUpdate() throws SQLException{
		return pst.executeUpdate();
	}
	/**
	 * 
	 * @return int[]
	 * @throws SQLException
	 */
	public int[] executeBatch() throws SQLException{
		return pst.executeBatch();
	}


	/**
	 * 静态SQL模式，获取新增记录ID
	 * @param sql
	 * @return Long 主键
	 * @throws SQLException 
	 */
	public long insertDataReturnID(String sql) throws SQLException{
		ResultSet rs = null;
		long id = 0;
		pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
		pst.executeUpdate(); 
		//检索由于执行此 Statement 对象而创建的所有自动生成的键 
		rs = pst.getGeneratedKeys();
		if (rs.next()) { 
			//知其仅有一列，故获取第一列 
			id= rs.getLong(1);
		}
		rs.close();
		return id;
	}

	/**
	 * 静态SQL模式，可获取返回ID的写法。
	 * 获取ID时，请使用getGeneratedKeys函数获取
	 * @param tableName
	 * @param fields
	 * @throws SQLException
	 */
	public void setInsertFieldsReturn(String tableName,Vector<String> fields) throws SQLException{
		pst = con.prepareStatement(this.insertDataStr(tableName, fields), Statement.RETURN_GENERATED_KEYS);

	}
	/**
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public void setInsertFieldsReturn(String sql) throws SQLException{
		pst = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

	}

	/**
	 * 获取保存后自动生成的键
	 * 需setInsertFieldsReturn 函数使用
	 * @return long
	 * @throws SQLException
	 */
	public long getGeneratedKeys() throws SQLException{
		ResultSet rs = null;
		long id = 0;
		//检索由于执行此 Statement 对象而创建的所有自动生成的键 
		rs = pst.getGeneratedKeys();
		if (rs.next()) { 
			//知其仅有一列，故获取第一列 
			id= rs.getLong(1);
		}
		rs.close();
		return id;
	}
	/**
	 * 预编插入记录SQL语句
	 * @param tableName
	 * @param fields
	 * @return String
	 */
	public String insertDataStr(String tableName, Vector<String> fields) {
		StringBuffer instr = new StringBuffer("Insert into " + tableName + "(");
		StringBuffer valuesstr = new StringBuffer("values(");
		for (int i = 0; i < fields.size(); i++) {
			if (i != fields.size() - 1) {
				instr.append(fields.get(i).toString()).append(",");
				valuesstr.append("?,");
			} else {
				instr.append(fields.get(i).toString()).append(")");
				valuesstr.append("?)");
			}
		}
		instr.append(valuesstr);
		return instr.toString();

	}
	/**
	 * 预编更新记录SQL语句
	 * @param tableName
	 * @param fields
	 * @param key
	 * @return String
	 */
	public String updateDataStr(String tableName, Vector<String> fields,String key) {
		StringBuffer instr = new StringBuffer(200);
		instr.append(this.updateDataStr(tableName, fields));
		instr.append(" where ").append(key).append("=?");
		return instr.toString();
	}
	/**
	 * 预编更新记录SQL语句
	 * @param tableName
	 * @param fields
	 * @return String
	 */
	public String updateDataStr(String tableName, Vector<String>  fields) {
		StringBuffer instr = new StringBuffer(200);
		instr.append("update ").append(tableName).append(" set ");
		for (int i = 0; i < fields.size(); i++) {
			if (i != fields.size() - 1) {
				instr.append(fields.get(i).toString()).append("=?,");
			} else {
				instr.append(fields.get(i).toString()).append("=?");
			}
		}
		return instr.toString();
	}


	/**
	 * 执行sql语句，适用于insert into,update,delete 等语句
	 * @param sql
	 * @return int 
	 * @throws SQLException
	 */
	public int executeUpdate(String sql) throws SQLException {
		pst = con.prepareStatement(sql);
		return pst.executeUpdate();
	}

	/**
	 * 
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery() throws SQLException {
		return pst.executeQuery();
	}
	/**
	 * 执行select SQL语句 返回结果集
	 * @param selectStr
	 * @return ResultSet rs
	 * @throws SQLException 
	 */
	public ResultSet executeQuery(String selectStr) throws SQLException {
		pst = this.con.prepareStatement(selectStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		return pst.executeQuery();
	}	
	/**
	 * 执行select SQL语句 返回结果集
	 * @param selectStr
	 * @param searchkeys
	 * @return ResultSet
	 * @throws SQLException 
	 * @throws Exception
	 */
	public ResultSet executeQuery(String selectStr,Vector<?> searchkeys) throws SQLException{
		pst = this.con.prepareStatement(selectStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		for(int i=0;i<searchkeys.size();i++){
			pst.setObject(i+1, searchkeys.get(i));
			//pst.setString(i+1, searchkeys.get(i).toString());
		}
		return pst.executeQuery();
	}

	/**
	 * 执行select SQL语句 返回结果集
	 * @param selectStr
	 * @param searchkeys
	 * @return ResultSet
	 * @throws SQLException 
	 * @throws Exception
	 */
	public ResultSet executeQuery(String selectStr,Object searchkeys) throws SQLException{
		pst = this.con.prepareStatement(selectStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		pst.setObject(1, searchkeys);
		return pst.executeQuery();
	}
	/**
	 * 执行select SQL语句 返回结果集
	 * @param selectStr
	 * @param value 使用多值，使用muilt分隔符
	 * @param muilt 分隔符
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery(String selectStr,String value,String muilt) throws SQLException{
		pst = this.con.prepareStatement(selectStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		String arr[] = value.split(muilt);
		for(int i=0;i<arr.length;i++){
			pst.setString(i+1,arr[i].trim());
		}
		return pst.executeQuery();
	}

	/**
	 * 换取连接
	 * @return Connection
	 */
	public Connection getCon() {
		return con;
	}

	/**
	 * 设置链接
	 * @param con
	 */
	public void setCon(Connection con) {
		this.con = con;
	}

	/**
	 * 获取预处理
	 * @return PreparedStatement
	 */
	public PreparedStatement getPst() {
		return pst;
	}

	/**
	 * 设置预
	 * @param pst
	 */
	public void setPst(PreparedStatement pst) {
		this.pst = pst;
	}

	/**
	 * 关闭数据库链接
	 * 
	 */
	public void recyclePool() {
		try {
			if (pst != null ) pst.close();
			if(con != null) con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * @param name 要设置的 configname
	 */
	public void setConfigname(String name) {
		configname = name;
	}

	/**
	 * @return configname
	 */
	public String getConfigname() {
		return configname;
	}
}
