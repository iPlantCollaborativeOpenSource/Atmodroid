package org.iplantcollaborative.atmo.mobile.bird;

import org.iplantcollaborative.atmo.mobile.bird.R;


import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
public class CreateInstanceFromApp extends ListActivity {
  private ProgressDialog m_ProgressDialog = null;
  private ArrayList<AtmoApp> m_orders = null;
  private OrderAdapter m_adapter;
  private Runnable viewOrders;
  private AtmoAPI myatmo;
  private Handler run_handler;
  private LayoutInflater mInflater;


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
      Bundle b = getIntent().getExtras();
      if (b != null && b.getParcelable("atmoapi") != null) {
			myatmo = b.getParcelable("atmoapi");
		} else {
			myatmo = AtmoDroid.getAtmo();
		}
      mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

      m_orders = new ArrayList<AtmoApp>();
      this.m_adapter = new OrderAdapter(this, R.layout.row, m_orders);
      setListAdapter(this.m_adapter);
     
      viewOrders = new Runnable(){
          public void run() {
              getOrders();
          }
      };
      ListView lv = getListView();
      lv.setTextFilterEnabled(true);
      lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				String inst = ((TextView)((RelativeLayout) view).getChildAt(2)).getText().toString();
				inst = inst.replace(getNamePrefix(), "");

				//Lookup info on VM and show
				Bundle b = getIntent().getExtras();
				if (b != null && b.getParcelable("atmoapi") != null) {
					myatmo = b.getParcelable("atmoapi");
				} else {
					myatmo = AtmoDroid.getAtmo();
				}
				AtmoApp ai = myatmo.getApp(inst);
				AlertDialog alert = createOptions(ai);
				alert.show();
			}

      });
      Thread thread =  new Thread(null, viewOrders, "AtmoDroidBackground");
      thread.start();
      run_handler = new Handler() {
			public void handleMessage(Message msg) {
				Bundle b = msg.getData();
				String inst_id = b.getString("instanceID");
				if(inst_id == null) {
					Toast.makeText(getApplicationContext(),"Instance Creation Failed", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(getApplicationContext(), "Instance is being created..\nYou will be notified when creation is complete.", Toast.LENGTH_LONG).show();
					onDestroy();
				}
			}
		};
		//Present a dialog box if not already showing
		if(m_ProgressDialog == null || m_ProgressDialog.isShowing() == false)
			m_ProgressDialog = ProgressDialog.show(CreateInstanceFromApp.this, "Downloading Data", "Retrieving Apps From Atmo..", true);
  }
  	//Context Menu (Long Press)
	public AlertDialog createOptions(AtmoApp aapp) {
		final CharSequence[] items = {"Create Instance", "Get Description", "Cancel"};
		final AtmoApp aa = aapp;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select an Action..");
		builder.setItems(items, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		        switch(item) {
		        case 0:
		        	Thread t = new LaunchThread(aa, myatmo);
		        	t.start();
		    		Toast.makeText(getApplicationContext(), "Creating Instance.. ", Toast.LENGTH_LONG).show();
		    		break;
		        case 1:
		        	String display = aa.toString();
		        	displayPopup(display);
		        	//detailsPopup(display);
		        	//Toast.makeText(getApplicationContext(), display, Toast.LENGTH_LONG).show();
		        	break;
	        	default:
	        		break;
		        }
		    }


		});
		return builder.create();
	}
	public void displayPopup(String display) {
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		helpBuilder.setTitle("App");
		helpBuilder.setMessage(display);
		helpBuilder.setPositiveButton("Done",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// Do nothing but close the dialog
				}
			});
		  
		  // Remember, create doesn't show the dialog
		  AlertDialog helpDialog = helpBuilder.create();
		  helpDialog.show();
	 }


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuItem return_item = menu.add("Back");
		return_item.setIcon(R.drawable.ic_menu_revert);
		MenuItem refresh_item = menu.add("Refresh");
		refresh_item.setIcon(R.drawable.ic_menu_rotate);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getTitle().equals("Back")) {
			this.finish();
		} else if(item.getTitle().equals("Refresh")) {
			m_ProgressDialog = ProgressDialog.show(CreateInstanceFromApp.this,    
		            "Downloading Data", "Retrieving Apps From Atmo..", true);
			Thread thread =  new Thread(null, viewOrders, "AtmoDroidBackground");
		    thread.start();
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
			Message m = run_handler.obtainMessage();
			m.setData(b);
			run_handler.sendMessage(m);
		}
	}
