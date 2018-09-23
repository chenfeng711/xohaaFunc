package com.xohaa;

import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Action {
	/**
	 * 判断Vector是否包含
	 * @param ar
	 * @param value
	 * @return true 包含，false 不包含
	 */
	public static boolean isInVector(Vector <String> ar,String value) {
		for(int i=0;i<ar.size();i++){
			if(value.equals(ar.get(i).toString())){
				return true;
			}
		}
		return false;
	}

	/**
	 * 把vector转成字符串。
	 * @param ar
	 * @param must
	 * @return StringBuffer
	 */
	public static StringBuffer vectorSetToString(Vector<String> ar,String must) {
		boolean b = false;
		StringBuffer temp = new StringBuffer();
		for(int i=0;i<ar.size();i++){
			if(b){
				temp.append(must);
			}else{
				b=true;
			}
			temp.append(ar.get(i));
		}
		return temp;
	}

	/**
	 * 获取当前时间
	 * @return String ：yyyy-MM-dd HH:mm:ss
	 */
	public static String getCurDataTime(){
		return getCurDataTime("yyyy-MM-dd HH:mm:ss");
	}

	/**
	 * 把v2合并到v1
	 * @param v1
	 * @param v2
	 */

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void extendVector(Vector v1,Vector v2){
		for(int i=0;i<v2.size();i++){
			v1.add(v2.get(i));
		}
	}
	/**
	 * 获取当前时间
	 * @param format 自定义显示格式
	 * @return String
	 */
	public static String getCurDataTime(String format){
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date());
	}


	/**
	 * 排列组合
	 * @param item
	 * @param rows
	 * @param length
	 * @return String [][]
	 */
	public static String [][] PaiLieZH(String item[][],int rows,int length){
		String [][]grid = new String[rows][length];
		int r = 0;
		int pl = 0;
		int hj = 0;
		for(int c=length-1;c>=0;c--){ //grid纵向倒推
			r = 0;
			hj = rows / item[c].length; //每列显示的个
			if(pl > 0){
				hj = hj/pl;
			}
			for(int p=0;p<hj;p++){

				for(int i=0;i<item[c].length;i++){ //原数组横向

					if (pl>0){
						for(int t=0;t<pl;t++){
							grid[r++][c] = item[c][i];
						}
					}else{
						grid[r++][c] = item[c][i];
					}
				}
			}

			if(pl ==0){
				pl = item[c].length;
			}else{
				pl = pl * item[c].length;
			}
		}
		return grid;
	}

	/**
	 * 将resultSet转化为JSON数组
	 * @param rs
	 * @return JSONArray
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONArray resultSetToJsonArry(ResultSet rs) throws SQLException,JSONException{ 
		// json数组 
		JSONArray array = new JSONArray(); 
		// 获取列数 
		ResultSetMetaData metaData = rs.getMetaData(); 
		int columnCount = metaData.getColumnCount(); 
		// 遍历ResultSet中的每条数据 
		while (rs.next()) { 
			JSONObject jsonObj = new JSONObject(); 
			// 遍历每一列 
			for (int i = 1; i <= columnCount; i++) { 
				String columnName =metaData.getColumnLabel(i); 
				String value = rs.getString(columnName); 
				jsonObj.put(columnName, value); 
			} 
			array.put(jsonObj); 
		} 
		return array; 
	}

	/**
	 * 自定义列表使用
	 * @param rs
	 * @return viewentry:[{id:"",ColsValue[a,b,c,d]}] 字符
	 * @throws SQLException
	 * @throws JSONException 
	 */
	public static StringBuffer resultSetToStringForList(ResultSet rs,String keyid) throws SQLException, JSONException{
		// 获取列数 
		ResultSetMetaData metaData = rs.getMetaData(); 
		StringBuffer buf = new StringBuffer(200);
		int columnCount = metaData.getColumnCount(); 
		buf.append("\"viewentry\":");
		JSONArray array = new JSONArray();
		while (rs.next()) {
			JSONObject jsonObj = new JSONObject(); 
			// 遍历每一列 
			jsonObj.put("id", rs.getString(keyid));
			JSONArray ColsValue = new JSONArray();
			for (int i = 1; i <= columnCount; i++){ 
				ColsValue.put(rs.getObject(i));
			}
			jsonObj.put("ColsValue", ColsValue);
			array.put(jsonObj);
		}

		buf.append(array.toString());
		return buf; 
	}

	/**
	 * 将ResultSet转为字符的json
	 * @param rs
	 * @return data:[{"字段名":"字段值","...":"..."},{...}]] 
	 * @throws SQLException
	 * @throws JSONException 
	 */
	public static StringBuffer resultSetToJsonString(ResultSet rs) throws SQLException, JSONException{
		JSONArray array = new JSONArray();
		StringBuffer buf = new StringBuffer(200);
		ResultSetMetaData metaData = rs.getMetaData(); 
		int columnCount = metaData.getColumnCount(); 
		buf.append("\"data\":");
		while (rs.next()){
			array.put(resultSetToJsonObject(rs,metaData,columnCount));
		}
		buf.append(array.toString());
		return buf; 
	}
	/**
	 * 把记录集中某个字段返回
	 * @param rs
	 * @param fieldname
	 * @return Vector
	 * @throws SQLException
	 */
	public static Vector<String> resultFieldSetToVector(ResultSet rs,String fieldname) throws SQLException{
		Vector<String> v = new Vector <String>();
		if (rs !=null){
			// 遍历ResultSet中的每条数据 
			while (rs.next()) { 
				v.add(rs.getString(fieldname));
			}
		}
		return v; 
	}

	/**
	 * 将一行数据转化为JSONObject
	 * {"字段名":"字段值","...":"..."},{...}
	 * @param rs
	 * @return JSONObject
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONObject resultSetToJsonObject(ResultSet rs,ResultSetMetaData metaData,int columnCount) throws SQLException,JSONException{ 

		JSONObject jsonObj = new JSONObject(); // json对象
		// 遍历每一列 
		for (int i = 1; i <= columnCount; i++) { 
			String columnName =metaData.getColumnLabel(i); 
			jsonObj.put(columnName, rs.getObject(i));
		}
		
		return jsonObj; 
	}
	
	/**
	 * 将一行数据转化为JSONObject
	 * {"字段名":"字段值","...":"..."},{...}
	 * @param rs
	 * @return
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONObject resultSetToJsonObject(ResultSet rs) throws SQLException,JSONException{ 
		ResultSetMetaData metaData = rs.getMetaData(); 
		int columnCount = metaData.getColumnCount(); 
		JSONObject jsonObj = new JSONObject(); // json对象
		// 遍历每一列 
		for (int i = 1; i <= columnCount; i++) { 
			String columnName =metaData.getColumnLabel(i); 
			jsonObj.put(columnName, rs.getObject(i));
		}
		return jsonObj; 
	}
	/**
	 * 自定义列表使用
	 * @param rs
	 * @return viewentry:[{id:"",ColsValue[a,b,c,d]}] 字符
	 * @throws SQLException
	 * @throws JSONException 
	 */
	public static StringBuffer resultSetToStringForList(ResultSet rs,String keyid,boolean iszc) throws SQLException, JSONException{
		// 获取列数 
		ResultSetMetaData metaData = rs.getMetaData(); 
		StringBuffer buf = new StringBuffer(200);
		int columnCount = metaData.getColumnCount(); 
		buf.append("\"viewentry\":");
		JSONArray array = new JSONArray();
		while (rs.next()) {
			JSONObject jsonObj = new JSONObject(); 
			// 遍历每一列 
			jsonObj.put("id", rs.getString(keyid));
			if(iszc){
				jsonObj.put("duibizd", rs.getString("duibizd"));
				jsonObj.put("showcontext_zb", rs.getString("showcontext_zb"));
			}
			JSONArray ColsValue = new JSONArray();
			for (int i = 1; i <= columnCount; i++){ 
				ColsValue.put(rs.getString(i));
			}
			jsonObj.put("ColsValue", ColsValue);
			array.put(jsonObj);
		}

		buf.append(array.toString());
		return buf; 
	}

	/**
	 * 将resultSet转化为JSON数组
	 * @param rs
	 * @return JSONArray{Data:[{key:[]},{key2:[]}}
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONArray resultSetToJsonArryBySame(ResultSet rs,String keyname,String[] fieldsname) throws SQLException,JSONException{ 
		JSONArray array = new JSONArray();
		JSONArray arrayData = new JSONArray();

		JSONObject jsonkey = new JSONObject();
		String temp = "";
		boolean b = false;
		while (rs.next()) {
			b = true;
			if(temp.equals("") || temp.equals(rs.getString(keyname))){
				JSONObject json = new JSONObject();
				for (int i=0; i<fieldsname.length; i++) {
					json.put(fieldsname[i],rs.getObject(fieldsname[i])); 
				}
				array.put(json);

			}else{
				jsonkey.put(temp,array);
				arrayData.put(jsonkey);
				array = new JSONArray();
				jsonkey = new JSONObject();

				JSONObject json = new JSONObject();
				for (int i=0; i<fieldsname.length; i++) {
					json.put(fieldsname[i],rs.getObject(fieldsname[i])); 
				}
				array.put(json);
			}
			temp = rs.getString(keyname);
		}

		if(b){
			jsonkey.put(temp, array);
			arrayData.put(jsonkey);
		}

		return arrayData; 
	}

	/**
	 * 根据关键字生成json数据。
	 * @param rs
	 * @param searchkey
	 * @param fieldname
	 * @return JSONObject  {"key1":{"":"","":""},key2:{"":"","":""}}
	 * @throws SQLException
	 * @throws JSONException
	 */
	public static JSONObject resultSetToJsonObject_byCustomKey(ResultSet rs,String searchkey,String[]fieldname) throws SQLException,JSONException{ 
		JSONObject jsonObj = new JSONObject(); // json对象
		if(fieldname != null && fieldname.length>0){
			while (rs.next()){
				JSONObject sub = new JSONObject();
				for(int i=0;i<fieldname.length;i++){					
					sub.put(fieldname[i], rs.getObject(fieldname[i]));
				}
				jsonObj.put(rs.getString(searchkey), sub);
			}
			return jsonObj; 
		}else{
			return null;
		}
	}

	/**
	 * 处理多值数据
	 * @param value
	 * @param muilt 分割符
	 * @return Vector
	 */
	public static Vector<String> getMuiltValue(String value,String muilt){
		Vector<String> ve = new Vector<String>();
		String arr[] = value.split(muilt);
		for(int i=0;i<arr.length;i++){
			ve.add(arr[i].trim());
		}
		return ve;
	}

	/**
	 * 计算时间和现在的时间差
	 * @param dt
	 * @return String
	 */
	public static String timeDifferentDaytime(String dt){
		String timeDifferentDaytime = null;
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			Date now = new Date();
			Date date = df.parse(dt);
			long l= now.getTime() - date.getTime();
			long day=l/(24*60*60*1000);
			long hour=(l/(60*60*1000)-day*24);
			long min=((l/(60*1000))-day*24*60-hour*60);
			//long s=(l/1000-day*24*60*60-hour*60*60-min*60);
			timeDifferentDaytime = "" + day + "天" + hour + "小时" + min + "分";
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timeDifferentDaytime;
	}

	/**
	 * 
	 * @param ml
	 * @param iscreate true 创建，false 不创建
	 * @return String
	 * @throws Exception 
	 */
	public static String getCurMkdir(String ml,boolean iscreate) throws Exception{
		StringBuffer buf = new StringBuffer();
		File file = new File("");
		String p = File.separator;
		buf.append(file.getCanonicalPath());
		buf.append(p).append("data").append(p).append("domino").append(p).append("html").append(p).append(ml).append(p);
		if(iscreate){
			File mkdir = new File(buf.toString());
			if (!mkdir.exists()) {
				if (mkdir.mkdirs()) {
					System.out.println("创建目录成功");
				}
			}
		}
		return buf.toString();
	}
	
	
	
	/**
	 * 
	 * @param e
	 * @return String
	 */
	public static String printErr(Exception e){
		String msgerr;
		try {
			msgerr = e.getMessage();
		} catch (Exception e2) {
			msgerr = e.toString();
			// TODO 自动生成 catch 块
			e2.printStackTrace();
		}
		return msgerr;
	}
	
	
}
