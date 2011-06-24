package org.iplantcollaborative.atmo.mobile.bird;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class ImageRequestForm extends Activity {
	public static final String TAG = "AtmoDroid";
	private TextView rf_username_label, rf_imagename_label, rf_imagedesc_label, rf_imagesw_label, rf_ebsmount_label, rf_sysfile_label, rf_imagetag_label, rf_imagevis_label;
	private EditText rf_username_edit, rf_imagename_edit, rf_imagedesc_edit, rf_imagesw_edit, rf_ebsmount_edit, rf_sysfile_edit, rf_imagetag_edit, rf_imagevis_edit;
	private Button rf_submit;
	private Intent emailIntent;
	private String email_text = "";
	private String email_subject = "Image Creation Request for Instance ID:";
	private String email_author = "support@iplantcollaborative.org";
	private String warntext;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.requestform);
		prepareFields();
		initializeFields(getIntent().getExtras());
		//prepareEmail();
		Log.v(TAG,"IRF SETUP: Fields Prepared.");
		addListeners();
		Log.v(TAG,"IRF SETUP: Listeners Added.");
		openDialog();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d(TAG, "IRF Information:"+requestCode+","+resultCode+","+data);
		this.finish();
	}
	
	/**
	 *   @Name:openDialog
	 *  @Input:
	 * @Output: 
	 */
	private void openDialog() {
		// TODO Auto-generated method stub
		if(warntext == null) {
			warntext = "NOTE:\n";
			warntext += "The iPlant Collaborative staff cannot create VM images with licensed software that has not been purchased by iPlant or software whose licensing prevents use inside the cloud or a virtualized environment.\n"
					   +"By sending a request to Atmosphere support staff, users are expected to remove licensed software from their instance before an image is created.\n\n";
			warntext += "Other Important Notes:\n";
			warntext += "\n1. The following directories are deleted as part of the imaging process:\n";
			warntext += "/home/\n";
			warntext += "/mnt/\n";
			warntext += "tmp/\n";
			warntext += "/root/\n";
			warntext += "\n2. If you have any files installed in /home (e.g. your /home directory), please save them to your EBS volume, iRODS, or to another storage external to your VM.\n";
			warntext += "\n3. EBS volumes and iRODS FUSE mounts will not be copied as part of the image.\n";
			warntext += "\n4. System Files managed by iPlant: The following system files are typically overwritten by iPlant for security or operational reasons:\n";
			warntext += "/etc/ldap.conf\n";
			warntext += "/etc/sshd/\\*\n";
			warntext += "/var/log\n";
			warntext += "/etc/sysconfig/iptables\n";
			warntext += "/etc/passwd\n";
			warntext += "/etc/shadow\n";
			warntext += "/etc/group\n";
			warntext += "/etc/fstab\n";
			warntext += "/etc/resolve.conf\n";
			warntext += "/etc/host.conf\n";
			warntext += "/etc/hosts\n";
			warntext += "/etc/host.allow\n";
			warntext += "/etc/host.deny\n";
			warntext += "/root/\\*\n";
		}
		displayPopup(warntext);
	}

	public void displayPopup(String display) {
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		helpBuilder.setTitle("Notes before creating an Image:");
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
	
	/**
	 *   @name initializeFields 
	 *  @param extras
	 */
	private void initializeFields(Bundle extras) {
		try {
		email_subject += extras.getString("instanceid");
		rf_username_edit.setText(extras.getString("username"));
		} catch(Exception e) {Log.e(TAG,"Error parsing extras",e);}
	}

	/**
	 *   @Name:prepareEmail
	 *  @Input:
	 * @Output: 
	 */
	private void prepareEmail() {
		email_text = getString(R.string.rf_username_hint)+rf_username_edit.getText()+"\n";
		email_text += getString(R.string.rf_imagename_hint)+rf_imagename_edit.getText()+"\n";
		email_text += getString(R.string.rf_imagedesc_hint)+rf_imagedesc_edit.getText()+"\n";
		email_text += getString(R.string.rf_imagesw_hint)+rf_imagesw_edit.getText()+"\n";
		email_text += getString(R.string.rf_ebsmount_hint)+rf_ebsmount_edit.getText()+"\n";
		email_text += getString(R.string.rf_sysfile_hint)+rf_sysfile_edit.getText()+"\n";
		email_text += getString(R.string.rf_imagetag_hint)+rf_imagetag_edit.getText()+"\n";
		email_text += getString(R.string.rf_imagevis_hint)+rf_imagevis_edit.getText()+"\n";
		email_text += "Additional Information:";

		emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] { email_author  });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, email_subject);
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, email_text);
		this.startActivityForResult(Intent.createChooser(emailIntent,"Send e-mail.."),0);
	}

	/**
	 *   @Name:addListeners
	 *  @Input:
	 * @Output:Setup listeners for views 
	 */
	private void addListeners() {
		rf_submit.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				prepareEmail();
			}
		});
	}

	/**
	 *   @Name:prepareFields
	 *  @Input:
	 * @Output:All static fields will be instantiated 
	 */
	private void prepareFields() {
		rf_username_label = (TextView) findViewById(R.id.rf_username_label);
		rf_imagename_label = (TextView) findViewById(R.id.rf_imagename_label);
		rf_imagedesc_label = (TextView) findViewById(R.id.rf_imagedesc_label);
		rf_imagesw_label = (TextView) findViewById(R.id.rf_imagesw_label);
		rf_ebsmount_label = (TextView) findViewById(R.id.rf_ebsmount_label);
		rf_sysfile_label = (TextView) findViewById(R.id.rf_sysfile_label);
		rf_imagetag_label = (TextView) findViewById(R.id.rf_imagetag_label);
		rf_imagevis_label = (TextView) findViewById(R.id.rf_imagevis_label);

		rf_username_edit = (EditText) findViewById(R.id.rf_username_edit);
		rf_imagename_edit = (EditText) findViewById(R.id.rf_imagename_edit);
		rf_imagedesc_edit = (EditText) findViewById(R.id.rf_imagedesc_edit);
		rf_imagesw_edit = (EditText) findViewById(R.id.rf_imagesw_edit);
		rf_ebsmount_edit = (EditText) findViewById(R.id.rf_ebsmount_edit);
		rf_sysfile_edit = (EditText) findViewById(R.id.rf_sysfile_edit);
		rf_imagetag_edit = (EditText) findViewById(R.id.rf_imagetag_edit);
		rf_imagevis_edit = (EditText) findViewById(R.id.rf_imagevis_edit);

		rf_submit = (Button) findViewById(R.id.rf_submit);
	}
}
