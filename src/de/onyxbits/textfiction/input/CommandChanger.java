package de.onyxbits.textfiction.input;

import de.onyxbits.textfiction.R;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Allows the user to redefine the command buttons in the inputfragment. Just
 * bind an object of this class as an OnLongClickListener to the button in
 * question and make sure the button has a CmdIcon as its tag.
 * 
 * @author patrick
 * 
 */
class CommandChanger implements OnItemClickListener, OnLongClickListener {

	private CmdIcon cmdIcon;
	private CheckBox atOnce;
	private String text;
	private AlertDialog dialog;
	private TextView cmdLine;
	private ImageView target;

	public CommandChanger(TextView cmdLine) {
		this.cmdLine = cmdLine;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		cmdIcon.atOnce = atOnce.isChecked();
		cmdIcon.cmd = text;
		cmdIcon.imgid = CmdIcon.ICONS[position];
		cmdIcon.save(view.getContext());
		target.setImageResource(cmdIcon.imgid);
		dialog.dismiss();
	}

	@Override
	public boolean onLongClick(View v) {

		Context ctx = v.getContext();
		text = cmdLine.getText().toString();
		
		if (text.length() > 0) {
			text = cmdLine.getEditableText().toString();
			LayoutInflater li = (LayoutInflater) ctx
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			cmdIcon = (CmdIcon) v.getTag();
			View layout = li.inflate(R.layout.quickcmdsettings, null);
			GridView gridView = (GridView) layout.findViewById(R.id.iconselect);
			atOnce = (CheckBox) layout.findViewById(R.id.executeatonce);
			gridView.setAdapter(new IconAdapter(ctx));
			TextView txt = (TextView) layout.findViewById(R.id.replacementcmd);
			txt.setText("'" + text.trim() + "'");
			gridView.setOnItemClickListener(this);
			atOnce.setChecked(cmdIcon.atOnce);
			target = (ImageView)v;
			AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
			dialog = builder.setTitle(R.string.title_change_commmand).setView(layout)
					.create();
			dialog.show();
		}
		else {
			Toast.makeText(ctx, ctx.getString(R.string.msg_no_cmd),
					Toast.LENGTH_SHORT).show();
		}
		return true;
	}

}
