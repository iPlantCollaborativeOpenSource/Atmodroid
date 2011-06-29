/** @Title :CreateInstanceTab.java
 *  @Author:Steven "esteve" Gregory
 *    @Date:Jun 29, 2011 - 8:28:13 AM
 */
package org.iplantcollaborative.atmo.mobile.bird;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

/**
 * @author esteve
 *
 */
public class CreateInstanceTab extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.tabs);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, CreateInstanceFromApp.class);
	    intent.putExtra("atmoapi", getIntent().getExtras().getParcelable("atmoapi"));
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("apps").setIndicator("Apps",
	                      res.getDrawable(R.drawable.atmoslogo_normal))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, CreateInstanceFromImg.class);
	    intent.putExtra("atmoapi", getIntent().getExtras().getParcelable("atmoapi"));
	    spec = tabHost.newTabSpec("images").setIndicator("Images",
	                      res.getDrawable(R.drawable.atmoslogo_normal))
	                  .setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(0);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
	}

}
