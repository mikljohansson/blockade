package se.embargo.blockade;

import se.embargo.blockade.R;
import se.embargo.blockade.phone.CallService;
import se.embargo.blockade.phone.PhonecallListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent args = new Intent(this, CallService.class);
		args.putExtra(CallService.EXTRA_EVENT, CallService.EXTRA_STATE_BOOT);
		startService(args);
		
		setContentView(R.layout.main_activity);

		Fragment fragment = Fragment.instantiate(this, PhonecallListFragment.class.getName());
		getFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_options, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.editSettingsButton: {
				// Start preferences activity
				Intent intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				return true;
			}

			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
