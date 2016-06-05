package com.newhope.vcard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.view.ViewTreeObserver.OnDrawListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import com.newhope.vcard.R;
import com.newhope.vcard.common.ImageLoaderManager;
import com.newhope.vcard.utils.BitmapUtil;
import com.newhope.vcard.utils.ChinaPhoneUtils;
import com.newhope.vcard.utils.ChineseNameUtils;
import com.newhope.vcard.utils.ImageLoaderUtils;
import com.newhope.vcard.utils.ImageUriCache;
import com.newhope.vcard.widget.ClearEditText;

public class ContactsVCardActivity extends Activity implements OnTouchListener, Callback{
	private static final String TAG = "ContactsVCardActivity";
	public static final String VCARD_FILE_PATH = Environment
			.getExternalStorageDirectory() + "/vcard";
	
	private Button submitBtn = null;
	private TextView mMoreSettings = null;
	private ClearEditText contactsCountField = null;
	private ImageView mRepeatContactsCheckBox = null;
	private ImageView mMorePhonesCheckBox = null;
	private Button mSelectAvatorsBtn = null;
	
	private View mSettingsContainer = null;
	private View mContainer = null;
	
	private View mRepeatItem = null;
	private View mMorePhonesItem = null;
	
	private static final int MAX_NUMBER_OF_COUNTS = 100000;

	/* 两次返回键之间的间隔 */
	private long exitTime = 0;
	
	private int originY = -1;
	private boolean mFirstLoaded = true;
	
	private boolean[] mCheckStatus = new boolean[2];
	
	private ProgressDialog mLoadingDialog = null;
	
	private Handler mUIThreadHandler;
	
	private String mAbsolutePath = null;
	
	private static final int GENERATING_VCARD_END = 1;
	
	private static final int GENERATING_VCARD_FAILED = 2;
	
	private static final int GENERATING_VCARD_SUCCESS = 3;
	
	private static final int ACTION_SELECT_AVATOR_CODE = 1000;
	
	private ArrayList<Uri> mImageUriList = new ArrayList<Uri>();
	
	private ImageLoaderManager mImageLodader;
	
