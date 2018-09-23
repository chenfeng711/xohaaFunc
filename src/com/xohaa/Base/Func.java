package com.xohaa.Base;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.json.JSONObject;


import com.xohaa.Action;
import com.xohaa.Opt;

import lotus.domino.Agent;
import lotus.domino.Database;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.RichTextStyle;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.Item;
import lotus.domino.ViewEntryCollection;
/**
 * 操作类，继承Base类，处理与Domino相关方法的操作。
 * @author Created 2016-5-15 by cjx
 *
 */
public class Func extends Base{
	Database appdb = null;
	Document appdoc = null;
	View view = null;

	/**
	 * 构造函数，调用父类。
	 * @param se
	 * @throws NotesException
	 */
	public Func(Session se) throws NotesException{
		super(se);
	}

	/**
	 * 构造函数
	 */
	public Func(){
		super();
	}

	/**
	 * 返回当前操作文档对象。
	 * @return Document
	 */
	public Document getFuncAppDoc(){
		return this.appdoc;
	}

	/**
	 * 打开数据库
	 * @param dbname  数据名称
	 * @param isCurPATH false dbname 要传带目录的数据库名称
	 * @return Database
	 * @throws NotesException
	 */
	public Database OpenDB(String dbname,boolean isCurPATH) throws NotesException{
		String path = null;
		if(isCurPATH){
			path = this.getCurPATH() + "/";
		}else{
			path = "";
		}
		return session.getDatabase(session.getServerName(),path + dbname,false);
	}

	/**
	 * 文档字段转JSON输出
	 * @param doc
	 * @return JSONObject
	 * @throws NotesException
	 */
	public static JSONObject documentSetToJsonObject(Document doc) throws NotesException{
		JSONObject json = new JSONObject();
		if(doc == null){
			return null;
		}else{
			//Vector vec = doc.getItems();
			Vector<?> items = doc.getItems();
			for (int j=0; j<items.size(); j++) {
				Item item = (Item)items.elementAt(j);
				String name = item.getName();
				Vector<?> ect = item.getValues();
				if( ect != null && name.indexOf("$")<0){
					if(ect.size() == 1){
						json.put(name, ect.get(0));
					}else{
						json.put(name, ect);
					}
				}
			}
		}
		return json;
	}
	/**
	 * 把doc的item转成JSON输出【禁用】
	 * @param doc
	 * @return JSONObject
	 * @throws NotesException
	 */
	public static JSONObject setDocumentToJsonData(Document doc) throws NotesException{
		Vector<?> items = doc.getItems();
		JSONObject datarr = new JSONObject();
		for (int j=0; j<items.size(); j++) {
			Item item = (Item)items.elementAt(j);
			datarr.put(item.getName(), item.getText());
			item.recycle();
		}
		datarr.put("unid", doc.getUniversalID());
		return datarr;
	}
	/**
	 * 打开当前db目录下的数据库
	 * @param dbname
	 * @return Database
	 * @throws NotesException
	 */
	public Database OpenDB(String dbname) throws NotesException{
		return this.OpenDB(dbname,true);
	}
	/**
	 * 打开指定数据库的视图
	 * @param appdb
	 * @param viewname
	 * @return View
	 * @throws NotesException
	 */
	public static View openView(Database appdb,String viewname) throws NotesException{
		if(appdb == null){
			System.out.println("数据库无法打开，请检查！");
			return null;
		}
		View view = appdb.getView(viewname);
		view.setAutoUpdate(false);
		return view;
	}
	/**
	 * 打开当前数据库的视图
	 * @param viewname
	 * @return View
	 * @throws NotesException 
	 */
	public View openView(String viewname) throws NotesException{
		return Func.openView(db,viewname);
	}

	/**
	 * 打开指定数据库文件名的视图
	 * @param dbname
	 * @param viewname
	 * @return View
	 * @throws Exception
	 */
	public View openView(String dbname,String viewname) throws Exception{
		appdb = this.OpenDB(dbname);
		if(appdb == null){
			throw new Exception("无法打开数据库["+ dbname +"]");
		}
		return Func.openView(appdb,viewname);
	}

