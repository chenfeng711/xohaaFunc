package com.xohaa.tablelist.excel;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.xohaa.ListArray.Field;
import com.xohaa.ListArray.FieldList;
import com.xohaa.SQLDB.HandleSQLCon;
import com.xohaa.tablelist.TableListBase;

public class ImportExcelData {
	//准备废弃
	private Document configDoc = null;
	private View fieldView = null;
	private Vector<String> fieldnames = null; 

	private Vector<String> fieldtitles = null;
	private FieldList fl = null;
	private HandleSQLCon F = null;
	private int index = 1;


	private int successCount = 0;
	private int cellNum = 0;
	private StringBuffer msgerr = null;
	private ArrayList<?> rows = null;

	private DecimalFormat df = null; 
	private String listkey_unid = null; //列表key_unid
	private String datatype = null; //数据源
	private String parentid = null; //从表时要写入parentid
	private Database tdb = null;
	private Document curdoc = null;
	/**
	 * 
	 * @param session
	 * @throws Exception 
	 */
	public ImportExcelData(Session session) throws Exception{
		F = new HandleSQLCon(session);
		tdb = F.OpenDB("Sys_Tablelist.nsf");
		curdoc = F.getCurDoc();
		configDoc = tdb.getDocumentByUNID(F.getCurDoc().getItemValueString("dataname_unid"));
		df = new DecimalFormat("#");

		fieldnames = new Vector<String>();
		fieldtitles = new Vector<String>();
		//数据源
		datatype = configDoc.getItemValueString("DataType");

		//参数列表数据
		if (!configDoc.getItemValueString("YYKEY_UNID").equals("")){
			listkey_unid = configDoc.getItemValueString("YYKEY_UNID");
		}else{
			listkey_unid = configDoc.getItemValueString("key_UNID");
		}

		//从表ID
		if(configDoc.getItemValueString("TableListType").equals("2")){ // 从表
			if(F.getCurDoc().getItemValueString("parentID").equals("")){
				throw new Exception("parentID为空！");
			}else{
				parentid = F.getCurDoc().getItemValueString("parentID");
			}
		}
	}


	/**
	 * 
	 * @return HandleSQLCon
	 */
	public HandleSQLCon getF() {
		return F;
	}

	/**
	 * 
	 * @param f
	 */
	public void setF(HandleSQLCon f) {
		F = f;
	}
	/**
	 * 获取所有行。
	 * @return ArrayList
	 */
	public ArrayList<?> getRows() {
		return rows;
	}
	/**
	 * 
	 * @return Document
	 */
	public Document getCurdoc() {
		return curdoc;
	}

	/**
	 * 
	 * @param fieldname
	 * @return int
	 */
	public int getFieldIndex(String fieldname){
		return fieldnames.indexOf(fieldname);
	}
	
	/**
	 * 提交数据
	 */
	public void CommitData(){
		if (datatype.equals("2")){
			F.Commit();
		}
	}

