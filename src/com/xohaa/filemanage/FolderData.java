package com.xohaa.filemanage;
import java.util.Vector;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;

import com.xohaa.Action;
import com.xohaa.Opt;
import com.xohaa.Base.Func;
import com.xohaa.domino.DocumentSaveimpl;
/**
 * 创建或修改分类
 * @author chenjianxiong
 *
 */
public class FolderData extends DocumentSaveimpl{
	private String FolderName;
	private String FolderID;
	private String Desc;
	private String Status;
	private String type;
	private String Options;
	private String Readonly;
	private Vector<String> Sys_Reader;
	private Vector<String> Sys_Reader_Show;
	private Vector<String> FolderSysAdmin;
	private Vector<String> FolderSysAdmin_Show;
	private Vector<String> Sys_RoleReader;
	

	/**
	 * 1 表示这是分类
	 */
	public static String Type_FenLei = "1";
	
	/**
	 * 1所有人可查看
	 */
	public static String Options_ALL = "1";
	
	/**
	 * 2自定义查看
	 */
	public static String Options_CUSTOM = "2";
	
	/**
	 * 3本人查看
	 */
	public static String Options_OWN = "3";
	
	/**
	 * 1 是可用。
	 */
	public static String Status_USABLE = "1";
	
	/**
	 * 0 代表删除到回收站。
	 */
	public static String Status_DELETE = "0";
	
	/**
	 * 只读
	 */
	public static String Readonly_Read = "1";

	/**
	 * 分类名称
	 * @param folderName
	 */
	public void setFolderName(String folderName) {
		FolderName = folderName;
	}

	/**
	 * 设置只读权限角色
	 * @param Sys_RoleReader
	 */
	public void setSys_AllReader(Vector<String> role) {
		Sys_RoleReader = role;
	}
	/**
	 * 分类ID
	 * @param folderID
	 */
	public void setFolderID(String folderID) {
		FolderID = folderID;
	}

	/**
	 * 设置分类系统管理编号
	 * @param folderSysAdmin
	 */
	public void setFolderSysAdmin(Vector<String> folderSysAdmin) {
		FolderSysAdmin = folderSysAdmin;
	}

	/**
	 * 设置分类系统管理中文
	 * @param folderSysAdmin_Show
	 */
	public void setFolderSysAdmin_Show(Vector<String> folderSysAdmin_Show) {
		FolderSysAdmin_Show = folderSysAdmin_Show;
	}

	/**
	 * 设置描述
	 * @param desc
	 */
	public void setDesc(String desc) {
		Desc = desc;
	}
	
	/**
	 * 设置分类只能查看。
	 * @param readonly
	 */
	public void setReadonly(String readonly) {
		Readonly = readonly;
	}

	/**
	 * 设置状态 1是可用，0 代表删除到回收站。
	 * @param status
	 */
	public void setStatus(String status) {
		Status = status;
	}

	/**
	 * //1表示这是分类
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * 设置权限方式 ：1所有人可查看，2自定义，3 本人
	 * @param options
	 */
	public void setOptions(String options) {
		Options = options;
	}
	
	/**
	 * 设置自定义查看权限编号
	 * @param sys_Reader
	 */
	public void setSys_Reader(Vector<String> sys_Reader) {
		Sys_Reader = sys_Reader;
	}

	/**
	 * 设置自定义查看权限中文
	 * @param sys_Reader_Show
	 */
	public void setSys_Reader_Show(Vector<String> sys_Reader_Show) {
		Sys_Reader_Show = sys_Reader_Show;
	}	
	
