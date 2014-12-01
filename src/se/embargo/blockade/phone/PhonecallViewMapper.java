package se.embargo.blockade.phone;

import se.embargo.blockade.R;
import se.embargo.blockade.database.Phonecall;
import se.embargo.blockade.database.Phonecall.Action;
import se.embargo.core.Dates;
import se.embargo.core.databinding.IViewMapper;
import android.content.ContentValues;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class PhonecallViewMapper implements IViewMapper<ContentValues> {
    public static final int ID_TAG = R.id.itemThumbnail;
    public static final int URI_TAG = R.id.itemTitle;
    public static final int TYPE_TAG = R.id.itemModified;
    
    @Override
	public View convert(ContentValues item, View view, ViewGroup parent) {
    	int type = getItemViewType(item);		
    	if (view == null) {
			LayoutInflater inflater = (LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.phonecall_listitem, parent, false);
		}
    	    	
		// Bind the thumbnail
    	Phonecall.Action action = Phonecall.toAction(item.getAsInteger(Phonecall.ACTION));
		ImageView thumbnailview = (ImageView)view.findViewById(R.id.itemThumbnail);
		TextView descriptionview = (TextView)view.findViewById(R.id.itemDescription);
		
		switch (action) {
			case ALLOWED:
				thumbnailview.setImageResource(R.drawable.ic_action_phone_incoming);
				descriptionview.setText(R.string.phonecall_allowed_through);
				descriptionview.setVisibility(View.VISIBLE);
				break;
				
			default:
				thumbnailview.setImageResource(R.drawable.ic_action_phone_missed);
				descriptionview.setVisibility(View.GONE);
				break;
		}
		
		// Bind the date
		TextView modifiedview = (TextView)view.findViewById(R.id.itemModified);
		modifiedview.setText(Dates.formatRelativeTimeSpan(item.getAsLong(Phonecall.MODIFIED)));
		
		view.setTag(ID_TAG, item.getAsString(Phonecall.ID));
		view.setTag(TYPE_TAG, type);
		return view;
	}
	
	@Override
	public int getItemViewType(ContentValues item) {
		return 0;
	}
	
	@Override
	public int getViewTypeCount() {
		return 1;
	}
}