	private int mImageSize;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacs_vcard);
		
		mImageSize = getResources().getDimensionPixelSize(R.dimen.image_thumb_item_size);
		mImageLodader = ImageLoaderManager.getInstance(getApplicationContext());

		submitBtn = (Button) findViewById(R.id.submit_btn);
		submitBtn.setOnClickListener(new OnSubmitBtnClickListener(this));
		
		mSettingsContainer = findViewById(R.id.settings_container);
		
		mContainer = findViewById(R.id.content_container);
		
		mMoreSettings = (TextView) findViewById(R.id.more_settings);
		
		mMoreSettings.setOnClickListener(new View.OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mSettingsContainer.getVisibility() == View.GONE){
					mSettingsContainer.setVisibility(View.VISIBLE);
					mMoreSettings.setText(getString(R.string.more_settings_fold));
				}else{
					mSettingsContainer.setVisibility(View.GONE);
					mMoreSettings.setText(getString(R.string.more_settings));
				}
				
				mContainer.getViewTreeObserver().addOnDrawListener(new OnDrawListener(){

					@Override
					public void onDraw() {
						// TODO Auto-generated method stub
						startAnimation();
						mContainer.getViewTreeObserver().removeOnDrawListener(this);
					}
				});
			}
		});

		// 联系人个数
		contactsCountField = (ClearEditText) findViewById(R.id.contacts_count);
		
		mRepeatContactsCheckBox = (ImageView) findViewById(R.id.repeat_contacts_checkbox);
		mMorePhonesCheckBox = (ImageView) findViewById(R.id.more_numbers_checkbox);
		
		mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onGlobalLayout() {
				startAnimation();
				mContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
			}
		});
		
		mCheckStatus[0] = mCheckStatus[1] = false;
		
		mRepeatItem = findViewById(R.id.repeat_contacts_item);
		mMorePhonesItem = findViewById(R.id.more_numbers_item);
		
		mRepeatItem.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCheckStatus[0] = !mCheckStatus[0];
				
				if(mCheckStatus[0]){
					mRepeatContactsCheckBox.setImageResource(R.drawable.btn_check_on);
				}else{
					mRepeatContactsCheckBox.setImageResource(R.drawable.btn_check_off);
				}
			}
		});
		
		mMorePhonesItem.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mCheckStatus[1] = !mCheckStatus[1];
				
				if(mCheckStatus[1]){
					mMorePhonesCheckBox.setImageResource(R.drawable.btn_check_on);
				}else{
					mMorePhonesCheckBox.setImageResource(R.drawable.btn_check_off);
				}
			}
		});
		
		mSelectAvatorsBtn = (Button)findViewById(R.id.select_avator);
		mSelectAvatorsBtn.setOnClickListener(new View.OnClickListener() {		
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ContactsVCardActivity.this, AvatorsSelectActivity.class);
				startActivityForResult(intent, ACTION_SELECT_AVATOR_CODE);
			}
		});
		
		mUIThreadHandler = new Handler(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "[onActivityResult] resultCode=" + resultCode);
		if (resultCode == Activity.RESULT_OK && data!= null) {
			HashSet<Uri> imageUriSet = ImageUriCache.getCache();
			
			if (imageUriSet != null) {
				Log.d(TAG, "[onActivityResult] imageUriSet size=" + imageUriSet.size());
				mImageUriList.clear();
				
				Iterator<Uri> it = imageUriSet.iterator();
				
				while (it.hasNext()) {
					mImageUriList.add(it.next());
				}
			} else {
				Log.d(TAG, "[onActivityResult] imageUriSet is null");
			}
		}
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	@SuppressLint("NewApi")
	private void startAnimation(){
		
		
		if(originY == -1){
			int[] position = new int[2];
			
			mContainer.getLocationInWindow(position);
			originY = position[1];
		}
		
		int containerHeight = mContainer.getHeight();
		
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		Point size = new Point(); 
		display.getSize(size);
		
		int screenHeight = size.y;
		
		float offsetY = (float)(screenHeight - containerHeight)/2 - originY;
	
		mContainer.animate().translationY(offsetY).setDuration(mFirstLoaded?1000:300);
		mFirstLoaded = false;
	}
	
	private void showLoadingDialog(){
		if(mLoadingDialog == null){	
			mLoadingDialog = new ProgressDialog(this);
			mLoadingDialog.setTitle(R.string.loading_dialog_title);
			mLoadingDialog.setMessage(getString(R.string.loading_dialog_message));
			mLoadingDialog.setCancelable(false);
			mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			mLoadingDialog.show();
			
		}else if(mLoadingDialog!=null && !mLoadingDialog.isShowing()){
			mLoadingDialog.show();
		}		
	}
	
	private void dismissLoadingDialog(){
		if(mLoadingDialog != null && mLoadingDialog.isShowing()){
			mLoadingDialog.dismiss();
		}
	}

	// 按返回键判断两次按下返回键的事件决定是否退出
	public void exit() {
		if ((System.currentTimeMillis() - exitTime) > 1000) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.keyback_hint), Toast.LENGTH_SHORT)
					.show();
			exitTime = System.currentTimeMillis();
		} else {
			finish();
			System.exit(0);
		}
	}

	// 生成vcard文件
	@SuppressLint("SimpleDateFormat")
	public void generatorVCard(int contactCount) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, getString(R.string.no_external_storage),
					Toast.LENGTH_SHORT).show();

			return;
		}

		File vCardDir = new File(VCARD_FILE_PATH);

		if (!vCardDir.exists()) {
			vCardDir.mkdir();
		}

		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd-hh-mm-ss");
		String date = sDateFormat.format(new java.util.Date());
		String vcardName = date + ".vcf";

		// 生成vcard文件路径
		File vCardFile = new File(VCARD_FILE_PATH, vcardName);
		mAbsolutePath = VCARD_FILE_PATH + "/" + vcardName;

		try {
			OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(vCardFile), "UTF-8");

			VCardComposer composer = new VCardComposer();
			// create a contact

			for (int i = 0; i < contactCount; i++) {
				ContactStruct contact = new ContactStruct();
				byte[] imageBytes = getRandomImageBytes();
				
				if (imageBytes != null) {
					contact.title="hello kitty";
					contact.photoType = "JPEG";
					contact.photoBytes = imageBytes;
				}

				String surName = ChineseNameUtils.getChineseSurname();
				String name = ChineseNameUtils.getChineseName();
				
				String tel = ChinaPhoneUtils.getPhoneNumber();

				contact.name = surName + name;

				contact.addPhone(Contacts.Phones.TYPE_MOBILE, tel, null, true);
				
				
				if(mCheckStatus[1]){//允许有多个号码，可以重复2-3个号码
					if((int) (Math.random()*5) == 3){
						tel = ChinaPhoneUtils.getPhoneNumber();
						contact.addPhone(Contacts.Phones.TYPE_HOME, tel, null, true);
					}
					
					if((int) (Math.random()*5) == 3){
						tel = ChinaPhoneUtils.getPhoneNumber();
						contact.addPhone(Contacts.Phones.TYPE_WORK, tel, null, true);
					}
				}
				

				// create vCard representation
				String vcardString = composer.createVCard(contact,
						VCardComposer.VERSION_VCARD30_INT);

				// write vCard to the output stream
				writer.write(vcardString);
				writer.write("\n"); // add empty lines between contacts
				
				if(mCheckStatus[0]){//如果允许重复联系人，可以完全相同，或者号码相同，或者姓名相同
					if((int) (Math.random()*7) == 4){
						int index = (int) (Math.random()*3);
						
						switch(index){
							case 0: //完全相同
								writer.write(vcardString);
								writer.write("\n"); // add empty lines between contacts
								break;
							case 1: //姓名不同，号码相同
								contact.name = ChineseNameUtils.getChineseSurname() + ChineseNameUtils.getChineseName();
								vcardString = composer.createVCard(contact,
										VCardComposer.VERSION_VCARD30_INT);
								writer.write(vcardString);
								writer.write("\n"); // add empty lines between contacts
								break;
								
							case 2://姓名相同，号码不同
								ContactStruct anoContact = new ContactStruct();
								anoContact.title="hello kitty";
								anoContact.name = contact.name;
								anoContact.photoBytes = contact.photoBytes;
								anoContact.photoType = "JPEG";
								anoContact.addPhone(Contacts.Phones.TYPE_MOBILE, tel, null, true);
								if(mCheckStatus[1]){//允许有多个号码，可以重复2-3个号码
									if((int) (Math.random()*5) == 3){
										tel = ChinaPhoneUtils.getPhoneNumber();
										contact.addPhone(Contacts.Phones.TYPE_HOME, tel, null, true);
									}
									
									if((int) (Math.random()*5) == 3){
										tel = ChinaPhoneUtils.getPhoneNumber();
										contact.addPhone(Contacts.Phones.TYPE_WORK, tel, null, true);
									}
								}
								
								vcardString = composer.createVCard(anoContact,
										VCardComposer.VERSION_VCARD30_INT);
								writer.write(vcardString);
								writer.write("\n"); // add empty lines between contacts
								break;	
						}
						
						i++;
					}
				}
			}

			writer.close();
			
			Message msg = new Message();
			msg.what = GENERATING_VCARD_SUCCESS;
			mUIThreadHandler.sendMessage(msg);
		} catch (Exception e) {
			Message msg = new Message();
			msg.what = GENERATING_VCARD_FAILED;
			
			mUIThreadHandler.sendMessage(msg);
			e.printStackTrace();
		}
	}
	
	private byte[] getRandomImageBytes() {
		int size = mImageUriList.size();
		if (size > 0) {
			int index = (int)(Math.random()*size);
			Uri uri = mImageUriList.get(index);
			Log.d(TAG, "[getRandomImageBytes]index = " + index + ", uri=" + uri);
			Bitmap bitmap = mImageLodader.getCachedBitmap(uri);
			
			if (bitmap == null){
				byte[] bitmapBytes = ImageLoaderUtils.loadImages(getContentResolver(), uri);
				Bitmap originBitmap = BitmapUtil.decodeBitmapFromBytes(bitmapBytes, 1);
				bitmap = BitmapUtil.resizeBitMap(originBitmap, mImageSize);
			} else {
				bitmap = BitmapUtil.resizeBitMap(bitmap, mImageSize);
			}

			return BitmapUtil.Bitmap2Bytes(bitmap);
		}
		
		return null;
	}
	
	private void hideSoftKeyboard() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(ContactsVCardActivity.this
						.getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public boolean onTouch(View v, MotionEvent motionEvent) {
		switch (motionEvent.getActionMasked()) {
		// 显示更多设置
		case MotionEvent.ACTION_DOWN: {
			hideSoftKeyboard();
			return true;
		}
		}
		return false;
	}

	// 提交数据
	private class OnSubmitBtnClickListener implements OnClickListener {
		private Context mContext;

		public OnSubmitBtnClickListener(Context context) {
			mContext = context;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String contactCountStr = contactsCountField.getText().toString();

			if (null == contactCountStr || "".endsWith(contactCountStr)) {
				Toast.makeText(mContext,
						getString(R.string.no_empty_in_contacts_field), Toast.LENGTH_SHORT).show();

				return;
			}
			
			try{
				contactCountStr = contactCountStr.replace("\\s+", "");
				
				int contactCount = 0;
				if(contactCountStr.length() > 6){//如果输入的数字字符串长度超过6位时，直接输出数字太大，避免太长无法转化成int类型
					Toast.makeText(mContext,
							getString(R.string.no_more_contacts_number), Toast.LENGTH_SHORT).show();
					return;
				}else{
					contactCount = Integer.parseInt(contactCountStr);
					
					if(contactCount >= MAX_NUMBER_OF_COUNTS){
						Toast.makeText(mContext,
								getString(R.string.no_more_contacts_number), Toast.LENGTH_SHORT).show();
						
						return;
					}
				}
				
				submitBtn.setEnabled(false);
				
				showLoadingDialog();
				
				final int count = contactCount;
				new Thread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						generatorVCard(count);
						
						Message msg = new Message();
						msg.what = GENERATING_VCARD_END;
						
						mUIThreadHandler.sendMessage(msg);
					}
				}).start();
			}catch(Exception e){
				Message msg = new Message();
				msg.what = GENERATING_VCARD_FAILED;
				
				mUIThreadHandler.sendMessage(msg);
				
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		// TODO Auto-generated method stub
		if(msg.what == GENERATING_VCARD_END){
			dismissLoadingDialog();
			submitBtn.setEnabled(true);
			return true;
		}else if(msg.what == GENERATING_VCARD_FAILED){
			Toast.makeText(this, getString(R.string.vcard_generated_failed),
					Toast.LENGTH_LONG).show();
			return true;
		}else if(msg.what == GENERATING_VCARD_SUCCESS){
			Toast.makeText(this, "vCard:" + mAbsolutePath, Toast.LENGTH_LONG)
			.show();
			
			contactsCountField.setText("");
		}
		
		return false;
	}
}
