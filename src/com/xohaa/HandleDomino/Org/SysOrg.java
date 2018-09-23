package com.xohaa.HandleDomino.Org;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.DocumentCollection;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xohaa.Opt;
import com.xohaa.Base.Func;
import com.xohaa.Base.HandleData;
public class SysOrg {
	Database OrgDb = null;//组织库
	Database PostDb = null;//岗位库
	Database ZWDb = null;//职位
	Database ConfigDb = null;//数据字典
	Database namesDb = null;
	View groupView = null;

	private RSAEncrypt RS = new RSAEncrypt(3);
	private String org = "/O=org";
	private String comid = "";
	public String getComid() {
		return comid;
	}

	public void setComid(String comid) {
		this.comid = comid;
	}

	public String getDeptParentID() {
		return deptParentID;
	}

	public void setDeptParentID(String deptParentID) {
		this.deptParentID = deptParentID;
	}

	public String getDeptParentName() {
		return deptParentName;
	}

	public void setDeptParentName(String deptParentName) {
		this.deptParentName = deptParentName;
	}

	private String deptParentID= "";
	private String deptParentName = "";
	Func F = null;

	/**
	 * 构造函数，对此类的对象进行初始化
	 * @param se
	 * @throws NotesException
	 */
	public SysOrg(Session se) throws Exception{
		//初始化全局变量
		F = new Func(se);
		setAllDB();
	}

	public SysOrg(Func f) throws Exception{
		F = f;
		setAllDB();
	}
	/**
	 * 初始化此类所用到的数据库对象
	 * @throws NotesException
	 */
	private void setAllDB()throws Exception{
		OrgDb = F.OpenDB(Opt.Org_DBName);
		PostDb = F.OpenDB(Opt.GW_DBName);
		ZWDb = F.OpenDB(Opt.ZW_DBName);
		ConfigDb = F.OpenDB(Opt.Config_DBName);
		namesDb = F.OpenDB(Opt.Names_DBName, false);
		//groupView = Func.openView(namesDb, "Groups");
		groupView = namesDb.getView("Groups");

	}
	/**
	 * 更新names人员
	 * @param docAIS 
	 * @throws NotesException 
	 */
	public void updateNamesPerson(Document docAIS) throws NotesException{
		View view = namesDb.getView("$VIMPeople");
		Document newdoc = view.getDocumentByKey(F.getSession().createName(docAIS.getItemValueString("TxtUserName")).getAbbreviated());
		if(newdoc != null){
			newdoc.replaceItemValue("Form","Person");
			newdoc.replaceItemValue("Type","Person");
			newdoc.replaceItemValue("Owner",docAIS.getItemValueString("TxtUserName"));
			newdoc.replaceItemValue("LastName",docAIS.getItemValueString("PersonName"));
			newdoc.replaceItemValue("FullName","");
			Item item = newdoc.getFirstItem("FullName");
			item.appendToTextList(docAIS.getItemValueString("TxtUserName"));
			item.appendToTextList(docAIS.getItemValueString("PersonName"));
			
			if(!item.containsValue(docAIS.getItemValueString("PeWorkID"))){
				item.appendToTextList(docAIS.getItemValueString("PeWorkID"));
			}

			if(!item.containsValue(docAIS.getItemValueString("Phone"))){
				item.appendToTextList(docAIS.getItemValueString("Phone"));
			}
			newdoc.replaceItemValue("ShortName", newdoc.getItemValue("FullName"));

			newdoc.replaceItemValue("MailSystem","5");
			newdoc.replaceItemValue("MailAddress",docAIS.getItemValueString("Email"));

			newdoc.replaceItemValue("CompanyName",docAIS.getItemValueString("ParentCompanyNumber"));
			newdoc.replaceItemValue("Department",docAIS.getItemValueString("ParentDepartmentNumber"));
			newdoc.replaceItemValue("EmployeeID",docAIS.getItemValueString("PostNumber"));
			newdoc.replaceItemValue("CellPhoneNumber",docAIS.getItemValueString("Phone"));
			newdoc.replaceItemValue("JobTitle",docAIS.getItemValueString("PositionNumber"));
			newdoc.save(true,false);
			if (item != null) item.recycle();
		}else{
			System.out.println("--->找不到用户文档 ");
		}
		if (newdoc != null) newdoc.recycle();
		if (view != null) view.recycle();
	}
	/**
	 * 删除:公司，部门
	 * @param comid 公司编号
	 * @throws NotesException 
	 */
	public void deleteComORG(String comid) throws NotesException{
		View view = OrgDb.getView("vw_Company_byNumber");

		Document tempdoc = view.getDocumentByKey(comid, true);
		if (tempdoc != null){
			System.out.println("删除公司：" + tempdoc.getItemValueString("CompanyName"));
			tempdoc.remove(true);
			tempdoc.recycle();
			F.delDataForViewSkey(groupView, comid);
		}
		view.recycle();
		deleteDepartment(comid);
	}
	/**
	 * 删除部门
	 * @param comid
	 * @throws NotesException
	 */
	public void deleteDepartment(String comid) throws NotesException{
		Document depdoc = null;
		DocumentCollection dc = F.getAllDocumentsBykey(OrgDb, "vw_Department", comid, true);
		if(dc != null){
			depdoc = dc.getFirstDocument();
			while(depdoc != null){
				System.out.println("删除部门：" + depdoc.getItemValueString("DepartmentNumber"));
				deletePosition(depdoc.getItemValueString("DepartmentNumber"));
				F.delDataForViewSkey(groupView, depdoc.getItemValueString("DepartmentNumber"));
				depdoc = dc.getNextDocument(depdoc);
			}
			dc.removeAll(true);
			dc.recycle();
		}
	}

	/**
	 * 删除职位
	 * @param depid
	 * @throws NotesException
	 */
	public void deletePosition(String depid) throws NotesException{
		Document depdoc = null;
		DocumentCollection dc = F.getAllDocumentsBykey(ZWDb, "vw_Position_byDepartment", depid, true);
		if(dc != null){
			depdoc = dc.getFirstDocument();
			while(depdoc != null){
				System.out.println("删除职位：" + depdoc.getItemValueString("PositionNumber"));
				deletePost(depdoc.getItemValueString("PositionNumber"));
				F.delDataForViewSkey(groupView, depdoc.getItemValueString("PositionNumber"));
				depdoc = dc.getNextDocument(depdoc);
			}
			dc.removeAll(true);
			dc.recycle();
		}
	}
	/**
	 * 删除岗位
	 * @param PositionNumber
	 * @throws NotesException
	 */
	public void deletePost(String PositionNumber) throws NotesException{
		Document depdoc = null;
		DocumentCollection dc = F.getAllDocumentsBykey(PostDb, "vw_Post_byPosition", PositionNumber, true);
		if(dc != null){
			depdoc = dc.getFirstDocument();
			while(depdoc != null){
				F.delDataForViewSkey(groupView, depdoc.getItemValueString("PostNumber"));
				System.out.println("删除岗位：" + depdoc.getItemValueString("PostNumber"));
				deletePostSubID(depdoc.getItemValueString("PostNumber"));
				depdoc = dc.getNextDocument(depdoc);
			}
			dc.removeAll(true);
			dc.recycle();
		}
	}

	/**
	 * 删除编制ID
	 * @param postid
	 * @throws NotesException
	 */
	public void deletePostSubID(String postid) throws NotesException{
		Document depdoc = null;
		DocumentCollection dc = F.getAllDocumentsBykey(PostDb, "vw_subPost", postid, true);
		if(dc != null){
			depdoc = dc.getFirstDocument();
			while(depdoc != null){
				F.delDataForViewSkey(groupView, depdoc.getItemValueString("PostNumber"));
				System.out.println("删除编制ID：" + depdoc.getItemValueString("PostNumber"));
				depdoc = dc.getNextDocument(depdoc);
			}
			dc.removeAll(true);
			dc.recycle();
		}
	}

