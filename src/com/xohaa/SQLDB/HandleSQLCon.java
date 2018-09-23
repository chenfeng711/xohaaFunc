package com.xohaa.SQLDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Vector;

import com.xohaa.Opt;
import com.xohaa.Base.Func;
import com.xohaa.SQLDB.Pool;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

/**
 * 
 * SQL Server 操作类库，继承Func类库
 * @author Created 2016-5-15 by cjx
 * 
 */
public class HandleSQLCon extends Func {
	private Connection con;
	private PreparedStatement pst;
	private PreparedStatement pst2 = null;
	private Database initDb = null;
	private String configname = "ZXFX";
	/**
	 * 
	 * @return Database
	 */
	public Database getInitDb() {
		return initDb;
	}
	
	/**
	 * 
	 * @param initDb
	 */
	public void setInitDb(Database initDb) {
		this.initDb = initDb;
	}

	/**
	 * 构造函数
	 * @param session
	 * @param isInit true 初始化连接sql连接池
	 * @throws Exception 
	 */
	public HandleSQLCon(Session session,boolean isInit) throws Exception {
		super(session);	
		initPoolCon(isInit,true);

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
			//this.initCon(conAutoCommit);
			Pool pool = new Pool();
			if(initDb == null){
				initDb = this.OpenDB(Opt.SQLCon_DBName);
			}
			if(initDb == null) throw new Exception("无法打开数据库["+ Opt.SQLCon_DBName +"]");
			StringBuffer buf = new StringBuffer();
			buf.append(this.getCurPATH().toUpperCase()).append("_").append(configname).append("Pool");
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
			if(initDb == null){
				initDb = this.OpenDB(Opt.SQLCon_DBName);
			}
			Document appdoc = this.getDocumentBykey(initDb, "vw_fmLinkList_ConnectionName",this.configname, true);
			dbtype = appdoc.getItemValueString("DBType").toLowerCase();
			appdoc.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
		return dbtype;
	}
	/**
	 * 直接连接sql
	 * @throws Exception 
	 */
	public void initCon(boolean conAutoCommit) throws Exception{
		if(initDb == null){
			initDb = this.OpenDB(Opt.SQLCon_DBName);
		}
		Document appdoc = this.getDocumentBykey(initDb, "vw_fmLinkList_ConnectionName", configname, true);
		if(appdoc != null){
			con = Pool.CreateConn(appdoc);
			this.setAutoCommit(conAutoCommit);
			appdoc.recycle();
		}else{
			
			throw new Exception("找不到数据连接配置："+ this.configname);
		}
	}

	/**
	 * 构造函数
	 * @param session
	 * @param conAutoCommit 手动提交事务（false）
	 * @param isInit true 连接sql数据库
	 * @throws Exception 
	 */
	public HandleSQLCon(Session session,boolean isInit,boolean conAutoCommit) throws Exception {
		super(session);
		initPoolCon(isInit,conAutoCommit);
	}
	
	/**
	 * 
	 * @param session
	 * @param isInit
	 * @param conAutoCommit
	 * @param configname
	 * @throws Exception
	 */
	public HandleSQLCon(Session session,boolean isInit,boolean conAutoCommit,String configname) throws Exception {
		super(session);
		this.setConfigname(configname);
		initPoolCon(isInit,conAutoCommit);
	}	

	/**构造函数
	 * @param session 当前会话
	 * @throws NotesException
	 */
	public HandleSQLCon(Session session) throws NotesException {
		super(session);	
	}

