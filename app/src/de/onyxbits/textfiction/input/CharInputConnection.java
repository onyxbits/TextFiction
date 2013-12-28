package de.onyxbits.textfiction.input;

import de.onyxbits.textfiction.GameActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

/**
 * An inputconnection for capturing individual keypresses and forward them to
 * the game engine in single character mode.
 * @author patrick
 *
 */
public class CharInputConnection extends BaseInputConnection {

	private View view;

	public CharInputConnection(View targetView, boolean fullEditor) {
		super(targetView, fullEditor);
		this.view = targetView;
	}

	@Override
	public boolean sendKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			byte[] tmp = { (byte) event.getUnicodeChar() };
			// FIXME: Don't callback by making the assumption that the context is
			// GameActivity
			((GameActivity) view.getContext()).executeCommand(tmp);
		}
		return true;
	}

	@Override
	public boolean deleteSurroundingText(int beforeLength, int afterLength) {
		// NOTE: In Android 4.x, the system IME will not send a KeyEvent.DEL, but 
		// rather call this method instead.
		// FIXME: Don't callback by making the assumption that the context is
		// GameActivity
		((GameActivity) view.getContext()).executeCommand(InputFragment.DELETE);
		return true;
	}

}