	/**
	 * 根据传入的公司及人员的JSON对象，创建公司、人员等数据，最后创建通讯录信息
	 * 返回一个Json对象，包含公司及人员的编号信息
	 * JSON中，cid是指公司编号，uid是账号全称
	 * @param com
	 * @param js_person
	 * @return CreateFXSCom
	 * @throws NotesException
	 * @throws JSONException
	 */
	public JSONObject CreateFXSCom(JSONObject com,JSONObject js_person)throws Exception, JSONException{
		String rootId = F.getDirkey("分销商公司");
		if (rootId.equals("")){
			rootId = CreateRoot();
		}

		com.put("ParentCompanyNumber", rootId);
		com.put("ParentCompanyNumber_Show", "分销商");
		this.comid = this.CreateCom(com,false);
		setNameGroup(rootId,"分销商","公司群组",this.comid);

		JSONObject d_js = new JSONObject();
		JSONObject reJS = new JSONObject();

		//创建部门,分销商采购部
		d_js.put("ParentCompanyNumber_Show", com.getString("CompanyName"));
		d_js.put("ParentCompanyNumber", this.comid);
		d_js.put("DepartmentName", "分销商采购部");
		String deptID = this.CreateDepat(d_js,false);
		CreateNameGroup(this.comid,com.getString("CompanyName"),deptID,"公司群组_" + F.getCurPATH());

		//创建岗位
		JSONObject p_js = new JSONObject();
		p_js.put("name", js_person.getString("postname"));
		p_js.put("isagent", "1");
		js_person.put("isagent", "1");
		String [] postid = CreatePOST(p_js);

		//创建人员
		String person = CreatePerson_FXS(js_person,this.comid,deptID,postid[0],js_person.getString("postname"));

		reJS.put("cid", this.comid);
		reJS.put("uid",person);
		reJS.put("deptid",deptID);
		CreateNameGroup(deptID,d_js.getString("DepartmentName"),postid[1],"部门群组_" + F.getCurPATH());
		return reJS;
	}

	public String CreateRoot()throws NotesException{
		//1、创建代理商ID
		//2、调用创建数据字典配置
		View view = OrgDb.getView("vw_Company_byNumber");
		String num = getNum(view,"C",4);
		Document docNewDoc = OrgDb.createDocument();
		docNewDoc.replaceItemValue("Form","fmCompany");
		docNewDoc.replaceItemValue("key_unid", getKeyUnid(docNewDoc));
		docNewDoc.replaceItemValue("CompanyNumber", num); //公司编号
		docNewDoc.replaceItemValue("CompanyName","分销商"); //公司名称
		docNewDoc.replaceItemValue("CompanyName_Short", "分销商"); //公司简称
		docNewDoc.replaceItemValue("CompanyName_EN", "Agent");
		docNewDoc.replaceItemValue("ParentCompanyNumber", "RootCompany");
		docNewDoc.replaceItemValue("ParentCompanyNumber_Show", "RootCompany");
		docNewDoc.replaceItemValue("Address", "");
		docNewDoc.replaceItemValue("isagent","1");
		docNewDoc.replaceItemValue("Creater","*");
		docNewDoc.replaceItemValue("CreaterPost","");
		docNewDoc.replaceItemValue("CreaterZW","");
		docNewDoc.getFirstItem("Creater").setAuthors(true);
		docNewDoc.getFirstItem("CreaterPost").setAuthors(true);
		docNewDoc.getFirstItem("CreaterZW").setAuthors(true);
		docNewDoc.replaceItemValue("DATA_READER","*");
		docNewDoc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(docNewDoc);
		docNewDoc.save(true, false);
		docNewDoc.recycle();
		CreateDir("分销商公司",num,"分销商公司");
		setNameGroup(num,"分销商公司","公司群组","");
		return num;
	}

	/**
	 * 读取部门JSON
	 * @param comid
	 * @return JSONArray
	 * @throws Exception 
	 */
	public JSONArray getAllDepatsByComID(String comid) throws Exception{
		JSONArray arr = new JSONArray();
		ViewEntryCollection vc = F.getAllEntriesBykey(OrgDb, "vw_Department", comid, true);
		ViewEntry en = vc.getFirstEntry();
		Document tempdoc = null;
		JSONObject json = null;
		while(en != null){
			json = new JSONObject();
			tempdoc = en.getDocument();
			json.put("id",tempdoc.getItemValueString("DepartmentNumber"));
			json.put("title",tempdoc.getItemValueString("DepartmentName"));
			arr.put(json);
			tempdoc.recycle();
			en = vc.getNextEntry(en);
		}

		return arr;
	}

	/**
	 * 读取公司所有岗位
	 * @param comid
	 * @return JSONArray
	 * @throws Exception
	 */
	public JSONArray getAllPostsByComID(String comid) throws Exception{
		JSONArray arr = new JSONArray();
		ViewEntryCollection vc = F.getAllEntriesBykey(PostDb, "vw_Post_byOrg", comid, true);
		ViewEntry en = vc.getFirstEntry();
		Document tempdoc = null;
		JSONObject json = null;
		while(en != null){
			json = new JSONObject();
			tempdoc = en.getDocument();
			json.put("id",tempdoc.getItemValueString("PostNumber"));
			json.put("title",tempdoc.getItemValueString("PostName"));
			json.put("zwid", tempdoc.getItemValueString("PositionNumber"));
			json.put("depid", tempdoc.getItemValueString("ParentDepartmentNumber"));
			arr.put(json);
			tempdoc.recycle();
			en = vc.getNextEntry(en);
		}

		return arr;
	}
	/**
	 * 创建部门信息
	 * @param js
	 * @throws NotesException
	 * @throws JSONException
	 */
	public String CreateDepat(JSONObject js)throws NotesException, JSONException{
		return this.CreateDepat(js, true);
	}
	/**
	 * 创建部门信息
	 * @param js
	 * @param isCreateNames
	 * @return String
	 * @throws NotesException
	 * @throws JSONException
	 */
	public String CreateDepat(JSONObject js,boolean isCreateNames)throws NotesException, JSONException{
		//创建部门
		View view = OrgDb.getView("vw_Department_byDepartmentNumber");
		String num = getNum(view,"D",6);
		Document docNewDoc = OrgDb.createDocument();
		docNewDoc.replaceItemValue("Form","fmDepartment");
		docNewDoc.replaceItemValue("key_unid", getKeyUnid(docNewDoc));
		docNewDoc.replaceItemValue("DepartmentNumber", num);
		docNewDoc.replaceItemValue("DepartmentName", js.getString("DepartmentName")); 
		docNewDoc.replaceItemValue("DepartmentName_Short", js.getString("DepartmentName"));
		docNewDoc.replaceItemValue("DepartmentName_EN", js.getString("DepartmentName"));
		docNewDoc.replaceItemValue("ParentCompanyNumber", js.getString("ParentCompanyNumber"));
		docNewDoc.replaceItemValue("ParentCompanyNumber_Show", js.getString("ParentCompanyNumber_Show"));
		if(js.has("ParentDepartmentNumber")){
			docNewDoc.replaceItemValue("ParentDepartmentNumber", js.getString("ParentDepartmentNumber"));
			docNewDoc.replaceItemValue("ParentDepartmentNumber_Show", js.getString("ParentDepartmentNumber_Show"));
		}else{
			docNewDoc.replaceItemValue("ParentDepartmentNumber", "RootDepartment");
			docNewDoc.replaceItemValue("ParentDepartmentNumber_Show", "无上级部门");
		}


		docNewDoc.replaceItemValue("Creater",F.getEffectiveUserName());
		docNewDoc.getFirstItem("Creater").setAuthors(true);

		docNewDoc.replaceItemValue("DATA_READER",js.getString("ParentCompanyNumber"));
		docNewDoc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(docNewDoc);
		if(isCreateNames){

			this.CreateNameGroup(num,js.getString("DepartmentName"),"","部门群组_" + F.getCurPATH());

			//this.CreateNameGroup(num,js.getString("DepartmentName"),"","部门群组_" + F.getCurPATH());
			this.setNameGroup(js.getString("ParentCompanyNumber"), js.getString("ParentCompanyNumber_Show"), "公司群组", num);
		}
		docNewDoc.save(true, false);
		docNewDoc.recycle();
		this.deptParentID = num;
		this.deptParentName = js.getString("DepartmentName");
		return num;
	}
	/**
	 * 创建公司
	 * @param com (CompanyNumber(不传则自动创建编号),CompanyName,CompanyName_Short,CompanyName_EN)
	 * ParentCompanyNumber,ParentCompanyNumber_Show 不传则无上级公司
	 * @return String
	 * @throws NotesException
	 */

