package com.xohaa.ListArray;

import java.util.ArrayList;
import java.util.List;


public class FieldList {

	private List<Field> list = null;
	/**
	 * 
	 */
	public FieldList(){
		list = new ArrayList<Field>();
	}
	/**
	 * 
	 * @param f
	 */
	public void add(Field f){
		list.add(f);
	}
	/**
	 * 
	 * @param index
	 * @return Field
	 */
	public Field get(int index){
		return (Field)list.get(index);
	}
	
	/**
	 * 
	 * @param index
	 * @param fieldKey
	 * @return String
	 */
	public String get(int index,String fieldKey){
		return list.get(index).get(fieldKey).toString();
	}
	
	
	/**
	 * 
	 * @return int
	 */
	public int size(){
		return list.size();
	}
}
