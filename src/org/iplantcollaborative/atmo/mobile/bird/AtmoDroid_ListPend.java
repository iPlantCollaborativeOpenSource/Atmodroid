package org.iplantcollaborative.atmo.mobile.bird;

import android.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class AtmoDroid_ListPend extends ListActivity {
	private static ArrayAdapter<String> adapter;
	private static Handler run_handler;
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
					AtmoInstance ai = myatmo.getInstance(inst);
					AlertDialog alert = createOptions(ai);
					alert.show();
				}
			});
			if(adapter.isEmpty() ) {
				TextView tv = new TextView(this.getApplicationContext());
				tv.setText("No Pending Instances");
				this.addContentView(tv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
			run_handler = new Handler() {
				public void handleMessage(Message msg) {
					boolean complete = (msg.arg1 == 1) ? true : false; 
					if(complete == false) {
						Toast.makeText(getApplicationContext(),"Instance Termination Failed", Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(getApplicationContext(), "Instance Terminated", Toast.LENGTH_LONG).show();
					}
					//Do something with data, then dismiss dialog
				}
			};

	}
	public AlertDialog createOptions(AtmoInstance ainst) {
		final CharSequence[] items = {"Terminate Instance", "Get Details", "Cancel"};
		final AtmoInstance ai = ainst;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an Action..");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        switch(item) {
		        case 0:
		        	Thread t = new DestroyThread(ai, AtmoDroid.getAtmo());
		        	t.start();
		    		Toast.makeText(getApplicationContext(), "Terminating Instance.. ", Toast.LENGTH_LONG).show();
//		        	boolean status = AtmoDroid.getAtmo().terminateInstance(ai.getInstance_id());
//		    		if(status) {
//		    			adapter.notifyDataSetChanged();
//		    			setListAdapter(adapter);
//		    			Toast.makeText(getApplicationContext(), ai.getInstance_name()+" - Instance terminated.", Toast.LENGTH_LONG).show();
//		    		} else {
//		    			Toast.makeText(getApplicationContext(), ai.getInstance_name()+" - Failed to terminate instance.", Toast.LENGTH_LONG).show();
//		    		}
		    		break;
		        case 1:
		        	String display = ai.toString();
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
	protected void onDestroy() {
		super.onDestroy();
		if(AtmoDroid.getDialog() != null)
			AtmoDroid.getDialog().cancel();
		this.finish();
	};

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
	
	class DestroyThread extends Thread {
		AtmoAPI myatmo;
		AtmoInstance ai;
		public DestroyThread(AtmoInstance ai, AtmoAPI myatmo) {
			this.myatmo = myatmo;
			this.ai = ai;
		}
		public void run() {
        	boolean destroyed = myatmo.terminateInstance(ai.getId());
			Message m = run_handler.obtainMessage();
			m.arg1 = (destroyed == true) ? 1 : 0;
			run_handler.sendMessage(m);
		}
	}

}