	public String CreateCom(JSONObject com)throws NotesException{
		return this.CreateCom(com, true);
	}
	/**
	 * 创建公司信息
	 * @param com
	 * @return String
	 */
	public String CreateCom(JSONObject com,boolean isCreateNames)throws NotesException{
		View view = OrgDb.getView("vw_Company_byNumber");
		String num = getNum(view,"C",4);
		Document docNewDoc = OrgDb.createDocument();
		docNewDoc.replaceItemValue("Form","fmCompany");
		docNewDoc.replaceItemValue("key_unid", getKeyUnid(docNewDoc));

		if(com.has("CompanyNumber")){
			docNewDoc.replaceItemValue("CompanyNumber",com.getString("CompanyNumber")); //公司编号
		}else{
			docNewDoc.replaceItemValue("CompanyNumber",num); //公司编号
		}

		docNewDoc.replaceItemValue("CompanyName", com.getString("CompanyName")); //公司名称
		docNewDoc.replaceItemValue("CompanyName_Short",com.getString("CompanyName_Short")); //公司简称
		docNewDoc.replaceItemValue("CompanyName_EN",com.getString("CompanyName_EN"));

		//上级公司
		if(com.has("ParentCompanyNumber")){
			docNewDoc.replaceItemValue("ParentCompanyNumber",com.getString("ParentCompanyNumber"));
			docNewDoc.replaceItemValue("ParentCompanyNumber_Show",com.getString("ParentCompanyNumber_Show"));

			if(com.getString("ParentCompanyNumber").toUpperCase().equals("ROOTCOMPANY")){
				docNewDoc.replaceItemValue("DATA_READER",num).setReaders(true);
			}else{
				docNewDoc.replaceItemValue("DATA_READER",com.getString("ParentCompanyNumber")).setReaders(true);
			}

		}else{
			docNewDoc.replaceItemValue("ParentCompanyNumber","RootCompany");
			docNewDoc.replaceItemValue("ParentCompanyNumber_Show","无上级公司");
			docNewDoc.replaceItemValue("DATA_READER",num).setReaders(true);
		}

		if(com.has("FXSID")){
			docNewDoc.replaceItemValue("Address", com.getString("Address"));
			docNewDoc.replaceItemValue("isagent","1");
			docNewDoc.replaceItemValue("FXSID",com.getString("FXSID"));
			docNewDoc.replaceItemValue("Creater",com.getString("Creater"));
			docNewDoc.replaceItemValue("CreaterPost",com.getString("CreaterPost"));
			docNewDoc.replaceItemValue("CreaterZW",com.getString("CreaterZW"));
			docNewDoc.getFirstItem("Creater").setAuthors(true);
			docNewDoc.getFirstItem("CreaterPost").setAuthors(true);
			docNewDoc.getFirstItem("CreaterZW").setAuthors(true);
		}
		docNewDoc.replaceItemValue("Creater",F.getEffectiveUserName()).setAuthors(true);		
		Func.setPublicFieldForDoc(docNewDoc);
		docNewDoc.save(true, false);
		if(isCreateNames){
			this.CreateNameGroup(num,com.getString("CompanyName"),"","公司群组_" + F.getCurPATH());
			if(com.has("ParentCompanyNumber") && !com.getString("ParentCompanyNumber").toUpperCase().equals("ROOTCOMPANY")){
				this.setNameGroup(com.getString("ParentCompanyNumber"), com.getString("ParentCompanyNumber_Show"), "公司群组", num);
			}
		}
		return num;
	}
	/**
	 * 创建数据字典信息
	 * @param key
	 * @param value
	 * @param MC
	 * @throws NotesException
	 */
	public void CreateDir(String key,String value,String MC)throws NotesException{
		Document tempdoc = ConfigDb.createDocument();
		tempdoc.replaceItemValue("form","fmSysdictionary");
		tempdoc.replaceItemValue("SAVEOPTIONS","1");
		tempdoc.replaceItemValue("ShuJuLB","系统参数配置");
		tempdoc.replaceItemValue("ShuJuLeiXing","0");
		tempdoc.replaceItemValue("ShuJuMC", MC);
		tempdoc.replaceItemValue("ShuJuZ_WB", value);
		tempdoc.replaceItemValue("SSMC", key);
		tempdoc.replaceItemValue("DATA_READER","*");
		tempdoc.getFirstItem("DATA_READER").setAuthors(true);
		Func.setPublicFieldForDoc(tempdoc);
		tempdoc.save();
		if (tempdoc != null)
			tempdoc.recycle();
	}
	/**
	 * 取得生成与的随机10位数
	 * @return String
	 * @throws NotesException
	 */
	public String getMid()throws NotesException{
		String mid = System.currentTimeMillis()+"";
		View view = OrgDb.getView("vw_Person");
		Document tempdoc = view.getDocumentByKey("CN=" + mid + org);
		if (tempdoc != null){
			mid = getMid();
		}
		if(tempdoc != null) {
			tempdoc.recycle();
		}
		if(view != null){
			view.recycle();
		}
		return mid;
	}
	/**
	 * 创建岗位
	 * @param json
	 * @return String [0] 岗位编号  [1] 职位编号
	 * @throws Exception
	 */
	public String[] CreatePOST(JSONObject json) throws Exception{

		String reStr [] = new String[2];
		View view = PostDb.getView("vw_Post");
		String num = getNum(view,"G",6);
		Document doc = PostDb.createDocument();
		doc.replaceItemValue("Form", "fmPost");
		doc.replaceItemValue("key_unid", doc.getUniversalID());
		if(json.has("ParentCompanyNumber")){
			doc.replaceItemValue("ParentCompanyNumber", json.getString("ParentCompanyNumber"));
		}else{
			doc.replaceItemValue("ParentCompanyNumber", comid);
		}

		if(json.has("isagent")){
			doc.replaceItemValue("isagent",json.get("isagent"));// 1是外部组织
		}else{
			doc.replaceItemValue("isagent","");//判断是否外部组织
		}
		doc.replaceItemValue("ParentDepartmentNumber", this.deptParentID);//所属部门
		doc.replaceItemValue("PostName", json.getString("name"));
		doc.replaceItemValue("PostNumber", num);
		doc.replaceItemValue("Creater",F.getEffectiveUserName());
		doc.getFirstItem("Creater").setAuthors(true);
		doc.replaceItemValue("DATA_READER","*");
		doc.getFirstItem("DATA_READER").setReaders(true);

		Func.setPublicFieldForDoc(doc);
		doc.replaceItemValue("PositionNumber",CreatePosition(json,doc));
		doc.replaceItemValue("PositionName", doc.getItemValueString("PostName"));
		reStr[0] = num;
		reStr[1] = doc.getItemValueString("PositionNumber");
		doc.save();

		doc.recycle();
		view.recycle();
		return reStr;
	}

	/**
	 * 创建岗位，不需要创建职位，在names库创建记录
	 * @param json
	 * @return String
	 * @throws Exception
	 */
	public String CreatePOST_OA(JSONObject json) throws Exception{
		return this.CreatePOST_OA(json, true);
	}

	/**
	 * 创建岗位，不需要创建职位，在names库创建记录
	 * @param json
	 * @param isCreateNames true 在names库创建记录
	 * @return String
	 * @throws Exception
	 */
	public String CreatePOST_OA(JSONObject json,boolean isCreateNames) throws Exception{
		View view = PostDb.getView("vw_Post");
		String num = getNum(view,"G",6);
		Document doc = PostDb.createDocument();
		doc.replaceItemValue("Form", "fmPost");
		doc.replaceItemValue("key_unid", doc.getUniversalID());
		doc.replaceItemValue("ParentCompanyNumber", json.getString("ParentCompanyNumber"));
		doc.replaceItemValue("ParentDepartmentNumber", json.getString("ParentDepartmentNumber"));//所属部门
		doc.replaceItemValue("PostName", json.getString("PostName"));
		doc.replaceItemValue("PostNumber", num);
		doc.replaceItemValue("Creater",F.getEffectiveUserName());
		doc.getFirstItem("Creater").setAuthors(true);
		doc.replaceItemValue("DATA_READER","*");
		doc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(doc);

		//外部组织
		if(json.has("isagent")){
			doc.replaceItemValue("isagent",json.get("isagent"));// 1是外部组织
		}else{
			doc.replaceItemValue("isagent","");//判断是否外部组织
		}

		doc.replaceItemValue("PositionNumber",json.getString("PositionNumber"));
		doc.replaceItemValue("PositionName", json.getString("PositionName"));
		if(isCreateNames){
			this.CreateNameGroup(num,json.getString("PostName"),"","岗位群组_" + F.getCurPATH());
			this.setNameGroup(json.getString("PositionNumber"), json.getString("PositionName"), "职位群组", num);
		}
		doc.save();
		doc.recycle();
		view.recycle();
		return num;
	}

