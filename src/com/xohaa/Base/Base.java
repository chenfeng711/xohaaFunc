package com.xohaa.Base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.RichTextStyle;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.RichTextItem;
/**
 * 基类，处理当前会话、URL参数、Ajax输出类型等。
 * @author Created 2016-5-15 by cjx
 *
 */
public class Base {
	protected Session session = null;
	protected Database db = null;
	protected Document doc = null;
	private String[][] QueryKey = null;
	private JSONObject JsonData = null;
	private String DBPATH = null;
	private String curAgentInfo = null;
	private String printStackTrace = "";
	private Date startdate = null;
	private StringBuffer poststr = null;
	private static final String version = "v1.4.20:20180809";

	/**
	 * 
	 * @return String
	 */
	public static String getVersion() {
		return version;
	}

	private boolean isPost = false;

	/**
	 * 
	 * @return JSONObject
	 */
	public JSONObject getJsonData() {
		return JsonData;
	}

	/**
	 * 
	 * @param jsonData
	 */
	public void setJsonData(JSONObject jsonData) {
		JsonData = jsonData;
	}

	/**
	 * 
	 * @return String[][]
	 */
	public String[][] getQueryKey() {
		return QueryKey;
	}

	/**
	 * 
	 * @param queryKey
	 */
	public void setQueryKey(String[][] queryKey) {
		QueryKey = queryKey;
	}

	/**
	 * 是否是Post
	 * @return boolean
	 */
	public boolean isPost() {
		return isPost;
	}

