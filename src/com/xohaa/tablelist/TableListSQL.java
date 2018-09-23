package com.xohaa.tablelist;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.xohaa.SQLDB.HandleSQLCon;
import com.xohaa.tablelist.TableListBase;

public class TableListSQL extends TableListBase {
	private String [][] fields = null;
	private HandleSQLCon FF = null;
	private ResultSet rs = null;
	protected String sqlkey = null;

	protected Vector<String> searchkeys = null;
	protected StringBuffer sqlcode = null;
	protected StringBuffer rscountcode = null;
	private boolean isZC = false;
	private boolean isfor = false;
	public TableListSQL(HandleSQLCon F,Database cdb,Document cdoc) throws Exception{
		super(F,cdb,cdoc);
		this.FF = F;
		checkSQLConfig();
		sqlkey = cdoc.getItemValueString("sql_key");
	}

	public Vector<String> getSearchkeys() {
		return searchkeys;
	}

	public StringBuffer getSqlcode() {
		return sqlcode;
	}
	/**
	 * 自定义列表使用
	 * @param rs
	 * @return viewentry:[{id:"",ColsValue[a,b,c,d]}] 字符
	 * @throws SQLException
	 * @throws JSONException 
	 * @throws NotesException 
	 */
	protected JSONArray resultSetToJSONArray() throws Exception{
		// 获取列数 
		ResultSetMetaData metaData = rs.getMetaData(); 
		int columnCount = metaData.getColumnCount(); 

		JSONArray array = new JSONArray();

		int has = 1;
		if(!"".equals(configdoc.getItemValueString("hasCheckBox"))){
			has = Integer.valueOf(configdoc.getItemValueString("hasCheckBox"));
		}else
			while (rs.next()) {
				JSONObject jsonObj = new JSONObject(); 
				// 遍历每一列 
				if("".equals(sqlkey) || "-".equals(sqlkey)){
					jsonObj.put("id", "");
				}else{
					jsonObj.put("id",rs.getString(sqlkey));
				}

				if(isZC){
					jsonObj.put("duibizd", rs.getString("duibizd"));
					jsonObj.put("showcontext_zb", rs.getString("showcontext_zb"));
				}

				JSONArray ColsValue = new JSONArray();
				if(isfor){
					for (int i = 1; i <= columnCount; i++){
						if(fields[1][i-1].equals("公式")){
							ColsValue.put(handleFormula(rs,columnCount,metaData,fields[0][i-1]));
						}else{
							ColsValue.put(rs.getString(i));
						}
					}
				}else{
					for (int i = 1; i <= columnCount; i++){
						ColsValue.put(rs.getString(i));
					}
				}


				jsonObj.put("hasCheckbox",has);
				jsonObj.put("ColsValue", ColsValue);
				array.put(jsonObj);
			}
		return array; 
	}

	/**
	 * 
	 */
	public void toSetJSONObject() throws Exception{
		this.putPageInfo();
		FF.putJsonData("viewentry",this.resultSetToJSONArray());
	}