	/**
	 * 创建子岗位,写入Names
	 * @param postNumber
	 * @param postName
	 * @param MEMBERS_SHOW
	 * @param MEMBERS
	 * @param isagent
	 * @return String
	 * @throws Exception
	 */
	public String CreatePost_Sub(String postNumber,String postName,String MEMBERS_SHOW,String MEMBERS,String isagent) throws Exception{
		View view = PostDb.getView("vw_subPost_byOrg");
		DocumentCollection alldc = view.getAllDocumentsByKey(postNumber);
		int i = 1;
		if (alldc != null){
			i = alldc.getCount()+1;
		}
		Integer allCount = new Integer(i);
		String num = postNumber+"_"+allCount;
		Document newDoc = PostDb.createDocument();
		newDoc.replaceItemValue("form","fmSubPost");
		newDoc.replaceItemValue("ParentPost",postNumber);
		newDoc.replaceItemValue("PostName",postName);
		newDoc.replaceItemValue("alleditors","*");
		newDoc.replaceItemValue("isUse","Y");
		newDoc.replaceItemValue("isEmpty","N");

		newDoc.replaceItemValue("SortNumber",allCount);
		newDoc.replaceItemValue("PostNumber",num);
		newDoc.replaceItemValue("MEMBERS_SHOW",MEMBERS_SHOW);
		newDoc.replaceItemValue("MEMBERS",MEMBERS);
		newDoc.replaceItemValue("isagent", isagent);
		newDoc.getFirstItem("alleditors").setAuthors(true);
		newDoc.save(true,false);
		ArrayList<String> dMembers = new ArrayList<String>();
		dMembers.add(newDoc.getItemValueString("PostNumber"));
		dMembers.remove(newDoc.getItemValueString("PostNumber"));
		dMembers.add(MEMBERS);

		CreateNameGroup(num,postName,dMembers,"子岗位群组_" + F.getCurPATH());
		this.setNameGroup(postNumber, postName, "岗位群组", num);
		//postNumber
		newDoc.recycle();
		if (view != null) view.recycle();
		if (alldc != null) alldc.recycle();
		return num;
	}

	/**
	 * 创建子岗位,Members不传则为空值。
	 * @param json (PostNumber,PostName,Members_Show,Members)
	 * @param isCreateNames
	 * @return String
	 * @throws Exception
	 */

	public String CreatePost_Sub(JSONObject json,boolean isCreateNames) throws Exception{
		View view = PostDb.getView("vw_subPost_byOrg");
		String postNumber = json.getString("PostNumber");
		DocumentCollection alldc = view.getAllDocumentsByKey(postNumber);

		int i = 1;
		if (alldc != null){
			i = alldc.getCount()+1;
		}
		Document newDoc = PostDb.createDocument();
		newDoc.replaceItemValue("form","fmSubPost");
		newDoc.replaceItemValue("ParentPost",postNumber);
		newDoc.replaceItemValue("PostName",json.getString("PostName"));
		newDoc.replaceItemValue("alleditors","*");
		newDoc.replaceItemValue("isUse","Y");


		if(json.has("Members")){
			newDoc.replaceItemValue("Members_Show",json.getString("Members_Show"));
			newDoc.replaceItemValue("Members",json.getString("Members"));
			newDoc.replaceItemValue("isEmpty","N");
			//MEMBERS = json.getString("Members");
		}else{
			newDoc.replaceItemValue("isEmpty","Y");
			//MEMBERS = "";
			newDoc.replaceItemValue("Members_Show","");
			newDoc.replaceItemValue("Members","");
		}

		Integer allCount = new Integer(i);
		String num = postNumber+"_"+allCount;
		newDoc.replaceItemValue("SortNumber",allCount);
		newDoc.replaceItemValue("PostNumber",num);
		newDoc.getFirstItem("alleditors").setAuthors(true);
		newDoc.save();

		if(isCreateNames){
			this.CreateNameGroup(num,json.getString("PostName"),"","子岗位群组_" + F.getCurPATH());
			this.setNameGroup(postNumber, json.getString("PostName"), "岗位群组", num);
		}
		newDoc.recycle();
		if (view != null) view.recycle();
		if (alldc != null) alldc.recycle();
		return num;
	}
	/**
	 * 创建职位,同步组
	 * @param json
	 * @param pdoc
	 * @return String
	 * @throws Exception
	 */
	public String CreatePosition(JSONObject json,Document pdoc) throws Exception{
		View view = ZWDb.getView("vw_Position");
		String num = getNum(view,"Z",6);
		Document doc = ZWDb.createDocument();
		doc.replaceItemValue("form", "fmPosition");
		doc.replaceItemValue("PositionNumber",num);
		doc.replaceItemValue("PositionName",json.getString("name"));
		doc.replaceItemValue("PositionMemo",json.getString("name"));
		doc.replaceItemValue("ParentOrg",comid +"_"+this.deptParentID);
		doc.replaceItemValue("ParentOrg_Show",this.deptParentName);
		doc.replaceItemValue("ParentLeaders_Show","无上级职位");//暂时设为最顶级职位，再调用职位处理
		doc.replaceItemValue("ParentLeaders","RootPosition");
		doc.replaceItemValue("ParentDepartmentNumber",this.deptParentID);//pdoc.getItemValueString("ParentDepartmentNumber")
		doc.replaceItemValue("ParentCompanyNumber",comid);
		doc.replaceItemValue("key_unid",doc.getUniversalID());

		//级别
		if(json.has("JobNumber")){
			doc.replaceItemValue("JobNumber",json.getString("JobNumber"));
			doc.replaceItemValue("JobName",json.getString("JobName"));
		}else{
			doc.replaceItemValue("JobNumber","");
			doc.replaceItemValue("JobName","");
		}
		if(json.has("isagent")){
			doc.replaceItemValue("isagent",json.get("isagent"));// 1是外部组织
		}else{
			doc.replaceItemValue("isagent","");//判断是否外部组织
		}

		pdoc.replaceItemValue("PositionName", num);//职位名称
		pdoc.replaceItemValue("PositionNumber", json.getString("name"));//职位编码

		doc.replaceItemValue("Creater",F.getEffectiveUserName());
		doc.getFirstItem("Creater").setAuthors(true);

		doc.replaceItemValue("DATA_READER","*");
		doc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(doc);
		//System.out.println("保存职位"+num);
		ArrayList<String> dMembers = new ArrayList<String>();
		dMembers.add(pdoc.getItemValueString("PostNumber"));
		CreateNameGroup(num,json.getString("name"),dMembers,"职位群组_" + F.getCurPATH());
		doc.save();
		doc.recycle();
		view.recycle();
		return num;
	}

	/**
	 * 创建职位,不同步组
	 * @param json  (PositionName,PositionMemo,DepartmentNumber,DepartmentName,CompanyNumber,ParentLeaders_Show,ParentLeaders,isagent)
	 * @return String
	 * @throws Exception
	 */
	public String CreatePosition(JSONObject json) throws Exception{
		return this.CreatePosition(json, true);
	}

