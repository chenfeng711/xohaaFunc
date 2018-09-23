package com.xohaa.ListArray;

import java.util.HashMap;
import java.util.Map;


public class Field {

	private Map<Object,Object> map = null;
	/**
	 * 
	 * @param key
	 * @param val
	 */
	public Field(Object key,Object val){
		map = new HashMap<Object,Object>();
		map.put(key, val);
	}
	/**
	 * 
	 */
	public Field(){
		map = new HashMap<Object,Object>();
	}
	/**
	 * 
	 * @param key
	 * @param value
	 */
	public void set(Object key,Object value){
		map.put(key, value);
	}
	/**
	 * 
	 * @param key
	 * @return Object
	 */
	public Object get(Object key){
		return map.get(key);
	}
	
	/**
	 * 
	 * @return int
	 */
	public int size(){
		return map.size();
	}
}
