package org.iplantcollaborative.atmo.mobile.bird;


import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AtmoDroid_ListApp extends ListActivity {
	private static ArrayAdapter<String> adapter;
	private static Handler app_handler;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			String[] list = (savedInstanceState == null) ? null : 
	        	(String[]) savedInstanceState.getSerializable("list");
			 if(list == null) {
		        	Bundle extras = getIntent().getExtras();
		        	list = extras != null ? extras.getStringArray("list") : null;
		        }
			adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, list);
			setListAdapter(adapter);
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
					String inst = ((TextView)view).getText().toString();
					//Lookup info on VM and show
					AtmoAPI myatmo = AtmoDroid.getAtmo();
					AtmoApp aa = myatmo.getApp(inst);
					AlertDialog alert = createOptions(aa);
					alert.show();
				}
				
			});
			if(adapter.isEmpty() ) {
				TextView tv = new TextView(this.getApplicationContext());
				tv.setText("No Apps Available");
				this.addContentView(tv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
			app_handler = new Handler() {
				public void handleMessage(Message msg) {
					Bundle b = msg.getData();
					String inst_id = b.getString("instanceID");
					if(inst_id == null) {
						Toast.makeText(getApplicationContext(),"Instance Creation Failed", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getApplicationContext(), "Instance created:"+inst_id, Toast.LENGTH_LONG).show();
					}
					//Do something with data, then dismiss dialog
				}
			};
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(AtmoDroid.getDialog() != null)
			AtmoDroid.getDialog().cancel();
		this.finish();
	};

	public AlertDialog createOptions(AtmoApp aapp) {
		final String error = "Failed to create an instance from this app.";
		final CharSequence[] items = {"Create an Instance", "Get Description", "Cancel"};
		final AtmoApp aa = aapp;
		final AtmoAPI myatmo = AtmoDroid.getAtmo();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an Action..");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialogInt, int item) {
		        switch(item) {
		        case 0:
		        	Thread t = new LaunchThread(aa, myatmo);
		        	t.start();
		    		Toast.makeText(getApplicationContext(), "Creating Instance.. ", Toast.LENGTH_LONG).show();
		    		break;
		        case 1:
		        	String display = aa.getName()+" : "+aa.getDesc();
		        	Toast.makeText(getApplicationContext(), display, Toast.LENGTH_LONG).show();
		        	break;
	        	default:
	        		break;
		        }
		    }


		});
		return builder.create();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem return_item = menu.add("Back");
		return_item.setIcon(R.drawable.ic_media_previous);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Back")) {
			this.finish();
		}
		return true;
	}
	
	class LaunchThread extends Thread {
		AtmoAPI myatmo;
		AtmoApp aa;
		public LaunchThread(AtmoApp aa, AtmoAPI myatmo) {
			this.myatmo = myatmo;
			this.aa = aa;
		}
		public void run() {
			String inst_id = myatmo.launchApp(aa.getName(), aa, null);
			Bundle b = new Bundle();
			b.putString("instanceID", inst_id);
			Message m = app_handler.obtainMessage();
			m.setData(b);
			app_handler.sendMessage(m);
		}
	}
}
