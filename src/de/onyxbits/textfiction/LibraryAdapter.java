package de.onyxbits.textfiction;

import java.io.File;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

class LibraryAdapter extends ArrayAdapter<File> implements OnClickListener,
		android.content.DialogInterface.OnClickListener {

	private File deleteMe;
	private boolean stripSuffix;

	public LibraryAdapter(Context context, int textViewResourceId,
			ArrayList<File> stories) {
		super(context, textViewResourceId, stories);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View ret = convertView;
		if (ret == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			ret = inflater.inflate(R.layout.library_item, null);
		}
		TextView name = (TextView) ret.findViewById(R.id.gamename);
		ImageButton trash = (ImageButton) ret.findViewById(R.id.btn_delete);
		if (stripSuffix) {
			name.setText(FileUtil.basename(getItem(position)));
		}
		else {
			name.setText(getItem(position).getName());
		}
		trash.setTag(getItem(position));
		trash.setOnClickListener(this);

		return ret;
	}

	public void setStripSuffix(boolean strip) {
		stripSuffix = strip;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btn_delete) {
			deleteMe = (File) v.getTag();
			AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
			ab.setTitle(R.string.title_really_delete).setMessage(deleteMe.getName())
					.setNegativeButton(android.R.string.no, this)
					.setPositiveButton(android.R.string.yes, this).create().show();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			FileUtil.deleteGame(deleteMe);
			remove(deleteMe);
			notifyDataSetChanged();
		}
	}
}
