package com.newhope.vcard.common;

import java.util.ArrayList;

import com.newhope.vcard.model.ImageModel;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

public class LocalImageLoaderTask extends AsyncTask<Void, Void, ArrayList<ImageModel>>{
	
	private static final Uri MEDIA_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	private static final String[] MEDIA_IMAGE_PROJECTION = new String[]{
			MediaStore.Images.Media.DISPLAY_NAME,
			MediaStore.Images.Media._ID
	};
	
	private static final int IMAGE_DISPLAY_NAME = 0;
	private static final int IMAGE_ID = 1;
	
	private Context mContext;
	private ImageQueryCallback mCallback;

	public LocalImageLoaderTask(Context context, ImageQueryCallback callback) {
		mContext = context;
		mCallback = callback;
	}
	
	@Override
	protected ArrayList<ImageModel> doInBackground(Void... params) {
		Cursor cursor = mContext.getContentResolver().query(MEDIA_IMAGE_URI, MEDIA_IMAGE_PROJECTION, null, null, null);
		
		Log.d("xxxx", "[doInBackground] cursor - size = " + cursor.getCount());
		ArrayList<ImageModel> imageList = new ArrayList<ImageModel>();
		try{
			if (cursor != null) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					String displayName = cursor.getString(IMAGE_DISPLAY_NAME);
					int id = cursor.getInt(IMAGE_ID);
					Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon().
		                      appendPath(Long.toString(id)).build();
					
					ImageModel imageModel = new ImageModel();
					imageModel.imageName = displayName;
					imageModel.uri = uri;
					
					Log.d("xxxx", "[doInBackground] uri = " + uri);
					imageList.add(imageModel);
				}
			}
		}finally{
			if (cursor != null) {
				cursor.close();
			}
		}
		return imageList;
	}
	
	@Override
	protected void onPostExecute(ArrayList<ImageModel> imageList) {
		if (mCallback != null) {
			mCallback.onQueryComplete(imageList);
		}
	}

}