	/**
	 * 创建职位,不同步组
	 * @param json
	 * @param isCreateNames
	 * @return String
	 * @throws Exception
	 */
	public String CreatePosition(JSONObject json,boolean isCreateNames) throws Exception{
		View view = ZWDb.getView("vw_Position");
		String num = getNum(view,"Z",6);
		Document doc = ZWDb.createDocument();
		doc.replaceItemValue("form", "fmPosition");
		doc.replaceItemValue("PositionNumber",num);
		doc.replaceItemValue("PositionName",json.getString("PositionName"));
		doc.replaceItemValue("PositionMemo",json.getString("PositionMemo"));

		doc.replaceItemValue("ParentDepartmentNumber",json.getString("DepartmentNumber"));
		doc.replaceItemValue("ParentCompanyNumber",json.getString("CompanyNumber"));
		doc.replaceItemValue("key_unid",getKeyUnid(doc));
		doc.replaceItemValue("ParentOrg",json.getString("CompanyNumber") + "_" + json.getString("DepartmentNumber"));
		doc.replaceItemValue("ParentOrg_Show",json.getString("DepartmentName"));

		//上级
		if(json.has("ParentLeaders")){
			doc.replaceItemValue("ParentLeaders_Show",json.getString("ParentLeaders_Show"));
			doc.replaceItemValue("ParentLeaders",json.getString("ParentLeaders"));
		}else{
			doc.replaceItemValue("ParentLeaders_Show","无上级职位");//暂时设为最顶级职位，再调用职位处理
			doc.replaceItemValue("ParentLeaders","RootPosition");
		}

		//级别
		if(json.has("JobNumber")){
			doc.replaceItemValue("JobNumber",json.getString("JobNumber"));
			doc.replaceItemValue("JobName",json.getString("JobName"));
		}else{
			doc.replaceItemValue("JobNumber","");
			doc.replaceItemValue("JobName","");
		}

		//外部组织
		if(json.has("isagent")){
			doc.replaceItemValue("isagent",json.get("isagent"));// 1是外部组织
		}else{
			doc.replaceItemValue("isagent","");//判断是否外部组织
		}

		if(isCreateNames){
			this.CreateNameGroup(num,json.getString("PositionName"),"","职位群组_" + F.getCurPATH());
			this.setNameGroup(json.getString("DepartmentNumber"), json.getString("DepartmentName"), "部门群组", num);
		}
		doc.replaceItemValue("Creater",F.getEffectiveUserName());
		doc.getFirstItem("Creater").setAuthors(true);
		doc.replaceItemValue("DATA_READER","*");
		doc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(doc);

		doc.save();
		doc.recycle();
		view.recycle();
		return num;
	}
	/**
	 * 创建人员信息
	 * @param js_p
	 * @param comnum
	 * @param depatnum
	 * @param postid
	 * @param postname
	 * @throws NotesException
	 * @throws JSONException
	 */
	public String CreatePerson_FXS(JSONObject js_p,String comnum,String depatnum,String postid,String postname)throws Exception, JSONException{
		//创建人员
		Document docNewDoc = OrgDb.createDocument();
		docNewDoc.replaceItemValue("Form","fmPerson");
		docNewDoc.replaceItemValue("key_unid", getKeyUnid(docNewDoc));
		docNewDoc.replaceItemValue("PersonName",js_p.getString("PersonName"));
		docNewDoc.replaceItemValue("PersonName_EN", js_p.getString("PersonName_EN"));
		docNewDoc.replaceItemValue("PeWorkID", js_p.getString("PeWorkID"));
		docNewDoc.replaceItemValue("Sex", js_p.getString("Sex"));
		docNewDoc.replaceItemValue("Sys_mobile","1"); //同步企业号


		if(js_p.has("isagent")){
			docNewDoc.replaceItemValue("isagent",js_p.get("isagent"));// 1是外部组织
		}else{
			docNewDoc.replaceItemValue("isagent","");//判断是否外部组织
		}

		String mid = getMid();
		docNewDoc.replaceItemValue("TxtUserName", "CN="+mid+org);

		String txtCode = F.getDirkey("用户登陆初始密码");
		if(txtCode == null || "".equals(txtCode.trim())){
			txtCode = "123";
		}

		String code = RS.Encrypt(txtCode,16);
		docNewDoc.replaceItemValue("TXTIDCODE",code);

		docNewDoc.replaceItemValue("SysLoginName", "");
		Item item = docNewDoc.getFirstItem("SysLoginName");
		if(item!=null){
			String prop = F.getDirkey("系统登录名");
			if(prop.indexOf("|")>0){
				String[] arr = prop.split("\\|");
				for(int i=0;i<arr.length;i++){
					if(arr[i].toString().trim().equals("中文名")){
						item.appendToTextList(docNewDoc.getItemValueString("PersonName"));
					}else if(arr[i].toString().trim().equals("工号")){
						item.appendToTextList(docNewDoc.getItemValueString("PeWorkID"));
					}
				}
			}else{
				if(prop.trim().equals("中文名")){
					item.appendToTextList(docNewDoc.getItemValueString("PersonName"));
				}else if(prop.trim().equals("工号")){
					item.appendToTextList(docNewDoc.getItemValueString("PeWorkID"));
				}
			}
		}

		docNewDoc.replaceItemValue("ParentCompanyNumber", comnum);
		docNewDoc.replaceItemValue("ParentDepartmentNumber", depatnum);
		docNewDoc.replaceItemValue("PersonStatus","1");
		docNewDoc.replaceItemValue("Address",js_p.getString("Address"));
		docNewDoc.replaceItemValue("Birthday",js_p.getString("Birthday"));
		docNewDoc.replaceItemValue("Email",js_p.getString("Email"));
		docNewDoc.replaceItemValue("HomeTel",js_p.getString("HomeTel"));
		docNewDoc.replaceItemValue("IDcard",js_p.getString("IDcard"));
		docNewDoc.replaceItemValue("Landline",js_p.getString("Landline"));
		docNewDoc.replaceItemValue("MSN",js_p.getString("MSN"));
		docNewDoc.replaceItemValue("National",js_p.getString("National"));
		docNewDoc.replaceItemValue("Phone",js_p.getString("Phone"));
		docNewDoc.replaceItemValue("QQ",js_p.getString("QQ"));
		docNewDoc.replaceItemValue("isagent","1");
		docNewDoc.replaceItemValue("FXSID", js_p.getString("FXSID"));
		docNewDoc.replaceItemValue("CreaterZW",js_p.getString("CreaterZW"));
		docNewDoc.getFirstItem("CreaterZW").setAuthors(true);
		docNewDoc.replaceItemValue("Language","ZH");
		docNewDoc.replaceItemValue("Creater",F.getEffectiveUserName());
		docNewDoc.getFirstItem("Creater").setAuthors(true);

		docNewDoc.getFirstItem("TxtUserName").setAuthors(true);
		docNewDoc.replaceItemValue("DATA_READER","*");
		docNewDoc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(docNewDoc);
		//docNewDoc.replaceItemValue("UpdateToNames", "1");
		String reStr = "";
		reStr = CreatePost_Sub(postid,postname,js_p.getString("PersonName"),docNewDoc.getItemValueString("TxtUserName"),"1");
		//如果reStr为空时，就报错，也不能保存
		docNewDoc.replaceItemValue("PostName", postname);
		docNewDoc.replaceItemValue("PostNumber", reStr);
		docNewDoc.save(true, false);
		String uid = docNewDoc.getItemValueString("TxtUserName");
		CreateNamePerson(docNewDoc,txtCode);
		docNewDoc.recycle();
		return uid;
	}
	/**
	 * 
	 * @param js_p
	 * @param comnum
	 * @param depatnum
	 * @param postid  
	 * @param postname
	 * @param istrue  true  postid 编制ID  false postid 岗位ID
	 * @return String 返回 人员全称&岗位ID
	 * @throws Exception
	 */
	public String CreatePerson_OA(JSONObject js_p,String comnum,String depatnum,String postid,String postname,boolean istrue)throws Exception{
		//创建人员
		Document docNewDoc = OrgDb.createDocument();
		docNewDoc.replaceItemValue("Form","fmPerson");
		docNewDoc.replaceItemValue("key_unid", getKeyUnid(docNewDoc));
		docNewDoc.replaceItemValue("PersonName",js_p.getString("PersonName"));
		docNewDoc.replaceItemValue("PersonName_EN", js_p.getString("PersonName_EN"));
		docNewDoc.replaceItemValue("PeWorkID", js_p.getString("PeWorkID"));
		docNewDoc.replaceItemValue("Sex", js_p.getString("Sex"));
		docNewDoc.replaceItemValue("Sys_mobile","1"); //同步企业号

		String mid = "CN=" + js_p.getString("PeWorkID") + org;
		HandleData h = new HandleData(OrgDb,"vw_Person");
		if(h.isContainsDataByKeys(mid)){
			h.recycle();
			throw new Exception("["+ mid +"]账号,系统已经存在！");
		}
		docNewDoc.replaceItemValue("TxtUserName",mid);

		String txtCode = null;
		if(!js_p.has("password")){
			txtCode = F.getDirkey("用户登陆初始密码");
			if(txtCode == null || "".equals(txtCode.trim())){
				txtCode = "123";
			}
		}else{
			txtCode = js_p.getString("password");
		}

		String code = RS.Encrypt(txtCode,16);
		docNewDoc.replaceItemValue("TXTIDCODE",code);

		docNewDoc.replaceItemValue("SysLoginName", "");
		Item item = docNewDoc.getFirstItem("SysLoginName");
		if(item!=null){
			String prop = F.getDirkey("系统登录名");


			if(prop.indexOf("|")>0){
				String[] arr = prop.split("\\|");
				for(int i=0;i<arr.length;i++){
					if(arr[i].toString().trim().equals("中文名")){
						item.appendToTextList(docNewDoc.getItemValueString(arr[i].toString().trim()));
					}else if(arr[i].toString().trim().equals("工号")){
						item.appendToTextList(docNewDoc.getItemValueString("PeWorkID"));
					}else{
						String str = docNewDoc.getItemValueString(arr[i].toString().trim());
						if(!str.equals("")){
							item.appendToTextList(str);
						}
					}
				}
			}else{
				if(prop.trim().equals("中文名")){
					item.appendToTextList(docNewDoc.getItemValueString("PersonName"));
				}else if(prop.trim().equals("工号")){
					item.appendToTextList(docNewDoc.getItemValueString("PeWorkID"));
				}else{
					String str = docNewDoc.getItemValueString(prop.trim());
					if(!str.equals("")){
						item.appendToTextList(str);
					}
				}
			}
		}


		docNewDoc.replaceItemValue("ParentCompanyNumber", comnum);
		docNewDoc.replaceItemValue("ParentDepartmentNumber", depatnum);
		docNewDoc.replaceItemValue("PersonStatus","1");
		docNewDoc.replaceItemValue("Address",js_p.getString("Address"));
		docNewDoc.replaceItemValue("Birthday",js_p.getString("Birthday"));
		docNewDoc.replaceItemValue("Email",js_p.getString("Email"));
		docNewDoc.replaceItemValue("HomeTel",js_p.getString("HomeTel"));
		docNewDoc.replaceItemValue("IDcard",js_p.getString("IDcard"));
		docNewDoc.replaceItemValue("Landline",js_p.getString("Landline"));
		docNewDoc.replaceItemValue("MSN",js_p.getString("MSN"));
		docNewDoc.replaceItemValue("National",js_p.getString("National"));
		docNewDoc.replaceItemValue("Phone",js_p.getString("Phone"));
		docNewDoc.replaceItemValue("QQ",js_p.getString("QQ"));
		docNewDoc.replaceItemValue("Language","ZH");
		docNewDoc.replaceItemValue("Creater",F.getEffectiveUserName());
		docNewDoc.getFirstItem("Creater").setAuthors(true);

		docNewDoc.getFirstItem("TxtUserName").setAuthors(true);
		docNewDoc.replaceItemValue("DATA_READER","*");
		docNewDoc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(docNewDoc);
		//docNewDoc.replaceItemValue("UpdateToNames", "1");
		String reStr = "";
		if(istrue){
			ModifyPost(postid,docNewDoc.getItemValueString("TxtUserName"),docNewDoc.getItemValueString("PersonName"));
			reStr = postid;

			docNewDoc.replaceItemValue("PositionNumber",js_p.get("PositionNumber"));

		}else{
			HandleData h2 = new HandleData(PostDb,"vw_Post");
			Document appdoc = h2.getDocumentBykey(postid, true);
			if(appdoc == null){
				throw new Exception("找不到岗位:"+ postid);
			}
			postname = appdoc.getItemValueString("PostName");
			reStr = CreatePost_Sub(postid,postname,docNewDoc.getItemValueString("PersonName"),docNewDoc.getItemValueString("TxtUserName"),"");
			docNewDoc.replaceItemValue("PositionNumber",appdoc.getItemValueString("PositionNumber"));
			h2.recycle();
			postname = postname + "[" + reStr  + "]";
			//ModifyPost(reStr,docNewDoc.getItemValueString("TxtUserName"),docNewDoc.getItemValueString("PersonName"));
		}
		//如果reStr为空时，就报错，也不能保存
		docNewDoc.replaceItemValue("PostNumber", reStr);
		docNewDoc.replaceItemValue("PostName", postname);
		docNewDoc.save(true, false);
		String uid = docNewDoc.getItemValueString("TxtUserName");
		CreateNamePerson(docNewDoc,txtCode);
		docNewDoc.recycle();
		return uid +"&"+reStr;
	}

