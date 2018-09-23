package com.xohaa.tablelist.excel;

import java.sql.ResultSet;
import java.util.Vector;

import lotus.domino.Document;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.xohaa.ListArray.Field;
import com.xohaa.ListArray.FieldList;
import com.xohaa.tool.ExcelExporter;

public class ExportSQLData extends ExcelExporter {
	private FieldList fl = null;
	private FieldList dl = null;
	private Vector<String> fieldnames = null;
	public ExportSQLData(String sheetname) throws Exception {
		super(sheetname);
		// TODO 自动生成的构造函数存根
	}

	/**
	 * 
	 */
	public String exporter(String filename) throws Exception {
		this.writeHeaderCols(fl);
		this.writeBodyCols(dl, fieldnames);
		return this.generateFile(filename);
		
	}

	/**
	 * 读取列表配置字段
	 * @param vec
	 * @throws Exception
	 */
	public void addFieldConfig(ViewEntryCollection vec) throws Exception{
		if(vec == null || vec.getCount()==0){
			throw new Exception("字段为空！");
		}
		
		fieldnames = new Vector<String>();
		fl = new FieldList();
		ViewEntry ve = vec.getFirstEntry();
		Field f = null;
		Document fieldDoc = null;
		while(ve != null){
			f = new Field();
			StringBuffer t = new StringBuffer();
			fieldDoc = ve.getDocument();
			if("".equals(fieldDoc.getItemValueString("IsNotOutput"))){
				if("".equals(fieldDoc.getItemValueString("ExcelTitle"))){
					t.append(fieldDoc.getItemValueString("F_FIELDSUBJECT"));
				}else{
					t.append(fieldDoc.getItemValueString("ExcelTitle"));
				}

				if(fieldDoc.getItemValueString("isrequired").equals("1")){
					t.append("*");
				}
				f.set("title", t);

				if(fieldDoc.getItemValueString("ExcelName_Out").equals("")){
					if(fieldDoc.getItemValueString("fieldValueOption").equals("公式")){
						f.set("name", fieldDoc.getItemValueString("F_FieldName"));
					}else{
						f.set("name", fieldDoc.getItemValueString("fieldValue"));
					}
				}else{
					f.set("name", fieldDoc.getItemValueString("ExcelName_Out"));
				}
				fl.add(f);
				fieldnames.add(f.get("name").toString());
			}
			fieldDoc.recycle();
			ve = vec.getNextEntry(ve);
		}
	}
	
	/**
	 * SQL数据源
	 * @throws Exception
	 */
	public void addSQLData(ResultSet rs) throws Exception{
		String fieldname = "";
		dl = new FieldList();
		while(rs.next()){
			Field d = new Field();
			for(int i=0;i<fieldnames.size();i++){
				fieldname = fieldnames.get(i).toString();
				d.set(fieldname, rs.getString(fieldname));
			}
			dl.add(d);
		}
	}
}

