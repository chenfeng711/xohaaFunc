package com.xohaa.HandleDomino;
import com.xohaa.Action;
import com.xohaa.Base.Func;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;

public class IntegrateAction extends Func {
	Document appdoc = null;
	Database appdb = null;

	public IntegrateAction(Session se) throws NotesException {
		super(se);
	}

	public IntegrateAction(){

	}
	/**
	 * 设置指定文档的 ItemValue根据URL参数写入。
	 * @param newdoc
	 * @param QueryString
	 * @param isMu true 多值
	 * @return Item
	 * @throws NotesException
	 */
	public Item setURLQueryStringForItem(Document newdoc,String QueryString,boolean isMu) throws NotesException{
		if(isMu){
			return newdoc.replaceItemValue(QueryString,this.getURLQueryVector(QueryString));
		}else{
			return newdoc.replaceItemValue(QueryString,this.getURLQueryString(QueryString));
		}
	}

	/**
	 * 设置指定文档的 ItemValue根据URL参数写入，多值。
	 * @param newdoc
	 * @param QueryString
	 * @param Mu 分割符
	 * @return Item
	 * @throws NotesException
	 */
	public Item setURLQueryStringForItem(Document newdoc,String QueryString,String Mu) throws NotesException{
		if(Mu.equals("")){
			return newdoc.replaceItemValue(QueryString,this.getURLQueryString(QueryString));
		}else{
			return newdoc.replaceItemValue(QueryString,Action.getMuiltValue(this.getURLQueryString(QueryString), Mu));
		}
	}
	/**
	 * 设置指定文档的 ItemValue根据URL参数写入
	 * @param newdoc
	 * @param QueryString
	 * @return Item
	 * @throws NotesException
	 */
	public Item setURLQueryStringForItem(Document newdoc,String QueryString) throws NotesException{
		return this.setURLQueryStringForItem(newdoc, QueryString, false);
	}

	/**
	 * 
	 */
	public void recycleFunc(){
		try{
			if(appdoc != null) appdoc.recycle();
			super.recycleFunc();
		}catch(Exception e){

		}
	}
}