	/**
	 * 释放
	 */
	public void recycle(){
		try {
			if (fieldView != null) fieldView.recycle();
			if (configDoc != null ) configDoc.recycle();
			if (curdoc != null ) curdoc.recycle();
			if (tdb != null ) tdb.recycle();

			if (datatype.equals("2")){
				F.recycleCon();
			}else{
				F.recycleFunc();
			}

		} catch (NotesException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 检查导入模板
	 * @param xr
	 * @return String
	 */
	public String checkTemplate(XSSFRow xr){
		XSSFCell xc = null;
		//System.out.println(fieldtitles.toString());
		for(int j=0;j<cellNum;j++){ //列
			xc = xr.getCell(j);
			if(xc!= null && xc.getCellType() == XSSFCell.CELL_TYPE_STRING){
				String value = xc.getStringCellValue().replaceAll("\\*", "");
				//String a[] = value.split("(");
				
				if(!fieldtitles.contains(value)){
					return "第"+ (j+1) + "列：" + value;
				}
			}
		}
		return "";
	}
	/**
	 * 导入数据
	 * @throws NotesException 
	 * 
	 */
	public boolean imputData() throws NotesException {
		try {

			if(datatype.equals("2")){
				F.SQLConnection(false);
			}

			//获取表单配置字段
			fl = getFieldList();
			if( cellNum == 0){
				throw new Exception("找不到配置的字段！");
			}

			//读取Excel数据写入数组。
			rows = readDataByXSSFWorkbook();

			//写入
			if(msgerr.length() == 0){
				if(datatype.equals("2")){
					if("oracle".equalsIgnoreCase(F.getDBType())){
						handleOra();
					}else{
						handleSQL();
					}

				}else{
					handleDomino(curdoc);
				}
			}else{
				throw new Exception(msgerr.toString());
			}


			curdoc.replaceItemValue("failedScore","成功导入<span style='color:red'> " + successCount + " </span>条记录！");
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			if(datatype.equals("2")){
				F.rollback();
			}

			try {
				curdoc.replaceItemValue("failedScore", "<span style='color:red'>" + TableListBase.returnErrMsg(configDoc, e) + "</span>");
			} catch (NotesException e1) {
				// TODO 自动生成 catch ??1??7????			
				e1.printStackTrace();
			}
			return false;
		}

	}

	/**
	 * 导入domino数据
	 * @param eo
	 * @throws Exception
	 */
	private void handleDomino(Document doc) throws Exception{
		Database dataDB = null;
		//处理数据库
		String dbname = doc.getItemValueString("dbname");
		if(dbname.equals("")){
			//当前表单的数据库，读取数据配置
			dbname = configDoc.getItemValueString("dbname");
			if(configDoc.getItemValueString("isCurPath").equals("1")){
				dataDB = F.OpenDB(dbname,true); //当前目录
			}else{
				dataDB = F.OpenDB(dbname,false);
			}
		}else{
			dataDB = F.OpenDB(dbname,false);
		}
		if(dataDB == null){
			throw new Exception("无法打开应用数据库["+ dbname +"]！");
		}

		//创建文档
		for(int i=0;i<rows.size();i++){
			createDocument(dataDB,(ArrayList<?>)rows.get(i));
		}

		if (dataDB != null) dataDB.recycle();
	}

	/**
	 * 获取字段
	 * @throws Exception
	 */
	private FieldList getFieldList() throws Exception{
		FieldList fl2 = new FieldList();
		int i = 0;
		Document fieldDoc = null;
		ViewEntryCollection vec = F.getAllEntriesBykey(tdb,"v_key_f_fields_form_Status",listkey_unid,true);
		ViewEntry ve = vec.getFirstEntry();

		while(ve != null){
			Field f = new Field();
			fieldDoc = ve.getDocument();

			String isneed = fieldDoc.getItemValueString("isRequired");
			f.set("index", String.valueOf(i));
			if(fieldDoc.getItemValueString("ExcelName").equals("")){
				f.set("name", fieldDoc.getItemValueString("F_FieldName").toUpperCase());
			}else{
				f.set("name", fieldDoc.getItemValueString("ExcelName").toUpperCase());
			}

			if("".equals(fieldDoc.getItemValueString("ExcelTitle"))){
				fieldtitles.add(fieldDoc.getItemValueString("F_FIELDSUBJECT"));
			}else{
				fieldtitles.add(fieldDoc.getItemValueString("ExcelTitle"));
			}
			
			if(isneed.equals("1")){
				f.set("subject", fieldDoc.getItemValueString("F_FIELDSUBJECT"));
				f.set("IsNeed", "1"); //必填
			}else{
				f.set("IsNeed", fieldDoc.getItemValueString("IsNeed")); //必填
			}

			f.set("IsOnly", fieldDoc.getItemValueString("IsOnly")); //是否唯一
			f.set("isMulti", fieldDoc.getItemValueString("isMulti")); //多值 
			f.set("format",fieldDoc.getItemValueString("ExcelFormat")); //导入数据格式化
			f.set("createvalue",fieldDoc.getItemValueString("CreateValue")); //生成值 
			f.set("defaultvalue",fieldDoc.getItemValueString("ExcelDefaultValue")); //生成默认值 
			f.set("isNotOut",fieldDoc.getItemValueString("IsNotOutput")); //为空 导出，1不导出。
			f.set("isNotIn",fieldDoc.getItemValueString("IsNotInput")); //为空 导入，1不导入。

			/*
				无|
				时间戳|1
				UUID|2
				从表ID|3
				取默认值(公式)|4
			 */
			if("".equals(fieldDoc.getItemValueString("ExcelDataType"))){ //导入数据类型
				f.set("DataType","0");
			}else{
				f.set("DataType",fieldDoc.getItemValueString("ExcelDataType"));
			}

			//System.out.println("--->"+ f.get("name"));
			if("".equals(fieldDoc.getItemValueString("IsNotInput"))){
				fieldnames.add(f.get("name").toString()); //导入字段名。
			}
			fl2.add(f);
			fieldDoc.recycle();
			i++;
			ve = vec.getNextEntry(ve);
		}
		cellNum = i;
		//if(ve != null) ve.recycle();
		if(vec != null) vec.recycle();
		return fl2;
	}


	/**
	 * 
	 * @throws Exception
	 */
	private void handleOra() throws Exception{
		String table = configDoc.getItemValueString("formname");
		if("".equals(table)){
			table = configDoc.getItemValueString("sql_tablename") ;
		}

		StringBuffer sql = new StringBuffer(50);

		sql.append("SELECT * FROM ").append(table)
		.append(" WHERE ").append(configDoc.getItemValueString("sql_key")).append("=''");
		ResultSet rs = F.executeQuery(sql.toString());
		ResultSetMetaData rr = rs.getMetaData();
		Map<String, String> map = new HashMap<String, String>();
		int length = rr.getColumnCount();

		/**
		 * 2 NUMBER
		 * 12 VARCHAR2
		 * 93 DATE
		 */
		for(int c=1;c<=length;c++){
			map.put(rr.getColumnLabel(c).toUpperCase(), String.valueOf(rr.getColumnType(c)));
		}

		rs.close();
		int colcount = fieldnames.size(); //必须写在写入重表ID之前。
		if(configDoc.getItemValueString("TableListType").equals("2")){ // 从表
			if(!configDoc.getItemValueString("key_fieldname").equals("")){
				fieldnames.addElement(configDoc.getItemValueString("key_fieldname"));
			}else{
				throw new Exception("从表ID没有配置！");
			}
		}

		F.setInsertFields(table, fieldnames);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		for(int i=0;i<rows.size();i++){ 
			ArrayList<?> col = (ArrayList<?>)rows.get(i);
			if(col.size() != colcount){
				System.out.println("--------------->"+ col);
				System.out.println("--------------->"+ fieldnames );
				throw new Exception("读取到的数据与导入字段不一致！");
			}

			for(int j=0;j<colcount;j++){
				Object o = map.get(fieldnames.get(j));
				if(o == null){
					throw new Exception(fieldnames.get(j) +"的字段在数据表中不存在");
				}
				String type = o.toString();
				Vector<?> value = (Vector<?>)col.get(j);
				if("2".equals(type)){ // Number
					if(value.size()==0){
						F.setPstvalue(j+1,0);
					}else{
						F.setPstvalue(j+1,value.get(0));
					}
				}else if("12".equals(type)){ // varchar2
					if(value.size()==0 || "".equals(value.get(0))){
						F.setPstvalue(j+1," ");
					}else{
						F.setPstvalue(j+1,value.get(0));
					}


				}else if("93".equals(type)){ // date
					String k = value.get(0).toString();
					if(k.indexOf(":") == -1){
						k += " 00:00:00";
					}
					F.setPstvalue(j+1,new java.sql.Date(sdf.parse(k).getTime()));
				}
			}

			//写入从表ID
			if(configDoc.getItemValueString("TableListType").equals("2")){ // 从表
				F.setPstvalue(colcount+1,this.parentid);
			}

			try{
				F.executeUpdate();
			}catch(SQLException e){
				System.out.println("--->"+ col.toString());
				throw new Exception ("第"+ (i+2) +"行：" +  TableListBase.returnErrMsg(configDoc, e));
			}
			successCount++;
		}
	}
	/**
	 * 处理关系型数据库数据
	 * @param eo
	 * @throws Exception
	 */
	private void handleSQL() throws Exception{

		String table = configDoc.getItemValueString("formname");
		if("".equals(table)){
			table = configDoc.getItemValueString("sql_tablename") ;
		}

		int colcount = fieldnames.size(); //必须写在写入重表ID之前。
		if(configDoc.getItemValueString("TableListType").equals("2")){ // 从表
			if(!configDoc.getItemValueString("key_fieldname").equals("")){
				fieldnames.addElement(configDoc.getItemValueString("key_fieldname"));
			}else{
				throw new Exception("从表ID没有配置！");
			}
		}

		F.setInsertFields(table, fieldnames);

		for(int i=0;i<rows.size();i++){ 
			ArrayList<?> col = (ArrayList<?>)rows.get(i);
			if(col.size() != colcount){
				System.out.println("--------------->"+ col);
				System.out.println("--------------->"+ fieldnames);
				throw new Exception("读取到的数据与导入字段不一致！");
			}

			for(int j=0;j<colcount;j++){
				Vector<?> value = (Vector<?>)col.get(j);
				F.setPstvalue(j+1,value.get(0));
			}

			//写入从表ID
			if(configDoc.getItemValueString("TableListType").equals("2")){ // 从表
				F.setPstvalue(colcount+1,this.parentid);
			}

			try{
				F.executeUpdate();
			}catch(SQLException e){
				System.out.println("--->"+ col.toString());
				throw new Exception ("第"+ (i+2) +"行：" +  TableListBase.returnErrMsg(configDoc, e));
			}
			successCount++;
		}
	}

	/**
	 * 从Excel中读取数据
	 * @return ArrayList
	 * @throws Exception
	 */
	public ArrayList<Object> readDataByXSSFWorkbook() throws Exception{
		String filename = F.Evaluate(curdoc, "@AttachmentNames").elementAt(0).toString();
		if(filename.equals("")){
			throw new Exception("Excel文件为空！");
		}

		String lastname = filename.substring(filename.indexOf("."), filename.length());
		if(!lastname.equalsIgnoreCase(".xlsx")){
			throw new Exception("上传文件不是2007版 Excel文件");
		}

		EmbeddedObject eo = curdoc.getAttachment(filename);
		XSSFWorkbook xwb = null;
		XSSFSheet xs = null;
		XSSFRow xr = null;
		XSSFCell xc = null;
		xwb = new XSSFWorkbook(eo.getInputStream());
		xs = xwb.getSheetAt(0);
		ArrayList<Object> rows = new ArrayList<Object>();
		ArrayList<Object> col = null;
		msgerr = new StringBuffer();
		try{
			String msg = this.checkTemplate(xs.getRow(0));
			if("".equals(msg)){
				for(int i=0;i<xs.getLastRowNum();i++){ //行
					xr = xs.getRow(i+1);
					col = new ArrayList<Object>();
					int excelcol = 0;
					for(int j=0;j<cellNum;j++){ //列
						Field f = fl.get(j);
						if("".equals(f.get("isNotIn"))){
							if("".equals(f.get("isNotOut"))){ //导出字段,从excel中读取值
								xc = xr.getCell(excelcol++);
								/*
								if(xc!= null && xc.getCellType() == XSSFCell.CELL_TYPE_FORMULA){
									throw new Exception("第：" + (i+2) + "行"+ (j+1) + "列"+ f.get("subject") +" 不使用计算公式，请转换成文本。<br>");
								}else{
								*/
									col.add(getValue(xc,f,i,j,false)); 
								//}
							}else{ //不出导字段，自动生成或读取默认值。
								col.add(getValue(xc,f,i,j,true)); 
							}
						}else{
							excelcol++;//跳过读取列
						}
					}
					rows.add(col);
				}
			}else{
				throw new Exception("导入的Excel文件与模板不一致["+ msg + "]");
			}
		}finally{
			if (eo!= null) eo.recycle();
		}
		return rows;
	}
	/**
	 * 
	 * @param xc
	 * @param f
	 * @param i
	 * @param j
	 * @param isdefaultvalue true 读取默认值 false 读取Excel数据
	 * @return Vector
	 */
	private Vector<Object> getValue(XSSFCell xc,Field f,int i,int j,boolean isdefaultvalue){
		Vector<Object> cvalue = new Vector<Object>();
		String cv = f.get("createvalue").toString(); //生成值
		//System.out.println("--->" + f.get("name"));
		if("".equals(cv)){  //无
			if(isdefaultvalue){
				cvalue.add(f.get("defaultvalue"));//写入默认值
			}else{
				cvalue = getCellValue2(xc,f);

				if(f.get("IsNeed").equals("1")){
					if(cvalue.size() == 0 || cvalue.get(0).equals("")){
						msgerr.append("第" + (i+2) + "行"+ (j+1) + "列："+ f.get("subject") +"必填！");
					}
				}
			}
		}else if("1".equals(cv)){ //时间
			cvalue.add(System.currentTimeMillis() + "_" + (index++));
		}else if("2".equals(cv)){ //uuid
			cvalue.add(UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
		}else if("3".equals(cv)){ //从表ID
			cvalue.add(this.parentid);
		}

		return cvalue;
	}
	/**
	 * 生??蒬omino文档
	 * @param dataDB
	 * @param col
	 * @throws Exception 
	 */
	private void createDocument(Database dataDB,ArrayList<?> col) throws Exception{
		Document thisdoc = dataDB.createDocument();
		thisdoc.replaceItemValue("form",configDoc.getItemValueString("formname"));
		Item item = thisdoc.replaceItemValue("Creater", F.getEffectiveUserName());
		item.setAuthors(true);
		item.setReaders(true);

		if(this.parentid != null){
			thisdoc.replaceItemValue("ParentID",this.parentid);
		}

		//reader item
		String readeritem = configDoc.getItemValueString("readeritem");
		Vector<?> v = null;
		if(!readeritem.trim().equals("")){
			v = configDoc.getItemValue("readermember");
			if(v.get(0).toString().trim().equals("")){
				item = thisdoc.replaceItemValue(readeritem, "*");
			}else{
				item = thisdoc.replaceItemValue(readeritem, v);
			}
			item.setReaders(true);
		}

		//writer item
		String writeritem = configDoc.getItemValueString("writeritem");
		if(!writeritem.trim().equals("")){
			v = configDoc.getItemValue("writermember");
			if(v.get(0).toString().trim().equals("")){
				item = thisdoc.replaceItemValue(writeritem, "*");
			}else{
				item = thisdoc.replaceItemValue(writeritem, v);
			}
			item.setAuthors(true);
		}


		for(int j=0;j<fieldnames.size();j++){
			Vector<?> cvalue = (Vector<?>)col.get(j);
			String ItemName = fieldnames.get(j).toString();
			thisdoc.replaceItemValue(ItemName,cvalue);
		}

		successCount++;
		thisdoc.save(true,false);
		thisdoc.recycle();
	}

	/**
	 * 
	 * @param cell
	 * @return Vector
	 */
	private Vector<Object> getCellValue2(XSSFCell cell,Field f){
		Vector<Object> vec = new Vector<Object>();
		if (null == cell) {
			return vec;
		}
		Object cellValue = null;//cellValue的值
		switch (cell.getCellType()) {
		case XSSFCell.CELL_TYPE_STRING:
			if("2".equals(f.get("DataType"))){//long
				cellValue = Long.valueOf(cell.getStringCellValue());
				vec.add(cellValue);
			}else if("3".equals(f.get("DataType"))){//Double
				cellValue = Double.valueOf(cell.getStringCellValue());
				vec.add(cellValue);
			}else{
				if("1".equals(f.get("isMulti"))){
					String[] arr = cell.getStringCellValue().trim().split(";");
					for(int i=0;i<arr.length;i++){
						vec.add(arr[i]);
					}
				}else{
					cellValue = cell.getStringCellValue().trim();
					vec.add(cellValue);
				}
			}

			break;
		case XSSFCell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				cellValue= cell.getDateCellValue();
				//TODO 可以按日期格式转换
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
				String time = formatter.format(cellValue);
				cellValue = time;
			} else {
				if("0".equals(f.get("DataType"))){//String
					cell.setCellType(XSSFCell.CELL_TYPE_STRING);
					cellValue = cell.getStringCellValue();
				}else if("1".equals(f.get("DataType"))){//format
					String format = f.get("format").toString();
					if(!"".equals(format)){
						df.applyPattern(format);
					}
					cellValue = df.format(cell.getNumericCellValue());
				}else if("2".equals(f.get("DataType"))){//long
					cellValue = Long.valueOf((long) cell.getNumericCellValue());
				}else if("3".equals(f.get("DataType"))){//Double
					cellValue = Double.valueOf(cell.getNumericCellValue());
				}else{
					cellValue = Double.valueOf(cell.getNumericCellValue());
				}
			}
			vec.add(cellValue);
			break;
		case XSSFCell.CELL_TYPE_BLANK:
			vec.add("");
			break;
		case XSSFCell.CELL_TYPE_FORMULA:
			if("0".equals(f.get("DataType"))){//String
				cell.setCellType(XSSFCell.CELL_TYPE_STRING);
				cellValue = cell.getStringCellValue();
			}else if("1".equals(f.get("DataType"))){//format
				String format = f.get("format").toString();
				if(!"".equals(format)){
					df.applyPattern(format);
				}
				cellValue = df.format(cell.getNumericCellValue());
			}else if("2".equals(f.get("DataType"))){//long
				cellValue = Long.valueOf((long) cell.getNumericCellValue());
			}else if("3".equals(f.get("DataType"))){//Double
				cellValue = Double.valueOf(cell.getNumericCellValue());
			}else{
				cellValue = Double.valueOf(cell.getNumericCellValue());
			}
			break;
		default:
			System.out.println("not find match type=" + cell.getCellType());
		vec.add("");
		}


		return vec ;
	}
}
