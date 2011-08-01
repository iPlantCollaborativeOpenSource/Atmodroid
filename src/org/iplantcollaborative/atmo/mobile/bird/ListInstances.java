package org.iplantcollaborative.atmo.mobile.bird;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * TODO: Add option to turn instance in to an image by sending pre-compiled
 * email to atmo
 * 
 * 
 */
public class ListInstances extends ListActivity implements OnClickListener {
	private ProgressDialog m_ProgressDialog = null;
	private AlertDialog helpDialog = null;
	private ArrayList<AtmoInstance> m_orders = null;
	private OrderAdapter m_adapter;
	private Runnable viewOrders;
	private AtmoAPI myatmo;
	private Handler run_handler;
	private AtmoInstance ai;
	private LayoutInflater mInflater;
	

	/**
	 * Refreshes the list of instances after an activity is complete (Create app/img)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			Thread thread = new Thread(null, viewOrders, "AtmoDroidBackground");
			thread.start();
			m_ProgressDialog = ProgressDialog.show(ListInstances.this,
					"Downloading Data", "Retrieving Instances From Atmo..",
					true);
		}
	}

	/**
	 * Refreshes the list of instances after the activity is resumed
	 */
	@Override
	protected void onResume() {
		//Refresh if m_ProgressDialog is not showing or initialized and helpdialog is not showing or initialized..
		super.onResume();
		if((m_ProgressDialog == null || m_ProgressDialog.isShowing() == false) && (helpDialog == null || helpDialog.isShowing() == false)){
			m_ProgressDialog = ProgressDialog.show(ListInstances.this,
					"Downloading Data",
					"Retrieving Instances From Atmo..", true);
			Thread thread = new Thread(null, viewOrders,
					"AtmoDroidBackground");
			thread.start();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.listinstances);

		mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		//Cancel any current notification / progress dialog
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		mNotificationManager.cancelAll();
		Bundle b = getIntent().getExtras();
		myatmo = b.getParcelable("atmoapi");
		m_orders = new ArrayList<AtmoInstance>();
		this.m_adapter = new OrderAdapter(this, R.layout.row, m_orders);
		setListAdapter(this.m_adapter);

		viewOrders = new Runnable() {
			public void run() {
				getOrders();
			}
		};
		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long id) {
				String inst = ((TextView)((RelativeLayout) view).getChildAt(2)).getText().toString(); 
				inst = inst.replace(getNamePrefix(), "");

				// Lookup info on VM and show
				ai = myatmo.getInstance(inst);
				AlertDialog alert = createOptions(ai);
				alert.show();
			}

		});
		Thread thread = new Thread(null, viewOrders, "AtmoDroidBackground");
		thread.start();
		run_handler = new Handler() {
			/**
			 * Notify user of instance termination status
			 */
			public void handleMessage(Message msg) {
				boolean complete = (msg.arg1 == 1) ? true : false;
				if (complete == false) {
					Toast.makeText(getApplicationContext(),
							"Instance Termination Failed", Toast.LENGTH_LONG)
							.show();
				} else {
					//Show dialog & refresh screen
					if(m_ProgressDialog == null || m_ProgressDialog.isShowing() == false)
						m_ProgressDialog = ProgressDialog.show(ListInstances.this,"Downloading Data","Retrieving Instances From Atmo..", true);
					Thread thread = new Thread(null, viewOrders,
							"AtmoDroidBackground");
					thread.start();
				}
				// Do something with data, then dismiss dialog
			}
		};
		//First time load, show dialog
		if(m_ProgressDialog == null || m_ProgressDialog.isShowing() == false)
			m_ProgressDialog = ProgressDialog.show(ListInstances.this,"Downloading Data", "Retrieving Instances From Atmo..", true);
	}

	// Context Menu (Long Press)
	public AlertDialog createOptions(AtmoInstance ainst) {
		final CharSequence[] items = { "Terminate Instance", "Get Details",
				"Create Image from Instance", "Cancel" };
		ai = ainst;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an Action..");
		builder.setItems(items, this);
		return builder.create();
	}

	public void onClick(DialogInterface dialog, int item) {
		switch (item) {
		case 0://Terminate Instance
			if(m_ProgressDialog != null && m_ProgressDialog.isShowing()) {
				Log.e(AtmoDroid.TAG,"Special case progressdialog+onclick");
				return;
				//Don't start another thread if we're waiting..
			}
			Thread t = new DestroyThread(ai, AtmoDroid.getAtmo());
			t.start();
			Toast.makeText(getApplicationContext(), "Terminating Instance..",
					Toast.LENGTH_LONG).show();
			break;
		case 1://Get Details
			String display = ai.toString();
			displayPopup(display);
			break;
		case 2:
			Intent i = new Intent(getApplicationContext(), ImageRequestForm.class);
			i.putExtra("instanceid", ai.getId());
			i.putExtra("username", myatmo.getUser());
			startActivityForResult(i, 0);
			break;
		default:
			break;
		}
	}

	public void displayPopup(String display) {
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		helpBuilder.setTitle("Instance Details");
		helpBuilder.setMessage(display);
		helpBuilder.setPositiveButton("Done",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						// Do nothing but close the dialog
					}
				});

		// Remember, create doesn't show the dialog
		helpDialog = helpBuilder.create();
		helpDialog.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem return_item = menu.add("Back");
		return_item.setIcon(R.drawable.ic_menu_revert);
		MenuItem refresh_item = menu.add("Refresh");
		refresh_item.setIcon(R.drawable.ic_menu_rotate);
		MenuItem create_item = menu.add("Create Instance");
		create_item.setIcon(R.drawable.ic_menu_add);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getTitle().equals("Back")) {
			this.finish();
		} else if (item.getTitle().equals("Refresh")) {
			m_ProgressDialog = ProgressDialog.show(ListInstances.this,
					"Downloading Data", "Retrieving Instances From Atmo..",
					true);
			Thread thread = new Thread(null, viewOrders, "AtmoDroidBackground");
			thread.start();
		} else if (item.getTitle().equals("Create Instance")) {
			Intent i = new Intent(getApplicationContext(), CreateInstanceTab.class);
			i.putExtra("atmoapi", myatmo);
			startActivityForResult(i, 0);
		}
		return true;
	}

	class DestroyThread extends Thread {
		AtmoInstance ai;
		AtmoAPI myatmo;

		public DestroyThread(AtmoInstance ai, AtmoAPI myatmo) {
			this.ai = ai;
			this.myatmo = myatmo;
		}

		public void run() {
			boolean destroyed = myatmo.terminateInstance(ai.getId());
			Message m = run_handler.obtainMessage();
			m.arg1 = (destroyed == true) ? 1 : 0;
			run_handler.sendMessage(m);
		}
	}

	// End Context Menu (Long Press)
	private Runnable returnRes = new Runnable() {
		// A Smaller thread to be run on the UI after slow method completes
		public void run() {
			m_ProgressDialog.dismiss();
			if (m_orders != null && m_orders.size() > 0) {
				m_adapter.clear();
				m_adapter.notifyDataSetChanged();
				for (int i = 0; i < m_orders.size(); i++)
					m_adapter.add(m_orders.get(i));
			}
			m_adapter.notifyDataSetChanged();
		}
	};

	private void getOrders() {
		try {
			// Get AtmoInstances from server & add to array
			m_orders = new ArrayList<AtmoInstance>(myatmo.getInstances()
					.values());
			Log.i("ARRAY", "" + m_orders.size());
		} catch (Exception e) {
			Log.e("BACKGROUND_PROC", e.getMessage());
		}
		// Call fast UI thread after slow part is done..
		runOnUiThread(returnRes);
	}

	// Custom ListActivity Adapter
	private class OrderAdapter extends ArrayAdapter<AtmoInstance> {

		private ArrayList<AtmoInstance> items;

		public OrderAdapter(Context context, int textViewResourceId,
				ArrayList<AtmoInstance> items) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Each time Adapter refreshes..
			ViewHolder holder;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.row, parent, false);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.toptext);
				holder.desc = (TextView) convertView
						.findViewById(R.id.bottomtext);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon2);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// Find the Instance corresponding to the position in the adapter
			AtmoInstance o = items.get(position);
			if (o != null) {
				// Add Name, Status and Image to row before returning view.
				holder.name.setText(getNamePrefix() + o.getName());
				holder.name.setSelected(true);
				String status = o.getInstance_state();
				holder.desc.setText(getStatusPrefix() + status);
				holder.desc.setSelected(true);
				holder.icon.setImageResource((status.equalsIgnoreCase("pending")) ? R.drawable.pending : (status.equalsIgnoreCase("running")) ? R.drawable.running : R.drawable.shutting_down);
			}
			return convertView;
		}
	}

	private String getNamePrefix() {
		return "Name: ";
	}

	private String getStatusPrefix() {
		return "Status: ";
	}
}