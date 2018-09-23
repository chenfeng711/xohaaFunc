package com.xohaa.HandleDomino.Org;
//===================f_RSAEncrypt 开始========================================
//作用:实现加密算法
//Houlf 2008-05-07


public class RSAEncrypt{
	private double N;	               //建议N为 1到9间的数据
	
  	public RSAEncrypt(double primeN){
  		if (primeN != 0){
  			N = primeN;
  		}
  	}
  	
	//加密[算法:先得到ASCII码然后该数字乘变量N，再在尾数补上传入参数同N的模]
	//参数;EncStr,加密的字符串
	//JE,数字参数，用来得到同N的模	
	public String Encrypt(String EncStr, double JE){
		String EncryptStr;
		int i;
		String ChrStr;
		double ChrNum;
		Integer tmpNum;
		String st1, st2;

		EncryptStr = "";
		if (!EncStr.equals("")){
			for (i = 0; i < EncStr.length(); i++){
				//算法
				tmpNum = new Integer((int)EncStr.charAt(i));
				ChrNum = tmpNum.doubleValue() * N;
				
				st1 = Double.toString(ChrNum);
				st2 = Double.toString((JE - (i + 1)) % N);
				ChrStr = st1.substring(0, st1.length() - 2) + st2.substring(0, st2.length() - 2);
				EncryptStr += (ChrStr + "+");
			}
		}
		
		return EncryptStr;
	}
	
	//解密[算法:逆向解析加密过的字符串]
	//参数:DecStr,需要解密的字符串
	//     DE,数字参数,用来得到同N的模[DE=JE + N*2]
	//说明:解密函数只能够对用上面加密过的字符串进行解密
	public String UnEncrypt(String DecStr, double DE){
		String UnEncryptStr, JieMiStr, tok, ChrStr, tmpStr;
		int i, z, t, ptr;
		double tmpDE;
		Double tmpDB;
		
		UnEncryptStr = "";
		i = DecStr.indexOf("+");
		//System.out.println("DecStr=" + DecStr);
		if (DecStr.substring(0, i) != ""){
			//把数字参数修改回来(也就是同加密的数字参数相等)
			tmpDE = DE - N * 2;
			t = 0; //用来记载原始字符穿长度以便数字参数求模
			JieMiStr = "";
			for (z = 0; z < DecStr.length(); z ++){
				t ++;
				
				//算法
				ptr = DecStr.indexOf("+", z);
				tok = DecStr.substring(z, ptr); //得到了以+分开的数字串
				tmpStr = Double.toString((tmpDE - t) % N);
				//得到真正的数字串
				ChrStr = tok.substring(0, tok.length() - tmpStr.substring(0, tmpStr.length() - 2).length());
				tmpDB = new Double(ChrStr);
				JieMiStr += (char)(tmpDB.doubleValue() / N);
				
				z = ptr;
			}
			UnEncryptStr = JieMiStr;
		}
		
		return UnEncryptStr;		
	}
} 

