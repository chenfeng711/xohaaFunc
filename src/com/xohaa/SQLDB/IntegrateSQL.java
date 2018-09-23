package com.xohaa.SQLDB;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.xohaa.Action;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;
/**
 * 
 * @author Created 2016-5-15 by cjx
 *
 */
public class IntegrateSQL {
	private HandleSQLCon F = null;
	public IntegrateSQL(HandleSQLCon f){
		F = f;
	}
	
	/**
	 * 写入权限
	 * @param tableName
	 * @throws SQLException
	 */
	private void setInsert_Power(String tableName) throws SQLException{
		StringBuffer sql = new StringBuffer();
		sql.append("insert ").append(tableName).append("(")
		.append("FXSID,powerID,YeWuLX,powertype,updatefieldname) values (?,?,?,?,?)");
		F.setInsertFields(sql.toString());
	}
	/**
	 * 先删除，后添加。
	 * @param FXSID
	 * @param powertype
	 * @param fn
	 * @param yewulx
	 * @throws SQLException
	 */
	public void update_Power(long FXSID,Vector<String> powertype,Vector<String> fn,int yewulx) throws SQLException{
		StringBuffer sql = new StringBuffer();
		sql.append("delete from QX_Power where FXSID=? and updatefieldname in(");
		boolean b = true;
		Vector<String> v = new Vector<String>();
		for(int i=0;i<fn.size();i++){
			if(b){
				sql.append("?");
				b = false;
			}else{
				sql.append(",?");
			}
			v.addElement(F.getURLQueryString(fn.get(i).toString()));
		}
		sql.append(")");
		F.setSQLCodePrepare(sql.toString());
		F.setPstvalue(1, FXSID);
		for(int i=0;i<fn.size();i++){
			F.setPstvalue(i+2, fn.get(i).toString());
		}

		F.executeUpdate();
		this.insert_Power(FXSID, v, yewulx, powertype, fn);

	}

	/**
	 * 写入多行权限表
	 * @param tableName 表名
	 * @param FXSID 分销商ID
	 * @param powerID 权限ID－岗位ID或职位ID
	 * @param yewulx  业务类型。默认为0
	 * @param powertype 权限类型：1读，2 读写。
	 * @param fn 更新的字段名
	 * @throws SQLException
	 */
	public void insert_Power(String tableName,long FXSID,Vector<String> powerID,int yewulx,Vector<String> powertype,Vector<String> fn) throws SQLException{
		setInsert_Power(tableName);
		if(powertype !=null){
			for(int i=0;i<powerID.size();i++){
				F.setPstvalue(1, FXSID);
				F.setPstvalue(2, powerID.get(i).toString());
				F.setPstvalue(3, yewulx);
				F.setPstvalue(4, powertype.get(i).toString());
				F.setPstvalue(5, fn.get(i).toString());
				F.addBatch();
			}
		}else{
			for(int i=0;i<powerID.size();i++){
				F.setPstvalue(1, FXSID);
				F.setPstvalue(2, powerID.get(i).toString());
				F.setPstvalue(3, yewulx);
				F.setPstvalue(4, 1);
				F.setPstvalue(5, fn == null?"":fn.get(i).toString());
				F.addBatch();
			}
		}
		F.executeBatch();
	}	
	/**
	 * 写入多行权限表
	 * @param FXSID 分销商ID
	 * @param powerID 权限ID－岗位ID或职位ID
	 * @param yewulx  业务类型。默认为0
	 * @param powertype 权限类型：1读，2 读写。
	 * @throws SQLException 
	 */
	public void insert_Power(long FXSID,Vector<String> powerID,int yewulx,Vector<String> powertype,Vector<String> fn) throws SQLException{
		this.insert_Power("QX_Power", FXSID, powerID, yewulx, powertype,fn);
	}
	/**
	 * 写入多行权限表,默认为写入读的权限。
	 * @param FXSID
	 * @param powerID
	 * @throws SQLException
	 */
	public void insert_Power(long FXSID,Vector<String> powerID) throws SQLException{
		this.insert_Power("QX_Power", FXSID, powerID, 0, null,null);
	}
	/**
	 * 
	 * 写入1行权限表
	 * @param tableName 表名
	 * @param FXSID 分销商id
	 * @param powerID 权限ID－岗位ID或职位ID
	 * @param yewulx  业务类型。默认为0
	 * @param powertype 权限类型：1读，2 读写。
	 * @throws SQLException
	 */
	public void insert_Power(String tableName,long FXSID,String powerID,int yewulx,int powertype,String updatefieldname) throws SQLException{
		setInsert_Power(tableName);
		F.setPstvalue(1, FXSID);
		F.setPstvalue(2, powerID);
		F.setPstvalue(3, yewulx);
		F.setPstvalue(4, powertype);
		F.setPstvalue(5, updatefieldname);
		F.executeUpdate();
	}
	/**
	 * 写入1行权限表,默认写入QX_Power。
	 * @param FXSID
	 * @param powerID
	 * @throws SQLException
	 */
	public void insert_Power(long FXSID,String powerID) throws SQLException{
		this.insert_Power("QX_Power", FXSID, powerID, 0, 1,"");
	}
	
