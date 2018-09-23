package com.xohaa.HandleDomino.Org;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import com.xohaa.Base.Func;

public class PersionInfo {
	private Database orgdb = null;
	private Document orgdoc = null;
	private View psonview = null;
	private Func F = null;
	/**
	 * 
	 * @param F
	 * @param postid
	 * @throws NotesException
	 */
	public PersionInfo(Func F,String postid) throws NotesException{
		this.orgdb = F.OpenDB("Sys_org.nsf");
		this.F = F;
		this.initPersion(postid);
	}
	/**
	 * 
	 * @param postid
	 * @throws NotesException
	 */
	public void initPersion(String postid) throws NotesException{
		this.psonview = Func.openView(orgdb, "vw_Person_byPost");
		this.orgdoc = F.getDocumentBykey(psonview,postid, true);
	}
	/**
	 * 
	 * @param F
	 * @throws NotesException
	 */
	public PersionInfo(Func F) throws NotesException{
		this.orgdb = F.OpenDB("Sys_org.nsf");
		this.F = F;
	}
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public View getPsonview() throws NotesException {
		if(psonview == null){
			this.psonview = Func.openView(orgdb, "vw_Person_byPost");
		}
		return psonview;
	}
	
	/**
	 * 
	 */
	public void recycle(){
		try {
			if(orgdoc != null) orgdoc.recycle();
			if(psonview != null) psonview.recycle();
			if(!F.getCurDBFileName().equalsIgnoreCase("sys_org.nsf")){
				orgdb.recycle();
			}
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @return
	 */
	public Document getOrgdoc() {
		return orgdoc;
	}
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getPersionName() throws NotesException{
		return this.getOrgItemValue("PersonName");
	}
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getCNUserName() throws NotesException{
		return this.getOrgItemValue("TXTUSERNAME");
	}
	
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getPostName() throws NotesException{
		return this.getOrgItemValue("PostName");
	}
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getPositionNumber() throws NotesException{
		return this.getOrgItemValue("PositionNumber");
	}
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getCompanyNumber() throws NotesException{
		return this.getOrgItemValue("ParentCompanyNumber");
	}
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getDepartmentNumber() throws NotesException{
		return this.getOrgItemValue("ParentDepartmentNumber");
	}
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getCompanyName() throws NotesException{
		return F.getItemValueString(orgdb,"vw_Company_byNumber", orgdoc.getItemValueString("ParentCompanyNumber"),"CompanyName");
	}
	
	/**
	 * 
	 * @return
	 * @throws NotesException
	 */
	public String getDepartmentName() throws NotesException{
		return F.getItemValueString(orgdb,"vw_Department_byDepartmentNumber", orgdoc.getItemValueString("ParentDepartmentNumber"),"DepartmentName");
	}
	/**
	 * 
	 * @param name
	 * @return
	 * @throws NotesException
	 */
	public String getOrgItemValue(String name) throws NotesException{
		return orgdoc.getItemValueString(name);
	}
}
