package de.onyxbits.textfiction.input;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;

/**
 * An inputconnection for capturing individual keypresses and forward them to
 * the game engine in single character mode.
 * 
 * @author patrick
 * 
 */
class CharInputConnection extends BaseInputConnection {

	private InputProcessor inputProcessor;

	public CharInputConnection(View view, boolean fullEditor, InputProcessor ip) {
		super(view, fullEditor);
		this.inputProcessor=ip;
	}

	@Override
	public boolean sendKeyEvent(KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP) {
			char[] tmp = { (char)event.getUnicodeChar() };
			inputProcessor.executeCommand(tmp);
		}
		return true;
	}

	@Override
	public boolean deleteSurroundingText(int beforeLength, int afterLength) {
		// NOTE: In Android 4.x, the system IME will not send a KeyEvent.DEL, but
		// rather call this method instead.
		inputProcessor.executeCommand(InputFragment.DELETE);
		return true;
	}

}
