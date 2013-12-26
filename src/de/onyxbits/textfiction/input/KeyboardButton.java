package de.onyxbits.textfiction.input;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.CorrectionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.ExtractedText;
import android.view.inputmethod.ExtractedTextRequest;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;

/**
 * A subclass of imagebutton that pops up the keyboard and provides an
 * inputconnection that is able to capture individual keypresses.
 * 
 * @author patrick
 * 
 */
public class KeyboardButton extends ImageButton {

	private CharInputConnection con;

	public KeyboardButton(Context context, AttributeSet attrs) {
		this(context,attrs,0);
	}
	
	public KeyboardButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public InputConnection onCreateInputConnection(EditorInfo info) {
		info.inputType = InputType.TYPE_NULL; // We can't do fancy stuff!
		if (con==null) {
			con= new CharInputConnection(this,false);
		}
		return con;
	}

	public boolean onCheckIsTextEditor() {
		return true;
	}

	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			requestFocus();
			InputMethodManager inputMethodManager = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (inputMethodManager != null) {
				inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			}
		}
		return true;

	}
}
