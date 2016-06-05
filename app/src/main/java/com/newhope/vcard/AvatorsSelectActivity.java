package com.newhope.vcard;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.newhope.vcard.common.ImageQueryCallback;
import com.newhope.vcard.common.LocalImageLoaderTask;
import com.newhope.vcard.common.searcher.BaiduSearcher;
import com.newhope.vcard.list.ImageListAdapter;
import com.newhope.vcard.model.ImageModel;
import com.newhope.vcard.utils.ImageUriCache;
import com.newhope.vcard.widget.CheckableImageView;

import android.util.Log;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
    
    private String mKeyword = "美女";
    private boolean mIsLoadingFromNetwork = false;
    private boolean mIsLoadAllowed = false;
    private boolean mIsFirstLoad = true;
    
    private static int sPageIndex = 0;
    
    private View mFooterView;
    
    private static final int MAX_NETWOR_IMAGE_COUNT = 100;
    
    private Intent mIntent;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mIntent = getIntent();
		
		setContentView(R.layout.activity_select_avators);
		
		mPager = (ViewPager) findViewById(R.id.viewpager);
		mTabStrip = (PagerTabStrip) findViewById(R.id.tabstrip);
		mTabStrip.setTextSpacing(200);
		
		View tab1View = LayoutInflater.from(this).inflate(R.layout.local_avator_select_tab, null);
		mLocalImageListView = (ListView)tab1View.findViewById(R.id.local_list);
		
		View tab2View = LayoutInflater.from(this).inflate(R.layout.network_avator_select_tab, null);
		mNetworkImageListView = (ListView)tab2View.findViewById(R.id.network_list);
		
		mFooterView = LayoutInflater.from(this).inflate(R.layout.network_list_footer, null);
		mFooterView.setVisibility(View.GONE);
		mNetworkImageListView.addFooterView(mFooterView);
		
		mViewContainter.add(tab1View);
		mViewContainter.add(tab2View);
		
		mTitleContainer.add(getString(R.string.local_images));
		mTitleContainer.add(getString(R.string.network_images));
		
		
		mImageUriSet.clear();
		
		setupPagerView();
		setupListView();
	}
	
	public void onResume() {
		super.onResume();
		mLocalImageAdapter.resumeImageLoader();
		mNetworkImageAdapter.resumeImageLoader();
	}
	
	public void onStop() {
		mLocalImageAdapter.pauseImageLoader();
		mNetworkImageAdapter.pauseImageLoader();
		
		
		
		super.onStop();
	}
	
	public void onBackPressed(){
		if (mIntent != null) {
			Log.d(TAG, "[onStop] putExtra");
			ImageUriCache.setCache(mImageUriSet);
			setResult(Activity.RESULT_OK, mIntent);
		} else {
			Log.d(TAG, "[onStop] mIntent is null");
		}
		
		super.onBackPressed();
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
		
		mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pageIndex) {
				if (mPager.getCurrentItem() == pageIndex && pageIndex == 1) {
					Log.d(TAG, "[onPageSelected] start load network image");
					startLoadNetworkData();
				};
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {

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
		
		mFooterView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mIsLoadAllowed = true;
				
				startLoadNetworkData();
				
				mIsLoadAllowed = false;
			}
		});
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
	
	private void startLoadNetworkData() {
		if (mNetworkImageAdapter.getImageList() != null 
				&& mNetworkImageAdapter.getImageList().size() >= MAX_NETWOR_IMAGE_COUNT) {
			return;
		}
		
		if (mIsFirstLoad) {
			mIsFirstLoad = false;
			mIsLoadingFromNetwork = true;
			sPageIndex = 0;
			SendTask task = new SendTask();
			task.execute(mKeyword);
		} else if(mIsLoadAllowed && !mIsLoadingFromNetwork){
			mIsLoadAllowed = false;
			mIsLoadingFromNetwork = true;
			SendTask task = new SendTask();
			task.execute(mKeyword);
		}
	}
	
	private class SendTask extends AsyncTask<String, Void, ArrayList<ImageModel>>{
		@Override
		protected ArrayList<ImageModel> doInBackground(String... params) {
			
			if (params == null || params.length<1) {
				return null;
			}
			
			String keyworkd = params[0];
			List<String> imageUrls = BaiduSearcher.loadData(sPageIndex, keyworkd);
			sPageIndex++;
			
			ArrayList<ImageModel> imageUris = new ArrayList<ImageModel>();
			
			if (imageUrls != null) {
				for (String url : imageUrls) {
					Log.d(TAG, "[onPageSelected]url = " + url);
					ImageModel imageModel = new ImageModel();
					imageModel.uri = Uri.parse(url);
					imageUris.add(imageModel);
				}
			}
			return imageUris;
		}
		
		protected void onPostExecute(ArrayList<ImageModel> result) {
			ArrayList<ImageModel> imageList = mNetworkImageAdapter.getImageList();
			
			if (imageList == null) {
				imageList = result;
			} else{
				imageList.addAll(result);
			}
			mNetworkImageAdapter.changeList(imageList);
			
			mIsLoadingFromNetwork = false;
			
			if (imageList.size() > 0) {
				mFooterView.setVisibility(View.VISIBLE);
			}
		}
	}
}
