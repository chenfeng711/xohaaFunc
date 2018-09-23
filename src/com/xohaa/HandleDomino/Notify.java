package com.xohaa.HandleDomino;
import java.util.Vector;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import com.xohaa.Opt;
import com.xohaa.Base.Func;

public class Notify {
	Database wxdb = null;
	Database smsdb = null;
	Func F = null;

	public Notify(){

	}

	public Notify(Func f) throws NotesException{
		F = f;
	}

	/**
	 * 
	 * @throws NotesException
	 */
	public void openWXPushDB() throws NotesException{
		wxdb = F.OpenDB(Opt.WXPush_DBName);
	}

	public void openSmsDB() throws NotesException{
		smsdb = F.OpenDB(Opt.Sms_DBName);
	}
	/**
	 * 发送微信消息
	 * @param wxdb
	 * @param org 组织
	 * @param peWork 工号
	 * @param title 标题
	 * @param context 信息
	 * @param url 链接
	 * @throws NotesException
	 */
	public static void sendWXPushDocNews(Database wxdb,String org,String peWork,String title,
			String context,String url ) throws NotesException{
		Document newdoc = wxdb.createDocument();
		newdoc.replaceItemValue("Form", "fmPush");
		newdoc.replaceItemValue("user", peWork);
		newdoc.replaceItemValue("org", org);

		newdoc.replaceItemValue("title", title);
		newdoc.replaceItemValue("type", "news");
		newdoc.replaceItemValue("context", context);
		newdoc.replaceItemValue("url", url);
		newdoc.replaceItemValue("DATA_Reader", "*").setAuthors(true);
		newdoc.save(true,false);
		newdoc.recycle();
	}
	
	public static void sendWXPushDocNews(Database wxdb,Vector<String> org,String peWork,String title,
			String context,String url ) throws NotesException{
		Document newdoc = wxdb.createDocument();
		newdoc.replaceItemValue("Form", "fmPush");
		newdoc.replaceItemValue("user", peWork);
		newdoc.replaceItemValue("org", org);

		newdoc.replaceItemValue("title", title);
		newdoc.replaceItemValue("type", "news");
		newdoc.replaceItemValue("context", context);
		newdoc.replaceItemValue("url", url);
		newdoc.replaceItemValue("DATA_Reader", "*").setAuthors(true);
		newdoc.save(true,false);
		newdoc.recycle();
	}
	/**
	 * 发送微信消息
	 * @param wxdb
	 * @param org  组织
	 * @param peWork 工号
	 * @param context 消息内容
	 * @throws NotesException
	 */
	public static void sendWXPushDocText(Database wxdb,String org,String peWork,String context) throws NotesException{
		Vector<String> vec = new Vector<String>();
		vec.addElement(org);
		Notify.sendWXPushDocText(wxdb,vec, peWork, context);
	}
	
	/**
	 * 发送微信消息
	 * @param wxdb
	 * @param org 传入数组
	 * @param peWork
	 * @param context
	 * @throws NotesException
	 */
	public static void sendWXPushDocText(Database wxdb,Vector<String> org,String peWork,String context) throws NotesException{
		Document newdoc = wxdb.createDocument();
		newdoc.replaceItemValue("Form", "fmPush");
		newdoc.replaceItemValue("user", peWork);
		newdoc.replaceItemValue("org", org);
		newdoc.replaceItemValue("type", "text");
		newdoc.replaceItemValue("context", context);
		newdoc.replaceItemValue("DATA_Reader", "*").setAuthors(true);
		newdoc.save(true,false);
		newdoc.recycle();
	}
	/**
	 * 发送微信消息
	 * @param org
	 * @param peWork
	 * @param context
	 * @throws NotesException
	 */
	public void sendWXPushDocText(String org,String peWork,String context) throws NotesException{
		Notify.sendWXPushDocText(wxdb,org,peWork,context);
	}

	/**
	 * 发送微信消息
	 * @param org
	 * @param peWork
	 * @param title
	 * @param context
	 * @param url
	 * @throws NotesException
	 */
	public void sendWXPushDocNews(String org,String peWork,String title,String context,String url) throws NotesException{
		Notify.sendWXPushDocNews(wxdb,org,peWork,title,context,url);
	}

	/**
	 * 
	 * @param smsdb
	 * @param sender
	 * @param sendto
	 * @param subject
	 * @param content
	 * @param href
	 * @throws NotesException
	 */
	public static void sendSms(Database smsdb,String sender,Vector<?> sendto,String subject,String content,String href) throws NotesException{
		Document doc = null;
		if (smsdb != null){

			for(int i=0;i<sendto.size();i++){


				doc = smsdb.createDocument();
				doc.replaceItemValue("form", "fmSms_fx");
				doc.replaceItemValue("IsNewDoc", "0");
				doc.replaceItemValue("ReadFlag", "0");
				doc.replaceItemValue("sendFlag", "2");
				doc.replaceItemValue("MsgType", "1");

				doc.replaceItemValue("subject", subject);
				doc.replaceItemValue("content", content);
				doc.replaceItemValue("url", href);
				doc.replaceItemValue("Creater", sender);
				doc.replaceItemValue("sender", sender).setAuthors(true);
				doc.replaceItemValue("SYS_AUTHOR","Sys_Admin").setAuthors(true);
				doc.replaceItemValue("reader", sendto.get(i).toString()).setAuthors(true);
				doc.replaceItemValue("sendto", sendto.get(i).toString()).setAuthors(true);
				doc.replaceItemValue("sendto_show", "");
				doc.replaceItemValue("Creater_Show","");


				if (doc.computeWithForm(true, true)){
					doc.save(true,false);
				}
			}
			//			doc.AppDbServer = href(0)
			//			doc.AppDbPath = href(1)
			//			doc.AppDbName = href(2)
			//			doc.AppDocUnid = href(3)
		}
	}
	
	public void recycleNotify(){
		try{
			if(smsdb != null) smsdb.recycle();
			if(wxdb != null) wxdb.recycle();
		}catch(NotesException e){
			e.printStackTrace();
		}
	}
}
