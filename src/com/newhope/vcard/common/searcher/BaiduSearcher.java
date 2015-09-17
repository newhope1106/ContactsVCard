package com.newhope.vcard.common.searcher;

import java.net.URLEncoder;
import java.util.List;

import com.newhope.vcard.utils.ImageSearchUtils;

import android.util.Log;

public class BaiduSearcher {
	private static final String TAG = "BaiduSearcher";
	private static final String url="http://image.baidu.com/i?";
	
	private BaiduSearcher(){}
	
	private static String packParams(int page, String keyword) {
		String paramsStr = null;
		try{
			paramsStr = "tn=baiduimagejson&ie=utf-8&ic=0&rn=20&pn="+page+"&word=" + URLEncoder.encode(keyword,"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "pack params failed");
		}
		
		return url + paramsStr;
	}
	
	public static List<String> loadData(int page, String keyword) {
		String path = packParams(page, keyword);
		Log.d(TAG, "path = " + path);
		try{
			String json = ImageSearchUtils.sendGETRequest(path);
			String str = new String (json.getBytes("GBK"),"UTF-8");
			List<String> data = ImageSearchUtils.resolveImageData(str);
			
			return data;
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "[loadData] load data failed, " + e.getMessage());
		}
		
		return null;
	}
	
	public static List<String> loadData(String keyword) {
		return loadData(1, keyword);
	}
}
