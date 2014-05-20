package com.newhope.contactsvcard;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.newhope.contactsvcard.activities.NormalContactsVCardActivity;
import com.newhope.contactsvcard.activities.SameContactsVCardActivity;
import com.newhope.contactsvcard.activities.ContactsWithAvatarVCardActivity;


public class ContactsVCardActivity extends Activity{
	
	/*两次返回键之间的间隔*/
	private long exitTime = 0;
	private GridView gridViews = null;
	private ArrayList<Class<?>> vcardActivities;
	private ArrayList<String> activityTitles;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_contacs_vcard);
		
		vcardActivities = new ArrayList<Class<?>>();
		
		activityTitles = new ArrayList<String>();
		
		activityTitles.add("完全相同联系人");
		activityTitles.add("普通联系人");
		activityTitles.add("有头像联系人");
		activityTitles.add("自定义联系人");
		activityTitles.add("从联系人生成");
		activityTitles.add("添加头像资源");

		gridViews = (GridView)findViewById(R.id.grid_views);
		gridViews.setAdapter(new GridViewAdapter(this));
		gridViews.setOnItemClickListener(new GridViewOnClickListener());
		
		setupVCardModules();
	}
	
	
	
	public void setupVCardModules(){
		vcardActivities.add(SameContactsVCardActivity.class);
		vcardActivities.add(NormalContactsVCardActivity.class);
		vcardActivities.add(ContactsWithAvatarVCardActivity.class);
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
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
	
	public class GridViewAdapter extends BaseAdapter{
		private Context mContext;
		private LayoutInflater layoutInflater;

		public GridViewAdapter(Context context){
			mContext = context;
			
			layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return activityTitles.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			TextView titleView = null;
			
			if(convertView == null){
				titleView = (TextView) layoutInflater.inflate(R.layout.grid_item_view_layout, null);
			}else{
				titleView = (TextView) convertView;
			}

			titleView.setText(activityTitles.get(position));
			
			return titleView;
		}
		
	}
	
	public class GridViewOnClickListener implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub

			if(position < vcardActivities.size()){
				Class<?> targetActivityClass = vcardActivities.get(position);
				Intent intent = new Intent(ContactsVCardActivity.this, targetActivityClass);
				startActivity(intent);
			}else{
				Toast.makeText(getApplicationContext(), "it is comming soon", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
}
