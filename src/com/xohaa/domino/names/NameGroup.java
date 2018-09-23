package com.xohaa.domino.names;

import java.util.Vector;
import com.xohaa.Base.Func;
import com.xohaa.domino.DocumentSaveimpl;
import lotus.domino.Database;
import lotus.domino.NotesException;

public class NameGroup extends DocumentSaveimpl{
	private Vector<String> Members;
	private String AvailableForDirSync;
	private String ListDescription;
	private String ListCategory;
	private String ListOwner;
	private String LocalAdmin;
	private String IsAisGroup;
	private String AisGroupType;
	private String ListName;
	private String ListName_Show;

	public static String AisGroupType_GS = "公司群组";
	public static String AisGroupType_BM = "部门群组";
	public static String AisGroupType_ZW = "职位群组";
	public static String AisGroupType_GW = "岗位群组";
	public static String AisGroupType_ZGW = "子岗位群组";
	public static String AisGroupType_QT = "其它群组";

	/**
	 * 替换所有子项
	 * @param members
	 */
	public void setMembers(Vector<String> members) {
		Members = members;
	}

	/**
	 * 替换所有子项
	 * @param members
	 */
	public void setMembers(String members) {
		Vector<String> vec = new Vector<String>();
		vec.add(members);
		this.setMembers(vec);
	}

	/**
	 * 添加子项
	 * @param members
	 * @throws Exception
	 */
	public void addMember(String members) throws Exception {
		if(Members == null){
			throw new Exception("Members为空值！");
		}
		Members.add(members);
	}	

	/**
	 * 启用外部目录同步
	 * @param availableForDirSync
	 */
	public void setAvailableForDirSync(String availableForDirSync) {
		AvailableForDirSync = availableForDirSync;
	}

	/**
	 * 说明
	 * @param listDescription
	 */
	public void setListDescription(String listDescription) {
		ListDescription = listDescription;
	}

	/**
	 * 列表的类别
	 * @param listCategory
	 */
	public void setListCategory(String listCategory) {
		ListCategory = listCategory + "_" + F.getCurPATH();
	}

	/**
	 * 管理-所有者
	 * @param listOwner
	 */
	public void setListOwner(String listOwner) {
		ListOwner = listOwner;
	}
	/**
	 * 管理-管理员
	 * @param localAdmin
	 */
	public void setLocalAdmin(String localAdmin) {
		LocalAdmin = localAdmin;
	}
	/**
	 * 同步标志
	 * @param isAisGroup
	 */
	public void setIsAisGroup(String isAisGroup) {
		IsAisGroup = isAisGroup;
	}
	/**
	 * 组织架构同步关键字，部门群组_dev
	 * @param aisGroupType
	 */
	public void setAisGroupType(String aisGroupType) {
		AisGroupType = aisGroupType + "_" + F.getCurPATH();
	}
	/**
	 * 群组名称ID
	 * @param listName
	 */
	public void setListName(String listName) {
		ListName = listName;
	}
	/**
	 * 群组名称
	 * @param listName_Show
	 */
	public void setListName_Show(String listName_Show) {
		ListName_Show = listName_Show;
	}

	/**
	 * 
	 * @param filedatadb
	 * @throws NotesException
	 */
	public NameGroup(Func F,Database db) throws NotesException{
		super(F,db);
		this.initParam();
	}
	/**
	 * 
	 */
	@Override
	protected void initParam(){
		Members = new Vector<String>();
		AvailableForDirSync = "1";
		ListDescription = "";
		ListCategory = "";
		ListOwner = "*";
		LocalAdmin = "*";
		IsAisGroup = "1";
		AisGroupType = "";
		ListName = "";
		ListName_Show = "";
		unid = "";
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initParamforDoc() throws NotesException{
		Members = doc.getItemValue("Members");
		AvailableForDirSync = doc.getItemValueString("AvailableForDirSync");
		ListDescription = doc.getItemValueString("ListDescription");
		ListCategory = doc.getItemValueString("ListCategory");
		ListOwner = doc.getItemValueString("ListOwner");
		LocalAdmin = doc.getItemValueString("LocalAdmin");
		IsAisGroup = doc.getItemValueString("IsAisGroup");
		AisGroupType = doc.getItemValueString("AisGroupType");
		ListName = doc.getItemValueString("ListName");
		ListName_Show = doc.getItemValueString("ListName_Show");
		unid = doc.getUniversalID();
	}


	/**
	 * 
	 */
	//@SuppressWarnings("unchecked")
	public void doSave() throws Exception {
		if(unid != null && !unid.equals("")){
			if(doc == null){
				doc = db.getDocumentByUNID(unid);
			}
			//this.Members = doc.getItemValue("Members");
		}else{
			doc = db.createDocument();
			//doc.computeWithForm(false, false);
			doc.replaceItemValue("Form","Group");
			doc.replaceItemValue("Type","Group");
			doc.replaceItemValue("GroupTitle","0");
			doc.replaceItemValue("GroupType","0");
			doc.replaceItemValue("DocumentAccess","[GroupModifier]");
		}

		this.isNeed("ListName",ListName);
		this.isNeed("AisGroupType",AisGroupType);
		this.isNeed("ListName_Show",ListName_Show);
		this.isNeed("ListCategory",ListCategory);

		doc.replaceItemValue("Members",this.Members);
		doc.replaceItemValue("AvailableForDirSync",this.AvailableForDirSync); //启用外部目录同步:
		doc.replaceItemValue("ListDescription",this.ListDescription); //说明
		doc.replaceItemValue("ListCategory",this.ListCategory); //类别
		doc.replaceItemValue("ListOwner",this.ListOwner);
		doc.replaceItemValue("LocalAdmin",this.LocalAdmin);

		//标志群组并保存
		doc.replaceItemValue("IsAisGroup",this.IsAisGroup);
		doc.replaceItemValue("AisGroupType",this.AisGroupType); //组织架构同步关键字，部门群组_dev
		doc.replaceItemValue("ListName",this.ListName); //群组编号
		doc.replaceItemValue("ListName_Show",this.ListName_Show);//群组名称
		doc.save(true,false);
		this.unid = doc.getUniversalID(); //必须要回传unid
	}

	@Override
	public String getRelationalID() {
		return null; //不支持
	}

	@Override
	public void setRelationalID(String id) {
		//不支持
	}

	@Override
	protected String getByIDViewName() {
		// TODO Auto-generated method stub
		return "Groups";
	}
}
