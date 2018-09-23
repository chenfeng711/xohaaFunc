package com.xohaa.tablelist;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;

import com.xohaa.Base.Func;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.ViewEntryCollection;

public abstract class TableListBase {
	protected Func F = null;
	protected String keyunid = null;
	protected Document configdoc = null;
	protected Database configdb = null;
	private long stratindex = 0,endindex = 0,count = 0,pageno = 0,pagesize = 0;

	/**
	 * 
	 * @return long
	 */
	public long getStratindex() {
		return stratindex;
	}

	/**
	 * 
	 * @return long
	 */
	public long getEndindex() {
		return endindex;
	}

	/**
	 * 
	 * @param F
	 * @param cdb
	 * @param cdoc
	 * @throws Exception
	 */
	public TableListBase(Func F,Database cdb,Document cdoc) throws Exception{
		this.F = F;
		this.configdb = cdb;
		this.configdoc = cdoc;

		if (!cdoc.getItemValueString("YYKEY_UNID").equals("")){
			keyunid = cdoc.getItemValueString("YYKEY_UNID");
		}else{
			keyunid = cdoc.getItemValueString("key_UNID");
		}
	}
	/**
	 * 
	 */
	public void recycle(){
		try{
			//			if(cdoc != null) cdoc.recycle();
			//			if(tdb != null) tdb.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param cdoc
	 * @param e
	 * @return String
	 * @throws NotesException
	 */
	public static String returnErrMsg(Document cdoc,Exception e){
		String msg = null;
		try{
			msg = e.getMessage();
			if(msg == null || "".equals(msg)){
				msg = e.toString().replaceAll("java.lang.Exception:", "");
			}
		}catch(Exception e1){
			msg = e.toString().replaceAll("java.lang.Exception:", "");
		}

		try{
			String errcode = cdoc.getItemValueString("ReturnErrCode");
			String errmsg = cdoc.getItemValueString("ReturnErrMsg");
			if(!"".equals(errcode) && !"".equals(errmsg)){
				String[] codes = errcode.split(";");
				String[] msgs = errmsg.split(";");
				if(codes.length == msgs.length){
					for(int i=0;i<codes.length;i++){
						if(msg.indexOf(codes[i])!=-1){
							return msgs[i];
						}
					}

				}
			}
		}catch(Exception e2){
			e2.printStackTrace();
		}
		return msg;
	}

	/**
	 * 
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getFormFieldCollection() throws Exception{
		return this.getViewCollection("v_key_f_fields_form_Status");
	}

	/**
	 * 
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getListFieldCollection() throws Exception{
		return this.getViewCollection("v_key_f_fields_Status");
	}
	
	/**
	 * 
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getSearchFieldCollection() throws Exception{
		return this.getViewCollection("vw_f_search_Status");
	}
	/**
	 * 
	 * @param viewname
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	private ViewEntryCollection getViewCollection(String viewname) throws Exception{
		ViewEntryCollection vc = F.getAllEntriesBykey(configdb,viewname,keyunid, true);
		if(vc.getCount() == 0){
			throw new Exception(viewname + "视图记录为空！");
		}
		return vc;
		
	}

	/**
	 * 计算分页
	 * @param pn
	 * @param pz
	 */
	public void pagesize(String pn,String pz){
		pageno = Long.parseLong(pn);
		pagesize = Long.parseLong(pz);

		if (pageno == 1){
			stratindex = 0;
			endindex = pagesize;
		}else{
			stratindex = (pageno-1)* pagesize;
			endindex = pageno * pagesize;
		}

	}

	/**
	 * 实现生成JSON数据方法
	 * @throws Exception
	 */
	public abstract void toSetJSONObject() throws Exception;
	/**
	 * 
	 */
	public void putPageInfo(){
		F.putJsonData("pagesize", pagesize);
		F.putJsonData("pageno", pageno);
		F.putJsonData("count", count);
	}


	/**
	 * 
	 * @param rs
	 * @param columnCount
	 * @param metaData
	 * @param formula
	 * @return String
	 */
	public String handleFormula(ResultSet rs,int columnCount,ResultSetMetaData metaData,String formula){
		try{
			for (int i = 1; i <= columnCount; i++) {
				F.getCurDoc().replaceItemValue(metaData.getColumnLabel(i),rs.getString(i));
			}
			Vector<String> result = F.Evaluate(F.getCurDoc(),formula);
			return result.get(0).toString();
		}catch (Exception e){
			e.printStackTrace();
			return "执行公式出错";
		}

	}

	/**
	 * 
	 * @return long
	 */
	public long getCount() {
		return count;
	}

	/**
	 * 
	 * @param count
	 */
	public void setCount(long count) {
		this.count = count;
	}

	/**
	 * 
	 * @param id
	 */
	public abstract void copyNewRow(String id) throws Exception;
}
