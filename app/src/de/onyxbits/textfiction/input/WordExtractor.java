package de.onyxbits.textfiction.input;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.TextView;

/**
 * Attach this as an OnTouchListener to any textview to have touched words
 * extracted and sent to the InputFragment.
 * 
 * @author patrick
 * 
 */
public class WordExtractor implements OnTouchListener {

	private InputFragment target;
	private long lastUp = -1;
	private static final int DOUBLE_TAP_TIMEOUT = ViewConfiguration
			.getDoubleTapTimeout();

	public WordExtractor(Context context, InputFragment target) {
		this.target = target;

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		TextView tv = (TextView) v;
		int pos = getOffsetForPosition(tv, event.getX(), event.getY());
		int start = pos;
		int end = pos;
		String sel = "";

		// Word touched? -> Append it to the commandline
		char[] buf = tv.getText().toString().toCharArray();
		if (pos > -1 && pos < buf.length) {
			while (start > 0 && Character.isLetterOrDigit(buf[start])) {
				start--;
			}
			if (!Character.isLetterOrDigit(buf[start])) {
				start++;
			}
			while (end < buf.length - 1 && Character.isLetterOrDigit(buf[end])) {
				end++;
			}
			if (!Character.isLetterOrDigit(buf[end])) {
				end--;
			}

			if (end - start + 1 > 0) {
				sel = new String(buf, start, end - start + 1);
				target.appendToCommandLine(sel);
			}
		}

		// Empty space double tapped? -> Clear the commandline
		if ((event.getEventTime() - lastUp < DOUBLE_TAP_TIMEOUT)
				&& (sel.length() == 0)) {
			target.appendToCommandLine("");
		}
		lastUp = event.getEventTime();

		return false;
	}

	private int getOffsetForPosition(TextView tv, float x, float y) {
		if (tv.getLayout() == null)
			return -1;
		final int line = getLineAtCoordinate(tv, y);
		final int offset = getOffsetAtCoordinate(tv, line, x);
		return offset;
	}

	private float convertToLocalHorizontalCoordinate(TextView tv, float x) {
		x -= tv.getTotalPaddingLeft();
		// Clamp the position to inside of the view.
		x = Math.max(0.0f, x);
		x = Math.min(tv.getWidth() - tv.getTotalPaddingRight() - 1, x);
		x += tv.getScrollX();
		return x;
	}

	private int getLineAtCoordinate(TextView tv, float y) {
		y -= tv.getTotalPaddingTop();
		// Clamp the position to inside of the view.
		y = Math.max(0.0f, y);
		y = Math.min(tv.getHeight() - tv.getTotalPaddingBottom() - 1, y);
		y += tv.getScrollY();
		return tv.getLayout().getLineForVertical((int) y);
	}

	private int getOffsetAtCoordinate(TextView tv, int line, float x) {
		x = convertToLocalHorizontalCoordinate(tv, x);
		return tv.getLayout().getOffsetForHorizontal(line, x);
	}

}
