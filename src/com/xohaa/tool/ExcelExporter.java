package com.xohaa.tool;
import java.io.File;

import java.io.FileOutputStream;
import java.util.Vector;

import lotus.domino.ViewEntryCollection;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;

import com.xohaa.Action;
import com.xohaa.ListArray.Field;
import com.xohaa.ListArray.FieldList;
/**
 * 
 * @author chenjianxiong
 * 导出处理
 */
public abstract class ExcelExporter extends ExcelBase {
	/**
	 * 
	 * @param sheetname
	 * @throws Exception
	 */
	public ExcelExporter(String sheetname) throws Exception {
		super();
		this.createSheet(sheetname);
	}
	
	/**
	 * 创建工作表
	 * @param sheetname
	 */
	public void createSheet(String sheetname){
		this.sheet = this.getWorkbook().createSheet();
	}

	/**
	 * 写入标题列。
	 * @param fl <[index].get("title")>
	 */
	public void writeHeaderCols(FieldList fl){
		XSSFCell xc = null;
		XSSFRow xr = sheet.createRow(0);
		XSSFFont xFont = this.getWorkbook().createFont();
		XSSFCellStyle xCellStyle = null;
		//xFont.setColor(IndexedColors.RED.index);
		xFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		xCellStyle = this.getWorkbook().createCellStyle();
		if(fl != null){
			for(int index=0;index<fl.size();index++){
				xc = xr.createCell(index);
				sheet.setColumnWidth(index, 4000);
				xc.setCellValue(fl.get(index).get("title").toString());
				xCellStyle.setFont(xFont);
				xc.setCellStyle(xCellStyle);
			}
		}
	}
	
	/**
	 * 写入标题列。
	 * @param vec <String> 数组。
	 */
	public void writeHeaderCols(Vector<String> vec){
		XSSFCell xc = null;
		XSSFRow xr = sheet.createRow(0);
		XSSFFont xFont = this.getWorkbook().createFont();
		XSSFCellStyle xCellStyle = null;
		xFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		xCellStyle = this.getWorkbook().createCellStyle();
		if(vec != null){
			for(int index=0;index<vec.size();index++){
				xc = xr.createCell(index);
				sheet.setColumnWidth(index, 4000);
				xc.setCellValue(vec.get(index).toString());
				xCellStyle.setFont(xFont);
				xc.setCellStyle(xCellStyle);
			}
		}
	}
	
	/**
	 * 输出内容
	 * @param dl
	 * @param fieldnames <String>
	 */
	public void writeBodyCols(FieldList dl,Vector<String> fieldnames){
		XSSFRow xr = null;
		XSSFCell xc = null;
		Field d = null;
		String value = "";
		if(dl != null && dl.size()>0){
			for(int index=0;index<dl.size();index++){
				xr = this.getSheet().createRow(index + 1);
				d = dl.get(index);
				for(int j=0;j<fieldnames.size();j++){
					xc = xr.createCell(j);
					String fieldname = fieldnames.get(j).toString();
					Object o = d.get(fieldname);
					if(o == null){
						value = "";
					}else{
						value = d.get(fieldname).toString();
						if(value == null){
							value = "";
						}
					}
					xc.setCellValue(value);
				}
			}
		}
	}
	
	
	/**
	 * 生成文件，返回路径。
	 * @param filename
	 * @throws Exception 
	 */
	public String generateFile(String filename) throws Exception{
		String exce = "excel";
		String filepath = Action.getCurMkdir(exce, true);
		StringBuffer strfile = new StringBuffer(40);

		strfile.append(filepath).append(filename).append(".xlsx");
		File file = new File(strfile.toString());
		FileOutputStream fos = new FileOutputStream(file);
		this.getWorkbook().write(fos);
		fos.flush();
		fos.close();
		
		StringBuffer urlfile = new StringBuffer(40);
		urlfile.append(File.separator);
		urlfile.append(exce).append(File.separator).append(filename).append(".xlsx");
		return urlfile.toString();
	}
	
	/**
	 * 导出文件处理
	 * @param filename 文件名
	 * @throws Exception
	 */
	public abstract String exporter(String filename) throws Exception;
	
	/**
	 * 添加导出字段
	 * @param vec
	 * @throws Exception
	 */
	public abstract void addFieldConfig(ViewEntryCollection vec) throws Exception;
}
