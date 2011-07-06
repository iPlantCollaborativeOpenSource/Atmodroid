/** @Title :C2DMReceiver.java
 *  @Author:Steven "esteve" Gregory
 *    @Date:Jun 24, 2011 - 1:42:48 PM
 */
package org.iplantcollaborative.atmo.mobile.bird;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class C2DMReceiver extends C2DMBaseReceiver {
	private static final String TAG = "AtmoDroid";
	public C2DMReceiver() {
		// Email address currently not used by the C2DM Messaging framework
		super("dummy@google.com");
	}

	@Override
	public void onRegistered(Context context, String registrationId)
			throws java.io.IOException {
		// The registrationId should be send to your applicatioin server.
		// We just log it to the LogCat view
		// We will copy it from there
		Log.i(TAG, "C2DM: Registration ID:"+registrationId);
		AtmoDroid.getAtmo().setRegistrationID(registrationId);
	};

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.e(TAG, "C2DM: - Message: Fantastic!!!");
		// Extract the payload from the message
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String message = (String) extras.get("message");
			Log.i(TAG, "C2DM: - Message received from Atmosphere:"+message);
			notifyUser(message);
			// Now do something smart based on the information
		}
	}

	/**
	 *   @Name:notifyUser
	 * @param message
	 */
	private void notifyUser(String message) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		Notification note = new Notification(R.drawable.atmoslogo, message, System.currentTimeMillis());
		note.ledARGB = 0xaaddcc00;
		note.ledOnMS = 500;
		note.ledOffMS = 1000;
		note.flags |= Notification.FLAG_SHOW_LIGHTS;
		note.defaults |= Notification.DEFAULT_SOUND;
		note.defaults |= Notification.DEFAULT_VIBRATE;
		Context c = getApplicationContext();
		String contentTitle = TAG;
		String contentText = message;
		Intent notificationIntent;
		//If user is logged in: open list of instances
		if(AtmoDroid.getAtmo() != null && AtmoDroid.getAtmo().getUser() != null) {
			notificationIntent = new Intent(this, ListInstances.class);
			notificationIntent.putExtra("atmoapi", AtmoDroid.getAtmo());
		} else {
		//Otherwise open the login screen
			notificationIntent = new Intent(this, AtmoDroid.class);
		}
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		note.setLatestEventInfo(c, contentTitle, contentText, contentIntent);
		mNotificationManager.notify(1,note);
		Log.i(TAG, "C2DM: Notified user with message");
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.e("C2DM", "Error occured!!!");
	}

}