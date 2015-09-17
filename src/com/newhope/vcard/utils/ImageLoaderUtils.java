package com.newhope.vcard.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

public class ImageLoaderUtils {
	private static final String TAG = "ImageLoaderUtils";
	
	private static final int BUFFER_SIZE = 1024*16;
	
	/**
	 * it is better running in child threads
	 * */
	public static byte[] loadImages(ContentResolver resolver, Uri uri) {
		if (uri == null) {
			return null;
		}
		byte buffer[] = new byte[BUFFER_SIZE];
		
		try{
			final String scheme = uri.getScheme();
			InputStream is = null;
			if (scheme.equals("http") || scheme.equals("https")) {
	            is = new URL(uri.toString()).openStream();
	        } else {
	            is = resolver.openInputStream(uri);
	        }
			if (is != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    int size;
                    while ((size = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, size);
                    }
                    
                    return baos.toByteArray();
                } finally {
                    is.close();
                }
			} else {
				Log.d(TAG, "load failed : uri=" + uri);
			}
		} catch (Exception ex) {
			Log.d(TAG, "load failed : uri=" + uri);
			ex.printStackTrace();
		}
		return null;
	}
}
