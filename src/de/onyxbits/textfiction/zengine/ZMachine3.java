/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

public class ZMachine3 extends ZMachine {

	public ZMachine3(ZScreen screen, ZStatus status_line, byte[] memory_image) {
		super(screen, status_line, memory_image);

		header = new ZHeader3(memory_image);
		objects = new ZObjectTree3(this);
		zd = new ZDictionary3(this);
		globals = header.global_table();
		window = new ZWindow[2];
		window[0] = new ZWindow(screen,false);
		window[1] = new ZWindow(screen,true);
		current_window = window[0];
		zi = new ZInstruction(this);
	}

	public void update_status_line() {
		ZHeader3 header = (ZHeader3) this.header;

		status_redirect = true;
		status_location = "";
		print_string(objects.short_name_addr(get_variable((short) 16)));
		status_redirect = false;
		if (header.time_game())
			status_line.update_time_line(status_location, get_variable((short) 17),
					get_variable((short) 18));
		else
			status_line.update_score_line(status_location, get_variable((short) 17),
					get_variable((short) 18));
		status_location = null;
	}

	public int string_address(short addr) {
		return (((int) addr) & 0xFFFF) << 1;
	}

	public int routine_address(short addr) {
		return (((int) addr) & 0xFFFF) << 1;
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
		ZHeader3 header = (ZHeader3) this.header;

		super.set_header_flags();
		header.set_status_unavailable(false);
		header.set_splitting_available(true);
		header.set_variable_default(false);
	}
}
