package com.newhope.vcard.model;

import android.net.Uri;

public class ImageModel {
	public Uri uri;
	
	public String imageName;
	
	@Override
    public int hashCode() {
		final int prime = 31;
        int result = 1;
        
        result = prime * result + ((uri == null) ? 0 : uri.hashCode());
        
        return result;
	}
	
	public static ImageModel createFromUri(Uri uri, String imageName) {
		ImageModel imageModel = new ImageModel();
		imageModel.uri = uri;
		imageModel.imageName = imageName;
		
		return imageModel;
	}
	
	@Override
    public boolean equals(Object obj) {
        if (this == obj) {
        	return true;
        }
        
        if (obj == null) {
        	return false;
        }
        
        if (getClass() != obj.getClass()) {
        	return false;
        }
        
        final ImageModel that = (ImageModel)obj;
        
        if (uri == null && that.uri==null) {
        	return true;
        }
        
        return uri.equals(that.uri);
	}
	
	public Object getKey() {
		return uri == null ? -1 : uri;
	}
}