	/**
	 * 释放Domino对象，重载父类方法。
	 * @throws NotesException
	 */
	public void recycleFunc(){
		try{
			if(view != null) view.recycle();
			if(appdb != null) appdb.recycle();
			super.recycleBase();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param appdb
	 * @param viewname
	 * @param searchkey
	 * @param exact
	 * @return DocumentCollection
	 * @throws NotesException 
	 */
	public  DocumentCollection getAllDocumentsBykey(Database appdb,String viewname,String searchkey,boolean exact) throws NotesException{
		view = Func.openView(appdb, viewname);
		return view.getAllDocumentsByKey(searchkey,exact);
	}
	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param appdb
	 * @param viewname
	 * @param searchkeys
	 * @param exact
	 * @return DocumentCollection
	 * @throws NotesException 
	 */
	public DocumentCollection getAllDocumentsBykey(Database appdb,String viewname,Vector<String> searchkeys,boolean exact) throws NotesException{
		view = Func.openView(appdb, viewname);
		return view.getAllDocumentsByKey(searchkeys,exact);
	}

	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param viewname
	 * @param searchkeys
	 * @return DocumentCollection
	 * @throws Exception
	 */
	public DocumentCollection getAllDocumentsBykey(String viewname,Vector<String> searchkeys,boolean exact) throws Exception{
		return this.getAllDocumentsBykey(db, viewname, searchkeys,true);
	}

	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param viewname
	 * @param searchkey
	 * @return DocumentCollection
	 * @throws Exception
	 */
	public DocumentCollection getAllDocumentsBykey(String viewname,String searchkey,boolean exact) throws Exception{
		return this.getAllDocumentsBykey(db, viewname, searchkey, true);
	}

	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param dbname
	 * @param viewname
	 * @param searchkeys
	 * @param exact
	 * @return DocumentCollection
	 * @throws Exception
	 */
	public DocumentCollection getAllDocumentsBykey(String dbname,String viewname,Vector<String> searchkeys,boolean exact) throws Exception{
		openAppDB(dbname);
		return this.getAllDocumentsBykey(appdb, viewname, searchkeys,true);
	}

	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param dbname
	 * @param viewname
	 * @param searchkey
	 * @param exact
	 * @return DocumentCollection
	 * @throws Exception
	 */
	public DocumentCollection getAllDocumentsBykey(String dbname,String viewname,String searchkey,boolean exact) throws Exception{
		openAppDB(dbname);
		return this.getAllDocumentsBykey(appdb, viewname, searchkey,true);
	}

	/**
	 * 根据key获取多条文档
	 * @param db
	 * @param viewname
	 * @param searchkeys
	 * @param exact
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(Database db,String viewname,Vector<String> searchkeys,boolean exact) throws Exception{
		view = Func.openView(db, viewname);
		return view.getAllEntriesByKey(searchkeys,exact);
	}
	/**
	 * 根据key获取多条行
	 * @param db
	 * @param viewname
	 * @param searchkey
	 * @param exact
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(Database db,String viewname,String searchkey,boolean exact) throws Exception{
		view = Func.openView(db, viewname);
		return view.getAllEntriesByKey(searchkey,exact);
	}
	/**	 
	 * 根据key获取多条文档,返回文档集合
	 * @param dbname
	 * @param viewname
	 * @param searchkeys
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(String dbname,String viewname,Vector<String> searchkeys,boolean exact) throws Exception{
		openAppDB(dbname);
		return this.getAllEntriesBykey(appdb, viewname, searchkeys, true);
	}
	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param dbname
	 * @param viewname
	 * @param searchkey
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(String dbname,String viewname,String searchkey,boolean exact) throws Exception{
		openAppDB(dbname);
		return this.getAllEntriesBykey(appdb, viewname, searchkey, true);
	}
	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param viewname
	 * @param searchkey
	 * @param exact
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(String viewname,String searchkey,boolean exact) throws Exception{
		view = Func.openView(db, viewname);
		return view.getAllEntriesByKey(searchkey,exact);
	}
	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param viewname
	 * @param searchkeys
	 * @param exact
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(String viewname,Vector<String> searchkeys,boolean exact) throws Exception{
		view = Func.openView(db, viewname);
		return view.getAllEntriesByKey(searchkeys,exact);
	}

	/**
	 * 
	 * @param viewname
	 * @return
	 * @throws NotesException
	 */
	public ViewEntryCollection getAllEntries(String viewname) throws NotesException{
		view = Func.openView(db, viewname);
		return view.getAllEntries();
	}
	
	/**
	 * 
	 * @param appdb
	 * @param viewname
	 * @return
	 * @throws NotesException
	 */
	public ViewEntryCollection getAllEntries(Database appdb,String viewname) throws NotesException{
		view = Func.openView(appdb, viewname);
		return view.getAllEntries();
	}
	
	/**
	 * 
	 * @param dbname
	 * @param viewname
	 * @return
	 * @throws NotesException
	 */
	public ViewEntryCollection getAllEntries(String dbname,String viewname) throws NotesException{
		openAppDB(dbname);
		view = Func.openView(appdb, viewname);
		return view.getAllEntries();
	}	
	
	/**
	 * 根据key获取指定数据库下的单条文档
	 * @param appdb
	 * @param viewname
	 * @param searchkeys
	 * @param exact
	 * @return Document
	 * @throws NotesException 
	 */
	public  Document getDocumentBykey(Database appdb,String viewname,Vector<String> searchkeys,boolean exact) throws NotesException{
		view = Func.openView(appdb, viewname);
		return view.getDocumentByKey(searchkeys, exact);
	}
	/**
	 * 根据key获取指定数据库下的单条文档
	 * @param appdb
	 * @param viewname
	 * @param searchkey
	 * @param exact
	 * @return Document
	 * @throws NotesException 
	 */
	public Document getDocumentBykey(Database appdb,String viewname,String searchkey,boolean exact) throws NotesException{
		if(appdb == null){
			throw new RuntimeException("传入的数据库为空值！");
		}
		view = Func.openView(appdb, viewname);
		
		if(view == null){
			throw new RuntimeException(appdb.getFileName() +"的视图不存在："+ viewname);
		}
		return view.getDocumentByKey(searchkey, exact);
	}
	/**
	 * 根据key获取当前数据库的单条文档
	 * @param viewname
	 * @param searchkey
	 * @param exact
	 * @return Document
	 * @throws NotesException
	 */
	public Document getDocumentBykey(String viewname,String searchkey,boolean exact) throws NotesException{
		return this.getDocumentBykey(db, viewname, searchkey, exact);
	}

	/**
	 * 
	 * @param viewname
	 * @param searchkeys
	 * @param exact
	 * @return Document
	 * @throws NotesException
	 */
	public Document getDocumentBykey(String viewname,Vector<String> searchkeys,boolean exact) throws NotesException{
		return this.getDocumentBykey(db, viewname, searchkeys, exact);
	}
	
	/**
	 * 
	 * @param view
	 * @param searchkeys
	 * @param exact
	 * @return
	 * @throws NotesException
	 */
	public Document getDocumentBykey(View view,String searchkeys, boolean exact) throws NotesException {
		return view.getDocumentByKey(searchkeys, exact);
	}
	/**
	 * 根据key获取单条文档
	 * @param dbname
	 * @param viewname
	 * @param searchkey
	 * @return Document
	 * @throws NotesException 
	 */
	public Document getDocumentBykey(String dbname,String viewname,String searchkey,boolean exact) throws NotesException{
		openAppDB(dbname);
		if(appdb == null){
			System.out.println("getDocumentBykey 函数的数据库对象为空值！");
			return null;
		}
		return this.getDocumentBykey(appdb, viewname, searchkey, true);
	}
	/**
	 * 根据key获取单条文档
	 * @param dbname
	 * @param viewname
	 * @param searchkeys
	 * @return Document
	 * @throws Exception
	 */
	public Document getDocumentBykey(String dbname,String viewname,Vector<String> searchkeys,boolean exact) throws Exception{
		openAppDB(dbname);
		if(appdb == null){
			System.out.println("getDocumentBykey 函数的数据库对象为空值！");
			return null;
		}
		return this.getDocumentBykey(appdb, viewname, searchkeys, true);
	}

	/**
	 * 根据UNID从当前数据库中获取文档。
	 * @param dbname
	 * @param unid
	 * @return Document
	 * @throws NotesException
	 */
	public Document getDocumentByUNID(String dbname,String unid) throws NotesException{
		openAppDB(dbname);
		if(appdb == null){
			System.out.println("getDocumentByUNID 函数的数据库对象为空值！");
			return null;
		}
		return appdb.getDocumentByUNID(unid);
	}

	/**
	 * 
	 * @param dbname
	 * @param unid
	 * @return
	 * @throws NotesException
	 */
	public Document getDocumentByID(String dbname,String notesid) throws NotesException{
		openAppDB(dbname);
		if(appdb == null){
			System.out.println("getDocumentByUNID 函数的数据库对象为空值！");
			return null;
		}
		return appdb.getDocumentByID(notesid);
	}
	/**
	 * @throws NotesException 
	 * 
	 */
	public Document getDocumentByAgentID() throws NotesException{
		Agent agent = this.session.getAgentContext().getCurrentAgent();
		return db.getDocumentByID(agent.getParameterDocID());
	}
	/**
	 * 
	 * @param unid
	 * @return
	 * @throws NotesException
	 */
	public Document getDocumentByID(String unid) throws NotesException{
		return db.getDocumentByID(unid);
	}
	/**
	 * 根据UNID从当前数据库中获取文档。
	 * @param unid
	 * @return Document
	 * @throws NotesException
	 */
	public Document getDocumentByUNID(String unid) throws NotesException{
		return db.getDocumentByUNID(unid);
	}
	/**
	 * 获取字段值
	 * @param dbname
	 * @param viewname
	 * @param searchkey
	 * @return String
	 * @throws NotesException 
	 */
	public String getItemValueString(String dbname,String viewname,String searchkey,String fieldname) throws NotesException{
		appdoc = this.getDocumentBykey(dbname, viewname, searchkey, true);
		if(appdoc == null){
			System.out.println("getItemValueString 的文档对象为空值！");
			return null;
		}
		return appdoc.getItemValueString(fieldname);
	}
	/**
	 * 获取字段值
	 * @param db
	 * @param viewname
	 * @param searchkey
	 * @param fieldname
	 * @return String
	 * @throws NotesException
	 */
	public String getItemValueString(Database db,String viewname,String searchkey,String fieldname) throws NotesException{
		appdoc = this.getDocumentBykey(db, viewname, searchkey, true);
		return appdoc.getItemValueString(fieldname);
	}

	/**
	 * 获取字段值
	 * @param viewname
	 * @param searchkey
	 * @param fieldname
	 * @return String
	 * @throws NotesException
	 */
	public String getItemValueString(String viewname,String searchkey,String fieldname) throws NotesException{
		appdoc = this.getDocumentBykey(viewname, searchkey, true);
		return appdoc.getItemValueString(fieldname);
	}

	/**
	 * 
	 * @param view
	 * @param searchkey
	 * @param fieldname
	 * @return String
	 * @throws NotesException
	 */
	public String getItemValueString(View view,String searchkey,String fieldname) throws NotesException{
		appdoc = view.getDocumentByKey(searchkey,true);
		if(appdoc == null){
			System.out.println("在"+ view.getName() + "中找不到：" + searchkey );
			return null;
		}
		return appdoc.getItemValueString(fieldname);
	}

	/**
	 * 
	 * @param view
	 * @param searchkeys
	 * @param fieldname
	 * @return String
	 * @throws NotesException
	 */
	public String getItemValueString(View view,Vector<String> searchkeys,String fieldname) throws NotesException{
		appdoc = view.getDocumentByKey(searchkeys,true);
		return appdoc.getItemValueString(fieldname);
	}
	/**
	 * 
	 * @param fieldname
	 * @return String
	 * @throws NotesException
	 */
	public String getItemValueString(String fieldname) throws NotesException{
		return appdoc.getItemValueString(fieldname);
	}	
	/**
	 * 获取字段值的数组
	 * @param dbname
	 * @param viewname
	 * @param searchkey
	 * @param fieldname
	 * @return Vector
	 * @throws NotesException
	 */

	public Vector<?> getItemValue(String dbname,String viewname,String searchkey,String fieldname) throws NotesException{
		appdoc = this.getDocumentBykey(dbname, viewname, searchkey, true);
		return appdoc.getItemValue(fieldname);
	}

	/**
	 * 获取字段值的数组
	 * @param db
	 * @param viewname
	 * @param searchkey
	 * @param fieldname
	 * @return Vector
	 * @throws NotesException
	 */
	public Vector<?> getItemValue(Database db,String viewname,String searchkey,String fieldname) throws NotesException{
		appdoc = this.getDocumentBykey(db, viewname, searchkey, true);
		return appdoc.getItemValue(fieldname);
	}
	/**
	 * 提取代码出错的信息
	 * @param e
	 * @return String
	 */
	public String getErrorStack(Exception e) {
		String strtemp = "";
		try {
			if (e != null) {
				StackTraceElement[] ste = e.getStackTrace();

				for (int i = 0, j = ste.length; i < j; i++) {
					strtemp += " at " + ste[i].getClassName() + "."
							+ ste[i].getMethodName() + "("
							+ ste[i].getFileName() + ":"
							+ ste[i].getLineNumber() + ")<br>";
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return strtemp;
	}

	/**
	 * 获取人员群组列表
	 * @return StringBuffer
	 * @throws NotesException
	 */
	public StringBuffer getUserGroupNameList() throws NotesException{
		Vector<?> S = this.getSession().getUserGroupNameList();
		StringBuffer powerid = new StringBuffer();
		boolean b = true;
		for(int i=0;i<S.size();i++){
			if(S.get(i).toString().indexOf("*")<0){
				powerid.append(b? "'" + S.get(i) + "'":",'" + S.get(i) + "'");
				b = false;
			}
		}
		return powerid;
	}
	/**
	 * 提取会话中的岗位ID。目前只能判断[包括兼岗]
	 * @return Vector
	 * @throws NotesException
	 */
	public Vector<String> getGWIDByUserGroupNameList() throws NotesException{
		Vector<?> S = this.session.getUserGroupNameList();
		Vector<String> vec = new Vector<String>();
		for(int i=0;i<S.size();i++){
			String temp = S.get(i).toString();
			if(temp.indexOf("_")>1){
				vec.add(temp);
			}
		}
		return vec;
	}
	/**
	 * 提取会话中的岗位ID。目前只能判断[包括兼岗]
	 * @return StringBuffer 'G00001_1','G0000023_1'
	 * @throws NotesException
	 */
	public StringBuffer getGWIDByUserGroupNameList_IN() throws NotesException{
		Vector<?> S = this.session.getUserGroupNameList();
		StringBuffer powerid = new StringBuffer();
		boolean b = true;
		for(int i=0;i<S.size();i++){
			String temp = S.get(i).toString();
			if(temp.indexOf("_")>1){
				powerid.append(b? "'" + S.get(i) + "'":",'" + S.get(i) + "'");
				b = false;
			}
		}
		return powerid;
	}
	/**
	 * 在指定文档对象中执行公式
	 * @param doc
	 * @param formula
	 * @return Vector
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	public Vector<String> Evaluate(Document doc,String formula) throws NotesException{
		return this.session.evaluate(formula,doc);
	}

	/**
	 * 在当前文档对象中执行公式
	 * @param formula
	 * @return Vector
	 * @throws NotesException
	 */
	@SuppressWarnings("unchecked")
	public Vector<String> Evaluate(String formula) throws NotesException{
		return this.session.evaluate(formula,this.getCurDoc());
	}

	/**
	 * 
	 * @return Database
	 * @throws NotesException
	 */
	public Database getPlatFormConfig() throws NotesException{
		return this.OpenDB(Opt.PlatConfig_DBName, false);
	}

	/**
	 * 获取编号
	 * @param searchkey C,D,Z,G 代表组织
	 * @param l
	 * @return String
	 * @throws NotesException 
	 */
	public String getNumber(String searchkey,int l) throws NotesException{
		appdb = this.getPlatFormConfig();
		return this.getNumber(appdb,searchkey, l);
	}

	/**
	 * 获取组织编号
	 * @param db
	 * @param searchkey 
	 * @param l
	 * @return String
	 * @throws NotesException
	 */
	public String getNumber(Database db,String searchkey,int l) throws NotesException{
		appdoc = this.getDocumentBykey(db, "vw_getNumber", searchkey, true);
		long num = 1;
		DecimalFormat df = new DecimalFormat(getLengthNumber(l));
		if (appdoc != null){
			num = appdoc.getItemValueInteger("number");
			num += 1;
			appdoc.replaceItemValue("number",num);
			appdoc.save(true,false);
		}else{
			appdoc = db.createDocument();
			appdoc.replaceItemValue("number", 1);
			appdoc.replaceItemValue("searchkey", searchkey);
			appdoc.replaceItemValue("allEditors", "*").setAuthors(true);
			appdoc.save(true,false);
		}
		return df.format(num);
	}

	/**
	 * 
	 * @param l
	 * @return String
	 */
	private String getLengthNumber(int l){
		StringBuffer num = new StringBuffer();
		for (int x=0; x<l; x++){
			num.append("0");
		}
		return num.toString();
	}


	/**
	 * 获取多值
	 * @param key
	 * @return Vector
	 * @throws NotesException
	 */
	public Vector<?> getMultData(String key) throws NotesException{
		openAppDB(Opt.MultData_DBName);
		return getMultData(appdb,key);

	}
	/**
	 * 获取多值
	 * @param db
	 * @param key
	 * @return Vector
	 * @throws NotesException
	 */
	public Vector<?> getMultData(Database db,String key) throws NotesException{
		return this.getItemValue(db, "vw_fmMultdata", key, "DataList");

	}
	/**
	 * 写入公共字段
	 * @param doc
	 */
	public static void setPublicFieldForDoc(Document doc){
		try {
			doc.replaceItemValue("Sys_Manager", "Sys_Admin").setAuthors(true);
			doc.replaceItemValue("Sys_AutoUNID",doc.getUniversalID());
			doc.replaceItemValue("Sys_AutoNoteID",doc.getNoteID());
		} catch (NotesException e) {
			// TODO 自动生成 catch 块
			e.printStackTrace();
		}
	}
	/**
	 * 写入管理员权限
	 * @param doc
	 */
	public void setSysAdminPower(Document doc){
		try{
			doc.replaceItemValue("SYS_AUTHOR","Sys_Admin").setAuthors(true);
			doc.replaceItemValue("DATA_AUTHOR","Sys_DataManager" + "_" + this.getCurPATH()).setAuthors(true);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
	/**
	 * 检查当前是不是管理员
	 * @return
	 * @throws NotesException
	 */

	public boolean isSysAdmin() throws NotesException{
		boolean b = false;
		String da = "Sys_DataManager" + "_" + this.getCurPATH();
		Vector<?> vec = this.session.getUserGroupNameList();
		for(int i=0;i<vec.size();i++){
			if("Sys_Admin".equalsIgnoreCase(vec.get(i).toString())){
				b = true;
				break;
			}else if(da.equalsIgnoreCase(vec.get(i).toString())){
				b = true;
				break;
			}
		}

		return b;
	}
	/**
	 * 打开数据库，如果已经打开则判断是否重复。
	 * @param dbname
	 * @throws NotesException
	 */
	private void openAppDB(String dbname) throws NotesException{
		if(appdb == null){
			appdb = this.OpenDB(dbname);
		}else{
			if(!dbname.toLowerCase().equals(appdb.getFileName().toLowerCase())){
				appdb = this.OpenDB(dbname);
			}
		}
	}

	/**
	 * 读取数据字典，根据关键字返回数据字典
	 * @param configdb
	 * @param key
	 * @return String
	 * @throws NotesException
	 */
	public String getDirkey(Database configdb,String key)throws NotesException{
		HandleData h = new HandleData(configdb,"vw_sysdictionary_byKey");
		String reStr = h.getItemValueString(key, "ShuJuZ_WB");
		h.recycle();
		return reStr;
	}	
	/**
	 * 读取数据字典，根据关键字返回数据字典
	 * @param key
	 * @return String
	 * @throws NotesException
	 */
	public String getDirkey(String key)throws NotesException{
		openAppDB(Opt.Config_DBName);
		return this.getDirkey(appdb,key);
	}

	/**
	 * 根据岗位ID获取职位ID
	 * @param gwid
	 * @return String
	 * @throws Exception 
	 */
	public String getPosibyGWID(String gwid) throws Exception{
		appdoc = this.getDocumentBykey(Opt.GW_DBName, "vw_Post", gwid.toUpperCase(), true);
		return appdoc.getItemValueString("PositionNumber");
	}

	/**
	 * 根据职位ID获取上级的职位ID
	 * @param zwid
	 * @return String
	 * @throws Exception 
	 */
	public String getParentPosibyZWID(String zwid) throws Exception{
		appdoc = this.getDocumentBykey(Opt.ZW_DBName,"vw_Position", zwid.toUpperCase(), true);
		return appdoc.getItemValueString("ParentLeaders");
	}



	/**
	 * 需要写数据到主文档的函数，通过数组传值,阻止流程提交
	 * @param doc 当前文档
	 * @param reStr 错误内容，不为空时，阻止流程提交
	 * @param ar  要保存的字段[0][0] = fieldname  [0,1]=fieldvalue
	 */
	public void setFlowLog(Document doc,String reStr,String ar[][]){
		Database flowlog = null;
		Document tempdoc = null;
		Item item = null;
		try{
			flowlog = this.OpenDB(Opt.FlowLog_DBName);
			tempdoc = flowlog.createDocument();
			tempdoc.replaceItemValue("form","fmlog");
			tempdoc.replaceItemValue("C_flowID", doc.getItemValueString("C_FlowId"));
			tempdoc.replaceItemValue("C_stepID", doc.getItemValueString("C_stepID"));
			tempdoc.replaceItemValue("parentid",doc.getUniversalID());
			tempdoc.replaceItemValue("Author", "*");
			item = tempdoc.getFirstItem("Author");
			tempdoc.replaceItemValue("remessage", reStr);
			item.setReaders(true);
			item.setAuthors(true);
			tempdoc.replaceItemValue("item", "");
			item = tempdoc.getFirstItem("item");
			for(int i=0;i<ar.length;i++){
				item.appendToTextList(ar[i][0]+"~!~"+ar[i][1]);
			}
			tempdoc.save(true,false);
			if (tempdoc != null) tempdoc.recycle();
			if (item != null) item.recycle();
			if (flowlog != null) flowlog.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * 生成流程提交日志
	 * @param doc
	 * @param reStr 不为空时，阻止流程提交
	 * @throws Exception
	 */
	public void setFlowLog(Document doc,String reStr){
		Database flowlog = null;
		Document tempdoc = null;
		Item item = null;
		try{
			flowlog = this.OpenDB(Opt.FlowLog_DBName);
			tempdoc = flowlog.createDocument();
			tempdoc.replaceItemValue("form","fmlog");
			tempdoc.replaceItemValue("C_flowID", doc.getItemValueString("C_FlowId"));
			tempdoc.replaceItemValue("C_stepID", doc.getItemValueString("C_stepID"));
			tempdoc.replaceItemValue("parentid",doc.getUniversalID());
			tempdoc.replaceItemValue("Author", "*");
			item = tempdoc.getFirstItem("Author");
			tempdoc.replaceItemValue("remessage", reStr);
			item.setReaders(true);
			item.setAuthors(true);
			tempdoc.save(true,false);

			item.recycle();
			tempdoc.recycle();
			flowlog.recycle();

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 根据配置文档上的保存类型来保成文档。
	 * @param newdoc
	 * @param configdoc
	 * @throws NotesException
	 */
	public void setURLQueryStringForConfigDoc(Document newdoc,Document configdoc)throws NotesException{
		String fieldname = configdoc.getItemValueString("itemname");
		String datatype = configdoc.getItemValueString("datatype");
		String multi = configdoc.getItemValueString("isMulti");
		String Fieldtype = configdoc.getItemValueString("fieldtype");
		this.setURLQueryStringForItemByType(newdoc,fieldname,datatype,Fieldtype,multi);
	}
	/**
	 * 根据类型保存文档
	 * @param newdoc
	 * @param fieldname
	 * @param datatype
	 * @param Fieldtype
	 * @param multi
	 * @throws NotesException
	 */
	public void setURLQueryStringForItemByType(Document newdoc,String fieldname,String datatype,String Fieldtype,String multi) throws NotesException{
		if(Fieldtype.equals("checkbox")){//复选框
			Vector<String> vec = this.getURLQueryVector(fieldname);
			if (vec.size()>0){
				newdoc.replaceItemValue(fieldname,vec);
			}
		}else{ //其它的

			String value = getURLQueryString(fieldname);
			if (value != null){
				if (datatype.equals("text")|| datatype.equals("")){
					if(!multi.equals("")){ //多值
						newdoc.replaceItemValue(fieldname,Action.getMuiltValue(value,multi));
					}else{

						newdoc.replaceItemValue(fieldname,value);
					}

				}else if (datatype.equals("datatime")){
					DateTime timenow = session.createDateTime(value);
					newdoc.replaceItemValue(fieldname,timenow);
				}else if (datatype.equals("reader")){
					if(!multi.equals("")){ //多值
						newdoc.replaceItemValue(fieldname,Action.getMuiltValue(value,multi)).setReaders(true);
					}else{
						newdoc.replaceItemValue(fieldname,value).setReaders(true);
					}

				}else if (datatype.equals("author")){
					if(!multi.equals("")){ //多值
						newdoc.replaceItemValue(fieldname,Action.getMuiltValue(value,multi)).setAuthors(true);
					}else{
						newdoc.replaceItemValue(fieldname,getURLQueryString(fieldname)).setAuthors(true);
					}
				}else if (datatype.equals("name")){
					if(!multi.equals("")){ //多值
						newdoc.replaceItemValue(fieldname,Action.getMuiltValue(value,multi)).setNames(true);
					}else{
						newdoc.replaceItemValue(fieldname,value).setNames(true);
					}
				}else if (datatype.equals("integer")){

					newdoc.replaceItemValue(fieldname,new Integer(value));
				}else if (datatype.equals("double")){	
					newdoc.replaceItemValue(fieldname,new Double(value));
				}else if (datatype.equals("ueditor")){
					if(newdoc.hasItem(fieldname)){
						newdoc.removeItem(fieldname);
					}
					RichTextStyle rts = session.createRichTextStyle();
					rts.setPassThruHTML(1);
					RichTextItem rtitem = newdoc.createRichTextItem(fieldname);
					rtitem.appendStyle(rts);
					rtitem.appendText(value);
					
				}else{
					newdoc.replaceItemValue(fieldname,value);
				}
			}
		}
	}

	/**
	 * 删除视图数据
	 * @param view
	 * @param searchkey
	 * @return boolean
	 */
	public boolean delDataForViewSkey(View view,Vector<String> searchkey){
		boolean b = false;
		try {
			ViewEntryCollection vc = view.getAllEntriesByKey(searchkey, true);
			vc.removeAll(true);
			if (vc != null) vc.recycle();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}

	/**
	 * 删除视图数据
	 * @param view
	 * @param searchkey
	 * @return boolean
	 */
	public boolean delDataForViewSkey(View view,String searchkey){
		boolean b = false;
		try {
			ViewEntryCollection vc = view.getAllEntriesByKey(searchkey, true);
			vc.removeAll(true);
			if (vc != null) vc.recycle();
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}	


	/**
	 * 生成流程编号（当编号不为空时，才生成）
	 * @param appdb
	 * @param doc
	 * @throws NotesException
	 */
	public void setFlowRequestNumber(Database appdb,Document doc) throws NotesException{
		if(!doc.getItemValueString("RequestNumber").equals("")){
			return ;
		}
		String formatStr,FlowCode2;
		int RequestNumber = 0;
		Database flowdb = this.OpenDB(Opt.Flow_DBName);
		Document tempdoc = this.getDocumentBykey(flowdb, "vw_Flow_byFlowID", doc.getItemValueString("C_FlowID"), true);
		if(tempdoc != null){

			formatStr = tempdoc.getItemValueString("FlowCode1").equals("")?"yyMMdd":tempdoc.getItemValueString("FlowCode1").replaceAll("mm", "MM");
			SimpleDateFormat sFormat = new SimpleDateFormat(formatStr);
			formatStr = tempdoc.getItemValueString("FlowCode") + sFormat.format(new Date());
			FlowCode2 = tempdoc.getItemValueString("FlowCode2").equals("")?"000":tempdoc.getItemValueString("FlowCode2");
			tempdoc.recycle();

			tempdoc = this.getDocumentBykey(appdb, "vw_requestnumber", formatStr, true);
			if(tempdoc == null){
				tempdoc = appdb.createDocument();
				tempdoc.replaceItemValue("form", "f_requestnumber");
				tempdoc.replaceItemValue("RequestYear", formatStr);
				tempdoc.replaceItemValue("RequestNumber", new Integer(0));
				tempdoc.replaceItemValue("PermAR_Common","*").setReaders(true);
				tempdoc.replaceItemValue("PermAR_Common","*").setAuthors(true);
				Func.setPublicFieldForDoc(tempdoc);
			}else{
				RequestNumber = tempdoc.getItemValueInteger("RequestNumber");
			}

			RequestNumber += 1;
			tempdoc.replaceItemValue("RequestNumber", new Integer(RequestNumber));
			tempdoc.save(true,false);

			StringBuffer Str_RequestNumber = new StringBuffer();
			DecimalFormat df2 = new DecimalFormat(FlowCode2);
			Str_RequestNumber.append(formatStr).append(df2.format(RequestNumber));
			tempdoc.recycle();
			flowdb.recycle();

			//'检查现有需求中是否有此需求编号
			tempdoc = this.getDocumentBykey(appdb, "vw_checkrequestnumber",Str_RequestNumber.toString(), true);
			if (tempdoc != null){
				setFlowRequestNumber(appdb,doc);
			}else{
				doc.replaceItemValue("RequestNumber", Str_RequestNumber.toString());
			}
		}else{
			this.printStr("没有找到流程配置文档");
		}
	}

	/**
	 * 生成流程字段
	 * @param db
	 * @param doc
	 * @param innerformName 子表单名称
	 * @throws NotesException
	 */
	public void setFlowFields(Database db, Document doc,String innerformName) throws NotesException{
		doc.replaceItemValue("Form", "fmMain");
		doc.replaceItemValue("Sys_PATH", this.getCurPATH()); 
		doc.replaceItemValue("Sys_CURDB", db.getFileName());
		doc.replaceItemValue("Sys_SYSDB", Opt.Org_DBName);
		doc.replaceItemValue("SysPostDB", Opt.GW_DBName);
		doc.replaceItemValue("SysPositionDB", Opt.ZW_DBName);

		doc.replaceItemValue("DbSearchKey", "/");
		doc.replaceItemValue("innerformName", innerformName);
		doc.computeWithForm(true, true);

		doc.removeItem("Sys_PATH");
		doc.removeItem("Sys_CURDB");
		doc.removeItem("Sys_SYSDB");
		doc.removeItem("SysPostDB");
		doc.removeItem("SysPositionDB");
		doc.removeItem("DbSearchKey");

	}

	/**
	 * 
	 * @param db
	 * @param doc
	 * @throws NotesException 
	 */
	public void setFlowFields(Database db,Document doc) throws NotesException{
		this.setFlowFields(db, doc,"innerform");
	}

	/**
	 * 删除待办
	 * @param mainunid
	 * @return boolean
	 * @throws Exception
	 * 
	 */
	public boolean delToDo(String mainunid){
		boolean b = false;
		try{
			Document tddoc = this.getDocumentBykey("Sys_Todo.nsf", "vw_ToDo_ByParentUNID",mainunid, true);
			if(tddoc != null) {
				tddoc.remove(true);
			}
			b = true;
			if (tddoc != null) tddoc.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
		return b;
	}
	
	/**
	 * 保存到代理接口默认的输出输入参数到管理数据
	 */
	public void saveAgentAPIManage(){
		this.saveAgentAPIManage(this.getJsonData().toString(), "json");
	}
	/**
	 * 保存到代理接口的输出输入参数到管理数据
	 * @param outStr 输出参数
	 * @param otype 输出类型
	 */
	public void saveAgentAPIManage(String outStr,String otype){
		try {
			String url = doc.getItemValueString("PATH_TRANSLATED");
			if("".equals(url)){
				return;
			}

			if(!"".equals(doc.getItemValueString("REQUEST_METHOD"))){
				Database tdb = this.OpenDB("Basic_APIManage.nsf");
				if(tdb == null){
					System.out.println("无法打开【Basic_APIManage.nsf】数据库！");
					return;
				}
				View view = tdb.getView("vw_fmAPI_byURL");
				if(view == null){
					if(tdb != null) tdb.recycle();
					return;
				}

				Document tdoc = view.getDocumentByKey(url, true);
				Document tdoc2 = null;
				if(tdoc == null){
					tdoc = tdb.createDocument();
					tdoc.replaceItemValue("form", "fmAPI");
					tdoc.replaceItemValue("reader", "*").setReaders(true);
					tdoc.replaceItemValue("Sys_Au", "*").setAuthors(true);
					tdoc.replaceItemValue("isWrite", "1");
				}else{
					String apitype = tdoc.getItemValueString("APIType");
					if(!"".equals(apitype)){
						String v = this.getURLQueryString(apitype);
						Vector<String> vec = new Vector<String>();
						vec.add(url);
						if(v == null) v = "";
						vec.add(apitype + "_" + v);

						tdoc2 = view.getDocumentByKey(vec, true);
						if(tdoc2 == null){
							tdoc2 = tdb.createDocument();
							tdoc2.replaceItemValue("form", "fmAPI");
							tdoc2.replaceItemValue("reader", "*").setReaders(true);
							tdoc2.replaceItemValue("Sys_Au", "*").setAuthors(true);
							tdoc2.replaceItemValue("isWrite", "1");
							tdoc2.replaceItemValue("APIType", apitype);
							tdoc2.replaceItemValue("APIType_Value",v);

							tdoc2.replaceItemValue("APIName",tdoc.getItemValueString("APIName"));
							tdoc2.replaceItemValue("AppType",tdoc.getItemValueString("AppType"));
							tdoc2.replaceItemValue("APINo",tdoc.getItemValueString("APINo"));
							tdoc2.replaceItemValue("Creater",tdoc.getItemValueString("Creater"));

							System.out.println("--->创建API："+ tdoc.getItemValueString("APIName"));

						}
						tdoc.recycle();
						tdoc = tdoc2;
					}
				}

				if("1".equals(tdoc.getItemValueString("isWrite"))){  //1的时候可以写入
					System.out.println("--->修改API：" + tdoc.getItemValueString("APIName"));
					tdoc.replaceItemValue("isWrite", "0");
					tdoc.replaceItemValue("REQUEST_METHOD",doc.getItemValueString("REQUEST_METHOD"));
					tdoc.replaceItemValue("OutDataType",otype);

					tdoc.replaceItemValue("OutStr",outStr).setSummary(false);
					tdoc.replaceItemValue("PATH_INFO",url);

					tdoc.replaceItemValue("AllURL",tdoc.getItemValueString("PATH_INFO"));
					String urls[] = url.split("/");

					if(urls.length >= 4){
						tdoc.replaceItemValue("PATH",urls[1]);
						tdoc.replaceItemValue("DBName",urls[2]);
						tdoc.replaceItemValue("AgentName",urls[3]);
					}

					String [][] qk = getQueryKey();
					Item item = tdoc.replaceItemValue("Input","");
					if(qk[0] != null){
						int length = qk[0].length;
						for(int i=0;i<length;i++){
							if(!"_".equals(qk[0][i])){
								item.appendToTextList(qk[0][i]);
								tdoc.replaceItemValue("FV_" + qk[0][i], qk[1][i]);
								//f.append(qk[0][i]);
							}
						}
					}

					tdoc.save(true,false);
				}

				if(tdoc2 != null) tdoc2.recycle();
				if(tdoc != null) tdoc.recycle();
				if(view != null) view.recycle();
				if(tdb != null) tdb.recycle();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
