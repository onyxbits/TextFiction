package de.onyxbits.textfiction;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
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
	private Typeface typeface;
	private float textSize;

	/**
	 * 
	 * @param context
	 * @param textViewResourceId
	 * @param mc
	 * @param listener
	 *          listener to set on the individual items.
	 */
	public StoryAdapter(Context context, int textViewResourceId,
			List<StoryItem> mc, OnTouchListener listener) {
		super(context, textViewResourceId, mc);
		this.listener = listener;
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

		TextView narrator = (TextView) ret.findViewById(R.id.text_narrator);
		TextView self = (TextView) ret.findViewById(R.id.text_self);

		if (typeface!=null) {
			self.setTypeface(typeface);
			narrator.setTypeface(typeface);
		}
		
		if (textSize>0) {
			self.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
			narrator.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
		}

		StoryItem mc = getItem(position);
		switch (mc.type) {

			case StoryItem.NARRATOR: {
				self.setVisibility(View.GONE);
				narrator.setVisibility(View.VISIBLE);
				narrator.setText(mc.message);
				break;
			}
			case StoryItem.MYSELF: {
				narrator.setVisibility(View.GONE);
				self.setVisibility(View.VISIBLE);
				self.setText(mc.message);
				break;
			}
			default: {

			}
		}
		return ret;
	}
	
	/**
	 * Override the typeface set by the theme
	 * @param tf new typefafe or null to return to the theme's default.
	 */
	public void setTypeface(Typeface tf) {
		typeface=tf;
	}
	
	/**
	 * Override the textsize set by the theme
	 * @param size a value larger 0 to override (in TypedValue.COMPLEX_UNIT_PX).
	 */
	public void setTextSize(float size) {
		textSize=size;
	}

	public boolean isEnabled(int pos) {
		return false; // Nothing is selectable!
	}
}
