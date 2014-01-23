/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

import java.util.Arrays;

public class ZWindow {
	public final static int ROMAN = 0;
	public final static int REVERSE = 1;
	public final static int BOLD = 2;
	public final static int ITALIC = 4;
	public final static int FIXED = 8;

	final static char FIRST_STYLE = '\u8000';
	final static char BUF_ROMAN = (char) (FIRST_STYLE + ROMAN);
	final static char BUF_REVERSE = (char) (FIRST_STYLE + REVERSE);
	final static char BUF_BOLD = (char) (FIRST_STYLE + BOLD);
	final static char BUF_ITALIC = (char) (FIRST_STYLE + ITALIC);
	final static char BUF_FIXED = (char) (FIRST_STYLE + FIXED);
	final static char LAST_STYLE = '\u800f';

	final static int NORMAL_FONT = 1;
	final static int PICTURE_FONT = 2;
	final static int GRAPHICS_FONT = 3;
	final static int FIXED_FONT = 4;

	final static char FIRST_FONT = '\u8010';
	final static char BUF_NORMAL_FONT = '\u8010';
	final static char BUF_PICTURE_FONT = '\u8011';
	final static char BUF_GRAPHICS_FONT = '\u8012';
	final static char BUF_FIXED_FONT = '\u8013';
	final static char LAST_FONT = '\u8013';

	private int top, left, width, height;
	int cursorx, cursory;
	int curzfont = NORMAL_FONT;
	int curzstyle = ROMAN;
	int stylestart = 0;
	int line_counter;
	int zforeground, zbackground;

	private ZScreen screen;

	public boolean upper;
	public char[] frameBuffer;
	public StyleRegion regions;
	public int cursor;
	public int maxCursor;
	public int endWindow;
	public int startWindow;

	public ZWindow(ZScreen screen, boolean upper) {
		this.upper = upper;
		top = 0;
		left = 0;
		width = 10;
		height = 10;
		line_counter = 0;
		zforeground = screen.zforeground;
		zbackground = screen.zbackground;
		this.screen = screen;
		// NOTE: This is ugly, but the original z-machine screen model has nothing
		// to do with modern screen models, hence we just reserve a large enough
		// buffer that should be sufficient for every game.
		if (upper) {
			frameBuffer = new char[screen.chars * screen.lines * 2];
			Arrays.fill(frameBuffer, 0, frameBuffer.length, ' ');
		}
		else {
			frameBuffer = new char[screen.chars * 255];
		}
	}

	public void reset_line_count() {
		line_counter = 0;
	}

	void count_line() {
		line_counter++;
	}

	public void erase_line(short arg) {
	}

	@Deprecated
	public void setwrapmode(boolean wrapmode) {
	}

	@Deprecated
	public void setscroll(boolean newscroll) {
	}

	public void moveto(int newleft, int newtop) {
		left = newleft;
		top = newtop;
		startWindow = top * width + left;
		endWindow = startWindow + width * height;
	}

	public void resize(int newwidth, int newheight) {
		width = newwidth;
		height = newheight;
		if (upper) {
			if ((cursorx >= newwidth) || (cursory >= newheight)) {
				cursorx = 0;
				cursory = 0;
				cursor = 0;
			}
		}
		startWindow = top * width + left;
		endWindow = startWindow + width * height;
	}

	public int getlines() {
		return height;
	}

	public int getchars() {
		return width;
	}

	public int getx() {
		return cursorx;
	}

	public int gety() {
		return cursory;
	}

	public void movecursor(int x, int y) {
		if (upper) {
			cursorx = x;
			cursory = y;
			cursor = (y * width + x);
			maxCursor = Math.max(cursor, maxCursor);
		}
	}

	/**
	 * Write a character to the framebuffer at the current cursor position.
	 * 
	 * @param ascii
	 *          the character
	 */
	public void printzascii(short ascii) {
		printChar(ZScreen.zascii_to_unicode(ascii));
	}

	public void flush() {
	}

	public void newline() {
		printChar('\n');
	}

	private void printChar(char ch) {
		frameBuffer[cursor] = ch;
		if (cursor < frameBuffer.length - 1) {
			cursor++;
			maxCursor = Math.max(cursor, maxCursor);
			cursorx++;
			if ((cursorx > screen.chars - 2) || (ch == '\n')) {
				cursory++;
				cursorx = 0;
			}
		}
	}

	public void clear() {
		if (upper) {
			cursor = top * width + left;
			Arrays.fill(frameBuffer, startWindow, endWindow, ' ');
		}
		else {
			cursor = 0;
		}
		cursorx = 0;
		cursory = 0;
		maxCursor = cursor;
	}

	@Deprecated
	public void set_color(int foreground, int background) {
	}

	public void set_text_style(int style) {
		StyleRegion region = new StyleRegion();
		region.style=style;
		region.start=cursor;
		region.end=cursor;
		if (regions==null) {
			regions=region;
		}
		else {
			StyleRegion tmp = regions;
			while(tmp.next!=null) {
				tmp=tmp.next;
			}
			tmp.end=cursor;
			tmp.next=region;
		}
	}

	public int getHeight() {
		return height;
	}

	/**
	 * Tell the buffer that it got fetched and can be overwritten on the next
	 * turn.
	 */
	public void retrieved() {
		if (upper) {
			// NOTE: The lower window doesn't support formatted text. Some games
			// (e.g. curses and anchorhead) work around that limitation by
			// temporarily expanding the upperwindow, printing formatted text there,
			// then collapsing it again. Whatever is in the upperwindow is suppose
			// to stay, the "overflow" technically becomes part of the lower window
			// and hence needs to be cleared.
			if (maxCursor > endWindow) {
				Arrays.fill(frameBuffer, endWindow, maxCursor, ' ');
			}
			maxCursor = endWindow;
		}
		else {
			cursorx = 0;
			cursory = 0;
			cursor = 0;
		}
		regions=null;
	}

	/**
	 * Transform a portion of the framebuffer into a string, add newline
	 * characters as required by the window width.
	 * 
	 * @param start
	 *          first character
	 * @param end
	 *          last character
	 * @return a formated string
	 */
	public String stringyfy(int start, int end) {
		if (width<1 || start>=end) {
			return "";
		}
		int len = width;
		int total = end - start;
		char[] tmp = new char[total + total / len];
		int i = start;
		int o = 0;
		while (i < total - len) {
			System.arraycopy(frameBuffer, i, tmp, o, len);
			i += len;
			o += (len + 1);
			tmp[o - 1] = '\n';
		}
		// copy rest
		if (i < total) {
			System.arraycopy(frameBuffer, i, tmp, o, total - i);
		}

		return new String(tmp);
	}

	/**
	 * Dirty hack! Calculate the length of the framebuffer excluding the last line
	 * containing the prompt.
	 * 
	 * @return length of the text in the buffer without the prompt.
	 */
	public int noPrompt() {
		int ret = cursor - 1; // Put ret on the last character.
		if (ret < 0) {
			return cursor;
		}
		while (ret > 0 && frameBuffer[ret] == ' ') {
			ret--;
		}
		if (ret > 1 && frameBuffer[ret] == '>') {
			ret--;
		}
		else {
			// No idea what this is ... better safe than sorry.
			return cursor;
		}
		while (ret > 0 && frameBuffer[ret] == '\n') {
			// Remove all trailing newlines.
			ret--;
		}
		ret++; // Because we x-ed the last real character.
		return ret;
	}

}
