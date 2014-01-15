package de.onyxbits.textfiction.input;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * For listing all the icons the user may assign to the quickcommand buttons.
 * @author patrick
 *
 */
class IconAdapter extends BaseAdapter {

	private Context context;
	
	public IconAdapter(Context context) {
		this.context=context;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView ret = (ImageView)convertView;
		if (ret==null) {
			ret= new ImageView(context);
		}
		
		ret.setImageResource(CmdIcon.ICONS[position]);
		ret.setTag(position);
		return ret;
	}

	@Override
	public int getCount() {
		return CmdIcon.ICONS.length;
	}

	@Override
	public Object getItem(int position) {
		return CmdIcon.ICONS[position];
	}

	@Override
	public long getItemId(int position) {
		return CmdIcon.ICONS[position];
	}

}
