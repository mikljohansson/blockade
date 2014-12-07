package se.embargo.blockade.phone;

import se.embargo.blockade.R;
import se.embargo.blockade.database.BlockadeRepository;
import se.embargo.blockade.database.Phonecall;
import se.embargo.blockade.widget.ListActionMode;
import se.embargo.core.database.CursorMapperAdapter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.actionbarsherlock.app.SherlockListFragment;

public class PhonecallListFragment extends SherlockListFragment {
	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Bind the phone call list view
        PhonecallAdapter adapter = new PhonecallAdapter();
        setListAdapter(adapter);
        setEmptyText(getString(R.string.msg_list_is_empty));
        
        ListActionMode actionmode = new ListActionMode(getSherlockActivity(), getListView());
        actionmode.setTablename(Phonecall.TABLENAME);
        getListView().setOnItemLongClickListener(actionmode);
    }

    private class PhonecallAdapter extends CursorMapperAdapter {
    	public PhonecallAdapter() {
			super(getActivity(), new PhonecallViewMapper());
		}

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			return new CursorLoader(
				getSherlockActivity(), BlockadeRepository.PHONECALL_URI, 
				null, null, null, Phonecall.MODIFIED + " DESC");
		}
    }
}