	/**
	 * 读取配置的字段
	 * @param ajaxitem
	 * @throws Exception
	 * @return StringBuffer
	 */
	protected StringBuffer getFieldConfig(String ajaxitem) throws Exception{
		StringBuffer buf = new StringBuffer(30);
		ViewEntryCollection vc = null;
		if(ajaxitem != null && "1".equals(ajaxitem)){
			vc = this.getFormFieldCollection();
		}else{
			vc = this.getListFieldCollection();
		}
		ViewEntry ve = null;
		Document fdoc = null;
		int count = 0,i = 0 ;
		count = vc.getCount();
		if (count > 0){
			ve = vc.getFirstEntry();
			fields = new String [2][count];
			boolean b = false;
			if(ajaxitem != null && "1".equals(ajaxitem)){ //form
				while(ve != null){
					fdoc = ve.getDocument();
					if(i>0){
						buf.append(",");
					}
					fields[0][i] = fdoc.getItemValueString("F_FieldName");
					fields[1][i] = "域名";
					buf.append(fdoc.getItemValueString("F_FieldName"));
					i++;
					if (!b){
						b = sqlkey.equals(fdoc.getItemValueString("F_FieldName"));
					}

					ve = vc.getNextEntry(ve);
					fdoc.recycle();
				}
			}else{ // list
				while(ve != null){
					fdoc = ve.getDocument();
					if(i>0){
						buf.append(",");
					}
					fields[0][i] = fdoc.getItemValueString("FieldValue");
					fields[1][i] = fdoc.getItemValueString("FieldValueOption");
					if(fields[1][i].equals("域名")){
						buf.append(fdoc.getItemValueString("FieldValue"));
					}else{
						isfor = true; //执行公式开关
						buf.append("'f_"+ i +"' as f_" + i);
					}
					i++;
					if (!b){
						b = sqlkey.equals(fdoc.getItemValueString("FieldValue"));
					}

					ve = vc.getNextEntry(ve);
					fdoc.recycle();
				}
			}

			//加载ID输出
			if(!"".equals(sqlkey) && !"-".equals(sqlkey)){
				if(!b){
					buf.append(",").append(sqlkey);
				}
			}

		}else{
			throw new Exception("没有找到相关的字段配置!");
		}

		if("1".equals(configdoc.getItemValueString("isZhuC"))){ //开启主从表显示
			isZC = true; 
			buf.append(",").append(configdoc.getItemValueString("DuiBiZD")).append(" as duibizd");
			buf.append(",").append(configdoc.getItemValueString("ZhuHTML")).append(" as showcontext_zb");
		}

		if(vc != null) vc.recycle();
		return buf;
	}

	/**
	 * 
	 * @throws Exception
	 */
	protected void checkSQLConfig() throws Exception{
		//检查相关配置项
		if(configdoc.getItemValueString("sql_tablename").equals("")){
			throw new Exception("配置中的表名或视图名不能为空值！");
		}

		if(configdoc.getItemValueString("sql_key").equals("")){
			throw new Exception("配置中的主键名不能为空值！");
		}

		if(configdoc.getItemValueString("sql_order").equals("")){
			throw new Exception("配置中的排序或主键不能为空值！");
		}
	}

	/**
	 * 
	 * @throws Exception
	 */
	public void handleSQLCode() throws Exception{
		this.handleSQLCode(FF.getURLQueryString("pn"),FF.getURLQueryString("pz"),FF.getURLQueryString("ajaxsubitem"));
	}

	@SuppressWarnings("unchecked")
	private StringBuffer getPower() throws NotesException{
		StringBuffer powerid = new StringBuffer(100);
		Vector<?> ves = F.getSession().getUserGroupNameList();
		Vector<String> power = configdoc.getItemValue("SelectPower"); //公司C，部门D，职位Z，岗位G，角色Role。

		boolean b = true;
		int i,j,length;
		String temp,k;
		for(i=0;i<ves.size();i++){
			temp = ves.get(i).toString();
			if(temp.equalsIgnoreCase("Sys_Admin")){
				if(b){
					powerid.append("'").append(temp).append("'");
					b = false;
				}else{
					powerid.append(",'").append(temp).append("'");
				}
			}else{
				for(j=0;j<power.size();j++){
					length = power.get(j).length();//编码长度。
					k = temp.substring(0, length).toUpperCase(); //截取编码前缀
					if(k.equalsIgnoreCase(power.get(j))){ //判断是否一样
						if(b){
							powerid.append("'").append(temp).append("'");
							b = false;
							break;
						}else{
							powerid.append(",'").append(temp).append("'");
							break;
						}
					}
				}
			}
		} 

		if(powerid.length()== 0){
			powerid.append("''");
		}
		return powerid;
	}
	/**
	 * 
	 * @return StringBuffer
	 * @throws NotesException
	 */

