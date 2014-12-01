package se.embargo.blockade.phone;

import se.embargo.blockade.SettingsActivity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class CleanupIntentService extends Service {
	public static final String EXTRA_EVENT = "se.embargo.blacklist.phone.CallService.event";
	public static final String EXTRA_STATE_BOOT = "boot";

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAMESPACE, Context.MODE_PRIVATE);
		int blocked = prefs.getInt(SettingsActivity.PREF_BLOCKED_CALLS, 0);
		prefs.edit().putInt(SettingsActivity.PREF_BLOCKED_CALLS_ACK, blocked).commit();
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}
