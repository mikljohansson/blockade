package se.embargo.blockade.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallReceiver extends BroadcastReceiver {
	private static final String TAG = "CallReceiver";
	private String _phonenumber = null;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (context == null || intent == null) {
			return;
		}
		
		if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			Log.i(TAG, "Received action: " + intent.getAction() + "/" + state);
			
			if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
				_phonenumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
				Log.i(TAG, "Incoming call from: " + _phonenumber);
				
				Intent serviceIntent = new Intent(context, CallService.class);
				serviceIntent.setAction(intent.getAction());
				serviceIntent.putExtras(intent.getExtras());
				context.startService(serviceIntent);
			}
			else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
				Log.i(TAG, "In call with: " + _phonenumber);
			}
			else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
				Log.i(TAG, "End call from: " + _phonenumber);
				_phonenumber = null;
			}
		}
	}
}
