package se.embargo.blockade.phone;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import se.embargo.blockade.MainActivity;
import se.embargo.blockade.R;
import se.embargo.blockade.SettingsActivity;
import se.embargo.blockade.database.BlockadeRepository;
import se.embargo.blockade.database.Phonecall;
import se.embargo.core.service.AbstractService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class CallService extends AbstractService {
	public static final String EXTRA_EVENT = "se.embargo.blacklist.phone.CallService.event";
	public static final String EXTRA_STATE_BOOT = "boot";
	
	public static final String EXTRA_STATE_RINGING = "ringing";
	public static final String EXTRA_STATE_PHONENUMBER = "phonenumber";

	private static final String TAG = "CallReceiver";
	private static final int NOTIFICATION_ID = 0;
	
	/**
	 * Application wide preferences
	 */
	protected SharedPreferences _prefs;
	
	/**
	 * The listener needs to be kept alive since SharedPrefernces only keeps a weak reference to it
	 */
	private PreferencesListener _prefsListener = new PreferencesListener();
	
	private AudioManager _audioManager;
	private TelephonyManager _telephonyManager;
	private PhoneStateListener _phoneStateListener;
	private Integer _prevRingerMode = null;

	@Override
	public void onCreate() {
		super.onCreate();
		_audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
		_telephonyManager = (TelephonyManager)getSystemService(TELEPHONY_SERVICE);
		
		_prefs = getSharedPreferences(SettingsActivity.PREFS_NAMESPACE, Context.MODE_PRIVATE);
		_prefs.registerOnSharedPreferenceChangeListener(_prefsListener);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		
		if (_phoneStateListener != null) {
			_telephonyManager.listen(_phoneStateListener, 0);
			_phoneStateListener = null;
		}
		
		_phoneStateListener = new StateListener();
		_telephonyManager.listen(_phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		_prefs.unregisterOnSharedPreferenceChangeListener(_prefsListener);
		
		if (_phoneStateListener != null) {
			_telephonyManager.listen(_phoneStateListener, 0);
			_phoneStateListener = null;
		}
	}
	
	private class StateListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
				case TelephonyManager.CALL_STATE_IDLE:
					restoreRingerMode();
					break;
			
				case TelephonyManager.CALL_STATE_RINGING:
					handleIncomingCall(incomingNumber);
					break;

				case TelephonyManager.CALL_STATE_OFFHOOK:
					restoreRingerMode();
					break;
			}
		}

		private void restoreRingerMode() {
			if (_prevRingerMode != null) {
				_audioManager.setRingerMode(_prevRingerMode);
				_prevRingerMode = null;
			}
		}
	}
	
	private boolean handleIncomingCall(String phonenumber) {
		Log.i(TAG, "Incoming call from: " + phonenumber);
		if (phonenumber != null && !"".equals(phonenumber)) {
			return false;
		}
		
		// Check if calls should be blocked
		if (!_prefs.getBoolean(SettingsActivity.PREF_PHONECALL_BLOCK_PRIVATE, SettingsActivity.PREF_PHONECALL_BLOCK_PRIVATE_DEFAULT)) {
			Log.i(TAG, "Call blocking disabled by preferences");
			insert(Phonecall.Action.ALLOWED);
			return false;
		}
		
		// Check how many calls were attempted in the last period
		if (_prefs.getBoolean(SettingsActivity.PREF_PHONECALL_ALLOW_RETRY, SettingsActivity.PREF_PHONECALL_ALLOW_RETRY_DEFAULT)) {
			int attempts = getRecentAttempts();
			if (attempts >= (SettingsActivity.PREF_PHONECALL_ALLOW_RETRY_NUMBER - 1)) {
				Log.i(TAG, "Allowing call through after " + attempts + " attempts");
				insert(Phonecall.Action.ALLOWED);
				return false;
			}
		}
		
		// Disconnect call
		Log.i(TAG, "Blocking call from: " + phonenumber);
		if (!endCall()) {
			return false;
		}

		// Update statistics used for notification counting
		int blocked = _prefs.getInt(SettingsActivity.PREF_BLOCKED_CALLS, 0) + 1;
		int acked = _prefs.getInt(SettingsActivity.PREF_BLOCKED_CALLS_ACK, 0);
		_prefs.edit().putInt(SettingsActivity.PREF_BLOCKED_CALLS, blocked).commit();
		
		// Insert into database
		insert(Phonecall.Action.REJECTED);
		
		// Notify about how many calls have been blocked
		if (_prefs.getBoolean(SettingsActivity.PREF_PHONECALL_NOTIFY_BLOCKED, SettingsActivity.PREF_PHONECALL_NOTIFY_BLOCKED_DEFAULT)) {
			PendingIntent contentIntent = TaskStackBuilder.create(this).
				addParentStack(MainActivity.class).
				addNextIntent(new Intent(this, MainActivity.class)).
				getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
	
			PendingIntent deleteIntent = 
				PendingIntent.getActivity(this, 0, 
				new Intent(this, CleanupIntentService.class), 0);
			
			int count = blocked - acked;
			NotificationCompat.Builder builder = new NotificationCompat.Builder(this).
				setSmallIcon(R.drawable.ic_notification_phone_missed).
				setNumber(count).
				setContentTitle(getString(count > 1 ? R.string.msg_blocked_call : R.string.msg_blocked_calls)).
				setContentText(getString(count > 1 ? R.string.msg_blocked_incoming_calls : R.string.msg_blocked_incoming_call, count)).
				setContentIntent(contentIntent).
				setDeleteIntent(deleteIntent);
	
			NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			manager.notify(NOTIFICATION_ID, builder.build());
		}
		
		return true;
	}

	private void insert(Phonecall.Action action) {
		ContentValues phonecall = Phonecall.create("", action);
		getContentResolver().insert(BlockadeRepository.PHONECALL_URI, phonecall);
	}
	
	private int getRecentAttempts() {
		Cursor c = getContentResolver().query(
		    BlockadeRepository.PHONECALL_URI,
		    new String[] { "COUNT(1) calls"},
		    "modified >= ?", new String[] {Long.toString(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(SettingsActivity.PREF_RETRY_PERIOD_DEFAULT))},
		    null);
		
		c.moveToFirst();
		int result = c.getInt(0);
		c.close();
		return result;
	}

	public boolean endCall() {
		if (_prevRingerMode == null) {
			_prevRingerMode = _audioManager.getRingerMode();
		}
		
		_audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
		
		try {
			String serviceManagerName = "android.os.ServiceManager";
			String serviceManagerNativeName = "android.os.ServiceManagerNative";
			String telephonyName = "com.android.internal.telephony.ITelephony";

			Class<?> telephonyClass = Class.forName(telephonyName);
			Class<?> telephonyStubClass = telephonyClass.getClasses()[0];
			Class<?> serviceManagerClass = Class.forName(serviceManagerName);
			Class<?> serviceManagerNativeClass = Class.forName(serviceManagerNativeName);

			Binder tmpBinder = new Binder();
			tmpBinder.attachInterface(null, "fake");
			Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
			Object serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
			Method getService = serviceManagerClass.getMethod("getService", String.class);
			IBinder retbinder = (IBinder)getService.invoke(serviceManagerObject, "phone");
			
			Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
			Object telephonyObject = serviceMethod.invoke(null, retbinder);
			Method telephonyEndCall = telephonyClass.getMethod("endCall");
			telephonyEndCall.invoke(telephonyObject);
			return true;
		}
		catch (Exception e) {
			Log.e(TAG, "Could not connect to telephony subsystem", e);
			return false;
		}
	}

	/**
	 * Listens for preference changes and applies updates
	 */
	private class PreferencesListener implements SharedPreferences.OnSharedPreferenceChangeListener {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			if (SettingsActivity.PREF_PHONECALL_NOTIFY_BLOCKED.equals(key)) {
				if (!prefs.getBoolean(SettingsActivity.PREF_PHONECALL_NOTIFY_BLOCKED, SettingsActivity.PREF_PHONECALL_NOTIFY_BLOCKED_DEFAULT)) {
					NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
					manager.cancel(NOTIFICATION_ID);
				}
			}
		}
	}
}
