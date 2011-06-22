package org.iplantcollaborative.atmo.mobile.bird;

import org.iplantcollaborative.atmo.mobile.bird.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils.TruncateAt;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

public class AtmoDroid extends Activity {
	public static final String TAG = "AtmoDroid";
	
	private static int ID_NUM = 1;
	/** Called when the activity is first created. */
	static String runningInstance = "";
	private static AtmoAPI myatmo;
	private Spinner serverspinner;
	private static final String[] servers = {"Atmo","Atmo (Beta)"};
	private static String serverselection;
	private ImageButton loginbutton;
	private EditText username, password;
	private RelativeLayout relative, block1;
	private static ProgressDialog dialog;
	private Handler login_handler;
	private boolean logged;
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// ignore orientation/keyboard change
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		prepareFields();
		Log.v(TAG,"SETUP: Fields Prepared.");
		enableButtons(false);
		Log.v(TAG,"SETUP: Buttons Enabled.");
		addListeners();
		Log.v(TAG,"SETUP: Listeners Added.");
		relative = prepareHomeLayout();
		Log.v(TAG,"SETUP: Layout Prepared.");
		setContentView(relative);
		Log.v(TAG,"SETUP: Layout Active.");
		createHandlers();
		Log.v(TAG,"SETUP: Handlers Prepared.");
		Log.v(TAG,"SETUP: Launching App.");
	}

	private void prepareFields() {
		myatmo = new AtmoAPI(serverselection);
		loginbutton = new ImageButton(this);

		username = new EditText(this);
		password = new EditText(this);
		serverspinner = new Spinner(this);
		logged = false;
		
		loginbutton.setId(ID_NUM++);
		username.setId(ID_NUM++);
		password.setId(ID_NUM++);

		serverspinner.setId(ID_NUM++);
		ArrayAdapter<String> serveradapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, servers);
		serveradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		serverspinner.setAdapter(serveradapter);
		
		loginbutton.setImageResource(R.drawable.login_button);
		loginbutton.setBackgroundResource(R.drawable.icon_bg);
		
		username.setHint(R.string.usernamelabel);
		password.setHint(R.string.passwordlabel);
		password.setEllipsize(TruncateAt.END);
		password.setSingleLine();
		password.setTransformationMethod(PasswordTransformationMethod
				.getInstance());

		
	}
	private void enableButtons(boolean enabled) {
		//Finally, Set corresponding image
		loginbutton.setImageResource((enabled == true) ? R.drawable.logout_button : R.drawable.login_button);
		
	}
	private void addListeners() {
		loginbutton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if(logged == false) {
					dialog = ProgressDialog.show(AtmoDroid.this, "Authentication",
							"Signing In..", true, true);
					Thread t = new LoginThread();
					t.start();
					return;
				} else {
					enableButtons(false);
					logged = false;
					username.setText("");
					password.setText("");
				}
			}

		});
		username.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_TAB) {
						password.requestFocus();
						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_ENTER) { 
						loginbutton.requestFocus();
						return true;
					}
				}
				return false;
			}
		});
		password.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_TAB) {
						username.requestFocus();
						return true;
					}
					if (keyCode == KeyEvent.KEYCODE_ENTER) { 
						loginbutton.requestFocus();
						return true;
					}
				}
				return false;
			}
		});
		serverspinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onNothingSelected(AdapterView<?> parentView) {
			}
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				String selection = servers[position];
				if(selection.equals("Atmo")) {
					serverselection = "https://atmo.iplantcollaborative.org:443/auth";
				} else {
					serverselection = "https://atmo-beta.iplantcollaborative.org:443/auth";
				}
				myatmo = new AtmoAPI(serverselection);
				logged = false;
				enableButtons(false);
			}
		});
	}
	private RelativeLayout prepareHomeLayout() {
		RelativeLayout.LayoutParams fillwidth = new RelativeLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams wrapcontent = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams matchwidth = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		
		
		RelativeLayout relative = new RelativeLayout(this);
		
		/* First Layer: Login Button + User/Pass */
		
		block1 = new RelativeLayout(this);
		block1.setId(ID_NUM++);
		RelativeLayout block2 = new RelativeLayout(this);
		block2.setId(ID_NUM++);
		
		RelativeLayout.LayoutParams lp_username = new RelativeLayout.LayoutParams(matchwidth);
		block1.addView(username, lp_username);
		RelativeLayout.LayoutParams lp_password = new RelativeLayout.LayoutParams(matchwidth);
		lp_password.addRule(RelativeLayout.BELOW, username.getId());
		block1.addView(password, lp_password);
		RelativeLayout.LayoutParams lp_block1 = new RelativeLayout.LayoutParams(matchwidth);
		lp_block1.addRule(RelativeLayout.LEFT_OF,loginbutton.getId());
		block2.addView(block1, lp_block1);
		
		RelativeLayout.LayoutParams lp_loginbutton = new RelativeLayout.LayoutParams(wrapcontent);
		lp_loginbutton.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
		lp_loginbutton.addRule(RelativeLayout.ALIGN_BOTTOM,block1.getId());
		lp_loginbutton.addRule(RelativeLayout.ALIGN_TOP,block1.getId());
		block2.addView(loginbutton, lp_loginbutton);
		relative.addView(block2);
		
		/* Second layer: Server selection Spinner */
		
		RelativeLayout.LayoutParams lp_serverspinner = new RelativeLayout.LayoutParams(fillwidth);
		lp_serverspinner.addRule(RelativeLayout.BELOW, block2.getId());
		relative.addView(serverspinner, lp_serverspinner);
		
		/* Third layer: Login Required Label */
		
//		RelativeLayout.LayoutParams lp_loginreqlabel = new RelativeLayout.LayoutParams(
//				matchwidth);
//		lp_loginreqlabel.addRule(RelativeLayout.BELOW, serverspinner.getId());
//		relative.addView(loginreqlabel, lp_loginreqlabel);
//
//		View ruler = new View(this);
//		ruler.setId(ID_NUM++);
//		ruler.setBackgroundColor(0xFFFFFFFF);
//		RelativeLayout.LayoutParams lp_ruler = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT,2);
//		lp_ruler.addRule(RelativeLayout.BELOW,loginreqlabel.getId());
//		relative.addView(ruler, lp_ruler);
//		
//		// Vertical Column 1
//		column1 = new RelativeLayout(this);
//		column1.setId(ID_NUM++);
//		RelativeLayout.LayoutParams lp_column1 = new RelativeLayout.LayoutParams(
//				wrapcontent);
//		lp_column1.addRule(RelativeLayout.BELOW, loginreqlabel.getId());
//
//		// Content Vertical Column 1
//		RelativeLayout.LayoutParams lp_createlabel = new RelativeLayout.LayoutParams(
//				wrapcontent);
//		column1.addView(createlabel, lp_createlabel);
//
//		RelativeLayout.LayoutParams lp_listimg = new RelativeLayout.LayoutParams(
//				fillwidth);
//		lp_listimg.addRule(RelativeLayout.BELOW, createlabel.getId());
//		lp_listimg.addRule(RelativeLayout.ALIGN_RIGHT, createlabel.getId());
//		column1.addView(listimg, lp_listimg);
//
//		RelativeLayout.LayoutParams lp_listapp = new RelativeLayout.LayoutParams(
//				fillwidth);
//		lp_listapp.addRule(RelativeLayout.BELOW, listimg.getId());
//		lp_listapp.addRule(RelativeLayout.ALIGN_RIGHT, createlabel.getId());
//		column1.addView(listapp, lp_listapp);
//		// Add First Vertical Column
//		relative.addView(column1, lp_column1);
//
//		// Vertical Column 2
//		column2 = new RelativeLayout(this);
//		column2.setId(ID_NUM++);
//		RelativeLayout.LayoutParams lp_column2 = new RelativeLayout.LayoutParams(
//				wrapcontent);
//		lp_column2.addRule(RelativeLayout.RIGHT_OF, column1.getId());
//		lp_column2.addRule(RelativeLayout.ALIGN_TOP, column1.getId());
//		// Content Vertical Column 2
//		RelativeLayout.LayoutParams lp_removelabel = new RelativeLayout.LayoutParams(
//				wrapcontent);
//		column2.addView(removelabel, lp_removelabel);
//
//		RelativeLayout.LayoutParams lp_listpend = new RelativeLayout.LayoutParams(
//				wrapcontent);
//		lp_listpend.addRule(RelativeLayout.BELOW, removelabel.getId());
//		lp_listpend.addRule(RelativeLayout.ALIGN_TOP, listimg.getId());
//		column2.addView(listpend, lp_listpend);
//
//		RelativeLayout.LayoutParams lp_listrun = new RelativeLayout.LayoutParams(
//				wrapcontent);
//		lp_listrun.addRule(RelativeLayout.BELOW, listpend.getId());
//		lp_listrun.addRule(RelativeLayout.ALIGN_TOP, listapp.getId());
//		column2.addView(listrun, lp_listrun);
//
//		relative.addView(column2, lp_column2);

		return relative;
	}
	private void createHandlers() {
		login_handler = new Handler() {
			public void handleMessage(Message msg) {
				// Differentiate message
				try {
					Bundle b = msg.getData();
					boolean value = b.getBoolean("truth");
					if (value) {
						Toast.makeText(getApplicationContext(),
								"Authentication Success", Toast.LENGTH_LONG).show();
						enableButtons(true);
						logged = true;
						Intent i = new Intent(getApplicationContext(), ListInstances.class);
						i.putExtra("atmoapi", myatmo);
						startActivityForResult(i, 0);
					} else {
						Toast.makeText(getApplicationContext(),
								"Authentication Failed", Toast.LENGTH_LONG).show();
						enableButtons(false);
						logged = false;
					}
				} catch (Exception e) {
					;
				}
				if (dialog != null)
					dialog.dismiss();
			}
		};
	}

	protected static String getSelectedServer() {
		if(serverselection.contains("beta"))
			return "https://atmo-beta.iplantcollaborative.org";
		else
			return "https://atmo.iplantcollaborative.org";
	}
	protected static AtmoAPI getAtmo() {
		return myatmo;
	}
	public static ProgressDialog getDialog() {
		return dialog;
	}
	public Message sendBundle(Handler handler, Bundle b) {
		Message m = handler.obtainMessage();
		m.setData(b);
		return m;
	}
	//Private Classes

	class LoginThread extends Thread {
		@Override
		public void run() {
			try {
				boolean value = myatmo.authenticate(username.getText()
						.toString().toLowerCase(), password.getText().toString());
				Bundle b = new Bundle();
				b.putBoolean("truth", value);
				login_handler.sendMessage(sendBundle(login_handler, b));
			} catch (Exception e) {
				Log.e(AtmoDroid.TAG,"---");
				Log.e(AtmoDroid.TAG,"EXCEPTION :"+e.getMessage());
				Log.e(AtmoDroid.TAG,Log.getStackTraceString(e));
				Log.e(AtmoDroid.TAG,"---");
			}

		}
	}
}
