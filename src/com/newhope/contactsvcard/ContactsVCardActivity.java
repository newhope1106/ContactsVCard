package com.newhope.contactsvcard;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

import com.newhope.contactsvcard.utils.ChinaPhoneUtils;
import com.newhope.contactsvcard.utils.ChineseNameUtils;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsVCardActivity extends Activity implements OnTouchListener {
	public static final String TAG = "ContactsVCardActivity";
	public static final String VCARD_FILE_PATH = Environment
			.getExternalStorageDirectory() + "/vcard";
	
	private Button submitBtn = null;
	private TextView mMoreSettings = null;
	private ClearEditText contactsCountField = null;
	private CheckBox mRepeatContactsCheckBox = null;
	private CheckBox mMorePhonesCheckBox = null;
	
	private View mSettingsContainer = null;
	
	private static final int MAX_NUMBER_OF_COUNTS = 100000;

	/* 两次返回键之间的间隔 */
	private long exitTime = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacs_vcard);

		submitBtn = (Button) findViewById(R.id.submit_btn);
		submitBtn.setOnClickListener(new OnSubmitBtnClickListener(this));
		
		mSettingsContainer = findViewById(R.id.settings_container);
		
		mMoreSettings = (TextView) findViewById(R.id.more_settings);
		
		mMoreSettings.setOnClickListener(new View.OnClickListener() {
			
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
			}
		});

		// 联系人个数
		contactsCountField = (ClearEditText) findViewById(R.id.contacts_count);
		
		mRepeatContactsCheckBox = (CheckBox) findViewById(R.id.repeat_contacts_checkbox);
		mMorePhonesCheckBox = (CheckBox) findViewById(R.id.more_numbers_checkbox);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			exit();
			return false;
		}

		return super.onKeyDown(keyCode, event);
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
	public void generatorVCard(int contactCount, int contactFaceCount) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			Toast.makeText(this, getString(R.string.no_external_storage),
					Toast.LENGTH_SHORT).show();

			return;
		}

		submitBtn.setEnabled(false);

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
		String absolutePath = VCARD_FILE_PATH + "/" + vcardName;

		Log.d(TAG, absolutePath);

		try {
			OutputStreamWriter writer = new OutputStreamWriter(
					new FileOutputStream(vCardFile), "UTF-8");

			VCardComposer composer = new VCardComposer();
			// create a contact

			for (int i = 0; i < contactCount; i++) {
				ContactStruct contact = new ContactStruct();

				String surName = ChineseNameUtils.getChineseSurname();
				String name = ChineseNameUtils.getChineseName();
				
				String tel = ChinaPhoneUtils.getPhoneNumber();

				contact.name = surName + name;

				contact.addPhone(Contacts.Phones.TYPE_MOBILE, tel, null, true);

				// create vCard representation
				String vcardString = composer.createVCard(contact,
						VCardComposer.VERSION_VCARD30_INT);

				// write vCard to the output stream
				writer.write(vcardString);
				writer.write("\n"); // add empty lines between contacts
			}

			writer.close();

			Toast.makeText(this, "vCard:" + absolutePath, Toast.LENGTH_LONG)
					.show();

		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.vcard_generated_failed),
					Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		submitBtn.setEnabled(true);

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
						getString(R.string.no_empty_in_contacts_field),
						Toast.LENGTH_SHORT).show();

				return;
			}

			int contactCount = Integer.parseInt(contactCountStr);
			
			if(contactCount >= MAX_NUMBER_OF_COUNTS){
				Toast.makeText(mContext,
						getString(R.string.no_more_contacts_number),
						Toast.LENGTH_SHORT).show();
			}

			generatorVCard(contactCount, 0);
		}

	}
}
