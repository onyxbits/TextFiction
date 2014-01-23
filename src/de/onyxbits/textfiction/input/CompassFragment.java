package de.onyxbits.textfiction.input;

import java.io.File;
import java.io.PrintWriter;

import org.json.JSONArray;

import de.onyxbits.textfiction.FileUtil;
import de.onyxbits.textfiction.R;
import de.onyxbits.textfiction.R.id;
import de.onyxbits.textfiction.R.layout;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

/**
 * The compass is simple container hosting eight (potentially transparent)
 * buttons (one per cardinal direction), each having a (movement) command bound
 * to it. The buttons can be edited by long pressing and every game can have
 * it's own setup.
 */
public class CompassFragment extends Fragment implements OnClickListener,
		OnLongClickListener, android.content.DialogInterface.OnClickListener {

	/**
	 * Filename in the data dir where we keep user defined button values.
	 */
	public static final String COMPASSFILE = "compass.json";

	/**
	 * References to the layout file
	 */
	private static final int DIRS[] = {
			R.id.compass_north,
			R.id.compass_northeast,
			R.id.compass_east,
			R.id.compass_southeast,
			R.id.compass_south,
			R.id.compass_southwest,
			R.id.compass_west,
			R.id.compass_northwest };

	/**
	 * One button per cardinal direction. The command of the button is bound to it
	 * through setTag().
	 */
	private ImageButton[] buttons;

	/**
	 * Used for modifying a buttons command. The button currently being modified
	 * is passed through via setTag()
	 */
	private EditText editCommand;

	private InputProcessor inputProcessor;

	private boolean doClick;
	private AudioManager audioManager;

	public CompassFragment() {
		// Required empty public constructor
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			inputProcessor = (InputProcessor) activity;
			audioManager = (AudioManager) activity
					.getSystemService(Context.AUDIO_SERVICE);
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
	}

	/**
	 * Play a click sound when a navbutton is clicked?
	 * 
	 * @param doClick
	 *          true to click
	 */
	public void setKeyclick(boolean doClick) {
		this.doClick = doClick;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		buttons = new ImageButton[DIRS.length];
		View view = inflater.inflate(R.layout.fragment_compass, container, false);

		String[] cmds = new String[DIRS.length];

		// Load user defined directions
		try {
			File file = new File(FileUtil.getDataDir(inputProcessor.getStory()),
					COMPASSFILE);
			JSONArray js = new JSONArray(FileUtil.getContents(file));
			for (int i = 0; i < buttons.length; i++) {
				cmds[i] = js.getString(i);
			}
		}
		catch (Exception e) {
			// No big deal. Probably the first time this game runs -> use defaults
			cmds = getActivity().getResources().getStringArray(
					R.array.initial_compass);
		}

		for (int i = 0; i < DIRS.length; i++) {
			buttons[i] = (ImageButton) view.findViewById(DIRS[i]);
			buttons[i].setOnClickListener(this);
			buttons[i].setOnLongClickListener(this);
			buttons[i].setTag(cmds[i]);
		}
		return view;
	}

	@Override
	public void onClick(View v) {
		if (doClick) {
			audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1f);
		}
		inputProcessor.executeCommand((v.getTag().toString() + "\n").toCharArray());
	}

	@Override
	public boolean onLongClick(View v) {
		editCommand = new EditText(getActivity());
		editCommand.setSingleLine(true);
		try {
			editCommand.setText(v.getTag().toString());
			editCommand.setTag(v);
		}
		catch (Exception e) {
			Log.w(getClass().getName(), e); // ?!
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(editCommand);
		builder.setTitle(R.string.title_edit_direction);
		builder.setPositiveButton(android.R.string.ok, this);
		builder.show();
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			String tmp = editCommand.getText().toString();
			((View) editCommand.getTag()).setTag(tmp);
		}
		try {
			File file = new File(FileUtil.getDataDir(inputProcessor.getStory()),
					COMPASSFILE);
			JSONArray array = new JSONArray();
			for (int i = 0; i < buttons.length; i++) {
				array.put(buttons[i].getTag().toString());
			}
			PrintWriter pw = new PrintWriter(file);
			pw.write(array.toString(2));
			pw.close();
		}
		catch (Exception e) {
			Log.w(getClass().getName(), e);
		}
	}
}