//	class DestroyThread extends Thread {
//		AtmoInstance ai;
//		AtmoAPI myatmo;
//
//		public DestroyThread(AtmoInstance ai, AtmoAPI myatmo) {
//			this.ai = ai;
//			this.myatmo = myatmo;
//		}
//		public void run() {
//        	boolean destroyed = myatmo.terminateInstance(ai.getId());
//			Message m = run_handler.obtainMessage();
//			m.arg1 = (destroyed == true) ? 1 : 0;
//			run_handler.sendMessage(m);
//		}
//	}
	//End Context Menu (Long Press)
  private Runnable returnRes = new Runnable() {
  	//A Smaller thread to be run on the UI after slow method completes
      public void run() {
      	//If 1+ instances... Notify datasetchange... Add each instance to adapter.
          if(m_orders != null && m_orders.size() > 0){
        	  m_adapter.clear();
              m_adapter.notifyDataSetChanged();
              for(int i=0;i<m_orders.size();i++)
              m_adapter.add(m_orders.get(i));
          }
          m_ProgressDialog.dismiss();
          m_adapter.notifyDataSetChanged();
      }
  };
  private void getOrders(){
        try{
        	// Get AtmoInstances from server & add to array
            m_orders = new ArrayList<AtmoApp>(myatmo.getApps().values());
            Log.i("ARRAY", ""+ m_orders.size());
          } catch (Exception e) {
            Log.e("BACKGROUND_PROC", e.getMessage());
          }
          //Call fast UI thread after slow part is done..
          runOnUiThread(returnRes);
      }

  //Custom ListActivity Adapter
  private class OrderAdapter extends ArrayAdapter<AtmoApp> {

      private ArrayList<AtmoApp> items;

      public OrderAdapter(Context context, int textViewResourceId, ArrayList<AtmoApp> items) {
              super(context, textViewResourceId, items);
              this.items = items;
      }


		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// Each time Adapter refreshes..
			ViewHolder holder;

			if (convertView == null) {
//				convertView = mInflater.inflate(R.layout.row, null);
				convertView = mInflater.inflate(R.layout.row, parent, false);

				holder = new ViewHolder();
				holder.name = (TextView) convertView.findViewById(R.id.toptext);
				holder.desc = (TextView) convertView.findViewById(R.id.bottomtext);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon2);
				//holder.icon.setBackgroundResource(R.drawable.app1);
				//Bitmap imgImage = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.app1), holder.icon.getWidth(), holder.icon.getHeight(), true);
				//holder.icon.setImageBitmap(imgImage);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			// Find the Instance corresponding to the position in the adapter
			AtmoApp o = items.get(position);
			if (o != null) {
				// Add Name, Status and Image to row before returning view.
				holder.name.setText(getNamePrefix() + o.getName());
				holder.name.setSelected(true);
				holder.desc.setText(getDescPrefix() + AtmoObject.removeTags(o.getDesc()));
				holder.desc.setSelected(true);
				holder.icon.setImageDrawable(drawable_from_url(AtmoDroid.getSelectedServer()+o.getIcon_path()));
			}
			return convertView;
		}
  }
	private String getNamePrefix() {return "Name: ";}
	private String getDescPrefix() {return "Desc: ";}
	private BitmapDrawable drawable_from_url(String url) {
		Bitmap x = null;
		try {
			InputStream input = new URL(url).openStream();
			x = BitmapFactory.decodeStream(input);
			input.close();
		} catch(Exception e) {
			Log.e(AtmoDroid.TAG,Log.getStackTraceString(e));
			
		}
		return new BitmapDrawable(x);
	}
}