	/**
	 * 
	 * @param doc
	 * @param e
	 */
	public void saveAgentErr(String errmsg){
		try {
			String url = "";
			if(doc != null){ //代理调用，doc 为null
				url = doc.getItemValueString("PATH_TRANSLATED");
			}
			
			if("".equals(url)){//过滤Aajax调用
				url = this.db.getFilePath()+ "/" + session.getAgentContext().getCurrentAgent().getName();
			}

			Database tdb = session.getDatabase(session.getServerName(),this.getCurPATH() + "/Sys_LoginLog.nsf");
			if(tdb == null){
				System.out.println("无法打开【Sys_LoginLog.nsf】数据库！");
				return;
			}
			View view = tdb.getView("vw_fmAgentErr");
			if(view == null){
				if(tdb != null) tdb.recycle();
				return;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Document tdoc = view.getDocumentByKey(url, true);
			if(tdoc == null){
				tdoc = tdb.createDocument();
				if(doc != null){
					doc.copyAllItems(tdoc, true);
				}
				tdoc.replaceItemValue("form", "fmAgentErr");
				tdoc.replaceItemValue("Sys_Reader", "*").setReaders(true);
				tdoc.replaceItemValue("Sys_Authors", "*").setAuthors(true);
				tdoc.replaceItemValue("ErrCount",Long.valueOf("1"));
				tdoc.replaceItemValue("ErrMsg",errmsg);
				tdoc.replaceItemValue("CurDBPath",db.getFilePath());
				tdoc.replaceItemValue("CreateTime",sdf.format(new Date()));
			}else{
				long c = tdoc.getItemValueInteger("ErrCount");
				tdoc.replaceItemValue("ErrCount",Long.valueOf(++c));
			}

			tdoc.save(true,false);

			view.recycle();
			view = tdb.getView("vw_fmPushMsg");
			Document cdoc = view.getFirstDocument();
			if(cdoc != null){
				if("1".equals(cdoc.getItemValueString("isUse"))){
					sendMail(tdoc,cdoc);
				}
			}

			if(cdoc != null) cdoc.recycle();
			if(tdoc != null) tdoc.recycle();
			if(view != null) view.recycle();
			if(tdb != null) tdb.recycle();

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	/**
	 * 
	 * @param tdoc
	 * @param cdoc
	 * @throws NotesException
	 */
	private void sendMail(Document tdoc,Document cdoc) throws NotesException{
		Database mdb = session.getDatabase("","mail.box");
		Document mdoc = mdb.createDocument();
		mdoc.replaceItemValue("form", "memo");
		mdoc.replaceItemValue("SendTo",cdoc.getItemValueString("SendTo"));
		mdoc.replaceItemValue("CopyTo",cdoc.getItemValueString("CopyTo"));
		mdoc.replaceItemValue("Subject",cdoc.getItemValueString("Subject") + " " + tdoc.getItemValueString("ErrMsg"));
		mdoc.replaceItemValue("Recipients",cdoc.getItemValueString("SendTo")).appendToTextList(cdoc.getItemValueString("CopyTo"));

		mdoc.replaceItemValue("PostedDate",session.createDateTime(new Date()));
		mdoc.replaceItemValue("From",cdoc.getItemValueString("From"));
		RichTextItem rtitem = mdoc.createRichTextItem("Body");
		RichTextStyle rts = session.createRichTextStyle();
		rts.setPassThruHTML(1);
		rtitem.appendStyle(rts);
		rtitem.appendText(tdoc.getItemValueString("PATH_TRANSLATED"));
		rtitem.appendText(printStackTrace);

		mdoc.save(true,true);
		if(mdoc != null) mdoc.recycle();
		if(mdb != null) mdb.recycle();
	}
	/**
	 * 构造函数，生成当前会话，数据库，文档对象。
	 * @param se
	 * @throws NotesException
	 */
	public Base(Session se) throws NotesException{
		this.session = se;
		this.db = session.getCurrentDatabase();
		this.doc = session.getAgentContext().getDocumentContext();
		this.JsonData = new JSONObject();
		this.putJsonDataSuccess();
	}

	/**
	 * 
	 */
	public void initStartTime(){
		startdate = new Date();
	}
	/**
	 * 
	 */
	public void printRunTime(){
		Date date2 = new Date();
		System.out.println("--->程序运行时间【"+ (date2.getTime() - startdate.getTime()) + "毫秒】");
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putJsonData(String key,Object value){
		JsonData.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putJsonData(String key,String value){
		JsonData.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putJsonData(String key,int value){
		JsonData.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putJsonData(String key,double value){
		JsonData.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putJsonData(String key,boolean value){
		JsonData.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putJsonData(String key,JSONObject value){
		JsonData.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void putJsonData(String key,JSONArray value){
		JsonData.put(key, value);
	}
	/**
	 * 
	 * @param msg
	 */
	public void putJsonDataSuccess(String msg){
		JsonData.put("iserror", 0);
		JsonData.put("msg",msg);
	}
	/**
	 * 
	 */
	public void putJsonDataSuccess(){
		this.putJsonDataSuccess("操作成功");
	}

	/**
	 * 构造函数
	 */
	public Base(){

	}
	/**
	 * 获取当前会话对象
	 * @return Session
	 */
	public Session getSession(){
		return session;
	}

	/**
	 * 设置当前会话对象
	 * @param se
	 */
	public void setSession(Session se){
		session = se;
	}

	/**
	 * 获取当前的_path目录
	 * @return String
	 */
	public String getCurPATH(){
		if(DBPATH == null){
			this.setPATH();
		}
		return DBPATH;
	}

	/**
	 * 设置当前目录
	 */
	private void setPATH(){
		String path = this.getURLQueryString("_path");
		if(path != null && !path.equals("")){
			DBPATH = path;
		}else{
			DBPATH = this.getDBPath(db);
		}
	}


	/**
	 * 获取指定数据库对象的目录名。
	 * @return String
	 * @throws NotesException 
	 */
	public String getDBPath(Database db){
		try {
			return db.getFilePath().substring(0, db.getFilePath().length() - (db.getFileName().length()+1));
		} catch (NotesException e) {
			e.printStackTrace();
			return "";
		}
	}
	/**
	 * 获取当前数据库对象的目录名。
	 * @return String
	 * @throws NotesException
	 */
	public String getCurDBPath() throws NotesException{
		return this.getDBPath(db);
	}

	/**
	 * 获取服务器名
	 * @return String
	 * @throws NotesException 
	 */
	public String getServerName() throws NotesException{
		return db.getServer();
	}
	
	/**
	 * 获取ReplicaID
	 * @return String
	 * @throws NotesException
	 */
	public String getReplicaID() throws NotesException{
		return db.getReplicaID();
	}
	/**
	 * 返回当前数据库名称。
	 * @return String
	 */
	public String getCurDBFileName(){
		try {
			return this.db.getFileName();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	/**
	 * 设置当前数据库目录。
	 * @param path
	 */
	public void setCurPATH(String path){
		DBPATH = path;
	}

	/**
	 * 获取当前的数据库
	 * @return Database
	 */
	public Database getCurDB(){
		return this.db;
	}

	/**
	 * 获取当前的文档
	 * @return Document
	 */
	public Document getCurDoc(){
		return this.doc;
	}

	/**
	 * 输出当前请求是 POST或GET，只用于ajax
	 * @return String
	 */
	public String getRequestMethod(){
		try {
			return doc.getItemValueString("REQUEST_METHOD").toUpperCase();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}

	/**
	 * 获取post数据
	 * @return String
	 * @throws NotesException 
	 */
	public String getRequestContent() throws NotesException{
		if(this.poststr == null){
			return doc.getItemValueString("Request_Content");
		}else{
			return this.poststr.toString();
		}
	}
	
	private int index = 0;
	private void getRequest_Content() throws Exception{
		String o = "REQUEST_CONTENT_00" + index;
		if(doc.hasItem(o)){
			poststr.append(doc.getItemValueString(o));
			index ++;
			getRequest_Content();
		}
	}
	
	/**
	 * 把post数据写入数组
	 */
	private void PostDataQueryValue(){
		try{
			if(doc == null){
				return;
			}
			String qy[] = null;
			int length = 0;
			String method = doc.getItemValueString("REQUEST_METHOD").toUpperCase();
			if(method.equals("POST")){
				isPost = true;
				String get = doc.getItemValueString("Query_String_Decoded");
				if(get.indexOf("isposthtml=1")>0){//开启HTML代码post写入。
					qy = doc.getItemValueString("Query_String_Decoded").split("&");
					length = qy.length-1;
					QueryKey = new String [2][length];
					for(int i=0;i<length;i++){
						String QS = qy[i+1];
						QueryKey[0][i] = QS.substring(0,QS.indexOf("=")); //域名
						QueryKey[1][i] = URLDecoder.decode(QS.substring(QS.indexOf("=")+1,QS.length()) ,"UTF-8");
					}
					
					if(!doc.hasItem("Request_Content")){
						this.poststr = new StringBuffer(1000);
						getRequest_Content();
					}
				}else{
					if(doc.hasItem("Request_Content")){
						qy = doc.getItemValueString("Request_Content").split("&");
						length = qy.length;
						if("".equals(qy[0])){//必须加判断，否则报空指针的错误。
							QueryKey = new String [2][length];
							QueryKey[0][0] = "";
							QueryKey[0][1] = "";
						}else{
							QueryKey = new String [2][length];
							for(int i=0;i<length;i++){
								String QS = qy[i];
								QueryKey[0][i] = QS.substring(0,QS.indexOf("=")); //域名
								QueryKey[1][i] = URLDecoder.decode(QS.substring(QS.indexOf("=")+1,QS.length()) ,"UTF-8");
							}
						}
					}else{
						this.poststr = new StringBuffer(1000);
						getRequest_Content();
						String html = URLDecoder.decode(this.poststr.toString(),"UTF-8");
						qy = html.split("&");
						length = qy.length;
						if("".equals(qy[0])){//必须加判断，否则报空指针的错误。
							QueryKey = new String [2][length];
							QueryKey[0][0] = "";
							QueryKey[0][1] = "";
						}else{
							QueryKey = new String [2][length];
							for(int i=0;i<length;i++){
								String QS = qy[i];
								QueryKey[0][i] = QS.substring(0,QS.indexOf("=")); //域名
								QueryKey[1][i] = QS.substring(QS.indexOf("=")+1,QS.length());
							}
						}
					}
				}
			}else if(method.equals("GET")){
				isPost = false;
				qy = doc.getItemValueString("Query_String_Decoded").split("&");
				length = qy.length-1;
				QueryKey = new String [2][length];
				for(int i=0;i<length;i++){
					String QS = qy[i+1];
					QueryKey[0][i] = QS.substring(0,QS.indexOf("=")); //域名
					QueryKey[1][i] = URLDecoder.decode(QS.substring(QS.indexOf("=")+1,QS.length()) ,"UTF-8");
				}
			}else{

				//throw new Exception("当前不是Ajax的POST或GET，无法获取参数！");
			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 打印输出参数
	 */
	public void SystemPrintQV(){
		if (QueryKey == null){
			this.PostDataQueryValue();
		}
		if(QueryKey == null) return ;
		int length = QueryKey[0].length;
		if( length> 0){
			for(int i=0;i<length;i++){
				System.out.println( QueryKey[0][i]+ " = " + QueryKey[1][i]);
			}
		}
	}
	/**
	 * 
	 * @param doc
	 * @throws NotesException
	 */
	public void setQueryKeyToDoc(Document doc) throws NotesException{
		if (QueryKey == null){
			this.PostDataQueryValue();
		}
		if(QueryKey == null) return ;
		int length = QueryKey[0].length;
		if( length> 0){
			for(int i=0;i<length;i++){
				doc.replaceItemValue(QueryKey[0][i],QueryKey[1][i]);
			}
		}
	}

	/**
	 * 根据key获取值
	 * @param keyd
	 * @return String
	 */
	public String getURLQueryString(String keyd){
		if (QueryKey == null){
			this.PostDataQueryValue();
		}
		if(QueryKey == null) return null;
		int length = QueryKey[0].length;
		if( length> 0){
			for(int i=0;i<length;i++){
				if(QueryKey[0][i].equals(keyd)){
					return QueryKey[1][i];
				}
			}
		}
		return null;
	}

	/**
	 * 判断必填，并且抛出异常
	 * @param keyd 参数名
	 * @param isneed true 判断必填
	 * @return String
	 * @throws Exception 
	 */
	public String getURLQueryString(String keyd,boolean isneed) throws Exception{
		return getURLQueryString(keyd,isneed,keyd);
	}


	/**
	 * 判断必填，并且抛出异常。
	 * @param keyd 参数名
	 * @param isneed true 判断必填
	 * @param tips 字段中文描述
	 * @return
	 * @throws Exception
	 */
	public String getURLQueryString(String keyd,boolean isneed,String tips) throws Exception{
		if (QueryKey == null){
			this.PostDataQueryValue();
		}
		if(QueryKey == null) return null;
		int length = QueryKey[0].length;
		if( length> 0){
			for(int i=0;i<length;i++){
				if(QueryKey[0][i].equals(keyd)){
					if(isneed){
						if("".equals(QueryKey[1][i])){
							throw new Exception("[" + tips + "]参数不能为空值！");
						}else{
							return QueryKey[1][i];
						}
					}else{
						return QueryKey[1][i];
					}

				}
			}
		}

		if(isneed){
			throw new Exception("[" + tips + "]参数不能为空值！");
		}else{
			return null;
		}
	}
	/**
	 * 把多个相同参数合并成数组。
	 * @param keyd
	 * @return Vector
	 */
	public Vector<String> getURLQueryVector(String keyd){
		if (QueryKey == null){
			this.PostDataQueryValue();
		}
		if(QueryKey == null) return null;

		Vector<String> ve = new Vector<String>();
		if(QueryKey[0].length > 0){
			for(int i=0;i<QueryKey[0].length;i++){
				if(keyd.equals(QueryKey[0][i])){
					ve.add(QueryKey[1][i]);
				}
			}
		}
		return ve;
	}

	/**
	 * 把多个相同参数合并成一个字符
	 * @param keyd
	 * @param hb
	 * @return String
	 */
	public String getURLQueryString(String keyd,String hb){
		if (QueryKey == null){
			this.PostDataQueryValue();
		}
		if(QueryKey == null) return null;
		StringBuffer buf = new StringBuffer();
		if(QueryKey[0].length > 0){
			boolean b = false;
			for(int i=0;i<QueryKey[0].length;i++){
				if(keyd.equals(QueryKey[0][i])){
					if(b){
						buf.append(hb);
					}else{
						b = true;
					}
					buf.append(QueryKey[1][i]);
				}
			}
		}
		return buf.toString();
	}

	/**
	 * 输出：application/json;charset=utf-8。
	 * @param getAgentOutput
	 * @param str
	 */
	public void printJsonData(PrintWriter getAgentOutput,String str){
		getAgentOutput.println("Content-type:application/json;charset=utf-8");
		getAgentOutput.println(str);
		getAgentOutput.close();
		//saveAPIData(str,"json");
	}

	/**
	 * 输出：application/json;charset=utf-8。
	 * @param getAgentOutput
	 */
	public void printJsonData(PrintWriter getAgentOutput){
		this.printJsonData(getAgentOutput,this.JsonData.toString());
	}

	/**
	 * 
	 * @param getAgentOutput
	 * @param json
	 */
	public void printJsonData(PrintWriter getAgentOutput,JSONObject json){
		this.printJsonData(getAgentOutput,json.toString());
	}


	/**
	 * 输出：text/html;charset=utf-8。
	 * @param getAgentOutput
	 * @param str
	 */
	public void printHTMLData(PrintWriter getAgentOutput,String str){
		getAgentOutput.println("Content-type:text/html;charset=utf-8");
		getAgentOutput.println(str);
		getAgentOutput.close();
		//saveAPIData(str,"html");
	}

	/**
	 * 输出：text/xml;charset=utf-8。
	 * @param getAgentOutput
	 * @param str
	 */
	public void printXMLData(PrintWriter getAgentOutput,String str){
		getAgentOutput.println("Content-type:text/XML;charset=utf-8");
		getAgentOutput.println(str);
		getAgentOutput.close();
		//saveAPIData(str,"xml");
	}

	/**
	 * 输出：text/plain;charset=utf-8。
	 * @param getAgentOutput
	 * @param str
	 */
	public void printTextData(PrintWriter getAgentOutput,String str){
		getAgentOutput.println("Content-type:text/plain;charset=utf-8");
		getAgentOutput.println(str);
		getAgentOutput.close();
		//saveAPIData(str,"text");
	}


	/**
	 * 输出成功的JSON数据。
	 * @param getAgentOutput
	 */
	public void printSuccessData(PrintWriter getAgentOutput){
		this.putJsonDataSuccess();
		this.printJsonData(getAgentOutput,JsonData);
	}

	/**
	 * 输出错误信息的JSON数据
	 * @param getAgentOutput
	 * @param msgerr
	 */
	public void printWriteErr(PrintWriter getAgentOutput,String msgerr){
		try {
			if(doc != null){
				System.out.println("出错地址：[" + doc.getItemValueString("PATH_INFO") + "]");
			}
			JsonData.put("iserror", 1);
			JsonData.put("msg",URLEncoder.encode(msgerr,"utf-8").replace("+", " "));
			this.saveAgentErr(msgerr);
			this.printJsonData(getAgentOutput,JsonData);
		} catch (Exception e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}

	/**
	 * 输出错误。
	 * @param getAgentOutput
	 * @param e
	 */
	public void printWriteErr(PrintWriter getAgentOutput,Exception e){
		try {
			String msgerr = e.getMessage();
			if(msgerr == null || "".equals(msgerr)){
				msgerr = e.toString();
			}

			StringWriter sw = new StringWriter();  
			PrintWriter pw = new PrintWriter(sw); 
			e.printStackTrace(pw);
			printStackTrace = sw.toString();

			pw.flush();
			pw.close();
			this.printWriteErr(getAgentOutput,msgerr);
		} catch (Exception e1) {
			// TODO 自动生成 catch 块
			this.printWriteErr(getAgentOutput, e.toString());
			e1.printStackTrace();
		}
	}

	/**
	 * 返回当前访问域名。字符包括http://或https://
	 * @param isgetHttpString true 输出http://字符
	 * @return String
	 */
	public String getHostName(boolean isgetHttpString){
		try{
			String host = doc.getItemValueString("HTTP_HOST");
			if(host.equals("")) return "";
			if(isgetHttpString){
				if(doc.getItemValueString("HTTPS").equals("OFF")){
					return "http://" + host;
				}else{
					return "https://" + host;
				}
			}else{
				return host;
			}
		}catch(NotesException e){
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 获取用户信息
	 * @return String
	 */
	public String getEffectiveUserName(){
		try{
			return session.getEffectiveUserName();
		}catch(NotesException e){
			return "";
		}
	}


	/**
	 * 输入出控制台
	 * @param str
	 */
	public void printStr(String str){
		if (curAgentInfo == null){
			String dbname ;
			String agentname;
			try {
				agentname = session.getAgentContext().getCurrentAgent().getName();
				dbname = this.getCurDB().getFilePath();
			} catch (NotesException e) {
				// TODO Auto-generated catch block
				dbname = "";
				agentname = "";
				e.printStackTrace();
			}
			curAgentInfo = "[" + dbname + "/" + agentname + "]:";
		}
		System.out.println(curAgentInfo + str);

	}
	/**
	 * 释放当前使用的Domino类。
	 * @throws NotesException
	 */
	public void recycleBase() throws NotesException{
		if(doc != null) doc.recycle();
		if(db != null) db.recycle();
		if (session !=null) session.recycle();

		System.gc();
	}
}
