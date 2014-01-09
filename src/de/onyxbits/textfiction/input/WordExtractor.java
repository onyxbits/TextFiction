package de.onyxbits.textfiction.input;


import android.content.Context;
import android.media.AudioManager;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

/**
 * Attach this as an OnTouchListener to any textview to have touched words
 * extracted and sent to the InputFragment.
 * 
 * @author patrick
 * 
 */
public class WordExtractor implements OnTouchListener, OnGestureListener,
		OnDoubleTapListener {

	private InputFragment inputFragment;
	private Context context;
	private TextView view;
	private AudioManager audioManager;
	private boolean doClick;
	private InputProcessor inputProcessor;

	private GestureDetectorCompat detector;

	public WordExtractor(Context context) {
		this.context = context;
		audioManager = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);
	}


	/**
	 * If set, the fragment will be notified when the user touches a word or
	 * double taps empty space.
	 * 
	 * @param input
	 *          target fragment
	 */
	public void setInputFragment(InputFragment input) {
		this.inputFragment = input;
	}

	public void setInputProcessor(InputProcessor input) {
		this.inputProcessor = input;
	}

	/**
	 * Play a click sound when words are touched for copy?
	 * 
	 * @param doClick
	 *          true to click
	 */
	public void setKeyclick(boolean doClick) {
		this.doClick = doClick;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (detector == null) {
			detector = new GestureDetectorCompat(context, this);
			detector.setOnDoubleTapListener(this);
			detector.setIsLongpressEnabled(true);
		}
		view = (TextView) v;
		return detector.onTouchEvent(event);
	}

	private String extractWord(TextView tv, MotionEvent event) {
		int pos = getOffsetForPosition(tv, event.getX(), event.getY());
		int start = pos;
		int end = pos;
		String sel = "";
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
			}
		}
		return sel;
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

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		String tmp = extractWord(view, e);
		if (inputFragment != null && tmp.length() != 0) {
			inputFragment.appendWord(tmp);
			if (doClick) {
				audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 1f);
			}
		}
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (inputFragment != null) {
			inputFragment.removeWord();
		}
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (inputProcessor!=null && extractWord(view, e).length() == 0) {
			inputProcessor.utterText(null);
		}
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		String tmp = extractWord(view, e);
		if (tmp.length() == 0) {
			if (inputProcessor != null) {
				inputProcessor.utterText(view.getText());
			}
		}
		else {
			inputProcessor.toggleTextHighlight(tmp);
		}
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}
}
