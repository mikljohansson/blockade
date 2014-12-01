package se.embargo.blockade.database;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.ContentValues;

public class Phonecall {
	public static final String TABLENAME = "phonecall";
	public static final String ID = "_id";
	public static final String PHONENUMBER = "phonenumber";
	public static final String ACTION = "action";
	public static final String MODIFIED = "modified";
	
	public enum Action { REJECTED, ALLOWED };
	
	@SuppressLint("DefaultLocale")
	public static ContentValues create(String phonenumber, Action action) {
		if (phonenumber == null) {
			phonenumber = "unknown";
		}

		ContentValues values = new ContentValues();
		values.put(ID, UUID.randomUUID().toString());
		values.put(PHONENUMBER, phonenumber);
		values.put(ACTION, toInteger(action));
		values.put(MODIFIED, System.currentTimeMillis());
		return values;
	}
	
	public static int toInteger(Action action) {
		switch (action) {
			case REJECTED:
				return 0;
				
			case ALLOWED:
				return 1;
		}
		
		return 0;
	}
	
	public static Action toAction(int action) {
		switch (action) {
			case 0:
				return Action.REJECTED;
				
			case 1:
				return Action.ALLOWED;
		}
		
		return Action.REJECTED;
	}
}
