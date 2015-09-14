package com.newhope.vcard.widget;

import com.newhope.vcard.R;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public class ImageTileRowLayout extends LinearLayout{
	private final int mPadding;
	private final int mMaxNumer;

	public ImageTileRowLayout(Context context, int maxNumber) {
		super(context);
		
		mMaxNumer = maxNumber;
		mPadding = context.getResources().getDimensionPixelSize(R.dimen.image_list_padding);
	}
	
	protected void onLayout(boolean changed, int left, int top, int right, int bottom){
        int width = right - left;
        int height = bottom - top;
		
		int totalPadding = mPadding*(mMaxNumer+1);
		
		int perWidth = (width - totalPadding)/mMaxNumer;
		
		int childCount = getChildCount();
		
		int leftBound = mPadding;
		for (int i=0; i< childCount; i++) {
			View childView = getChildAt(i);
			
			childView.layout(leftBound, mPadding, leftBound + perWidth, height - mPadding);
			
			leftBound = leftBound + perWidth + mPadding;
		}
	}
}
