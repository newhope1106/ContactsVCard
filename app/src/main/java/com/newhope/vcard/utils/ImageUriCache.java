package com.newhope.vcard.utils;

import java.util.HashSet;

import android.net.Uri;

public class ImageUriCache {
	private static HashSet<Uri> sImageUriSet;
	
	public static void setCache(HashSet<Uri> imageUriSet) {
		sImageUriSet = imageUriSet;
	}
	
	public static HashSet<Uri> getCache() {
		return sImageUriSet;
	}
}
