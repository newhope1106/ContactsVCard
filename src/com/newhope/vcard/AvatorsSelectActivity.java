package com.newhope.vcard;

import java.util.ArrayList;
import java.util.HashSet;

import com.newhope.vcard.common.ImageQueryCallback;
import com.newhope.vcard.common.LocalImageLoaderTask;
import com.newhope.vcard.list.ImageListAdapter;
import com.newhope.vcard.model.ImageModel;
import com.newhope.vcard.widget.CheckableImageView;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

public class AvatorsSelectActivity extends Activity{
	private static final String TAG = "AvatorsSelectActivity";
	
	private ViewPager mPager = null;
    private PagerTabStrip mTabStrip = null;
    
    private ArrayList<View> mViewContainter = new ArrayList<View>();
    private ArrayList<String> mTitleContainer = new ArrayList<String>();
    
    private ListView mLocalImageListView = null;
    private ListView mNetworkImageListView = null;
	
    private ImageListAdapter mLocalImageAdapter = null;
    private ImageListAdapter mNetworkImageAdapter = null;
    
    private OnTileItemClickListener mListener=null;
    
    private HashSet<Uri> mImageUriSet = new HashSet<Uri>();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_select_avators);
		
		mPager = (ViewPager) findViewById(R.id.viewpager);
		mTabStrip = (PagerTabStrip) findViewById(R.id.tabstrip);
		mTabStrip.setTextSpacing(200);
		
		View tab1View = LayoutInflater.from(this).inflate(R.layout.local_avator_select_tab, null);
		mLocalImageListView = (ListView)tab1View.findViewById(R.id.local_list);
		
		View tab2View = LayoutInflater.from(this).inflate(R.layout.network_avator_select_tab, null);
		mNetworkImageListView = (ListView)tab2View.findViewById(R.id.network_list);
		
		mViewContainter.add(tab1View);
		mViewContainter.add(tab2View);
		
		mTitleContainer.add(getString(R.string.local_images));
		mTitleContainer.add(getString(R.string.network_images));
		
		
		mImageUriSet.clear();
		
		setupPagerView();
		setupListView();
	}
	
	private void setupPagerView() {
		mPager.setAdapter(new PagerAdapter() {
			
			@Override
			public boolean isViewFromObject(View view1, Object view2) {
				return view1 == view2;
			}
			
			@Override
			public int getCount() {
				return mViewContainter.size();
			}
			
			@Override
            public void destroyItem(ViewGroup container, int position,
                    Object object) {
                ((ViewPager) container).removeView(mViewContainter.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ((ViewPager) container).addView(mViewContainter.get(position));
                return mViewContainter.get(position);
            }
            
            @Override
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }
 
            @Override
            public CharSequence getPageTitle(int position) {
                return mTitleContainer.get(position);
            }
		});
	}
	
	private void setupListView() {
		mLocalImageAdapter = new ImageListAdapter(this);
		mLocalImageListView.setAdapter(mLocalImageAdapter);
		
		mNetworkImageAdapter = new ImageListAdapter(this);
		mNetworkImageListView.setAdapter(mNetworkImageAdapter);
		
		mListener = new OnTileItemClickListener();
		
		mLocalImageAdapter.setOnClickLitener(mListener);
		mNetworkImageAdapter.setOnClickLitener(mListener);
		
		new LocalImageLoaderTask(this, new ImageQueryCallback() {
			
			@Override
			public void onQueryComplete(ArrayList<ImageModel> imageList) {
				mLocalImageAdapter.changeList(imageList);
			}
		}).execute();
	}
	
	private class OnTileItemClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			CheckableImageView imageView = (CheckableImageView) v;
			if (imageView != null) {
				imageView.setChecked(!imageView.isChecked());
				
				if (imageView.isChecked()) {
					mImageUriSet.add((Uri)imageView.getTag());
				} else {
					mImageUriSet.remove((Uri)imageView.getTag());
				}
			}
		}
	}
}
