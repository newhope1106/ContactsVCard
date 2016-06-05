package com.newhope.vcard.list;

import java.util.ArrayList;

import com.newhope.vcard.common.ImageLoaderManager;
import com.newhope.vcard.model.ImageModel;
import com.newhope.vcard.widget.CheckableImageView;
import com.newhope.vcard.widget.ImageTileRowLayout;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

public class ImageListAdapter extends BaseAdapter{
	private ArrayList<ImageModel> mImageList;
	private Context mContext;
	
	private final int MAX_NUMBER_PER_ROW = 4;
	
	private ImageLoaderManager mImageLoader;
	
	private OnClickListener mListener;
	
	public ImageListAdapter(Context context) {
		mContext = context;
		
		mImageLoader = ImageLoaderManager.getInstance(mContext.getApplicationContext());
	}
	
	public void changeList(ArrayList<ImageModel> imageList) {
		mImageList = imageList;

		notifyDataSetChanged();
	}
	
	public ArrayList<ImageModel> getImageList() {
		return mImageList;
	}
	
	public void setOnClickLitener(OnClickListener listener) {
		mListener = listener;
	}

	@Override
	public int getCount() {
		if (mImageList == null || mImageList.size()==0) {
			return 0;
		}
		
		return (mImageList.size()-1)/MAX_NUMBER_PER_ROW + 1;
	}

	@Override
	public ArrayList<ImageModel> getItem(int position) {
		
		if (mImageList == null) {
			return null;
		}
		
		ArrayList<ImageModel> oneRawList = new ArrayList<ImageModel>();
		
		int rowCount = MAX_NUMBER_PER_ROW;
		int startPos = position*MAX_NUMBER_PER_ROW;
		
		if (mImageList.size() < startPos+rowCount) {
			rowCount = mImageList.size() - startPos;
		}
		
		int endPos = startPos + rowCount;
		
		for(int i=startPos; i<endPos; i++) {
			oneRawList.add(mImageList.get(i));
		}
		
		return oneRawList;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ImageTileRowLayout tileRowView = (ImageTileRowLayout) convertView;
		
		if (tileRowView == null) {
			tileRowView = new ImageTileRowLayout(mContext, MAX_NUMBER_PER_ROW);
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			
			tileRowView.setLayoutParams(params);
			tileRowView.setOrientation(LinearLayout.HORIZONTAL);
			
			addChildTileRow(tileRowView, position);
		}
		
		loadRowPhotos(tileRowView, position);
		
		return tileRowView;
	}
	
	/**
	 * 后续动态设置间隔值
	 * */
	private void addChildTileRow(LinearLayout tileRowView, int position) {
		ArrayList<ImageModel> oneRowList = getItem(position);
		
		int rowCount = oneRowList.size();
		
		for (int i=0; i<rowCount; i++) {
			CheckableImageView childView = new CheckableImageView(mContext);
			ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			childView.setLayoutParams(params);

			childView.setOnClickListener(mListener);
			
			tileRowView.setDescendantFocusability(LinearLayout.FOCUS_AFTER_DESCENDANTS);
			
			tileRowView.addView(childView);
		}
	}
	
	private void loadRowPhotos(ViewGroup parentView, int position) {
		ArrayList<ImageModel> oneRowList = getItem(position);
		
		int childCount = Math.min(parentView.getChildCount(), oneRowList.size());

		int index = 0;
		for (int i=0; i<childCount; i++) {
			View childView = parentView.getChildAt(i);
			
			if (childView instanceof CheckableImageView) {
				mImageLoader.loadPhoto((CheckableImageView)childView, oneRowList.get(index).uri);
				childView.setTag(oneRowList.get(index).uri);
				index++;
			}
		}
	}
	
	public void resumeImageLoader() {
		mImageLoader.resume();
	}
	
	public void pauseImageLoader() {
		mImageLoader.pause();
	}
}