	protected StringBuffer SQLDode_Power() throws NotesException{
		StringBuffer power = new StringBuffer(50);
		if(configdoc.getItemValueString("isPower").equals("1")){
			StringBuffer powerid = this.getPower();
			String idname = configdoc.getItemValueString("powerID");
			power.append(" inner join (select DISTINCT ")
			.append(idname)
			.append(" from ")
			.append(configdoc.getItemValueString("powertable"))
			.append(" where ")
			.append(configdoc.getItemValueString("powerwhere"))
			.append(" and powerID in(")
			.append(powerid)
			.append(")) as p on ")
			.append(configdoc.getItemValueString("sql_tablename")).append(".")
			.append(idname).append("=p.").append(idname);
		}
		return power;
	}

	/**
	 * 
	 * @param pn
	 * @param pz
	 * @throws Exception
	 */
	public void handleSQLCode(String pn,String pz,String ajaxitem) throws Exception{
		//读取配置中的字段
		searchkeys = new Vector<String>();
		String table = configdoc.getItemValueString("sql_tablename");
		StringBuffer fields = this.getFieldConfig(ajaxitem);
		StringBuffer searchSQL = this.getSQLSearchCode(); //读取条件SQL语句

		this.pagesize(pn, pz);

		//拼组SQL语句
		this.rscountcode = new StringBuffer("SELECT count(*) FROM ").append(table);
		this.sqlcode = new StringBuffer(500);

		if("1".equals(configdoc.getItemValueString("isPower"))){ //使用权限表
			StringBuffer power = this.SQLDode_Power();
			if(this.getEndindex() == -1){//不使用分页
				sqlcode.append("SELECT ").append(fields).append(" FROM ").append(table)
				.append(power).append(searchSQL).append(" order by ")
				.append(configdoc.getItemValueString("sql_order"));
			}else{
				sqlcode.append("SELECT ").append(fields).append(" FROM (SELECT ").append(table).append(".*")
				.append(",ROW_NUMBER() OVER (ORDER BY ").append(table).append(".").append(configdoc.getItemValueString("sql_order"))
				.append(") AS RowNumber FROM ").append(table).append(power)
				.append(searchSQL)
				.append(") EmployeePage WHERE RowNumber > ")
				.append(this.getStratindex())
				.append(" AND RowNumber <=")
				.append(this.getEndindex())
				.append(" ORDER BY ").append(configdoc.getItemValueString("sql_order"));
			}

			this.rscountcode.append(power).append(searchSQL);
		}else{
			if(this.getEndindex() == -1){//不使用分页
				sqlcode.append("SELECT ").append(fields).append(" FROM ").append(table)
				.append(searchSQL)
				.append(" order by ")
				.append(configdoc.getItemValueString("sql_order"));
			}else{
				sqlcode.append("SELECT ").append(fields).append(" FROM (SELECT ").append(table).append(".*")
				.append(",ROW_NUMBER() OVER (ORDER BY ")
				.append(configdoc.getItemValueString("sql_order"))
				.append(") AS RowNumber FROM ")
				.append(table)
				.append(searchSQL)
				.append(") EmployeePage WHERE RowNumber > ")
				.append(this.getStratindex())
				.append(" AND RowNumber <=")
				.append(this.getEndindex())
				.append(" ORDER BY ").append(configdoc.getItemValueString("sql_order"));
			}
			this.rscountcode.append(searchSQL);
		}
	}

