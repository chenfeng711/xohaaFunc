package com.xohaa.Base;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntryCollection;
/**
 * 处理Domino数据
 * @author chenfeng
 *
 */
public class HandleData {
	private Database appdb = null;
	private View appview = null;
	private Document appdoc = null;
	private boolean isdb = false;
	/**
	 * 
	 * @param db
	 */
	public HandleData(Database db){
		if(db != null){
			appdb = db;
			isdb = true;
		}else{
			System.out.println("数据库无法打开！");
		}

	}
	
	/**
	 * 
	 * @param db
	 * @param viewname
	 */
	public HandleData(Database db,String viewname){
		isdb = true;
		appdb = db;
		this.setAppview(viewname);
	}
	
	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param searchkey
	 * @param exact
	 * @return DocumentCollection
	 * @throws NotesException 
	 */
	public  DocumentCollection getAllDocumentsBykey(String searchkey,boolean exact) throws NotesException{
		return appview.getAllDocumentsByKey(searchkey,exact);
	}
	/**
	 * 根据key获取多条文档,返回文档集合
	 * @param searchkeys
	 * @param exact
	 * @return DocumentCollection
	 * @throws NotesException 
	 */
	public DocumentCollection getAllDocumentsBykey(Vector<String> searchkeys,boolean exact) throws NotesException{
		return appview.getAllDocumentsByKey(searchkeys,exact);
	}

	/**
	 * 判断包含
	 * @param skey 字符
	 * @return boolean
	 */
	public boolean isContainsDataByKeys(String skey){
		try {
			appdoc = appview.getDocumentByKey(skey);
			if(appdoc != null){
				return true;
			}else{
				return false;
			}
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
	/**
	 * 根据key获取多条文档
	 * @param searchkeys
	 * @param exact
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(Vector<String> searchkeys,boolean exact) throws Exception{
		return appview.getAllEntriesByKey(searchkeys,exact);
	}
	/**
	 * 根据key获取多条行
	 * @param searchkey
	 * @param exact
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getAllEntriesBykey(String searchkey,boolean exact) throws Exception{
		return appview.getAllEntriesByKey(searchkey,exact);
	}


	
	/**
	 * 设置视图
	 * @param view
	 */
	public void setAppview(View view){
		if(view != null){
			appview = view;
		}else{
			System.out.println("视图无法打开！");
		}
	}
	/**
	 * 设置视图
	 * @param viewname
	 */
	public void setAppview(String viewname){
		try {
			appview = Func.openView(appdb, viewname);
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(appview == null){
			System.out.println("["+ viewname +"]视图无法打开！");
		}

	}

	/**
	 * 获取文档字段。
	 * @param searchkey
	 * @param fieldname
	 * @return String
	 * @throws NotesException
	 */
	public String getItemValueString(String searchkey,String fieldname) throws NotesException{
		appdoc = this.getDocumentBykey(searchkey, true);
		if(appdoc != null){
			return appdoc.getItemValueString(fieldname);
		}else{
			return "";
		}
	}
	/**
	 * 获取文档字段。
	 * @param searchkeys
	 * @param fieldname
	 * @return String
	 * @throws NotesException
	 */
	public String getItemValueString(Vector<String> searchkeys,String fieldname) throws NotesException{
		appdoc = this.getDocumentBykey(searchkeys, true);
		if(appdoc != null){
			return appdoc.getItemValueString(fieldname);
		}else{
			return "";
		}
	}
	/**
	 * 根据key获取指定数据库下的单条文档
	 * @param searchkeys
	 * @param exact
	 * @return Document
	 * @throws NotesException 
	 */
	public Document getDocumentBykey(Vector<String> searchkeys,boolean exact) throws NotesException{
		return appview.getDocumentByKey(searchkeys, exact);
	}
	/**
	 * 根据key获取指定数据库下的单条文档
	 * @param searchkey
	 * @param exact
	 * @return Document
	 * @throws NotesException 
	 */
	public Document getDocumentBykey(String searchkey,boolean exact) throws NotesException{
		return appview.getDocumentByKey(searchkey,exact);
	}

	/**
	 * 
	 */
	public void recycle(){
		try{
			if(appdoc != null) appdoc.recycle();
			if(appview != null) appview.recycle();
			if(!isdb){
				if(appdb != null) appdb.recycle();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}