package se.embargo.blockade;

import se.embargo.blockade.R;
import android.os.Bundle;

import android.preference.PreferenceActivity;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity {
	public static final String PREFS_NAMESPACE = "se.embargo.blacklist";

	public static final String PREF_PHONECALL_BLOCK_PRIVATE = "phonecall-block-private";
	public static final boolean PREF_PHONECALL_BLOCK_PRIVATE_DEFAULT = true;

	public static final String PREF_PHONECALL_ALLOW_RETRY = "phonecall-allow-retry";
	public static final boolean PREF_PHONECALL_ALLOW_RETRY_DEFAULT = true;
	public static final int PREF_PHONECALL_ALLOW_RETRY_NUMBER = 3;
	public static final int PREF_RETRY_PERIOD_DEFAULT = 30;

	public static final String PREF_PHONECALL_NOTIFY_BLOCKED = "phonecall-notify-blocked";
	public static final boolean PREF_PHONECALL_NOTIFY_BLOCKED_DEFAULT = true;
	
	public static final String PREF_BLOCKED_CALLS = "blocked-calls";
	public static final String PREF_BLOCKED_CALLS_ACK = "blocked-calls-ack";
	
	@Override
	@SuppressWarnings("deprecation")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getActionBar().setDisplayHomeAsUpEnabled(true);
		
		getPreferenceManager().setSharedPreferencesName(PREFS_NAMESPACE);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
