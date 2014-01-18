package com.newhope.contactsvcard;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.VCardComposer;

public class ContactsVCardActivity extends Activity {
	private Button submitBtn = null;
	private ClearEditText contactsCountField = null;
	private ClearEditText contactsFaceCountField = null;
	private ArrayList<String> familyNames = new ArrayList<String>();
	
	private AssetManager am = null;
	
	public static final String TAG = "ContactsVCardActivity";
	public static final String VCARD_FILE_PATH = Environment.getExternalStorageDirectory() + "/vcard";
	private final String familyNameFilePath = "txt/Chinese family name.txt";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contacs_vcard);
		
		submitBtn = (Button)findViewById(R.id.submit_btn);
		submitBtn.setOnClickListener(new OnSubmitBtnClickListener(this));
		
		contactsCountField = (ClearEditText)findViewById(R.id.contacts_counts);
		contactsFaceCountField = (ClearEditText)findViewById(R.id.contacts_face_counts);
		
		am = getAssets();
	}
	
	private void readChineseFamilyName(){
		try{
			InputStream familyName = am.open(familyNameFilePath);
			BufferedReader br = 
				     new BufferedReader(new InputStreamReader(familyName));
			
			String line = "";
			
			while((line = br.readLine()) != null){
				String[] str = line.split("\\s");
				
				for(int j=0; j<str.length; j++){
					familyNames.add(str[j]);
				}
			}
			
			am.close();
		}catch(Exception e){
			Toast.makeText(this, getString(R.string.open_file_failed), Toast.LENGTH_SHORT).show();
		}
	}
	
	public String generateFamilyName(){
		if(familyNames.size() == 0){
			return null;
		}
		
		int index = (int)(Math.random()*familyNames.size());
		
		return familyNames.get(index);
	}
	
	@SuppressLint("SimpleDateFormat")
	public void generatorVCard(int contactCount, int contactFaceCount){
		if(contactCount > 5000){
			Toast.makeText(this, getString(R.string.no_more_contacts_number), Toast.LENGTH_SHORT).show();
			return;
		}
		
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			Toast.makeText(this, getString(R.string.no_external_storage), Toast.LENGTH_SHORT).show();
			
			return ;
		}
		
		submitBtn.setEnabled(false);
		
		File vCardDir = new File(VCARD_FILE_PATH);
		
		if(!vCardDir.exists()){
			vCardDir.mkdir();
		}
		
		if(familyNames.size() == 0){
			readChineseFamilyName();
		}
		
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
		String date = sDateFormat.format(new java.util.Date());
		String vcardName = date + ".vcf";
		
		File vCardFile = new File(VCARD_FILE_PATH, vcardName);
		String absolutePath = VCARD_FILE_PATH+"/" + vcardName;
		
		Log.d(TAG, absolutePath);
		
		try{
			OutputStreamWriter writer = new OutputStreamWriter(
	                new FileOutputStream(vCardFile), "UTF-8");
			
			VCardComposer composer = new VCardComposer();
	        //create a contact
			
			for(int i=0; i<contactCount; i++){
				ContactStruct contact = new ContactStruct();
		        
		        String familyName = generateFamilyName();
		        String secondName = generateSecondName();
		        String tel = randomGenerateTel();
		        
		        contact.name = familyName + secondName;
		        
		        contact.addPhone(Contacts.Phones.TYPE_MOBILE, tel, null, true);
		        
		        //create vCard representation
		        String vcardString = composer.createVCard(contact, VCardComposer.VERSION_VCARD30_INT);
		        
		      //write vCard to the output stream
		        writer.write(vcardString);
		        writer.write("\n"); //add empty lines between contacts
			}
	        
			writer.close();
			
			Toast.makeText(this, "vCard:" + absolutePath, Toast.LENGTH_LONG).show();
	        
		}catch(Exception e){
			Toast.makeText(this, getString(R.string.vcard_generated_failed), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
		
		submitBtn.setEnabled(true);
		
	}
	
	public String randomGenerateTel(){
		StringBuffer tel = new StringBuffer("1");
		
		for(int i=0; i<10; i++){
			tel.append((int)(Math.random()*10));
		}
		
		return tel.toString();
	}
	
	public String generateSecondName(){
		int startIndex = 0x4E00;
		int endIndex = 0x9FFF;
		int range = (endIndex - startIndex);
		int valueUnicode = 0;
		int secondNameLength = (int)(Math.random()*2);
		
		StringBuffer secondName = new StringBuffer();
		
		while(secondNameLength >= 0){
			valueUnicode = startIndex + (int)(Math.random()*range);
			String unicodeName = "\\u" + Integer.toHexString(valueUnicode).toUpperCase();
			secondName.append(unicodeToUtf8(unicodeName));
			
			secondNameLength --;
		}
		
		return secondName.toString();
	}
	
	/**
    *
    * @param theString
    * @return String
    */
   public static String unicodeToUtf8(String theString) {
       char aChar;
       int len = theString.length();
       StringBuffer outBuffer = new StringBuffer(len);
       for (int x = 0; x < len;) {
           aChar = theString.charAt(x++);
           if (aChar == '\\') {
               aChar = theString.charAt(x++);
               if (aChar == 'u') {
                   // Read the xxxx
                   int value = 0;
                   for (int i = 0; i < 4; i++) {
                       aChar = theString.charAt(x++);
                       switch (aChar) {
                       case '0':
                       case '1':
                       case '2':
                       case '3':
                       case '4':
                       case '5':
                       case '6':
                       case '7':
                       case '8':
                       case '9':
                           value = (value << 4) + aChar - '0';
                           break;
                       case 'a':
                       case 'b':
                       case 'c':
                       case 'd':
                       case 'e':
                       case 'f':
                           value = (value << 4) + 10 + aChar - 'a';
                           break;
                       case 'A':
                       case 'B':
                       case 'C':
                       case 'D':
                       case 'E':
                       case 'F':
                           value = (value << 4) + 10 + aChar - 'A';
                           break;
                       default:
                           throw new IllegalArgumentException(
                                   "Malformed   \\uxxxx   encoding.");
                       }
                   }
                   outBuffer.append((char) value);
               } else {
                   if (aChar == 't')
                       aChar = '\t';
                   else if (aChar == 'r')
                       aChar = '\r';
                   else if (aChar == 'n')
                       aChar = '\n';
                   else if (aChar == 'f')
                       aChar = '\f';
                   outBuffer.append(aChar);
               }
           } else
               outBuffer.append(aChar);
       }
       return outBuffer.toString();
   }
	
	private class OnSubmitBtnClickListener implements OnClickListener{
		private Context mContext; 
		public OnSubmitBtnClickListener(Context context){
			mContext = context;
		}

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			String contactCountStr = contactsCountField.getText().toString();
			
			if(null == contactCountStr || "".endsWith(contactCountStr)){
				Toast.makeText(mContext, getString(R.string.no_empty_in_contacts_field), Toast.LENGTH_SHORT).show();
				
				return;
			}
			
			int contactCount = Integer.parseInt(contactCountStr);
			int contactFaceCount = 0;
			
			String contactFaceCountStr = contactsFaceCountField.getText().toString();
			
			if(null != contactCountStr && !"".endsWith(contactFaceCountStr)){
				contactFaceCount = Integer.parseInt(contactFaceCountStr);
			}

			generatorVCard(contactCount, contactFaceCount);
		}
		
	}
}
