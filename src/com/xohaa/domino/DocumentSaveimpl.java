package com.xohaa.domino;

import com.xohaa.Base.Func;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

public abstract class DocumentSaveimpl implements DocumentSave {
	protected Database db = null;
	protected Document doc = null;
	protected View view = null;
	protected Func F = null;
	protected String unid = null;
	protected String ParentID = null;
	protected String Creater = null;
	protected String Creater_Show = null;
	private boolean isinitdb = false;
	
	public Document getDoc() throws Exception {
		if(doc == null){
			throw new Exception("文档对象为空值！");
		}
		return doc;
	}
	/**
	 * 
	 * @param F
	 * @throws NotesException
	 */
	public DocumentSaveimpl(Func F,String dbname) throws Exception{
		this.db = F.OpenDB(dbname);
		if(this.db == null){
			throw new Exception("无法打开数据库："+ dbname);
		}
		this.isinitdb = true;
		this.F = F;
		this.initParam();
	}

	/**
	 * 
	 * @param F
	 * @param filedatadb
	 * @throws NotesException
	 */
	public DocumentSaveimpl(Func F,Database db) throws NotesException{
		this.F = F;
		this.db = db;
		this.initParam();
	}

	/**
	 * 通过自定义初始化Document对象。
	 * @param cid
	 * @return
	 * @throws Exception
	 */
	public boolean doGetViewDocumentbyID(String cid) throws Exception{
		if(!cid.equals("")){
			if(view == null){
				String viewname = this.getByIDViewName();
				if(viewname == null || viewname.equals("")){
					throw new Exception("getByIDViewName方法获取的视图不能为空");
				}
				view = db.getView(viewname); //"Groups"
				if(view == null){
					throw new Exception(db.getFileName() + "/"+ viewname + "视图不存 在！");
				}
			}
			
			if(doc != null) doc.recycle();
			doc = view.getDocumentByKey(cid,true);
			if(doc == null){
				return false;
			}else{
				initParamforDoc();
				return true;
			}
		}else{
			throw new Exception("参数不能为空");
		}
	}

	/**
	 * 初始化参数，来源于文档对象
	 * @throws NotesException
	 */
	protected abstract void initParamforDoc() throws NotesException;
	
	/**
	 * 初始化参数
	 * @throws NotesException
	 */
	protected abstract void initParam() throws NotesException;
	/**
	 * 定义视图名称,首列必须根据自定义ID字段进行排序。如客户编号。
	 * @return
	 */
	protected abstract String getByIDViewName();
	/**
	 * 判断必填
	 * @param fieldname
	 * @param value
	 * @throws Exception 
	 */
	protected void isNeed(String fieldname,String value) throws Exception{
		if(value == null || value == ""){
			throw new Exception(fieldname + " 参数为空！");
		}
	}

	@Override
	public void recycle() throws NotesException {
		if(this.doc != null) this.doc.recycle();
		if(this.view !=null) this.view.recycle();
		if(isinitdb){
			if(this.db != null) this.db.recycle();
		}
	}

	@Override
	public String getUnid() {
		return this.unid;
	}

	@Override
	public void setUnid(String unid) {
		this.unid = unid;
	}

	@Override
	public String getRelationalID() {
		return unid;
	}

	/**
	 * 设置创建人编号
	 * @param creater
	 */
	public void setCreater(String creater) {
		Creater = creater;
	}

	/**
	 * 新行
	 */
	public void addNewDoc(){
		this.setUnid("");
	}
	/**
	 * 设置创建人中文
	 * @param creater_Show
	 */
	public void setCreater_Show(String creater_Show) {
		Creater_Show = creater_Show;
	}

	/**
	 * 上级ID
	 * @param parentID
	 * @throws Exception 
	 */
	public void setParentID(String parentID) {
		ParentID = parentID;
	}

	/**
	 * 添加节点数据
	 * @param ds 父节点
	 * @throws Exception 
	 */
	public void appendTo(DocumentSaveimpl ds) throws Exception{
		if(doc == null){
			throw new Exception("文档对象为空！");
		}
		String parendid = ds.getRelationalID();
		this.isNeed("RelationalID", parendid);
		this.setRelationalID(parendid);
	}
	
	@Override
	public boolean isHasData(String FID) throws Exception{
		boolean b;
		Document doc1 = F.getDocumentBykey(this.db,this.getByIDViewName(), FID, true);
		if(doc1 != null){
			b = true;
		}else{
			b = false;
		}
		
		if(doc1 != null) doc1.recycle();
		return b;
	}
}
