package de.onyxbits.textfiction.input;

import de.onyxbits.textfiction.GameActivity;
import de.onyxbits.textfiction.R;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewFlipper;

/**
 * UI interface between the player and the engine's input buffer.
 */
public class InputFragment extends Fragment implements OnClickListener,
		OnEditorActionListener {

	public static final String PREFSFILE = "controls";

	private EditText cmdLine;
	private ImageButton submit;
	private ImageButton expand;
	private LinearLayout buttonBar;
	private ViewFlipper flipper;

	private ImageButton forwards;
	private ImageButton left;
	private ImageButton right;
	private ImageButton up;
	private ImageButton down;

	// Magic numbers! See: §3.8 of the zmachine standard document.
	public static final byte[] C_UP = { (byte) 129 };
	public static final byte[] C_DOWN = { (byte) 130 };
	public static final byte[] C_LEFT = { (byte) 131 };
	public static final byte[] C_RIGHT = { (byte) 132 };
	public static final byte[] ENTER = { (byte) 13 };
	public static final byte[] DELETE = { (byte) 8 };

	private int currentVerb = -1;

	private InputProcessor inputProcessor;

	public InputFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		flipper = (ViewFlipper) inflater
				.inflate(R.layout.fragment_input, container);
		buttonBar = (LinearLayout) flipper.findViewById(R.id.quickcmdcontainer);
		cmdLine = (EditText) flipper.findViewById(R.id.userinput);
		submit = (ImageButton) flipper.findViewById(R.id.submit);
		expand = (ImageButton) flipper.findViewById(R.id.expand);
		submit.setOnClickListener(this);
		cmdLine.setOnEditorActionListener(this);
		expand.setOnClickListener(this);

		forwards = (ImageButton) flipper.findViewById(R.id.forwards);
		down = (ImageButton) flipper.findViewById(R.id.cursor_down);
		left = (ImageButton) flipper.findViewById(R.id.cursor_left);
		right = (ImageButton) flipper.findViewById(R.id.cursor_right);
		up = (ImageButton) flipper.findViewById(R.id.cursor_up);

		forwards.setOnClickListener(this);
		down.setOnClickListener(this);
		left.setOnClickListener(this);
		right.setOnClickListener(this);
		up.setOnClickListener(this);

		Context ctx = getActivity();
		CommandChanger changer = new CommandChanger(cmdLine);

		// FIXME: make max number of buttons configurable
		for (int i = 0; i < 14; i++) {
			ImageButton b = (ImageButton) inflater.inflate(R.layout.style_cmdbutton,
					null).findViewById(R.id.protocmdbutton);
			CmdIcon ico = CmdIcon.create(ctx, i);
			b.setTag(ico);
			b.setImageResource(ico.imgid);
			b.setOnClickListener(this);
			b.setOnLongClickListener(changer);
			buttonBar.addView(b);
		}

		flipper.setInAnimation(AnimationUtils.loadAnimation(ctx,
				R.animator.slide_in_right));
		flipper.setOutAnimation(AnimationUtils.loadAnimation(ctx,
				R.animator.slide_out_left));

		return flipper;
	}

	protected void updateShortCut(CmdIcon ci) {
		for (int i = 0; i < CmdIcon.ICONS.length; i++) {
			ImageButton b = (ImageButton) buttonBar.getChildAt(i);
			if (ci == b.getTag()) {
				b.setImageResource(ci.imgid);
			}
		}
	}

	/**
	 * Toggle between commandline and keypress input
	 */
	public void toggleInput() {
		flipper.showNext();
	}

	/**
	 * Query the input style
	 * 
	 * @return true if showing the commandline
	 */
	public boolean isPrompt() {
		return (flipper.getCurrentView().getId() == R.id.inputcontainer);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			inputProcessor = (InputProcessor) activity;
		}
		catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnArticleSelectedListener");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onClick(View v) {
		if (v == submit) {
			executeCommand();
			return;
		}
		if (v == expand) {
			if (buttonBar.getVisibility() == View.VISIBLE) {
				buttonBar.setVisibility(View.GONE);
			}
			else {
				buttonBar.setVisibility(View.VISIBLE);
			}
			return;
		}
		if (v == forwards) {
			((GameActivity) getActivity()).executeCommand(ENTER);
		}
		if (v == up) {
			((GameActivity) getActivity()).executeCommand(C_UP);
		}
		if (v == down) {
			((GameActivity) getActivity()).executeCommand(C_DOWN);
		}
		if (v == left) {
			((GameActivity) getActivity()).executeCommand(C_LEFT);
		}
		if (v == right) {
			((GameActivity) getActivity()).executeCommand(C_RIGHT);
		}

		if (v.getTag() instanceof CmdIcon) {
			CmdIcon ci = (CmdIcon) v.getTag();
			if (ci.atOnce) {
				GameActivity ga = (GameActivity) getActivity();
				ga.executeCommand((ci.cmd + "\n").getBytes());
			}
			else {
				// Allow the player to select either the verb or an object first. If
				// an object is selected first and a verb second, assume the input to
				// be finished and execute it. The other way around: allow more objects
				// to be added.
				String tmp = "";
				if (currentVerb == -1) {
					tmp = cmdLine.getEditableText().toString().trim();
				}
				cmdLine.setText(ci.cmd.trim() + " " + tmp);
				cmdLine.setSelection(cmdLine.getEditableText().toString().length());
				currentVerb = ci.slot;
				if (tmp.length() > 0) {
					executeCommand();
				}
			}
		}
	}

	/**
	 * Call after running a command to clear the commandline
	 */
	public void reset() {
		cmdLine.setText("");
		currentVerb = -1;
	}

	/**
	 * Append a "word" to the prompt.
	 * 
	 * @param str
	 *          the string to add (does not require whitespaces at the start or
	 *          end).
	 */
	public void appendWord(String str) {
		if (str.length() > 0) {
			String tmp = cmdLine.getText().toString().trim();
			tmp = tmp.trim() + " " + str.trim();
			cmdLine.setText(tmp);
			cmdLine.setSelection(tmp.length());
		}
	}

	/**
	 * Delete the rightmost word on the prompt.
	 */
	public void removeWord() {
		String tmp = cmdLine.getText().toString().trim();
		int idx = tmp.lastIndexOf(' ');
		if (idx > 0) {
			tmp = tmp.substring(0, idx);
			cmdLine.setText(tmp);
			cmdLine.setSelection(tmp.length());
		}
		else {
			reset();
		}
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		executeCommand();
		return true;
	}

	private void executeCommand() {
		inputProcessor.executeCommand((cmdLine.getText().toString() + "\n")
				.getBytes());
	}

}
