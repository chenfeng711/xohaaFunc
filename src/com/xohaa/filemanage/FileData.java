package com.xohaa.filemanage;
import java.util.Vector;

import com.xohaa.Action;
import com.xohaa.Opt;
import com.xohaa.Base.Func;
import com.xohaa.domino.DocumentSaveimpl;
import lotus.domino.Database;
import lotus.domino.NotesException;
/**
 * 
 * 创建或修改文件
 * @author chenjianxiong
 *
 */
public class FileData extends DocumentSaveimpl  {
	
	private String Attdbname;
	private String AttachName;
	private String AttachNameSize;
	private String AttachNameType;
	private String targetFilePath;
	private String CreaterShortName;
	private String submitattachtime;
	private String Desc;
	private String AttachDocUNID;
	private Vector<String> Sys_Reader;
	private Vector<String> Sys_Reader_Show;
	private String Readonly;
	private Vector<String> Sys_RoleReader;
	public void setSys_RoleReader(Vector<String> sys_RoleReader) {
		Sys_RoleReader = sys_RoleReader;
	}

	/**
	 * 附件数据库名称
	 * @param attdbname
	 */
	public void setAttdbname(String attdbname) {
		Attdbname = attdbname;
	}
	
	/**
	 * 附件文件名称
	 * @param attachName
	 */
	public void setAttachName(String attachName) {
		AttachName = attachName;
	}

	/**
	 * 附件大小
	 * @param attachNameSize
	 */
	public void setAttachNameSize(String attachNameSize) {
		AttachNameSize = attachNameSize;
	}

	/**
	 * 附件文件类型
	 * @param attachNameType
	 */
	public void setAttachNameType(String attachNameType) {
		AttachNameType = attachNameType;
	}
	/**
	 * 设置分类只能查看1。
	 * @param readonly
	 */
	public void setReadonly(String readonly) {
		Readonly = readonly;
	}
	/**
	 * 设置描述
	 * @param desc
	 */
	public void setDesc(String desc) {
		Desc = desc;
	}
	
	/**
	 * 本地路径地址。
	 * @param targetFilePath
	 */
	public void setTargetFilePath(String targetFilePath) {
		this.targetFilePath = targetFilePath;
	}

	@Override
	public void setCreater_Show(String creater_Show) {
		Creater_Show = creater_Show;
		CreaterShortName = creater_Show;
	}

	/**
	 * 上传时间
	 * @param submitattachtime
	 */
	public void setSubmitattachtime(String submitattachtime) {
		this.submitattachtime = submitattachtime;
	}

	/**
	 * 附件文档的UNID
	 * @param attachDocUNID
	 */
	public void setAttachDocUNID(String attachDocUNID) {
		AttachDocUNID = attachDocUNID;
	}

	/**
	 * 
	 * @param sys_Reader
	 */
	public void setSys_Reader(Vector<String> sys_Reader) {
		Sys_Reader = sys_Reader;
	}

	/**
	 * 
	 * @param sys_Reader_Show
	 */
	public void setSys_Reader_Show(Vector<String> sys_Reader_Show) {
		Sys_Reader_Show = sys_Reader_Show;
	}

