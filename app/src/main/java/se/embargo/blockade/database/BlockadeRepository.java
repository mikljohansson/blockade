package se.embargo.blockade.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class BlockadeRepository extends ContentProvider {
	public static final String AUTHORITY = "se.embargo.blockade.repository";
	public static final String STREAM_MIMETYPE = "vnd.android.cursor.item/vnd." + AUTHORITY + ".stream";
	
	private static final int PHONECALLS_URI_ID = 0;
	private static final int PHONECALL_URI_ID = 1;
	
	private static final UriMatcher _matcher = new UriMatcher(UriMatcher.NO_MATCH);
	static {
	    _matcher.addURI(AUTHORITY, "phonecall/*", PHONECALL_URI_ID);
	    _matcher.addURI(AUTHORITY, "phonecall", PHONECALLS_URI_ID);
	}
	
	/**
	 * Base URI to the content provider
	 */
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/");

	public static final Uri PHONECALL_URI = Uri.withAppendedPath(CONTENT_URI, "phonecall");

	/**
	 * Open handle on the database
	 */
    private SQLiteDatabase _db;
    
	@Override
	public String getType(Uri uri) {
	    int uriType = _matcher.match(uri);

	    switch (uriType) {
		    case PHONECALL_URI_ID:
		    	return STREAM_MIMETYPE;
	    }
	    
	    return null;
	}
	
    @Override
	public boolean onCreate() {
		_db = new BlockadeDatabase(getContext()).getWritableDatabase();
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
	    int uriType = _matcher.match(uri);
	    String[] args = selectionArgs;
	    
	    SQLiteQueryBuilder query = new SQLiteQueryBuilder();

	    switch (uriType) {
		    case PHONECALLS_URI_ID:
			    query.setTables(uri.getLastPathSegment());
		        break;

		    case PHONECALL_URI_ID:
			    query.setTables(uri.getPathSegments().get(uri.getPathSegments().size() - 2));
		        query.appendWhere("_id = ?");
		        args = new String[] {uri.getLastPathSegment()};
		        break;

		    default:
		        throw new IllegalArgumentException("Unknown URI");
	    }
	 
	    Cursor cursor = query.query(_db, projection, selection, args, null, null, sortOrder);
	    cursor.setNotificationUri(getContext().getContentResolver(), CONTENT_URI);
	    return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = _matcher.match(uri);
		String tablename;
		
	    switch (uriType) {
		    case PHONECALL_URI_ID:
		    	tablename = uri.getPathSegments().get(uri.getPathSegments().size() - 2);
		        break;
		    
		    default:
		        throw new IllegalArgumentException("Unknown URI: " + uri);
	    }

		String query = "_id = ?";
	    String[] args = new String[] {uri.getLastPathSegment()};
	    int result = _db.delete(tablename, query, args);
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		return result;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = _matcher.match(uri);
		String tablename;
		
		switch (uriType) {
		    case PHONECALLS_URI_ID:
		    	tablename = uri.getLastPathSegment();
		        break;

		    default:
		        throw new IllegalArgumentException("Unknown URI");
	    }

	    long id = _db.replace(tablename, null, values);
		Uri result = Uri.parse(uri.toString() + "/" + id);
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		return result;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int uriType = _matcher.match(uri);
		String tablename;
		
	    switch (uriType) {
		    case PHONECALL_URI_ID:
		    	tablename = uri.getPathSegments().get(uri.getPathSegments().size() - 2);
		        break;
		        
		    default:
		        throw new IllegalArgumentException("Unknown URI");
	    }

	    String query = "_id = ?";
    	String[] args = new String[] {uri.getLastPathSegment()};
	    int result = _db.update(tablename, values, query, args);
		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		return result;
	}
}
