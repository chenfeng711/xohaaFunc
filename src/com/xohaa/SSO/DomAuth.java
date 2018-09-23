package com.xohaa.SSO;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import com.xohaa.Opt;

public class DomAuth {
	private String SassID = null;

	/**
	 * 登录系统并获取Domino返回的DomAuthSessId
	 * @param HostName
	 * @param userName
	 * @param password
	 * @throws IOException
	 */
	public void CreateDomAuthSessId(String HostName, String userName,String password) throws IOException {
		String cookeValue = null;
		URL url = new URL(HostName + "/"+ Opt.Names_DBName +"?Login");
		HttpURLConnection.setFollowRedirects(false);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
		urlConnection.setDoOutput(true);
		
		OutputStreamWriter wr = new OutputStreamWriter(urlConnection.getOutputStream());
		wr.write("username=" + userName + "&password=" + password + "&redirectto=/"+ Opt.Portal_DBName +"/login.json");
		wr.flush();
		wr.close();
		urlConnection.connect();
		//Map headerFields = urlConnection.getHeaderFields();
		//System.out.println("headerFields=" + headerFields.toString());
		cookeValue = urlConnection.getHeaderField("Set-Cookie");
		
/*		for (Iterator it = headerFields.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			System.out.println("key=" + key);
			if (key != null && key.equals("Set-Cookie")) {
				String value = urlConnection.getHeaderField(key);
				System.out.println("v=" + value);
				String[] cookies = value.split(";\\s*");
				for (int i = 0; i < cookies.length; i++) {
					String[] cookie = cookies[i].trim().split("=");
					if (cookie[0].equals("DomAuthSessId")) {
						cookeValue = cookie[1];
						break;
					}
				}
			}
		}*/
		urlConnection.disconnect();
		SassID = cookeValue;
	}

	/**
	 * 写入cookies
	 * @param getAgentOutput
	 * @param redirectTo
	 */
	public void printCookiesRedirectTo(PrintWriter getAgentOutput,String redirectTo){
		if(SassID != null && !SassID.equals("")){
			getAgentOutput.println("P3P: CP=\"NON DSP COR CURa ADMa DEVa TAIa PSAa PSDa IVAa IVDa CONa HISa TELa OTPa OUR UNRa IND UNI COM NAV INT DEM CNT PRE LOC\"");
			getAgentOutput.println("Set-Cookie:" + SassID);
			getAgentOutput.println("Location:"+ redirectTo);
		}else{
			getAgentOutput.println("系统验证失败，请与管理员联系！");
		}
	}

	/**
	 * 
	 * @return String
	 */
	public String getSassID(){
		return SassID;
	}
}
