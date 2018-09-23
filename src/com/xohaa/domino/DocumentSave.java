package com.xohaa.domino;

import lotus.domino.NotesException;

public interface DocumentSave {
	/**
	 * 释放
	 * @throws NotesException
	 */
	public void recycle() throws NotesException;
	/**
	 * 保存
	 * @throws NotesException 
	 */
	public void doSave() throws Exception;
	
	/**
	 * 设置文档UNID
	 */
	public void setUnid(String unid);

	/**
	 * 获取文档UNID
	 */
	public String getUnid();	
	/**
	 * 关联ID
	 * @return String
	 */
	public String getRelationalID();
	
	/**
	 * 写入关联ID
	 */
	public void setRelationalID(String id) throws Exception;
	
	
	/**
	 * 判断数据是否存在
	 * @param FID
	 * @return 
	 * @throws Exception
	 */
	public boolean isHasData(String FID) throws Exception;
}
