package com.newhope.vcard.common;

import java.util.ArrayList;

import com.newhope.vcard.model.ImageModel;

public interface ImageQueryCallback {
	public void onQueryComplete(ArrayList<ImageModel> imageList) ;
}