	/**
	 * 
	 * @throws NotesException
	 */
	public HandleSQLCon() throws NotesException {
		super();
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
		if (pst2 != null ) pst2.close();
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
	public boolean DeleteData(String tablename, String key, String keyvalue) throws SQLException {
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
		this.closePst();
		pst = con.prepareStatement(this.updateDataStr(tableName, fields,key));
	}

	/**
	 * 预编SQL语句:select,update,insert;
	 * @param sql sql语句
	 * @throws SQLException 
	 */
	public void setSQLCodePrepare(String sql) throws SQLException{
		this.closePst();
		pst = con.prepareStatement(sql);
	}

	/**
	 * 预编SQL语句:select,update,insert;
	 * @param sql sql语句
	 * @throws SQLException 
	 */
	public void setSQLCodePrepare(StringBuffer sql) throws SQLException{
		this.closePst();
		pst = con.prepareStatement(sql.toString());
	}
	
	/**
	 * 
	 * @param sql
	 * @throws SQLException
	 */
	public void setSQLCodePrepareUpdatetable(StringBuffer sql) throws SQLException{
		//pst = this.setSQLCodePrepareResultSetOption(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		pst = con.prepareStatement(sql.toString(),ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
	}
	
	/**
	 * 使用ResultSet 选项控制prepareStatement
	 * @param sql
	 * @param type
	 * @param concur
	 * @throws SQLException
	 */
	public void setSQLCodePrepareResultSetOption(StringBuffer sql,int type,int concur) throws SQLException{
		this.closePst();
		pst = con.prepareStatement(sql.toString(),type,concur);
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
	 * @throws SQLException 
	 * 
	 */
	public void closePst() throws SQLException{
		if(this.pst != null) this.pst.close();
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
	 * 执行sql语句，适用于insert into,update,delete 等语句
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate_prepare(String sql) throws SQLException {
		pst2 = con.prepareStatement(sql);
		return pst2.executeUpdate();
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
	 * 创建新的prepareStatement执行
	 * @param selectStr
	 * @return
	 * @throws SQLException
	 */
	public ResultSet executeQuery_prepare(String selectStr) throws SQLException{
		 pst2 = this.con.prepareStatement(selectStr, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		return pst2.executeQuery();
	}
	/**
	 * 执行SQLAPI
	 * @param APIName
	 * @return ResultSet
	 * @throws Exception
	 */
	public ResultSet executeSQLAPI(String APIName) throws Exception{
		SQLAPI api = new SQLAPI(this);
		return api.executeSQLAPI(APIName);
	}

	/**
	 * 执行多值配置的SQLAPI，可使用搜索条件
	 * @param APIName
	 * @param searchkeys
	 * @return ResultSet
	 * @throws Exception 
	 */
	public ResultSet executeSQLAPI(String APIName,Vector<?> searchkeys) throws Exception{
		SQLAPI api = new SQLAPI(this);
		return api.executeSQLAPI(APIName, searchkeys);
	}
	/**
	 *  执行多值配置的SQLAPI，可使用搜索条件,in(??)替换in(?,?)
	 * @param APIName
	 * @param searchkeys
	 * @param isIn
	 * @return ResultSet
	 * @throws Exception
	 */
	public ResultSet executeSQLAPI(String APIName,Vector<?> searchkeys,String isIn) throws Exception{
		SQLAPI api = new SQLAPI(this);
		return api.executeSQLAPI(APIName, searchkeys, isIn);
	}

	/**
	 * 执行指定的SQL语句，并返回第一行记录字段的值。
	 * @param sql
	 * @param fieldname
	 * @return String
	 * @throws SQLException
	 */
	public String SelectData(String sql, String fieldname) throws SQLException{
		String temp = null;
		ResultSet rs = this.executeQuery(sql);
		if(rs.next()){
			temp = rs.getString(fieldname);
		}else{
			temp = "";
		}
		rs.close();
		return temp;
	}

	/**
	 * 返回结果集条数
	 * @param key
	 * @param tableName
	 * @return long
	 */
	public long SelectCount(String tableName,String fieldname,String key) {
		String selectStr = "select count(*) from " + tableName + " where "+ fieldname +" = ?";
		long count = 0;
		ResultSet rs = null;
		try {
			pst2 = this.con.prepareStatement(selectStr);
			pst2.setString(1, key);
			rs = pst2.executeQuery();
			if (rs.next()) {
				count = rs.getLong(1); //结果集对象的当前行中指定��械闹��
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return count;
		} finally {

			try {
				if (rs != null)	rs.close();
				pst2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return count;
	}
	/**
	 * 
	 * @param tableName
	 * @param where
	 * @return long
	 */
	public long SelectCount(String tableName ,String where) {

		StringBuffer selectStr = new StringBuffer();
		selectStr.append("select count(*) from ").append(tableName);
		if(!where.equals("")){
			selectStr.append(" where ").append(where);
		}
		long count = 0;
		ResultSet rs = null;
		try {
			pst2 = this.con.prepareStatement(selectStr.toString());
			//pst.setString(1, key);
			rs = pst2.executeQuery();
			if (rs.next()) {
				count = rs.getLong(1); //结果集对象的当前行中指定列的值
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return count;
		} finally {

			try {

				if (rs != null)	rs.close();
				pst2.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return count;
	}
	/**
	 * 读取分销配置
	 * @param configid
	 * @return int
	 * @throws SQLException
	 */
	public int getConfigValueInt(String configid) throws SQLException{
		ResultSet rs = this.executeQuery("select * from Sys_FXConfig where configid='"+ configid +"'");
		int b = 0;
		if (rs.next()){
			b = rs.getInt("value_int");
		}
		rs.close();
		return b;
	}
	/**
	 * 读取分销配置
	 * @param configid
	 * @return String
	 * @throws SQLException
	 */
	public String getConfigValueString(String configid) throws SQLException{
		ResultSet rs = this.executeQuery("select * from Sys_FXConfig where configid='"+ configid +"'");
		String b = null;
		if (rs.next()){
			b = rs.getString("value_string");
		}
		rs.close();
		return b;
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
	 * 同时调用了父类recycleFunc
	 */
	public void recycleCon() {
		try {
			if (pst != null ) pst.close();
			if (pst2 != null ) pst2.close();
			if(con != null) con.close();
			if(initDb != null) initDb.recycle();
			//this.saveCurAgentinfo("结束-------->",2);
			super.recycleFunc();
		} catch (SQLException e) {
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 关闭数据库链接
	 * 同时调用了父类recycleFunc
	 * @param isSubmit true 调用commit()
	 */
	public void recycleCon(boolean isSubmit) {
		try {
			if(!con.getAutoCommit() && isSubmit) con.commit();
			if (pst != null ) pst.close();
			if (pst2 != null ) pst2.close();
			if(con != null) con.close();
			if(initDb != null) initDb.recycle();
			//this.saveCurAgentinfo("结束-------->",2);
			super.recycleFunc();
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