	/**
	 * 读取分销配置
	 * @param configid
	 * @return int
	 * @throws SQLException
	 */
	public int getConfigValueInt(String configid) throws SQLException{
		ResultSet rs = F.executeQuery("select * from Sys_FXConfig where configid='"+ configid +"'");
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
		ResultSet rs = F.executeQuery("select * from Sys_FXConfig where configid='"+ configid +"'");
		String b = null;
		if (rs.next()){
			b = rs.getString("value_string");
		}
		rs.close();
		return b;
	}
	
	/**
	 * 写入订单跟踪
	 * @param docunid
	 * @param desc
	 * @param SO
	 * @param stype
	 * @param FXSID
	 * @param dbname
	 * @param username
	 * @throws SQLException
	 */
	public void insertGenZong(String docunid,String desc,String SO,int stype,int FXSID,String dbname,String username) throws SQLException{
		F.setInsertFields("insert into sale_XSDD_GenZong(actiontime,actiondesc,caozuor,SO,FXSID,dbname,docunid,stype) values (?,?,?,?,?,?,?,?)");
		F.setPstvalue(1, Action.getCurDataTime());
		F.setPstvalue(2, desc);
		F.setPstvalue(3, username);
		F.setPstvalue(4, SO);
		F.setPstvalue(5, FXSID);
		F.setPstvalue(6, dbname);
		F.setPstvalue(7, docunid);
		F.setPstvalue(8, stype);
		F.executeUpdate();
	}

	/**
	 * 写入订单跟踪
	 * @param docunid
	 * @param desc
	 * @param SO
	 * @param stype
	 * @param FXSID
	 * @param dbname
	 * @param username
	 * @throws SQLException
	 */
	public void insertGenZong(String docunid,String desc,String SO,int stype,String FXSID,String dbname,String username) throws SQLException{
		F.setInsertFields("insert into sale_XSDD_GenZong(actiontime,actiondesc,caozuor,SO,FXSID,dbname,docunid,stype) values (?,?,?,?,?,?,?,?)");
		F.setPstvalue(1, Action.getCurDataTime());
		F.setPstvalue(2, desc);
		F.setPstvalue(3, username);
		F.setPstvalue(4, SO);
		F.setPstvalue(5, FXSID);
		F.setPstvalue(6, dbname);
		F.setPstvalue(7, docunid);
		F.setPstvalue(8, stype);
		F.executeUpdate();
	}

	/**
	 * 写入订单跟踪
	 * @param keyunid
	 * @param docunid
	 * @param desc
	 * @param SO
	 * @param stype
	 * @param FXSID
	 * @param dbname
	 * @param username
	 * @throws SQLException
	 */
	public void insertGenZong(String keyunid,String docunid,String desc,String SO,
			int stype,String FXSID,String dbname,String username) throws SQLException{
		F.setInsertFields("insert into sale_XSDD_GenZong(actiontime,actiondesc,caozuor,SO,FXSID,dbname,docunid,stype,Keyunid) values (?,?,?,?,?,?,?,?,?)");
		F.setPstvalue(1, Action.getCurDataTime());
		F.setPstvalue(2, desc);
		F.setPstvalue(3, username);
		F.setPstvalue(4, SO);
		F.setPstvalue(5, FXSID);
		F.setPstvalue(6, dbname);
		F.setPstvalue(7, docunid);
		F.setPstvalue(8, stype);
		F.setPstvalue(9, keyunid);
		F.executeUpdate();
	}
	/**
	 * 写入订单跟踪
	 * @param desc
	 * @param SO
	 * @param stype
	 * @param FXSID
	 * @param username
	 * @throws SQLException
	 * @throws NotesException
	 */
	public void insertGenZong(String desc,String SO,int stype, int FXSID,String username) throws SQLException, NotesException{
		this.insertGenZong(F.getCurDoc().getUniversalID(), desc, SO, stype, FXSID,F.getCurDB().getFileName(), username);
	}
	
	/**
	 * 拼接权限SQL语句
	 * @param doc 自定义的列表配置文档对象
	 * @return StringBuffer
	 * @throws NotesException
	 */
	public StringBuffer SQLDode_Power(Session session,Document doc) throws NotesException{
		StringBuffer power = new StringBuffer();
		if(doc.getItemValueString("isPower").equals("1")){
			Vector<?> S = session.getUserGroupNameList();
			StringBuffer powerid = new StringBuffer();

			boolean b = true;
			for(int i=0;i<S.size();i++){
				if(S.get(i).toString().indexOf("*")<0 && S.get(i).toString().indexOf("L")<0 && S.get(i).toString().indexOf("D")<0){
					powerid.append(b?"'"+S.get(i)+"'":",'" + S.get(i) +"'");
					b = false;
				}
			}

			power.append(" as a ")
			.append("inner join (select DISTINCT " + doc.getItemValueString("powerID") + " from ")
			.append(doc.getItemValueString("powertable"))
			.append(" where ")
			.append(doc.getItemValueString("powerwhere"))
			.append(" and powerID in(")
			.append(powerid)
			.append(")) as p on a.FXSID=p.FXSID");
		}else{
			power.append(" as a ");
		}
		return power;
	}
}