	/**
	 * 
	 */
	public void recycleSQL(){
		try{
			if(rs != null) rs.close();
			sqlkey = null;
			searchkeys = null;
			sqlcode = null;
			rscountcode = null;
			this.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @return ResultSet
	 * @throws SQLException
	 */
	public ResultSet executeQuery() throws SQLException{
		ResultSet rscount = null;
		if(searchkeys.size()>0){
			rs = FF.executeQuery(sqlcode.toString(),searchkeys);
			rscount = FF.executeQuery(rscountcode.toString(),searchkeys);
			if (rscount.next()) {
				this.setCount(rscount.getLong(1)); //结果集对象的当前行中指定列的值
			}
		}else{
			rs = FF.executeQuery(sqlcode.toString());
			rscount = FF.executeQuery(rscountcode.toString());
			if (rscount.next()) {
				this.setCount(rscount.getLong(1)); //结果集对象的当前行中指定列的值
			}
		}
		rscount.close();
		return rs;
	}
	/**
	 * 获取条件部分的SQL代码
	 * @return StringBuffer
	 * @throws Exception
	 */
	protected StringBuffer getSQLSearchCode() throws Exception{
		StringBuffer sql = new StringBuffer(200);
		StringBuffer where = new StringBuffer(200);
		//处理从表搜索
		if(configdoc.getItemValueString("TableListType").equals("2")){
			String keyid = FF.getURLQueryString("keyid"); //读取从表
			if(keyid == null || "".equals(keyid)){
				throw new Exception("从表ID为空！");
			}
			if(!configdoc.getItemValueString("key_fieldname").equals("")){
				where.append(configdoc.getItemValueString("key_fieldname")).append(" = ? ");
				searchkeys.add(keyid);
			}else{
				throw new Exception("key_fieldname为空值");
			}
		}

		// 处理默认搜索
		if (!configdoc.getItemValueString("sql_where").equals("")){
			String kv = FF.getURLQueryString("kv");
			if(kv != null && !kv.equals("")){
				if(where.length()>0) where.append(" and ");
				where.append(configdoc.getItemValueString("sql_where"));
				String kvs[] = kv.split(",");
				for(int i=0;i<kvs.length;i++){
					searchkeys.add(kvs[i]);
				}
			}
		}

		//处理创建人权限
		if (!configdoc.getItemValueString("sql_creator").equals("")){
			if(!FF.isSysAdmin()){
				if(where.length()>0) where.append(" and ");
				where.append(configdoc.getItemValueString("sql_creator"));
				searchkeys.add(FF.getEffectiveUserName());
			}
		}

		//处理自定义操作
		if("1".equals(configdoc.getItemValueString("IsSearch"))){ //开启搜索功能
			String senior = FF.getURLQueryString("senior"); //高级搜索标志
			if(senior != null && senior.equals("1")){			
				ViewEntryCollection vc = this.getSearchFieldCollection();
				ViewEntry en = vc.getFirstEntry();
				Document tdoc = null;
				while(en != null){
					tdoc = en.getDocument();
					String value = FF.getURLQueryString(tdoc.getItemValueString("F_FieldName"));
					if(value != null && !value.equals("")){
						if(where.length()>0) where.append(" and ");
						if(tdoc.getItemValueString("fieldSearchType").equals("1")){
							where.append(tdoc.getItemValueString("FieldName")).append("=?");
							searchkeys.add(value);
						}else{
							where.append(tdoc.getItemValueString("FieldName")).append(" like ?");
							searchkeys.add("%" + value+ "%");
						}
					}

					tdoc.recycle();
					en = vc.getNextEntry(en);
				}
				vc.recycle();
			}else{
				String searchkey = FF.getURLQueryString("SearchValue");
				if(searchkey != null && !searchkey.trim().equals("")){ //搜索

					if(!configdoc.getItemValueString("sql_Searchkeys").equals("")){ //模糊查询字段不为空
						if(where.length()>0) where.append(" and ");
						String f[] = configdoc.getItemValueString("sql_Searchkeys").split(",");
						where .append("(");

						for(int i=0;i<f.length;i++){
							if (i > 0) where.append(" or ");
							where.append(f[i] + " like ? ");
							searchkeys.add("%"+ searchkey + "%");
						}

						where .append(")");
					}
				}
			}
		}

		String dwhere = configdoc.getItemValueString("sql_where_d"); //固定条件
		if(where.length()>0){
			if(!dwhere.equals("")){
				sql.append(" where ").append(dwhere).append(" and ").append(where);
			}else{
				sql.append(" where ").append(where);
			}
		}else{
			if(!dwhere.equals("")){
				sql.append(" where ").append(dwhere);
			}
		}
		return sql;
	}

	@Override
	public void copyNewRow(String id) throws Exception {
		// TODO Auto-generated method stub

	}

}