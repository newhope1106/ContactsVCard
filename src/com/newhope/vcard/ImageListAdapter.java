package com.newhope.vcard;

import java.util.ArrayList;

import com.newhope.vcard.model.ImageModel;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ImageListAdapter extends BaseAdapter {
	private ArrayList<ImageModel> mImageList;

	public void changeList(ArrayList<ImageModel> imageList) {
		mImageList = imageList;
		
		notifyDataSetChanged();
	}
	
	@Override
	public int getCount() {
		
		if (mImageList == null) {
			return 0;
		}
		
		return mImageList.size();
	}

	@Override
	public Object getItem(int position) {
		
		return null;
	}

	@Override
	public long getItemId(int position) {
		
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		return null;
	}

}
