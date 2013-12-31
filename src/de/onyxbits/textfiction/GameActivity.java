package de.onyxbits.textfiction;

import java.io.File;

import de.onyxbits.textfiction.input.InputFragment;
import de.onyxbits.textfiction.input.WordExtractor;
import de.onyxbits.textfiction.zengine.ZMachine;
import de.onyxbits.textfiction.zengine.ZState;
import de.onyxbits.textfiction.zengine.ZStatus;
import de.onyxbits.textfiction.zengine.ZWindow;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
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

	/**
	 * Reference to the "save" entry in the menu
	 */
	private MenuItem menuSave;

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
		if (retainerFragment != null && retainerFragment.engine != null
				&& retainerFragment.engine.getRunState() == ZMachine.STATE_WAIT_CMD) {
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
		menuSave = menu.findItem(R.id.mi_save);
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
				retainerFragment.messageBuffer.add(new StoryItem(
						new String(tmp).trim(), StoryItem.MYSELF));
			}
			retainerFragment.engine.run();
			publishResult();
		}
	}

	/**
	 * Callback: publish results after the engine has run
	 */
	public void publishResult() {

		ZWindow upper = retainerFragment.engine.window[1];
		ZWindow lower = retainerFragment.engine.window[0];
		ZStatus status = retainerFragment.engine.status_line;
		String tmp;
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
			retainerFragment.messageBuffer
					.add(new StoryItem(tmp, StoryItem.NARRATOR));
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
	}

	/**
	 * show the correct prompt.
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

		// NOTE: on Gingerbread, onCreateOptionsMenu is not called till the user
		// presses the menu button. In that case menuSave is null.
		if (menuSave != null) {
			// See: ZMachine.restore()
			menuSave.setEnabled(inputFragment.isPrompt());
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
					retainerFragment.engine.restart();
					retainerFragment.engine.run();
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
