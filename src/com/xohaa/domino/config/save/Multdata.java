/**
 * 
 */
package com.xohaa.domino.config.save;
import java.util.Vector;
import lotus.domino.NotesException;
import com.xohaa.Action;
import com.xohaa.Base.Func;
import com.xohaa.domino.DocumentSaveimpl;

/**
 * @author chenjianxiong
 *
 */
public class Multdata extends DocumentSaveimpl {
	private String dataType;
	private String dataName_CN;
	private String dataName;
	private Vector<String> dataList;
	private String BeiZhu;
	private String moduleName;
	
	/**
	 * 数据类型
	 * @param dataType
	 */
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * 数据名称
	 * @param dataName_CN
	 */
	public void setDataName_CN(String dataName_CN) {
		this.dataName_CN = dataName_CN;
	}

	/**
	 * 数据关键字
	 * @param dataName
	 */
	public void setDataName(String dataName) {
		this.dataName = dataName;
	}

	/**
	 * 多值数据
	 * @param dataList
	 */
	public void setDataList(Vector<String> dataList) {
		this.dataList = dataList;
	}
	
	/**
	 * 添加一行多值。
	 * @param datalist
	 */
	public void addDataList(String datalist) {
		this.dataList.addElement(datalist);
	}
	/**
	 * 备注
	 * @param beiZhu
	 */
	public void setBeiZhu(String beiZhu) {
		BeiZhu = beiZhu;
	}

	/**
	 * 所属数据库名称
	 * @param moduleName
	 */
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	/**
	 * 系统数据
	 */
	public static String DATATYPE_SYSDATA = "0";
	
	/**
	 * 应用数据
	 */
	public static String DATATYPE_APPDATA = "1";
	
	
	/**
	 * 初始化参数
	 */
	@Override
	protected void initParam(){
		dataType = "";
		dataName_CN = "";
		dataName = "";
		BeiZhu = "";
		moduleName = "";
		dataList = new Vector<String>();
	}
	
	public Multdata(Func F, String dbname) throws Exception {
		super(F, dbname);
	}

	@Override
	public void doSave() throws Exception {
		if(unid != null && !unid.equals("")){
			if(doc == null){
				doc = db.getDocumentByUNID(unid);
			}
		}else{
			doc = db.createDocument();
			doc.replaceItemValue("Form","fmMultdata");
			doc.computeWithForm(false, false);
			doc.replaceItemValue("CreateTime", Action.getCurDataTime());
			doc.replaceItemValue("Sys_Manager", "Sys_Admin").setAuthors(true);
			doc.replaceItemValue("DATA_Reader", "*").setReaders(true);
		}

		this.isNeed("dataName_CN",this.dataName_CN);
		this.isNeed("dataName",this.dataName);
		//this.isNeed("dataList",this.dataList);

		doc.replaceItemValue("dataType", this.dataType);
		doc.replaceItemValue("dataName_CN", this.dataName_CN);
		doc.replaceItemValue("dataName", this.dataName);
		doc.replaceItemValue("dataList", this.dataList);
		doc.replaceItemValue("BeiZhu", this.BeiZhu);
		doc.replaceItemValue("moduleName", this.moduleName);

		doc.save(true,false);
		this.unid = doc.getUniversalID(); //必须要回传unid
		
	}

	@Override
	public void setRelationalID(String id) throws Exception {
		// TODO Auto-generated method stub
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void initParamforDoc() throws NotesException {
		this.dataList = doc.getItemValue("dataList");
		this.dataType = doc.getItemValueString("dataType");
		this.dataName_CN = doc.getItemValueString("dataName_CN");
		this.dataName = doc.getItemValueString("dataName");
		this.BeiZhu = doc.getItemValueString("BeiZhu");
		this.moduleName = doc.getItemValueString("moduleName");
		unid = doc.getUniversalID();
	}

	@Override
	protected String getByIDViewName() {
		return "vw_fmMultdata";
	}

}
