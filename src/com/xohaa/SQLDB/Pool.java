package com.xohaa.SQLDB;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.View;
import java.util.Properties;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.ProxoolFacade;
public class Pool {

	public Pool() {
		
	}
	/**
	 * 判断池是否存在。
	 * @param poolName
	 * @return boolean
	 */
	public boolean isPoolExist(String poolName){
		boolean falg = false;
		//System.out.println("--->POOL:"+ProxoolFacade.getAliases().length + " " + poolName);
		for (int i = 0; i < ProxoolFacade.getAliases().length; i++) {
			if (ProxoolFacade.getAliases()[i].toLowerCase().equals(poolName.toLowerCase())) {
				falg = true;
				break;
			}
		}
		return falg;
	}

	/**
	 * 获取池里的连接
	 * @param arg0
	 * @return Connection
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public Connection getConnection(String arg0)throws ClassNotFoundException, SQLException{
		Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
		return DriverManager.getConnection("proxool." + arg0.toUpperCase());
	}

	/**
	 * 根据连接池名称获取连接
	 * @param initDb
	 * @param arg1
	 * @return Connection
	 * @throws Exception
	 */
	public Connection getConnectionByPoolName(Database initDb, String arg1) throws Exception{
		View view = null;
		Document doc = null;
		if (!isPoolExist(arg1)){
			view = initDb.getView("vw_fmLinkList_PoolName");
			view.setAutoUpdate(false);
			doc = view.getDocumentByKey(arg1, true);
			if (doc != null) {
				CreatePool(doc);
			}else{
				throw new Exception("没有找到连接池："+ arg1);
			}
		}
		if (doc != null) doc.recycle();
		if (view != null) view.recycle();
		return getConnection(arg1);
	}

	/**
	 * 根据配置获取连接池的连接
	 * @param initDb
	 * @param skey
	 * @return Connection
	 * @throws Exception
	 */
	public Connection getConnectionByConfigName(Database initDb, String skey) throws Exception{
		View view = null;
		Document doc = null;
		view = initDb.getView("vw_fmLinkList_ConnectionName");
		view.setAutoUpdate(false);
		doc = view.getDocumentByKey(skey, true);
		String arg1 = null;
		if(doc != null){
			arg1 = doc.getItemValueString("PoolName");	
		}else{
			throw new Exception("没有找到配置：" + skey);
		}
		
		if (doc != null) doc.recycle();
		if (view != null) view.recycle();
		return this.getConnectionByPoolName(initDb,arg1);
	}

	/**
	 * 直接连接
	 * @param doc
	 * @return Connection
	 * @throws NotesException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static Connection CreateConn(Document doc)throws NotesException, ClassNotFoundException, SQLException{
		String driverName = doc.getItemValueString("driverClass");
		String dbURL = doc.getItemValueString("url");
		String userName = doc.getItemValueString("user");
		String userPwd = doc.getItemValueString("password");
		Class.forName(driverName);
		return DriverManager.getConnection(dbURL, userName, userPwd);
	}

	/**
	 * 创建连接池
	 * @param poolDoc
	 * @throws NotesException
	 * @throws ClassNotFoundException
	 * @throws ProxoolException
	 */
	public void CreatePool(Document poolDoc){
		try{
			Properties info = new Properties();
			info.setProperty("user", poolDoc.getItemValueString("user"));
			info.setProperty("password", poolDoc.getItemValueString("password"));
			info.setProperty("proxool.house-keeping-test-sql", "select 1");
			info.setProperty("proxool.minimun-connection-count", poolDoc.getItemValueString("setMinimunConnectionCount"));
			info.setProperty("proxool.maximum-connection-count", poolDoc.getItemValueString("setMaximumConnectionCount"));
			info.setProperty("proxool.maximum-connection-lifetime", poolDoc.getItemValueString("setMaxinmumConnectionLifetime"));
			info.setProperty("proxool.maximum-active-time", poolDoc.getItemValueString("setMaximumActivetime"));
			info.setProperty("proxool.simultaneous-build-throttle", poolDoc.getItemValueString("setSimultaneousBuildThrottle"));
			String alias = poolDoc.getItemValueString("PoolName").toUpperCase();
			String driverClass = poolDoc.getItemValueString("driverClass");
			
			
			
			String driverUrl = poolDoc.getItemValueString("url");
			String url = "proxool." + alias + ":" + driverClass + ":" + driverUrl;
			Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
			String tempStr = ProxoolFacade.registerConnectionPool(url, info);
			if (tempStr.length() > 0) {
				System.out.println("创建连接池成功：---->" + tempStr);
			}else{
				System.out.println("创建连接池失败");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
