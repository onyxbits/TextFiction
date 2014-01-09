package de.onyxbits.textfiction;

import java.io.File;
import java.util.List;
import java.util.Vector;

import de.onyxbits.textfiction.zengine.GrueException;
import de.onyxbits.textfiction.zengine.ZMachine;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Initializing a game is expensive and loosing gamestate on rotation would
 * bother the user. This fragment is simply a container for holding stuff that
 * needs to survive between configuration changes. Details:
 * 
 * http://www.androiddesignpatterns.com/2013/04/retaining-objects-across-config-
 * changes.html
 * 
 * @author patrick
 * 
 */
public class RetainerFragment extends Fragment {

	/**
	 * The engine of the currently running game
	 */
	public ZMachine engine;

	/**
	 * Stores the narrator/user messages.
	 */
	public List<StoryItem> messageBuffer;

	/**
	 * Always surround engine.run() in try/catch and put the caught exception
	 * here, call finish() afterwards.
	 */
	public GrueException postMortem;

	/**
	 * Contents of the upper window
	 */
	public String upperWindow;
	
	/**
	 * Words that should be highlighted in the story
	 */
	public Vector<String> highlighted;

	public RetainerFragment() {
		messageBuffer = new Vector<StoryItem>();
		highlighted = new Vector<String>();
		upperWindow = "";
		postMortem = null;
		engine = null;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retain this fragment across configuration changes.
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Activity act) {
		super.onAttach(act);
		if (engine == null) {
			File f = new File(act.getIntent().getStringExtra(GameActivity.LOADFILE));
			new LoaderTask(this).execute(f);
		}
	}

}