	/**
	 * 
	 * @param js_p
	 * @param istrue true 编制ID  false 岗位ID
	 * @return String
	 * @throws Exception
	 * @throws JSONException
	 */
	public String CreatePerson_OA(JSONObject js_p,boolean istrue)throws Exception{
		//创建人员
		String postname = null;
		Document docNewDoc = OrgDb.createDocument();
		docNewDoc.replaceItemValue("Form","fmPerson");
		docNewDoc.replaceItemValue("key_unid", getKeyUnid(docNewDoc));
		docNewDoc.replaceItemValue("PersonName",js_p.getString("PersonName"));
		docNewDoc.replaceItemValue("PersonName_EN", js_p.getString("PersonName_EN"));
		docNewDoc.replaceItemValue("PeWorkID", js_p.getString("PeWorkID"));
		docNewDoc.replaceItemValue("Sex", js_p.getString("Sex"));
		docNewDoc.replaceItemValue("Sys_mobile","1"); //同步企业号

		String mid = "CN=" + js_p.getString("PeWorkID") + org;
		HandleData h = new HandleData(OrgDb,"vw_Person");
		if(h.isContainsDataByKeys(mid)){
			h.recycle();
			throw new Exception("["+ mid +"]账号,系统已经存在！");
		}
		docNewDoc.replaceItemValue("TxtUserName",mid);

		String txtCode = null;
		if(!js_p.has("password")){
			txtCode = F.getDirkey("用户登陆初始密码");
			if(txtCode == null || "".equals(txtCode.trim())){
				txtCode = "123";
			}
		}else{
			txtCode = js_p.getString("password");
		}

		String code = RS.Encrypt(txtCode,16);
		docNewDoc.replaceItemValue("TXTIDCODE",code);

		docNewDoc.replaceItemValue("SysLoginName", "");
		Item item = docNewDoc.getFirstItem("SysLoginName");
		if(item!=null){
			String prop = F.getDirkey("系统登录名");


			if(prop.indexOf("|")>0){
				String[] arr = prop.split("\\|");
				for(int i=0;i<arr.length;i++){
					if(arr[i].toString().trim().equals("中文名")){
						item.appendToTextList(docNewDoc.getItemValueString(arr[i].toString().trim()));
					}else if(arr[i].toString().trim().equals("工号")){
						item.appendToTextList(docNewDoc.getItemValueString("PeWorkID"));
					}else{
						String str = docNewDoc.getItemValueString(arr[i].toString().trim());
						if(!str.equals("")){
							item.appendToTextList(str);
						}
					}
				}
			}else{
				if(prop.trim().equals("中文名")){
					item.appendToTextList(docNewDoc.getItemValueString("PersonName"));
				}else if(prop.trim().equals("工号")){
					item.appendToTextList(docNewDoc.getItemValueString("PeWorkID"));
				}else{
					String str = docNewDoc.getItemValueString(prop.trim());
					if(!str.equals("")){
						item.appendToTextList(str);
					}
				}
			}
		}


		docNewDoc.replaceItemValue("ParentCompanyNumber", js_p.getString("ParentCompanyNumber"));
		docNewDoc.replaceItemValue("ParentDepartmentNumber", js_p.getString("ParentDepartmentNumber"));
		docNewDoc.replaceItemValue("PersonStatus","1");
		docNewDoc.replaceItemValue("Address",js_p.getString("Address"));
		docNewDoc.replaceItemValue("Birthday",js_p.getString("Birthday"));
		docNewDoc.replaceItemValue("Email",js_p.getString("Email"));
		docNewDoc.replaceItemValue("HomeTel",js_p.getString("HomeTel"));
		docNewDoc.replaceItemValue("IDcard",js_p.getString("IDcard"));
		docNewDoc.replaceItemValue("Landline",js_p.getString("Landline"));
		docNewDoc.replaceItemValue("MSN",js_p.getString("MSN"));
		docNewDoc.replaceItemValue("National",js_p.getString("National"));
		docNewDoc.replaceItemValue("Phone",js_p.getString("Phone"));
		docNewDoc.replaceItemValue("QQ",js_p.getString("QQ"));
		docNewDoc.replaceItemValue("Language","ZH");
		docNewDoc.replaceItemValue("Creater",F.getEffectiveUserName());
		docNewDoc.getFirstItem("Creater").setAuthors(true);

		docNewDoc.getFirstItem("TxtUserName").setAuthors(true);
		docNewDoc.replaceItemValue("DATA_READER","*");
		docNewDoc.getFirstItem("DATA_READER").setReaders(true);
		Func.setPublicFieldForDoc(docNewDoc);
		docNewDoc.replaceItemValue("UpdateToNames", "1");
		String reStr = "";
		if(istrue){
			ModifyPost(js_p.getString("PostNumber"),docNewDoc.getItemValueString("TxtUserName"),docNewDoc.getItemValueString("PersonName"));
			reStr = js_p.getString("PostNumber");
			docNewDoc.replaceItemValue("PositionNumber",js_p.get("PositionNumber"));

		}else{
			HandleData h2 = new HandleData(PostDb,"vw_Post");
			Document appdoc = h2.getDocumentBykey(js_p.getString("PostNumber"), true);
			if(appdoc == null){
				throw new Exception("无法找到[" + js_p.getString("PostNumber") + "]对应的岗位！");
			}
			postname = appdoc.getItemValueString("PostName");
			reStr = CreatePost_Sub(js_p.getString("PostNumber"),postname,docNewDoc.getItemValueString("PersonName"),docNewDoc.getItemValueString("TxtUserName"),"");
			docNewDoc.replaceItemValue("PositionNumber",appdoc.getItemValueString("PositionNumber"));
			h2.recycle();
			postname = postname + "[" + reStr  + "]";
			//ModifyPost(reStr,docNewDoc.getItemValueString("TxtUserName"),docNewDoc.getItemValueString("PersonName"));
		}

		//如果reStr为空时，就报错，也不能保存
		docNewDoc.replaceItemValue("PostNumber", reStr);
		docNewDoc.replaceItemValue("PostName", postname);
		docNewDoc.save(true, false);
		String uid = docNewDoc.getItemValueString("TxtUserName");
		CreateNamePerson(docNewDoc,txtCode);
		docNewDoc.recycle();
		return uid +"&"+reStr;
	}
	/**
	 * 创建或是添加群组成员，群组如果存在则添加，不存在则创建。
	 * @param key 编号
	 * @param key_show 范围，群组。
	 * @param member 添加群组内的节点
	 * @throws NotesException
	 */
	public void setNameGroup(String key,String key_show,String group,String member)throws NotesException{
		Document tempdoc = groupView.getDocumentByKey(key);
		if (tempdoc == null){
			tempdoc = namesDb.createDocument();
			CreateNamesGroupDoc(tempdoc,key,key_show,group +"_" + F.getCurPATH());
		}
		if("".equals(tempdoc.getItemValueString("Members"))){
			tempdoc.replaceItemValue("Members", member);
		}else{
			tempdoc.getFirstItem("Members").appendToTextList(member);
		}
		tempdoc.save(true,false);
		if (tempdoc != null) tempdoc.recycle();

	}
	/**
	 * 创建或是添加群组成员，群组如果存在则添加，不存在则创建。
	 * @param key
	 * @param key_show
	 * @param group
	 * @param members
	 * @throws NotesException
	 */
	public void setNameGroup(String key,String key_show,String group,Vector<String> members)throws NotesException{
		Document tempdoc = groupView.getDocumentByKey(key);
		if (tempdoc == null){
			tempdoc = namesDb.createDocument();
			CreateNamesGroupDoc(tempdoc,key,key_show,group +"_" + F.getCurPATH());
		}
		if("".equals(tempdoc.getItemValueString("Members"))){
			tempdoc.replaceItemValue("Members", members);
		}else{
			tempdoc.getFirstItem("Members").appendToTextList(members);
		}
		tempdoc.save(true,false);
		if (tempdoc != null) tempdoc.recycle();
	}
	/**
	 * 创建通讯录群组
	 * @param newdoc
	 * @param key
	 * @param key_show
	 * @throws NotesException
	 */
	private void CreateNamesGroupDoc(Document newdoc,String key,String key_show,String grouptype)throws NotesException{
		Item item = null;
		newdoc.replaceItemValue("Form","Group");
		newdoc.replaceItemValue("Members","");
		newdoc.replaceItemValue("Type","Group");
		newdoc.replaceItemValue("AvailableForDirSync","1");
		newdoc.replaceItemValue("GroupTitle","0");
		newdoc.replaceItemValue("GroupType","0");
		newdoc.replaceItemValue("InternetAddress","");
		newdoc.replaceItemValue("ListDescription",key_show);
		newdoc.replaceItemValue("ListCategory",grouptype); 
		newdoc.replaceItemValue("DocumentAccess","[GroupModifier]");
		newdoc.replaceItemValue("ListOwner","*");
		newdoc.replaceItemValue("LocalAdmin","*");

		//增加读者作者权限
		item = newdoc.getFirstItem("DocumentAccess" );
		item.isAuthors();
		item.isReaders();
		item = newdoc.getFirstItem( "ListOwner" );
		item.isAuthors();
		item.isReaders();
		item = newdoc.getFirstItem( "LocalAdmin" );
		item.isAuthors();
		item.isReaders();

		//标志群组并保存
		newdoc.replaceItemValue("IsAisGroup","1");
		newdoc.replaceItemValue("AisGroupType",grouptype);
		newdoc.replaceItemValue("ListName",key); //群组名称
		newdoc.replaceItemValue("ListName_Show",key_show);
		if (item != null) item.recycle();
	}

