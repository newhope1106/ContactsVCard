package com.newhope.vcard.list;

import java.util.ArrayList;

import com.newhope.vcard.common.ImageLoaderManager;
import com.newhope.vcard.model.ImageModel;
import com.newhope.vcard.widget.CheckableImageView;

import a_vcard.android.util.Log;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ImageListAdapter extends BaseAdapter{
	private ArrayList<ImageModel> mImageList;
	private Context mContext;
	
	private ImageLoaderManager mImageLoader;
	
	public ImageListAdapter(Context context) {
		mContext = context;
		
		mImageLoader = ImageLoaderManager.getInstance(mContext);
	}
	
	public void changeList(ArrayList<ImageModel> imageList) {
		mImageList = imageList;
		
		Log.d("xxxx", "[changeList]");
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		Log.d("xxxx", "[getCount]");
		if (mImageList == null) {
			return 0;
		}
		
		return mImageList.size();
	}

	@Override
	public Object getItem(int position) {
		
		if (mImageList == null) {
			return null;
		}
		
		return mImageList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		CheckableImageView view;
		if (convertView == null) {
			view = new CheckableImageView(mContext);
		} else {
			view = (CheckableImageView)convertView;
		}
		
		Log.d("xxxx", "[getView] position = " + position);
		
		ImageModel imageModel = mImageList.get(position);
		
		mImageLoader.loadPhoto(view, imageModel.uri);
		
		return view;
	}

}
