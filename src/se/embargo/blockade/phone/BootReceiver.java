package se.embargo.blockade.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent args = new Intent(context, CallService.class);
		args.putExtra(CallService.EXTRA_EVENT, CallService.EXTRA_STATE_BOOT);
		context.startService(args);
	}
}
