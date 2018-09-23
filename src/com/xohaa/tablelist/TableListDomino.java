package com.xohaa.tablelist;

import java.util.Vector;

import com.xohaa.Base.Func;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntryCollection;

public class TableListDomino extends TableListBase  {
	private Database appdb = null;
	private View dataview = null;
	private Func F = null;

	/**
	 * 
	 * @param F
	 * @param cdb
	 * @param cdoc
	 * @throws Exception 
	 */
	public TableListDomino(Func F,Database cdb,Document cdoc) throws Exception{
		super(F, cdb, cdoc);
		this.F = F;
	}

	/**
	 * 
	 */
	public void recycleDomino(){
		try{
			if(dataview == null) dataview.recycle();
			if(appdb == null) appdb.recycle();
			super.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	 * @param dbname
	 * @throws NotesException
	 */
	private void openAppDB(String dbname) throws NotesException{
		appdb = F.OpenDB(dbname,true);
	}

	/**
	 * 
	 * @param dbname
	 * @param keyvalue
	 * @param keyid
	 * @param searchkey
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getDominoCollection(String dbname,String keyvalue,String keyid,String searchkey) throws Exception{
		if(appdb == null){
			this.openAppDB(dbname);
		}
		return this.getDominoCollection(keyvalue, keyid, searchkey);
	}
	
	/**
	 * 
	 * @param keyvalue
	 * @param keyid
	 * @param searchkey
	 * @return ViewEntryCollection
	 * @throws Exception
	 */
	public ViewEntryCollection getDominoCollection(String keyvalue,String keyid,String searchkey) throws Exception{
		ViewEntryCollection vc = null;
		if(appdb == null){
			this.openAppDB(configdoc.getItemValueString("dbname"));
		}
		
		if(dataview == null){
			this.dataview = appdb.getView(configdoc.getItemValueString("viewname"));
			if(this.dataview == null){
				throw new Exception(configdoc.getItemValueString("viewname") + "视图不存在。");
			}
		}
		
		Vector<String> vec = new Vector<String>();
		if("2".equals(configdoc.getItemValueString("TableListType"))){ // 从表
			vec.add(keyid);
		}

		if(!"".equals(keyvalue)){
			vec.add(keyvalue);
		}

		if(vec.size()== 0){
			vc = dataview.getAllEntries();
		}else{
			vc = dataview.getAllEntriesByKey(vec, true);
		}

		//搜索
		if("1".equals(configdoc.getItemValueString("IsSearch"))){
			if(!"".equals(searchkey)){
				vc.FTSearch(searchkey,0);
			}
		}
		return vc;
	}

	@Override
	public void toSetJSONObject() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyNewRow(String id) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
