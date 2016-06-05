package com.newhope.vcard.utils;

public final class ChinaPhoneUtils {
	//移动
	private static String[] sMobile = new String[]{"134","135","136","137","138","139","150","151","157","158","159","187","188"};
	
	//联通
	private static String[] sUnicom = new String[]{"130", "131", "132", "152", "155", "156", "185", "186"};
	
	//电信
	private static String[] sTelecom = new String[]{"133" ,"153", "180", "189"};
	
	
	public static String getPhoneNumber(){
		int carrieroperator = (int)(Math.random()*3);

		String telPrefix = "";
		int i = 0;
		switch(carrieroperator){
			case 0:
				i = (int)(Math.random()*sMobile.length);
				telPrefix = sMobile[i];
				break;
				
			case 1:
				i = (int)(Math.random()*sUnicom.length);
				telPrefix = sUnicom[i];
				break;
				
			case 2:
				i = (int)(Math.random()*sTelecom.length);
				telPrefix = sTelecom[i];
				break;
		}
		
		StringBuffer tel = new StringBuffer(telPrefix);
		
		for (i = 0; i < 8; i++) {
			tel.append((int) (Math.random() * 10));
		}
		
		return tel.toString();
	}
}