	/**
	 * 创建通讯录群组，添加成员
	 * @param key
	 * @param key_show
	 * @throws NotesException
	 */
	public void CreateNameGroup(String key,String key_show,Vector<String> allMembers,String groupType)throws NotesException{
		Document newdoc = groupView.getDocumentByKey(key);
		if(newdoc == null){
			newdoc = namesDb.createDocument();		
			CreateNamesGroupDoc(newdoc,key,key_show,groupType);
			newdoc.replaceItemValue("Members", allMembers);
		}else{
			Item item = newdoc.getFirstItem("Members");
			item.appendToTextList(allMembers);
		}


		newdoc.save(true,false);
		if (newdoc != null) newdoc.recycle();
	}
	/**
	 * 创建通讯录群组，添加成员
	 * @param key
	 * @param key_show
	 * @param allMembers
	 * @param groupType
	 * @throws NotesException
	 */
	public void CreateNameGroup(String key,String key_show,ArrayList<String> allMembers,String groupType)throws NotesException{
		Document newdoc = groupView.getDocumentByKey(key);
		if(newdoc == null){
			newdoc = namesDb.createDocument();
			CreateNamesGroupDoc(newdoc,key,key_show,groupType);
			Vector<String> vce = new Vector<String>();
			vce.addAll(allMembers);
			newdoc.replaceItemValue("Members", vce);
		}else{
			Item item = newdoc.getFirstItem("Members");
			Vector<String> vce = new Vector<String>();
			vce.addAll(allMembers);
			item.appendToTextList(vce);
		}

		newdoc.save(true,false);
		if (newdoc != null) newdoc.recycle();
	}
	/**
	 * 创建通讯录群组，添加成员
	 * @param key
	 * @param key_show
	 * @throws NotesException
	 */
	public void CreateNameGroup(String key,String key_show,String Member,String groupType)throws NotesException{
		Document newdoc = groupView.getDocumentByKey(key);
		if(newdoc == null){
			newdoc = namesDb.createDocument();
			CreateNamesGroupDoc(newdoc,key,key_show,groupType);
			newdoc.replaceItemValue("Members", Member);
		}else{
			Item item = newdoc.getFirstItem("Members");
			item.appendToTextList(Member);
		}
		newdoc.save(true,false);
		if (newdoc != null) newdoc.recycle();
	}

	/**
	 * 创建通讯录人员信息
	 * @param docAIS
	 * @throws NotesException
	 */
	public void CreateNamePerson(Document docAIS,String strpassword)throws NotesException{
		//创建人员
		Document newdoc = namesDb.createDocument();
		Item item = null;
		newdoc.replaceItemValue("Form","Person");
		newdoc.replaceItemValue("Type","Person");
		newdoc.replaceItemValue("Owner",docAIS.getItemValueString("TxtUserName"));
		newdoc.replaceItemValue("FullName","");
		newdoc.replaceItemValue("LastName",docAIS.getItemValueString("PersonName"));
		item = newdoc.getFirstItem("FullName");
		item.appendToTextList(docAIS.getItemValueString("TxtUserName"));
		item.appendToTextList(docAIS.getItemValueString("PersonName"));
		
		if(!item.containsValue(docAIS.getItemValueString("PeWorkID"))){
			item.appendToTextList(docAIS.getItemValueString("PeWorkID"));
		}

		if(!item.containsValue(docAIS.getItemValueString("Phone"))){
			item.appendToTextList(docAIS.getItemValueString("Phone"));
		}
		
		newdoc.replaceItemValue("ShortName", newdoc.getItemValue("FullName"));
		//String strpassword = RS.UnEncrypt(docAIS.getItemValueString("TXTIDCODE"),16);
		if (!strpassword.equals("")){
			newdoc.replaceItemValue("HTTPPassword",F.getSession().hashPassword(strpassword));
		}else{
			newdoc.replaceItemValue("HTTPPassword",F.getSession().hashPassword("123"));	
		}

		newdoc.replaceItemValue("MailSystem","5");
		newdoc.replaceItemValue("MailAddress",docAIS.getItemValueString("Email"));
		newdoc.computeWithForm(false, false);

		newdoc.replaceItemValue("CompanyName",docAIS.getItemValueString("ParentCompanyNumber"));
		newdoc.replaceItemValue("Department",docAIS.getItemValueString("ParentDepartmentNumber"));
		newdoc.replaceItemValue("EmployeeID",docAIS.getItemValueString("PostNumber"));
		newdoc.replaceItemValue("CellPhoneNumber",docAIS.getItemValueString("Phone"));
		newdoc.replaceItemValue("JobTitle",docAIS.getItemValueString("PositionNumber"));
		newdoc.save(true,false);
		if (item != null) item.recycle();
		if (newdoc != null) newdoc.recycle();
	}

