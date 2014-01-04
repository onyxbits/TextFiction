package de.onyxbits.textfiction;

import java.io.File;

import de.onyxbits.textfiction.input.InputFragment;
import de.onyxbits.textfiction.input.WordExtractor;
import de.onyxbits.textfiction.zengine.GrueException;
import de.onyxbits.textfiction.zengine.StyleRegion;
import de.onyxbits.textfiction.zengine.ZMachine;
import de.onyxbits.textfiction.zengine.ZState;
import de.onyxbits.textfiction.zengine.ZStatus;
import de.onyxbits.textfiction.zengine.ZWindow;

import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Build;

/**
 * The activity where actual gameplay takes place.
 * 
 * @author patrick
 * 
 */
public class GameActivity extends FragmentActivity implements 
		DialogInterface.OnClickListener {

	/**
	 * This activity must be started through an intent and be passed the filename
	 * of the game via this extra.
	 */
	public static final String LOADFILE = "loadfile";

	/**
	 * How many items to keep in the messagebuffer at most. Note: this should be
	 * an odd number so the log starts with a narrator entry.
	 */
	public static final int MAXMESSAGES = 81;


	private static final int PENDING_NONE = 0;
	private static final int PENDING_RESTART = 1;
	private static final int PENDING_RESTORE = 2;
	private static final int PENDING_SAVE = 3;

	/**
	 * Displays the message log
	 */
	private ListView storyBoard;

	/**
	 * Adapter for the story list
	 */
	private StoryAdapter messages;

	/**
	 * The "upper window" of the z-machine containing the status part
	 */
	private TextView statusWindow;

	/**
	 * Holds stuff that needs to survive config changes (e.g. screen rotation).
	 */
	private RetainerFragment retainerFragment;

	/**
	 * The input prompt
	 */
	private InputFragment inputFragment;

	/**
	 * Contains story- and status screen.
	 */
	private ViewFlipper windowFlipper;

	/**
	 * For entering a filename to save the current game as.
	 */
	private EditText saveName;

	/**
	 * The game playing in this activity
	 */
	private File storyFile;

	/**
	 * State variable for when we are showing a "confirm" dialog.
	 */
	private int pendingAction = PENDING_NONE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LayoutInflater infl = getLayoutInflater();
		requestWindowFeature(Window.FEATURE_PROGRESS);

		View content = infl.inflate(R.layout.activity_game, null);
		setContentView(content);

		FragmentManager fm = getSupportFragmentManager();
		inputFragment = (InputFragment) fm.findFragmentById(R.id.fragment_input);
		retainerFragment = (RetainerFragment) fm.findFragmentByTag("retainer");
		if (retainerFragment == null) {
			// First start
			retainerFragment = new RetainerFragment();
			fm.beginTransaction().add(retainerFragment, "retainer").commit();
		}
		else {
			// Likely a restart because of the screen being rotated. This may have
			// happened while loading, so don't figure if we don't have an engine.
			if (retainerFragment.engine != null) {
				figurePromptStyle();
				figureMenuState();
			}
		}
		storyFile = new File(getIntent().getStringExtra(LOADFILE));
		setTitle(storyFile.getName());

		// Show the Up button in the action bar.
		setupActionBar();


		storyBoard = (ListView) content.findViewById(R.id.storyboard);
		WordExtractor we = new WordExtractor(this, inputFragment);
		messages = new StoryAdapter(this, 0, retainerFragment.messageBuffer, we);
		storyBoard.setAdapter(messages);

		windowFlipper = (ViewFlipper) content.findViewById(R.id.window_flipper);
		statusWindow = (TextView) content.findViewById(R.id.status);
		statusWindow.setText(retainerFragment.upperWindow);
	}

	@Override
	public void onDestroy() {
		if (retainerFragment == null || retainerFragment.engine == null) {
			super.onDestroy();
			return;
		}

		if (retainerFragment.postMortem != null) {
			// Let's not go into details here. The user won't understand them anyways.
			Toast.makeText(this, R.string.msg_corrupt_game_file, Toast.LENGTH_SHORT)
					.show();
			super.onDestroy();
			return;
		}

		if (retainerFragment.engine.getRunState() == ZMachine.STATE_WAIT_CMD) {
			ZState state = new ZState(retainerFragment.engine);
			File f = new File(FileUtil.getSaveGameDir(storyFile),
					getString(R.string.autosavename));
			state.disk_save(f.getPath(), retainerFragment.engine.pc);
		}
		else {
			Toast.makeText(this, R.string.mg_not_at_a_commandprompt,
					Toast.LENGTH_LONG).show();
		}
		super.onDestroy();
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
			getActionBar().setSubtitle(storyFile.getName());
			setTitle(R.string.app_name);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		boolean rest = !(retainerFragment == null
				|| retainerFragment.engine == null
				|| retainerFragment.engine.getRunState() == ZMachine.STATE_RUNNING || retainerFragment.engine
				.getRunState() == ZMachine.STATE_INIT);
		menu.findItem(R.id.mi_save).setEnabled(rest && inputFragment.isPrompt());
		menu.findItem(R.id.mi_restore).setEnabled(rest && inputFragment.isPrompt());
		menu.findItem(R.id.mi_restart).setEnabled(rest);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_flip_view: {
				flipView(windowFlipper.getCurrentView() != storyBoard);
				return true;
			}
			case R.id.mi_save: {
				pendingAction = PENDING_SAVE;
				saveName = new EditText(this);
				saveName.setSingleLine(true);
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_save_game)
						.setPositiveButton(android.R.string.ok, this).setView(saveName)
						.show();
				return true;
			}
			case R.id.mi_restore: {
				String[] sg = FileUtil.listSaveName(storyFile);
				if (sg.length > 0) {
					pendingAction = PENDING_RESTORE;
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
					builder.setTitle(R.string.title_restore_game).setItems(sg, this)
							.show();
				}
				else {
					Toast.makeText(this, R.string.msg_no_savegames, Toast.LENGTH_SHORT)
							.show();
				}
				return true;
			}

			case R.id.mi_clear_log: {
				retainerFragment.messageBuffer.clear();
				messages.notifyDataSetChanged();
				return true;
			}
			case R.id.mi_help: {
				MainActivity.openUri(this, Uri.parse(getString(R.string.help_url)));
				return true;
			}
			case R.id.mi_restart: {
				pendingAction = PENDING_RESTART;
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle(R.string.title_please_confirm)
						.setMessage(R.string.msg_really_restart)
						.setPositiveButton(android.R.string.yes, this)
						.setNegativeButton(android.R.string.no, this).show();
				return true;
			}

			case android.R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask(this);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Fill the inputbuffer, start the engine. Calling this while the engine is
	 * not idle is a no-op.
	 * 
	 * @param inputBuffer
	 *          what to put on the inputbuffer
	 */
	public void executeCommand(byte[] inputBuffer) {
		ZMachine engine = retainerFragment.engine;
		if (engine != null && engine.getRunState() != ZMachine.STATE_RUNNING) {
			retainerFragment.engine.fillInputBuffer(inputBuffer);
			if (retainerFragment.engine.getRunState() != ZMachine.STATE_WAIT_CHAR) {
				String tmp = new String(inputBuffer).replaceAll("\n", "").trim();
				retainerFragment.messageBuffer.add(new StoryItem(new SpannableString(
						tmp), StoryItem.MYSELF));
			}
			try {
				retainerFragment.engine.run();
				publishResult();
			}
			catch (GrueException e) {
				retainerFragment.postMortem = e;
				finish();
			}
		}
	}

	/**
	 * Callback: publish results after the engine has run
	 */
	public void publishResult() {

		ZWindow upper = retainerFragment.engine.window[1];
		ZWindow lower = retainerFragment.engine.window[0];
		ZStatus status = retainerFragment.engine.status_line;
		String tmp = "";
		boolean showLower = false;

		if (status != null) {
			// Z3 game -> copy the status bar object into the upper window.
			retainerFragment.engine.update_status_line();
			retainerFragment.upperWindow = status.toString();
			statusWindow.setText(retainerFragment.upperWindow);
		}
		else {
			if (upper.maxCursor > 0) {
				// The normal, "status bar" upper window.
				tmp = upper.stringyfy(upper.startWindow, upper.maxCursor);
			}
			else {
				tmp = "";
			}
			statusWindow.setText(tmp);
			retainerFragment.upperWindow = tmp;
		}
		upper.retrieved();

		if (lower.cursor > 0) {
			tmp = new String(lower.frameBuffer, 0, lower.noPrompt());
			SpannableString stmp = new SpannableString(tmp);
			StyleRegion reg = lower.regions;
			if (reg != null) {
				while (reg != null) {
					if (reg.next == null) {
						// The printer does not "close" the last style since it doesn't know
						// when the last character is printed.
						reg.end = tmp.length() - 1;
					}
					switch (reg.style) {
						case ZWindow.BOLD: {
							stmp.setSpan(new StyleSpan(Typeface.BOLD), reg.start, reg.end, 0);
							break;
						}
						case ZWindow.ITALIC: {
							stmp.setSpan(new StyleSpan(Typeface.ITALIC), reg.start, reg.end,
									0);
							break;
						}
						case ZWindow.FIXED: {
							stmp.setSpan(new TypefaceSpan("monospace"), reg.start, reg.end, 0);
							break;
						}
					}
					reg = reg.next;
				}
			}
			retainerFragment.messageBuffer
					.add(new StoryItem(stmp, StoryItem.NARRATOR));
			showLower = true;
		}
		lower.retrieved();

		while (retainerFragment.messageBuffer.size() > MAXMESSAGES) {
			// Throw out old stuff.
			retainerFragment.messageBuffer.remove(0);
		}
		messages.notifyDataSetChanged();

		// NOTE:smoothScroll() does not work properly if the theme defines
		// dividerheight > 0!
		storyBoard
				.smoothScrollToPosition(retainerFragment.messageBuffer.size() - 1);

		inputFragment.reset();

		// Kinda dirty: assume that the lower window is the important one. If
		// anything got added to it, ensure that it is visible. Otherwise assume
		// that we are dealing with something like a menu and switch the display to
		// display the upperwindow
		flipView(showLower);
		figurePromptStyle();
		figureMenuState();
	}

	/**
	 * Show the correct prompt.
	 */
	private void figurePromptStyle() {
		if (retainerFragment.engine.getRunState() == ZMachine.STATE_WAIT_CHAR
				&& inputFragment.isPrompt()) {
			inputFragment.toggleInput();
		}
		if (retainerFragment.engine.getRunState() == ZMachine.STATE_WAIT_CMD
				&& !inputFragment.isPrompt()) {
			inputFragment.toggleInput();
		}
	}

	/**
	 * Enable/Disable menu items
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void figureMenuState() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			invalidateOptionsMenu();
		}
	}


	/**
	 * Make either the storyboard or the statusscreen visible
	 * 
	 * @param showstory
	 *          true to swtich to the story view, false to swtich to the status
	 *          screen. nothing happens if the desired view is already showing.
	 */
	private void flipView(boolean showstory) {
		View now = windowFlipper.getCurrentView();

		if (showstory) {
			if (now != storyBoard) {
				windowFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
						R.animator.slide_in_right));
				windowFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						R.animator.slide_out_left));
				windowFlipper.showPrevious();
			}
		}
		else {
			if (now == storyBoard) {
				windowFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
						android.R.anim.slide_in_left));
				windowFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
						android.R.anim.slide_out_right));
				windowFlipper.showPrevious();
			}
		}
	}


	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (pendingAction) {
			case PENDING_RESTART: {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					retainerFragment.messageBuffer.clear();
					try {
						retainerFragment.engine.restart();
						retainerFragment.engine.run();
					}
					catch (GrueException e) {
						// This should never happen
						retainerFragment.postMortem = e;
						finish();
					}
					publishResult();
				}
				break;
			}
			case PENDING_SAVE: {
				String name = saveName.getEditableText().toString();
				name = name.replace('/', '_');
				if (name.length() > 0) {
					ZState state = new ZState(retainerFragment.engine);
					File f = new File(FileUtil.getSaveGameDir(storyFile), name);
					state.disk_save(f.getPath(), retainerFragment.engine.pc);
					Toast.makeText(this, R.string.msg_game_saved, Toast.LENGTH_SHORT)
							.show();
				}
			}

			case PENDING_RESTORE: {
				if (which > -1) {
					File file = FileUtil.listSaveGames(storyFile)[which];
					ZState state = new ZState(retainerFragment.engine);
					if (state.restore_from_disk(file.getPath())) {
						statusWindow.setText(""); // Wrong, but the best we can do.
						retainerFragment.messageBuffer.clear();
						messages.notifyDataSetChanged();
						retainerFragment.engine.restore(state);
						figurePromptStyle();
						figureMenuState();
						Toast
								.makeText(this, R.string.msg_game_restored, Toast.LENGTH_SHORT)
								.show();
					}
					else {
						Toast.makeText(this, R.string.msg_restore_failed,
								Toast.LENGTH_SHORT).show();
					}
				}
			}
		}
		pendingAction = PENDING_NONE;
	}

}
