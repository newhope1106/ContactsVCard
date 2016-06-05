package com.newhope.vcard.widget;

import com.newhope.vcard.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

public class CheckableImageView extends ImageView{
	private boolean mIsChecked = false;
	private Drawable mCheckDrawable;
	
	private int mCheckWidth;
	private int mCheckHeight;

	public CheckableImageView(Context context) {
		super(context);
		
		mCheckDrawable = context.getResources().getDrawable(R.drawable.btn_check_on);
		mCheckWidth = mCheckDrawable.getIntrinsicWidth();
		mCheckHeight = mCheckDrawable.getIntrinsicHeight();
	}
	
	public void setChecked(boolean isChecked) {
		mIsChecked = isChecked;
		
		requestLayout();
	}
	
	public boolean isChecked() {
		return mIsChecked;
	}
	
	@Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (mIsChecked) {
			int width = right-left;
			int height = bottom-top;
			mCheckDrawable.setBounds(width - mCheckWidth, height - mCheckHeight, width, height);
		}
		
		super.onLayout(changed, left, top, right, bottom);
	}
	
	/**
	 * we will draw the checked image here
	 * */
	@Override
	protected void dispatchDraw(Canvas canvas) {
		
		if (mIsChecked) {
			mCheckDrawable.draw(canvas);
		}
		
		super.dispatchDraw(canvas);
	}
}
