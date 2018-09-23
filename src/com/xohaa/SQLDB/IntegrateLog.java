package com.xohaa.SQLDB;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;


/**
 * @author chenf
 *
 */
public class IntegrateLog{
	private static Map<String, String> map = null;

	public IntegrateLog() { 
		map = new HashMap<String, String>();
		map.put("form", "fmIntegrateLog");
		map.put("Type", "1");
		map.put("CN_Type", "成功"); 
	}

	public void writeLog(String arg0, String arg1){
		if ((map.containsKey(arg0)) && (arg0.equals("LogMessage")))
			map.put(arg0, (String)map.get(arg0) + "<br/>" + arg1);
		else
			map.put(arg0, arg1);
	}
	public void writeError(String arg0){
		writeLog("Type", "0");
		writeLog("CN_Type", "错误");
		writeLog("LogMessage", arg0);
	}

	public void writeLogMessage(String arg0) {
		writeLog("LogMessage", arg0);
	}

	public void writeException(String arg0, Exception e) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			e.printStackTrace(new PrintStream(baos));
			baos.close();
			writeLog("Type", "-1");
			writeLog("CN_Type", "异常");
			writeLog("LogMessage", arg0 + "<br/>" + baos.toString().replaceAll("\n", "<br/>"));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	public void doLog(Session session,String path){
		try {
			writeLogMessage("开始集成日志!");
			writeLog("createtime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			Database LogDB = session.getDatabase("", path + "/" + "Sys_DBIntegrateLog.nsf");
			Document doc = LogDB.createDocument();
			doc.replaceItemValue("Trigger", session.getEffectiveUserName());
			doc.replaceItemValue("pro", "*");
			doc.getFirstItem("pro").setReaders(true);
			Iterator<String> iterator = map.keySet().iterator();
			while (iterator.hasNext()) {
				String key = ((String)iterator.next()).toString();
				doc.replaceItemValue(key, map.get(key));
			}
			doc.save(true,false);
		} catch (NotesException e) {
			e.printStackTrace();
		}
	}
}
