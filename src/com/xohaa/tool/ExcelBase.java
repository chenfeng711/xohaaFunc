package com.xohaa.tool;
import java.io.IOException;

import lotus.domino.EmbeddedObject;
import lotus.domino.NotesException;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
public class ExcelBase {
	private XSSFWorkbook workbook = null;
	XSSFSheet sheet = null;
	/**
	 * 获取工作簿
	 * @return XSSFWorkbook
	 */
	public XSSFWorkbook getWorkbook() {
		return workbook;
	}
	
	/**
	 *  获取工作簿
	 * @param eo
	 * @return XSSFWorkbook
	 * @throws NotesException
	 * @throws IOException
	 */
	public XSSFWorkbook getWorkbook(EmbeddedObject eo) throws NotesException, IOException{
		this.workbook = new XSSFWorkbook(eo.getInputStream());
		return this.workbook;
	}
	
	/**
	 *  设置工作簿
	 * @param eo
	 * @throws NotesException
	 * @throws IOException
	 */
	public void setWorkbook(EmbeddedObject eo) throws NotesException, IOException {
		this.workbook = new XSSFWorkbook(eo.getInputStream());
	}
	
	/**
	 * 
	 * @return XSSFSheet
	 */
	public XSSFSheet getSheet() {
		return sheet;
	}
	
	/**
	 * 
	 * @param index
	 * @return XSSFSheet
	 */
	public XSSFSheet getSheet(int index) {
		return this.workbook.getSheetAt(index);
	}	
	
	/**
	 * 
	 */
	public ExcelBase(){
		this.workbook = new XSSFWorkbook();
	}
	
	
	
}