	/**
	 * 只读
	 */
	public static String Readonly_Read = "1";
	/**
	 * 
	 * @param F
	 * @throws Exception 
	 */
	public FileData(Func F) throws Exception{
		super(F, Opt.File_DBName);
	}
	
	
	/**
	 * 
	 * @param filedatadb
	 * @throws NotesException
	 */
	public FileData(Func F,Database filedatadb) throws NotesException{
		super(F,filedatadb);
	}
	/**
	 * 初始化参数
	 */
	@Override
	protected void initParam(){
		Attdbname = "";
		AttachName = "";
		AttachNameSize = "";
		AttachNameType = "";
		targetFilePath = "";
		Readonly = "";
		CreaterShortName = "";
		submitattachtime = Action.getCurDataTime();
		AttachDocUNID = "";
		Sys_Reader = new Vector<String>();
		Sys_Reader_Show = new Vector<String>();
		Sys_RoleReader = new Vector<String>();
		Desc = "";
		unid = "";
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void initParamforDoc() throws NotesException {
		Attdbname = doc.getItemValueString("Attdbname");
		AttachName = doc.getItemValueString("AttachName");
		AttachNameSize = doc.getItemValueString("AttachNameSize");
		AttachNameType = doc.getItemValueString("AttachNameType");
		targetFilePath = doc.getItemValueString("targetFilePath");
		Readonly = doc.getItemValueString("Readonly");
		CreaterShortName = doc.getItemValueString("CreaterShortName");
		submitattachtime = doc.getItemValueString("submitattachtime");
		AttachDocUNID = doc.getItemValueString("submitattachtime");
		Sys_Reader = doc.getItemValue("Sys_Reader");
		Sys_Reader_Show = doc.getItemValue("Sys_Reader_Show");
		Sys_RoleReader = doc.getItemValue("Sys_RoleReader");
		Desc = doc.getItemValueString("Desc");
		unid = doc.getUniversalID();
	}
	@Override
	public void doSave() throws Exception {
		if(unid != null && !unid.equals("")){
			doc = db.getDocumentByUNID(unid);
		}else{
			doc = db.createDocument();
			doc.replaceItemValue("form", "fmFile");
			doc.replaceItemValue("Status", "1");
			doc.replaceItemValue("Creater", Creater).setAuthors(true);
			doc.replaceItemValue("Creater_Show", Creater_Show);
			doc.replaceItemValue("CreateTime", Action.getCurDataTime());
			doc.replaceItemValue("Sys_Manager", "Sys_Admin").setAuthors(true);
		}
		
		
		this.isNeed("Creater", Creater);
		this.isNeed("Creater_Show", Creater_Show);
		this.isNeed("Attdbname", Attdbname);
		this.isNeed("AttachName", AttachName);
		this.isNeed("AttachNameSize", AttachNameSize);
		this.isNeed("AttachNameType", AttachNameType);
		this.isNeed("AttachDocUNID", AttachDocUNID);
		
		if(this.Sys_Reader.size() == 0){
			doc.replaceItemValue("Sys_Reader","*").setReaders(true);
			doc.replaceItemValue("Sys_Reader_Show","所有人");			
		}else{
			doc.replaceItemValue("Sys_Reader",this.Sys_Reader).setReaders(true);
			doc.replaceItemValue("Sys_Reader_Show",this.Sys_Reader_Show);	
		}
		doc.replaceItemValue("Sys_RoleReader", this.Sys_RoleReader).setReaders(true);
		doc.replaceItemValue("Desc", Desc);
		doc.replaceItemValue("Attdbname", Attdbname); 	//附件数据库
		doc.replaceItemValue("ParentID", ParentID);
		doc.replaceItemValue("AttachName", AttachName);
		doc.replaceItemValue("AttachNameSize",AttachNameSize);
		doc.replaceItemValue("AttachNameType", AttachNameType);
		doc.replaceItemValue("targetFilePath", targetFilePath);
		doc.replaceItemValue("CreaterShortName", CreaterShortName);
		doc.replaceItemValue("submitattachtime",submitattachtime);
		doc.replaceItemValue("AttachDocUNID",AttachDocUNID);
		doc.replaceItemValue("Readonly", this.Readonly);
		doc.save(true,false);
		this.unid = doc.getUniversalID();
	}

	@Override
	public String getRelationalID() {
		return null;
	}
	
	@Override
	public void setRelationalID(String id) throws Exception {
		doc.replaceItemValue("ParentID", id);
		doc.save(true,false);
	}

	@Override
	protected String getByIDViewName() {
		// TODO Auto-generated method stub
		return "vw_FileData";
	}
}
