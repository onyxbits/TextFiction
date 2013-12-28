package de.onyxbits.textfiction;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Glue between te list of MessageItems and the storyboard.
 * 
 * @author patrick
 * 
 */
public class StoryAdapter extends ArrayAdapter<StoryItem> {
	
	private OnTouchListener listener;

	/**
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param mc
	 * @param listener listener to set on the individual items.
	 */
	public StoryAdapter(Context context, int textViewResourceId,
			List<StoryItem> mc, OnTouchListener listener) {
		super(context, textViewResourceId, mc);
		this.listener=listener;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View ret = convertView;

		if (ret == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			ret = inflater.inflate(R.layout.story_item, null);
			ret.findViewById(R.id.text_narrator).setOnTouchListener(listener);
			ret.findViewById(R.id.text_self).setOnTouchListener(listener);
		}

		StoryItem mc = getItem(position);
		switch (mc.type) {

			case StoryItem.NARRATOR: {
				ret.findViewById(R.id.text_self).setVisibility(View.GONE);
				ret.findViewById(R.id.text_narrator).setVisibility(View.VISIBLE);
				((TextView) ret.findViewById(R.id.text_narrator)).setText(mc.message);
				break;
			}
			case StoryItem.MYSELF: {
				ret.findViewById(R.id.text_narrator).setVisibility(View.GONE);
				ret.findViewById(R.id.text_self).setVisibility(View.VISIBLE);
				((TextView) ret.findViewById(R.id.text_self)).setText(mc.message);
				break;
			}
			default: {

			}

		}
		return ret;
	}

	public boolean isEnabled(int pos) {
		return false; // Nothing is selectable!
	}
}
