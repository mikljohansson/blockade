package se.embargo.blockade.widget;

import java.util.ArrayList;
import java.util.Collection;

import se.embargo.blockade.R;
import se.embargo.blockade.database.BlockadeRepository;
import se.embargo.core.widget.SelectionActionMode;
import android.content.ContentProviderOperation;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.MenuItem;

public class ListActionMode extends SelectionActionMode {
	private static final String TAG = "SelectionListener";
	private String _tablename;
	
	public ListActionMode(SherlockFragmentActivity activity, ListView listview, int menuResource) {
		super(activity, listview, menuResource);
	}

	public ListActionMode(SherlockFragmentActivity activity, ListView listview) {
		this(activity, listview, R.menu.list_actionmode_options);
	}
	
	public void setTablename(String tablename) {
		_tablename = tablename;
	}
	
	private String getTablename(Cursor cursor) {
		return _tablename;
	}
	
	protected Collection<Uri> getSelectedUris() {
		ListView view = getListView();
		SparseBooleanArray items = view.getCheckedItemPositions();
		Collection<Uri> uris = new ArrayList<Uri>();
		
		for (int i = 0; items != null && i < items.size(); i++) {
			int position = items.keyAt(i);
			if (items.get(position)) {
				Cursor cursor = (Cursor)view.getItemAtPosition(position);
				Uri uri = Uri.withAppendedPath(Uri.withAppendedPath(BlockadeRepository.CONTENT_URI, getTablename(cursor)), cursor.getString(0));
				uris.add(uri);
			}
		}
		
		return uris;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		boolean result = false;
		
		switch (item.getItemId()) {
			case R.id.deleteButton: {
				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				for (Uri uri : getSelectedUris()) {
					ops.add(ContentProviderOperation.newDelete(uri).build());
				}
				
				try {
					getActivity().getContentResolver().applyBatch(BlockadeRepository.AUTHORITY, ops);
				}
				catch (RemoteException e) {
					Log.e(TAG, "Failed to delete objects", e);
				}
				catch (OperationApplicationException e) {
					Log.e(TAG, "Failed to delete objects", e);
				}
				
				result = true;
				mode.finish();
				break;
			}
		}

		return result;
	}
}
