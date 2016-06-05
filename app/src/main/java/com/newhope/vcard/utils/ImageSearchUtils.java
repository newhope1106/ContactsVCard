package com.newhope.vcard.utils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class ImageSearchUtils {
	/**
	 * 获取文件夹的字节大小
	 * */
	public static long getFileSize(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSize(flist[i]);
			} else {
				size = size + flist[i].length();
			}
		}
		return size;
	}
	
	public static String sendGETRequest(String path) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
		conn.setDoInput(true);
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		conn.connect();;
		InputStream inStream = conn.getInputStream();
		byte[] data = StreamTool.read(inStream);
		String result = new String(data);
		return result;
	}
	
	public static List<String> resolveImageData(String json) throws Exception {
	    List<String> list = new ArrayList<String>();
	    JSONObject object = new JSONObject(json);
	    JSONArray data = object.getJSONArray("data");
	    for (int i = 0; i < data.length() - 1; i++) {
	      String objURL = data.getJSONObject(i).getString("objURL");
	      list.add(objURL);
	    }
	    return list;
	  }
}