	/**
	 * 初始化参数
	 */
	@Override
	protected void initParam(){
		FolderName = "";
		FolderID = "";
		Desc = "";
		Creater_Show = "";
		unid = "";
		Readonly = "";
		Sys_RoleReader = new Vector<String>();
		Creater = F.getEffectiveUserName();
		Status = FolderData.Status_USABLE;
		type = FolderData.Type_FenLei;
		Options = FolderData.Options_OWN;
		FolderSysAdmin = new Vector<String>();
		FolderSysAdmin_Show = new Vector<String>();
		Sys_Reader = new Vector<String>();
		Sys_Reader_Show = new Vector<String>();
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void initParamforDoc() throws NotesException {
		FolderName = doc.getItemValueString("FolderName");
		FolderID = doc.getItemValueString("FolderID");
		Desc = doc.getItemValueString("Desc");
		Creater_Show = doc.getItemValueString("Creater_Show");
		Readonly = doc.getItemValueString("Readonly");
		Sys_RoleReader = doc.getItemValue("Sys_RoleReader");
		Creater = doc.getItemValueString("Creater");
		Status = doc.getItemValueString("Status");
		type = doc.getItemValueString("type");
		Options = doc.getItemValueString("Options");
		FolderSysAdmin = doc.getItemValue("FolderSysAdmin");
		FolderSysAdmin_Show = doc.getItemValue("FolderSysAdmin_Show");
		Sys_Reader = doc.getItemValue("Sys_Reader");
		Sys_Reader_Show = doc.getItemValue("Sys_Reader_Show");
		unid = doc.getUniversalID();
		
	}
	/**
	 * 
	 * @param F
	 * @throws Exception 
	 */
	public FolderData(Func F) throws Exception{
		super(F,Opt.File_DBName);
	}
	/**
	 * 
	 * @param F
	 * @param filedatadb
	 * @throws NotesException
	 */
	public FolderData(Func F,Database filedatadb) throws NotesException{
		super(F,filedatadb);
	}

	/**
	 * 判断文件名是否重复。
	 * @return
	 * @throws NotesException
	 */
	private boolean contains() throws NotesException{
		boolean b;
		Vector<String> vec = new Vector<String>();
		vec.add(this.ParentID);
		vec.add(this.FolderName);
		Document doc1 = F.getDocumentBykey(this.db,"vw_FileManage_CheckName", vec, true);
		if(doc1 != null){
			if(this.unid.equals(doc1.getUniversalID())){
				b = false;
			}else{
				b = true;
			}
		}else{
			b = false;
		}
		
		if(doc1 != null) doc1.recycle();
		return b;
	}
	
	@Override
	public void doSave() throws Exception {
		if(ParentID == null || ParentID.equals("")){
			ParentID = "ROOT";
		}
		if(this.contains()){
			throw new Exception("文件夹已存在，请重新填写!");
		}else{
			if(unid != null && !unid.equals("")){
				doc = db.getDocumentByUNID(unid);
			}else{
				doc = db.createDocument();
				doc.replaceItemValue("ParentID", ParentID);
				if(FolderID == null){
					this.FolderID = doc.getUniversalID();
				}

				doc.replaceItemValue("form", "fmFolder");
				doc.replaceItemValue("Creater", Creater).setAuthors(true);
				doc.replaceItemValue("Creater_Show", Creater_Show);
				doc.replaceItemValue("CreateTime", Action.getCurDataTime());
			}

			this.isNeed("Creater", Creater);
			this.isNeed("Creater_Show", Creater_Show);
			this.isNeed("FolderName", FolderName);
			this.isNeed("FolderID", FolderID);
			
			save();
			doc.save(true,false);
			this.unid = doc.getUniversalID();
		}

		if(doc!= null) doc.recycle();
	}
	
	
	/**
	 * 
	 * @param doc
	 * @throws Exception 
	 */
	private void save() throws Exception{
		doc.replaceItemValue("FolderName", this.FolderName);
		doc.replaceItemValue("FolderID", this.FolderID);
		doc.replaceItemValue("FolderSysAdmin", this.FolderSysAdmin).setAuthors(true);
		doc.replaceItemValue("FolderSysAdmin_Show", this.FolderSysAdmin_Show);
		doc.replaceItemValue("Desc", this.Desc);
		doc.replaceItemValue("Status",this.Status);
		doc.replaceItemValue("type",this.type); 
		doc.replaceItemValue("Options", this.Options);
		doc.replaceItemValue("Readonly", this.Readonly);
		doc.replaceItemValue("Sys_RoleReader", this.Sys_RoleReader).setReaders(true);
		F.setSysAdminPower(doc);
		
		if(Options.equals(FolderData.Options_ALL)){//所有人
			doc.replaceItemValue("Sys_AllReader", "*").setReaders(true);
			doc.replaceItemValue("Sys_Reader", "").setReaders(true);
			doc.replaceItemValue("Sys_Reader_Show", "");
		}else if(Options.equals(FolderData.Options_CUSTOM)){ //自定义
			doc.replaceItemValue("Sys_AllReader", "");
			doc.replaceItemValue("Sys_Reader", Sys_Reader).setReaders(true);
			doc.replaceItemValue("Sys_Reader_Show",Sys_Reader_Show);
		}else if(Options.equals(FolderData.Options_OWN)){ //创建人
			doc.replaceItemValue("Sys_AllReader",Creater).setReaders(true);
			doc.replaceItemValue("Sys_Reader", "");
			doc.replaceItemValue("Sys_Reader_Show", "");
		}else{
			throw new Exception("Options 类型错误！");
		}
	}
	
	@Override
	public String getRelationalID() {
		return this.FolderID;
	}

	@Override
	public void setRelationalID(String id) throws Exception {
		doc.replaceItemValue("ParentID", id);
		doc.save(true,false);
	}

	@Override
	protected String getByIDViewName() {
		// TODO Auto-generated method stub
		return "vw_FileManage_ByFolderID_All";
	}

}