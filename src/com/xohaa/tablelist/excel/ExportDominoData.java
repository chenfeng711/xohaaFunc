package com.xohaa.tablelist.excel;
import java.util.Vector;

import lotus.domino.Document;

import lotus.domino.Item;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

import com.xohaa.ListArray.Field;
import com.xohaa.ListArray.FieldList;
import com.xohaa.tool.ExcelExporter;
public class ExportDominoData extends ExcelExporter {
	private FieldList fl = null;
	private FieldList dl = null;
	private Vector<String> fieldnames = null;
	public ExportDominoData(String sheetname)throws Exception {
		super(sheetname);
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
	 * 数据源domino
	 * @throws Exception
	 */
	public void addDominoData(ViewEntryCollection vec) throws Exception{
		dl = new FieldList();
		Field d = null;
		ViewEntry ve = vec.getFirstEntry();
		Document dataDoc = null;
		while(ve != null){
			dataDoc = ve.getDocument();
			d = new Field();
			Item item = null;
			for(int j=0;j<fl.size();j++){
				String fieldname = fl.get(j).get("name").toString();
				item = dataDoc.getFirstItem(fieldname);
				if(item != null){
					d.set(fieldname, item.getText());
					item.recycle();
				}else{
					d.set(fieldname, "");
				}
			}
			dl.add(d);
			ve = vec.getNextEntry(ve);
		}
	}
	
	/**
	 * @throws Exception 
	 * 
	 */
	public String exporter(String filename) throws Exception{
		this.writeHeaderCols(fl);
		this.writeBodyCols(dl, fieldnames);
		return this.generateFile(filename);
	}
}
