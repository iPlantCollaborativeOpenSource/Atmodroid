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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class AtmoDroid_ListImage extends ListActivity {

	private static ArrayAdapter<String> adapter;
	private static Handler img_handler;
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
				String image = ((TextView)view).getText().toString();
				//Lookup info on VM and show
				AtmoAPI myatmo = AtmoDroid.getAtmo();
				AtmoImage ai = myatmo.getImage(image);
				AlertDialog alert = createOptions(ai);
				alert.show();
			}
		});
		if(adapter.isEmpty() ) {
			TextView tv = new TextView(this.getApplicationContext());
			tv.setText("No Images Available");
			this.addContentView(tv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		}
		//TODO: Duplicate code in AtmoDroid_ListApp, have both refer to this in another class\\
		img_handler = new Handler() {
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

	public AlertDialog createOptions(AtmoImage aimg) {
		final String error = "Failed to create an instance from this image.";
		final CharSequence[] items = {"Create an Instance", "Get Details", "Cancel"};
		final AtmoImage ai = aimg;
		final AtmoAPI myatmo = AtmoDroid.getAtmo();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an Action..");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        switch(item) {
		        case 0:
		        	Thread t = new LaunchThread(ai, myatmo);
		        	t.start();
		    		Toast.makeText(getApplicationContext(), "Creating Instance.. ", Toast.LENGTH_LONG).show();
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

	class LaunchThread extends Thread {
		AtmoAPI myatmo;
		AtmoImage ai;
		public LaunchThread(AtmoImage ai, AtmoAPI myatmo) {
			this.myatmo = myatmo;
			this.ai = ai;
		}
		public void run() {
			String inst_id = myatmo.launchInstance(ai.getName(), ai, null);
			Bundle b = new Bundle();
			b.putString("instanceID", inst_id);
			Message m = img_handler.obtainMessage();
			m.setData(b);
			img_handler.sendMessage(m);
		}
	}
}