	/**
	 * 根据规则生成编号
	 * @param n
	 * @param key
	 * @return String
	 */
	public String getNumber(int n,int key){
		String num = "";
		num = key+"";
		int j = num.length();
		for (int x=0;x<n-j;x++){
			num = "0" + num;
		}
		return num;
	}
	/**
	 * 获取编码
	 * @param view
	 * @param type
	 * @param n
	 * @return String
	 * @throws NotesException
	 */
	@SuppressWarnings("unused")
	public String getNum(View view,String type,int n) throws NotesException{
		Document tempdoc = null;
		String num = type + F.getNumber(type, n);//getNumber(n,key);
		tempdoc = view.getDocumentByKey(num);
		if(tempdoc != null){
			return getNum(view,type,n);			
		}
		if (tempdoc != null) tempdoc.recycle();
		return num;
	}
	/**
	 * 保存编码
	 * @param db
	 * @param key
	 * @param num
	 * @throws NotesException
	 */
	public void SaveNum(Database db,String key,int num) throws NotesException {
		Document tempdoc = null;
		View view = db.getView("vw_getNumber");
		tempdoc = view.getDocumentByKey(key);
		if (tempdoc != null){
			Item item = tempdoc.getFirstItem("NUMBER");
			item.setValueInteger(num);
			tempdoc.save(true,false);
			if(item != null) item.recycle();
			if (tempdoc != null) tempdoc.recycle();
		}//这里不考虑是否存在配置，系统初始化时就一定会有配置
		if(view != null) view.recycle();
	}
	/**
	 * 生成文档UNID
	 * @param doc
	 * @return String
	 * @throws NotesException
	 */
	private String getKeyUnid(Document doc) throws NotesException{
		String s = doc.getUniversalID();

		//(@Integer(@Random * 100000));
		Random r = new Random(1);
		s = s + String.valueOf(Math.round(r.nextInt()*100000));

		return s;
	}
	/**
	 * 根据传入公司Json对象，修改组织库的公司信息，
	 * CompanyNumber为关键字修改
	 * @param js
	 * @return boolean 返回ture为修改成功，false为修改失败
	 */
	public boolean ModifyCompany(JSONObject js)throws NotesException{
		Document docNewDoc = null;
		View view = OrgDb.getView("vw_Company_byNumber");
		try {
			docNewDoc = view.getDocumentByKey(js.getString("CompanyNumber"));
			if (docNewDoc != null){
				docNewDoc.replaceItemValue("CompanyName", js.getString("CompanyName"));
				docNewDoc.replaceItemValue("CompanyName_Short", js.getString("CompanyName_Short")); //公司简����
				docNewDoc.replaceItemValue("CompanyName_EN", js.getString("CompanyName_EN"));
				docNewDoc.replaceItemValue("Address", js.getString("Address"));
				docNewDoc.save();
				return true;
			}else{
				return false;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
	}

	/**
	 * 根据传入人员全称、名称、岗位ID
	 * 修改的关键字是岗位关联的账号全称、名称、岗位ID、岗位的状态
	 * 返回ture为修改成功，false为修改失败
	 * @return boolean
	 */
	public boolean ModifyPost(String postnumber,String user,String username) throws NotesException{
		Document postdoc = null;
		View view = PostDb.getView("vw_subPost");
		try {
			postdoc = view.getDocumentByKey(postnumber);
			if(postdoc != null){
				postdoc.replaceItemValue("isEmpty", "N");
				postdoc.replaceItemValue("Members",user);
				postdoc.replaceItemValue("Members_Show",username);
				postdoc.save(true,false);
			}
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} 
	}

	/**
	 * 根据传入人员的json对象，修改组织库的人员信息
	 * 修改的关键字是人员的账号全称 ,所有的字段都可以修改并且必须传到此函数
	 * @param js_p
	 * @return boolean 返回ture为修改成功，false为修改失败
	 * @throws NotesException
	 */
	public boolean ModifyPerson(JSONObject js_p) throws NotesException{
		Document docNewDoc = null;
		View view = OrgDb.getView("vw_Person");
		docNewDoc = view.getDocumentByKey(js_p.getString("TxtUserName"));
		if (docNewDoc != null){
			docNewDoc.replaceItemValue("PersonName",js_p.getString("PersonName"));
			docNewDoc.replaceItemValue("PersonName_EN", js_p.getString("PersonName_EN"));
			docNewDoc.replaceItemValue("PeWorkID", js_p.getString("PeWorkID"));
			docNewDoc.replaceItemValue("Sex", js_p.getString("Sex"));
			docNewDoc.replaceItemValue("Address",js_p.getString("Address"));
			docNewDoc.replaceItemValue("Birthday",js_p.getString("Birthday"));
			docNewDoc.replaceItemValue("Email",js_p.getString("Email"));
			docNewDoc.replaceItemValue("HomeTel",js_p.getString("HomeTel"));
			docNewDoc.replaceItemValue("IDcard",js_p.getString("IDcard"));
			docNewDoc.replaceItemValue("Landline",js_p.getString("Landline"));
			docNewDoc.replaceItemValue("MSN",js_p.getString("MSN"));
			docNewDoc.replaceItemValue("National",js_p.getString("National"));
			docNewDoc.replaceItemValue("Phone",js_p.getString("Phone"));
			docNewDoc.replaceItemValue("QQ",js_p.getString("QQ"));
			docNewDoc.save(true,false);
			this.updateNamesPerson(docNewDoc);
			docNewDoc.recycle();
			view.recycle();
			return true;
		}else{
			return false;
		}

	}


	/**
	 * 重新生成组织
	 * @param js_person
	 * @param CompanyNumber
	 * @param CompanyName
	 * @throws Exception
	 * @throws JSONException
	 */
	public void UpdateFXSDetBySQL(JSONObject js_person,String CompanyNumber,String CompanyName)throws Exception, JSONException{
		ArrayList<String> allMembers = new ArrayList<String>();
		JSONObject d_js = new JSONObject();
		//创建部门
		//1、总经办
		d_js.put("ParentCompanyNumber_Show", CompanyName);
		d_js.put("ParentCompanyNumber", CompanyNumber);
		d_js.put("DepartmentName", "总经办");
		String deptID = CreateDepat(d_js,false);
		allMembers.add(deptID);
		if (deptID != null){

			//创建岗位
			JSONObject p_js = new JSONObject();
			p_js.put("name", js_person.getString("postname"));
			String [] postid = CreatePOST(p_js);

			Document docNewDoc = F.getDocumentBykey(OrgDb, "vw_Person",js_person.getString("TxtUserName"),true);
			docNewDoc.replaceItemValue("ParentCompanyNumber", CompanyNumber);
			docNewDoc.replaceItemValue("ParentDepartmentNumber", deptID);
			docNewDoc.replaceItemValue("PositionNumber", postid[1]);
			String reStr = CreatePost_Sub(postid[0],js_person.getString("postname"),js_person.getString("PersonName"),docNewDoc.getItemValueString("TxtUserName"),"1");
			docNewDoc.replaceItemValue("PostName", js_person.getString("postname"));
			docNewDoc.replaceItemValue("PostNumber", reStr);
			docNewDoc.save(true, false);
			docNewDoc.recycle();
		}
	}

	/**
	 * 清空对象
	 */
	public void recycle(){
		try{
			if(OrgDb != null) OrgDb.recycle();
			if(PostDb != null) PostDb.recycle();
			if(ZWDb != null) ZWDb.recycle();
			//if(RoleDb != null) RoleDb.recycle();
			if(ConfigDb != null) ConfigDb.recycle();
			if(groupView != null) groupView.recycle();
			if(namesDb != null) namesDb.recycle();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
