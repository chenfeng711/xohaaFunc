package com.xohaa.HandleDomino.Flow;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class HandleFlow {
	private String urlString = null;
	public HandleFlow(String hostname,String dbpath){
		if(!hostname.equals("")){
			urlString = hostname + "/"+ dbpath +"/Sys_Public.nsf";
		}
	}
	
	
	/**
	 * 流程起草或是审批都适用
	 * @param dbname
	 * @param unid
	 * @param curcompany
	 * @param curdepartment
	 * @param curposition
	 * @param curpost
	 * @return String
	 */
	public String startFlow(String dbname,String unid,String curcompany,
			String curdepartment,String curposition,String curpost){
		try{
			URL url = new URL(urlString+"/BPMFLOW?openwebservice");
			StringBuffer params = new StringBuffer();
			params.append("<DBNAME xsi:type=\"xsd:string\">").append(dbname).append("</DBNAME>")
			.append("<UNID xsi:type=\"xsd:string\">").append(unid).append("</UNID>")
			.append("<CURCOMPANY xsi:type=\"xsd:string\">").append(curcompany).append("</CURCOMPANY>")
			.append("<CURDEPARTMENT xsi:type=\"xsd:string\">").append(curdepartment).append("</CURDEPARTMENT>")
			.append("<CURPOSITION xsi:type=\"xsd:string\">").append(curposition).append("</CURPOSITION>")
			.append("<CURPOST xsi:type=\"xsd:string\">").append(curpost).append("</CURPOST>");
			
			StringBuffer xmlFile = getSoapXML("STARTFLOW",params);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestProperty("Content-Length", String.valueOf(xmlFile.length()));
			httpConn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
			httpConn.setRequestMethod("POST");

			//表单参数与get形式一样
			httpConn.setDoOutput(true);
			byte[] bypes = xmlFile.toString().getBytes();
			httpConn.getOutputStream().write(bypes);
			String result = XmlDocument(httpConn.getInputStream(),"STARTFLOWResponse","STARTFLOWReturn");
			httpConn.disconnect();
			return result;
		}catch(Exception e){
			e.printStackTrace();
			return "0";
		}
		
	}
	/**
	 * 组装webservice所需要的XML文件
	 * @param Fname
	 * @param urnXml
	 * @return StringBuffer
	 */
	private StringBuffer getSoapXML(String Fname,StringBuffer urnXml){
		StringBuffer s = new StringBuffer(200);
		s.append("<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:urn=\"urn:DefaultNamespace\">")
		.append("<soapenv:Header/>")
		.append("<soapenv:Body>")
		.append("<urn:"+Fname+" soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">")
		.append(urnXml)
		.append("</urn:"+Fname+">")
		.append("</soapenv:Body>")
		.append("</soapenv:Envelope>");
		return s;
	}
	/**
	 * 解析XML
	 * @param inStream
	 * @param String
	 * @param String
	 * @return String
	 * @throws DocumentException
	 */
	private String XmlDocument(InputStream inStream,String el,String hs) throws DocumentException{
		SAXReader reader = new SAXReader();
		Document document = reader.read(inStream); 
		Element root = document.getRootElement();
		Element body = root.element("Body");
		Element STARTFLOWResponse=body.element(el);
		Element node = STARTFLOWResponse.element(hs);
		return node.getText();
	}
	/**
	 * 解释Webservice返回的内容，格式为XML
	 * @param inStream
	 * @return byte
	 * @throws Exception
	 */
	public static byte[] readInputStream(InputStream inStream) throws Exception{
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int len = 0;
		while( (len = inStream.read(buffer)) !=-1 ){
			outStream.write(buffer, 0, len);
		}
		byte[] data = outStream.toByteArray();//网页的二进制数据
		outStream.close();
		inStream.close();
		return data;
	}
	/**
	 * 流程终止功能
	 * @param dbname
	 * @param unid
	 * @return String
	 */
	public String endFlow(String dbname,String unid){
		try{

			URL url = new URL(urlString+"/BPMFLOW?openwebservice");
			StringBuffer params = new StringBuffer();
			params.append("<DBNAME xsi:type=\"xsd:string\">").append(dbname).append("</DBNAME>")
			.append("<UNID xsi:type=\"xsd:string\">").append(unid).append("</UNID>");
			StringBuffer xmlFile = getSoapXML("ENDFLOW",params);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setRequestProperty("Content-Length", String.valueOf(xmlFile.length()));
			httpConn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
			httpConn.setRequestMethod("POST");
			//表单参数与get形式一样
			httpConn.setDoOutput(true);
			byte[] bypes = xmlFile.toString().getBytes();
			httpConn.getOutputStream().write(bypes);
			String result = XmlDocument(httpConn.getInputStream(),"ENDFLOWResponse","ENDFLOWReturn");
			httpConn.disconnect();
			return result;
		}catch(Exception e){
			e.printStackTrace();
		}
		return "0";
	}
	
}

