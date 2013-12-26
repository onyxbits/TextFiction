/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

public class ZMachine5 extends ZMachine {

	public short argcount;
	ZState undo_state = null;

	public ZMachine5(ZScreen screen, byte[] memory_image) {
		super(screen, null, memory_image);
		header = new ZHeader5(memory_image);
		objects = new ZObjectTree5(this);
		zd = new ZDictionary5(this);
		globals = header.global_table();
		window = new ZWindow[2];
		window[0] = new ZWindow(screen,false);
		window[1] = new ZWindow(screen,true);
		current_window = window[0];
		zi = new ZInstruction5(this);
		argcount = 0;
	}

	public void update_status_line() {
	}

	public int string_address(short addr) {
		return (((int) addr) & 0xFFFF) << 2;
	}

	public int routine_address(short addr) {
		return (((int) addr) & 0xFFFF) << 2;
	}

	public void restart() {

		super.restart();

		window[0].moveto(0, 0);
		window[1].moveto(0, 0);
		window[0].resize(screen.getchars(), screen.getlines());
		window[1].resize(0, 0);
		window[0].movecursor(0, window[0].getHeight() - 1);
	}

	public void set_header_flags() { /* at start, restart, restore */
		ZHeader5 header = (ZHeader5) this.header;

		super.set_header_flags();
		header.set_revision(0, 0);

		/* screen model flags */
		header.set_colors_available(false);
		header.set_bold_available(true);
		header.set_italic_available(true);
		header.set_fixed_font_available(true);
		header.set_timed_input_available(false);
		header.set_graphics_font_available(false);

		/* other flags (is mouse part of screen model?) */
		header.set_undo_available(true);
		header.set_mouse_available(false);
		header.set_sound_available(false);
		header.set_interpreter_number(ZHeader5.INTERP_MSDOS);
		header.set_interpreter_version((int) 'J');
		header.set_screen_height_lines(screen.getlines());
		header.set_screen_width_characters(screen.getchars());

		/* TODO -- units */
		header.set_screen_height_units(screen.getlines());
		header.set_screen_width_units(screen.getchars());
		header.set_font_height_units(1);
		header.set_font_width_units(1);

		header.set_default_background_color(screen.getZBackground());
		header.set_default_foreground_color(screen.getZForeground());
	}

	int restore_undo() {
		if (undo_state != null) {
			undo_state.header.set_transcripting(header.transcripting());
			undo_state.restore_saved();
			set_header_flags();
			return 2;
		}
		return 0;
	}

	int save_undo() {
		if (undo_state == null) {
			undo_state = new ZState(this);
		}
		undo_state.save_current();
		return 1;
	}
}
