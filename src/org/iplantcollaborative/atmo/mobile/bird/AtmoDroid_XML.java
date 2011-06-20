package org.iplantcollaborative.atmo.mobile.bird;

import java.util.Set;

import esteve.apps.R;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class AtmoDroid_XML extends Activity {
	private static final int ACTIVITY_LISTVM=0;
	private static final int ACTIVITY_LISTAPPS=1;
	private static final int ACTIVITY_LISTRUNNING=2;
	private static final int ACTIVITY_LISTPENDING=3;
    /** Called when the activity is first created. */
	static String runningInstance = "";
	private static AtmoAPI myatmo; 

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        myatmo = new AtmoAPI();
        final EditText username = (EditText) findViewById(R.id.username);     
        final EditText password = (EditText) findViewById(R.id.password);
        final Button loginbutton = (Button) findViewById(R.id.button);
        final Button listrun = (Button) findViewById(R.id.listRunningButton);
        final Button listpend = (Button) findViewById(R.id.listPendingButton);
        final Button listapp = (Button) findViewById(R.id.listAppsbutton);
        final Button listimg = (Button) findViewById(R.id.listImagebutton);
        
        listrun.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				/* Move to a new view. On this view, show list of all running apps */
				listRunning();
			}
		});
        listpend.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				listPending();
			}
		});
		
        listapp.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				/* Move to a new view. On this view, show list of all apps */
				listApps();
			}
		});
        listimg.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				/* Move to a new view. On this view, show list of all VMs */
				listImages();
			}
		});
        loginbutton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v) {
        		/*if(username.getText().toString().equals("")) {
        			username.setText("esteve");
        			password.setText("smgreg224");
        		}*/
        		if(myatmo.authenticate(username.getText().toString(), password.getText().toString())) {
        			Toast.makeText(AtmoDroid_XML.this, "Authentication Success", Toast.LENGTH_LONG).show();
        			listrun.setEnabled(true);
        			listpend.setEnabled(true);
        			listapp.setEnabled(true);
        			listimg.setEnabled(true);
        		} else {
        			Toast.makeText(AtmoDroid_XML.this, "Authentication Failure", Toast.LENGTH_LONG).show();
        			listrun.setEnabled(false);
        			listpend.setEnabled(false);
        			listapp.setEnabled(false);
        			listimg.setEnabled(false);
        		}
        	}
        });

        username.setOnKeyListener(new OnKeyListener() {
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                	if(keyCode == KeyEvent.KEYCODE_TAB) {
                		password.requestFocus();
                		return true;
                	}
                	if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        // Perform action on key press
                        loginbutton.performClick();//Toast.makeText(AtmoDroid.this, password.getText(), Toast.LENGTH_SHORT).show();
                        return true;
                      } 
                }
                return false;
            }
        });
        
        password.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                	if(keyCode == KeyEvent.KEYCODE_TAB) {
                		username.requestFocus();
                		return true;
                	}
                	if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        // Perform action on key press
                        loginbutton.performClick();//Toast.makeText(AtmoDroid.this, password.getText(), Toast.LENGTH_SHORT).show();
                        return true;
                      } 
                }
                return false;
            }
        });
    }
	private void listApps() {
		Intent i = new Intent(this, AtmoDroid_ListApp.class);
    	//Get hashmap of apps
		Set<String> set = myatmo.getAppList();
		String[] list = set.toArray(new String[set.size()]);
		
    	i.putExtra("list", list);
    	startActivityForResult(i, ACTIVITY_LISTAPPS);
	}
	private void listRunning() {
		Intent i = new Intent(this, AtmoDroid_ListRun.class);
    	//Get hashmap of Running apps
    	//i.putExtra("hashmap", map);
		
		Set<String> set = myatmo.getRunningInstanceList();
		String[] list = set.toArray(new String[set.size()]); 
		i.putExtra("list", list);
    	startActivityForResult(i, ACTIVITY_LISTRUNNING);
	}
	private void listPending() {
		Intent i = new Intent(this, AtmoDroid_ListPend.class);

		Set<String> set = myatmo.getPendingInstanceList();
		String[] list = set.toArray(new String[set.size()]); 
		i.putExtra("list", list);
    	startActivityForResult(i, ACTIVITY_LISTPENDING);
	}
    private void listImages() {
    	Intent i = new Intent(this, AtmoDroid_ListImage.class);
    	//Get hashmap of VMs
    	//i.putExtra("hashmap", map);
    	//
    	Set<String> set = myatmo.getImageList();
    	String[] list = set.toArray(new String[set.size()]);
    	i.putExtra("list", list);
    	startActivityForResult(i, ACTIVITY_LISTVM);
    }
    protected static AtmoAPI getAtmo() {
    	return myatmo;
    }
}