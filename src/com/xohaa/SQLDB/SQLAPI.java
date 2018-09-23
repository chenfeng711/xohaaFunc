package com.xohaa.SQLDB;
import java.sql.ResultSet;
import java.util.Vector;
import com.xohaa.Action;

import lotus.domino.Document;
/**
 * 
 * @author Created 2016-5-15 by cjx
 *
 */
public class SQLAPI {
	private HandleSQLCon F;
	public SQLAPI(HandleSQLCon f) {
		F = f;
	}
	/**
	 * 执行SQLAPI
	 * @param APIName
	 * @return ResultSet
	 * @throws Exception
	 */
	public ResultSet executeSQLAPI(String APIName) throws Exception{
		String sql[] = this.getSQLAPICode(APIName);
		if(!sql[1].equals("")){
			return F.executeQuery(sql[0],Action.getMuiltValue(sql[1], ";"));
		}else{
			return F.executeQuery(sql[0]);
		}
	}

	/**
	 * 执行多值配置的SQLAPI，可使用搜索条件
	 * @param APIName
	 * @param searchkeys
	 * @return ResultSet
	 * @throws Exception 
	 */
	public ResultSet executeSQLAPI(String APIName,Vector<?> searchkeys) throws Exception{
		String sql[] = this.getSQLAPICode(APIName);
		if(!sql[1].equals("")){
			Action.extendVector(searchkeys, Action.getMuiltValue(sql[1], ";")); //合并
		}
		return F.executeQuery(sql[0],searchkeys);

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
		if(isIn != null && !isIn.equals("")){
			String sql[] = this.getSQLAPICode(APIName);
			StringBuffer buf = new StringBuffer();
			for(int i=0;i<searchkeys.size();i++){
				if(buf.length()>0){
					buf.append(",");
				}
				buf.append("?");
			}
			sql[0] = sql[0].replace("??",buf.toString());

			if(!sql[1].equals("")){
				Action.extendVector(searchkeys, Action.getMuiltValue(sql[1], ";")); //合并
			}

			return F.executeQuery(sql[0],searchkeys);
		}else{
			return F.executeSQLAPI(APIName, searchkeys);
		}
	}

	/**
	 * 获取SQLAPI的代码
	 * @param APIName
	 * @param wherekeys
	 * @return String[]
	 * @throws Exception
	 */
	private String[] getSQLAPICode(String APIName) throws Exception{
		StringBuffer sql = new StringBuffer();
		StringBuffer vkey = new StringBuffer();
		String arr[] = new String [2]; 
		Document cdoc = F.getDocumentBykey(F.OpenDB("zxfx/Sys_FXConfig.nsf", false), "vw_fmSQLAPI", APIName,true);
		if(cdoc == null){
			System.out.println("找不到["+ APIName +"]");
			throw new Exception("找不到["+ APIName +"]");
		}
		sql.append(cdoc.getItemValueString("SQLCode"));
		String where1 = cdoc.getItemValueString("where");
		String where2 = cdoc.getItemValueString("where2");
		String where3 = cdoc.getItemValueString("where3");
		String where4 = cdoc.getItemValueString("where4");

		SQLAPICodeWhere(sql,vkey,where1,"skey1","inkey1");
		SQLAPICodeWhere(sql,vkey,where2,"skey2","inkey2");
		SQLAPICodeWhere(sql,vkey,where3,"skey3","inkey3");
		SQLAPICodeWhere(sql,vkey,where4,"skey4","inkey4");

		arr[1] = vkey.toString();
		//获取权限
		String t = cdoc.getItemValueString("power");
		if(t.equals("")){
			arr[0] = sql.toString();
		}else if(t.equals("1")){
			arr[0] = sql.toString().replace("???", F.getUserGroupNameList());
		}else if(t.equals("2")){
			arr[0] = sql.toString().replace("???", F.getGWIDByUserGroupNameList_IN());
		}else{
			arr[0] = sql.toString();
		}

		cdoc.recycle();
		return arr;
	}
	/**
	 * 
	 * @param sql
	 * @param vkey
	 * @param where
	 * @param skeyName
	 * @param inkeyName
	 */
	private void SQLAPICodeWhere(StringBuffer sql,StringBuffer vkey, String where,String skeyName,String inkeyName){
		if(!where.equals("")){
			String autokeys = null;
			if("skey1".equals(skeyName)){
				autokeys = F.getURLQueryString(skeyName);
				if(autokeys == null || autokeys.equals("")){
					autokeys = F.getURLQueryString("autosearchkeys");
				}
			}else{
				autokeys = F.getURLQueryString(skeyName);
			}
			String inkeys = F.getURLQueryString(inkeyName);

			if(inkeys!=null && !inkeys.equals("")){
				StringBuffer buf = new StringBuffer();
				Vector<String> searchkeys = Action.getMuiltValue(inkeys, ";");
				for(int i=0;i<searchkeys.size();i++){
					if(buf.length()>0){
						buf.append(",");
					}
					buf.append("?");
				}
				sql.append(" ").append(where.replace("??",buf.toString()));
				if(vkey.length()>0){
					vkey.append(";").append(inkeys);
				}else{
					vkey.append(inkeys);
				}
			}else{
				if(autokeys!=null && !autokeys.equals("")){
					sql.append(" ").append(where);
					if(vkey.length()>0){
						vkey.append(";").append(autokeys);
					}else{
						vkey.append(autokeys);
					}
				}
			}
		}
	}
}
