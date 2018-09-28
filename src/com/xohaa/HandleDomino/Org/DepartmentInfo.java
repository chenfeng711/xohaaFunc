package com.xohaa.HandleDomino.Org;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;

import com.xohaa.Base.Func;

public class DepartmentInfo {
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
	public DepartmentInfo(Func F,String departmentnumber) throws NotesException{
		this.orgdb = F.OpenDB("Sys_org.nsf");
		this.F = F;
		this.initParam(departmentnumber);
	}
	/**
	 * 
	 * @param postid
	 * @throws NotesException
	 */
	public void initParam(String departmentnumber) throws NotesException{
		this.psonview = Func.openView(orgdb, "vw_Department_byDepartmentNumber");
		this.orgdoc = F.getDocumentBykey(psonview,departmentnumber, true);
	}
	/**
	 * 
	 * @param F
	 * @throws NotesException
	 */
	public DepartmentInfo(Func F) throws NotesException{
		this.orgdb = F.OpenDB("Sys_org.nsf");
		this.F = F;
	}
	
	/**
	 * 
	 */
	public void recycle(){
		try {
			if(psonview != null) psonview.recycle();
			if(orgdoc != null) orgdoc.recycle();
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
	public Document getDocument() {
		return orgdoc;
	}
	
	/**
	 * 部门领导
	 * @return
	 * @throws NotesException
	 */
	public String getDepartmentLeader() throws NotesException{
		return this.getDocItemValue("DepartmentLeader");
	}
	
	/**
	 * 分管领导
	 * @return
	 * @throws NotesException
	 */
	public String getFDepartmentLeader() throws NotesException{
		return this.getDocItemValue("fDepartmentLeader");
	}
	
	/**
	 * 部门名称
	 * @return
	 * @throws NotesException
	 */
	public String getDepartmentName() throws NotesException{
		return this.getDocItemValue("DepartmentName");
	}
	
	/**
	 * 读取文档字段
	 * @param name
	 * @return
	 * @throws NotesException
	 */
	private String getDocItemValue(String name) throws NotesException{
		return orgdoc.getItemValueString(name);
	}
}
