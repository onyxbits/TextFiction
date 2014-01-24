/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

public class ZInstruction5 extends ZInstruction {
	final static int OP_CALL_2S = 25;
	final static int OP_CALL_2N = 26;
	final static int OP_SET_COLOUR = 27;
	final static int OP_THROW = 28;
	final static int OP_CALL_1S = 136;
	final static int OP_CALL_1N = 143;
	final static int OP_OLD_SAVE = 181;
	final static int OP_OLD_RESTORE = 182;
	final static int OP_CATCH = 185;
	final static int OP_OLD_SHOW_STATUS = 188;
	final static int OP_VERIFY = 189;
	final static int OP_EXTENDED = 190;
	final static int OP_PIRACY = 191;
	final static int OP_CALL_VS = 224;
	final static int OP_AREAD = 228;
	final static int OP_CALL_VS2 = 236;
	final static int OP_ERASE_WINDOW = 237;
	final static int OP_ERASE_LINE = 238;
	final static int OP_SET_CURSOR = 239;
	final static int OP_GET_CURSOR = 240;
	final static int OP_SET_TEXT_STYLE = 241;
	final static int OP_BUFFER_MODE = 242;
	final static int OP_READ_CHAR = 246;
	final static int OP_SCAN_TABLE = 247;
	final static int OP_NOT = 248;
	public final static int OP_CALL_VN = 249;
	final static int OP_CALL_VN2 = 250;
	final static int OP_TOKENISE = 251;
	final static int OP_ENCODE_TEXT = 252;
	final static int OP_COPY_TABLE = 253;
	final static int OP_PRINT_TABLE = 254;
	final static int OP_CHECK_ARG_COUNT = 255;
	final static int OP_SAVE = 256;
	final static int OP_RESTORE = 257;
	final static int OP_LOG_SHIFT = 258;
	final static int OP_ART_SHIFT = 259;
	final static int OP_SET_FONT = 260;
	final static int OP_SAVE_UNDO = 265;
	final static int OP_RESTORE_UNDO = 266;
	final static int OP_PRINT_UNICODE = 267;
	final static int OP_CHECK_UNICODE = 268;

	final static int SCREEN_UNSPLIT = -1;
	final static int SCREEN_NOUNSPLIT = -2;

	protected short call_opnum;
	protected boolean has_returned = false;

	static boolean[] store5 = null;
	static boolean[] branch5 = null;

	ZInstruction5(ZMachine zm) {
		this.zm = zm;
		if (store5 == null) {
			store5 = new boolean[285];
			branch5 = new boolean[285];
			setupbs();
		}
		operands = new short[8];
	}

	public void decode_instruction() {
		has_returned = false;
		super.decode_instruction();
	}

	public void execute() {
		short result;

		switch (opnum) {
			case OP_JE:
				result = op_je();
				break;
			case OP_JL:
				result = op_jl();
				break;
			case OP_JG:
				result = op_jg();
				break;
			case OP_DEC_CHK:
				result = op_dec_chk();
				break;
			case OP_INC_CHK:
				result = op_inc_chk();
				break;
			case OP_JIN:
				result = op_jin();
				break;
			case OP_TEST:
				result = op_test();
				break;
			case OP_OR:
				result = op_or();
				break;
			case OP_AND:
				result = op_and();
				break;
			case OP_TEST_ATTR:
				result = op_test_attr();
				break;
			case OP_SET_ATTR:
				result = op_set_attr();
				break;
			case OP_CLEAR_ATTR:
				result = op_clear_attr();
				break;
			case OP_STORE:
				result = op_store();
				break;
			case OP_INSERT_OBJ:
				result = op_insert_obj();
				break;
			case OP_LOADW:
				result = op_loadw();
				break;
			case OP_LOADB:
				result = op_loadb();
				break;
			case OP_GET_PROP:
				result = op_get_prop();
				break;
			case OP_GET_PROP_ADDR:
				result = op_get_prop_addr();
				break;
			case OP_GET_NEXT_PROP:
				result = op_get_next_prop();
				break;
			case OP_ADD:
				result = op_add();
				break;
			case OP_SUB:
				result = op_sub();
				break;
			case OP_MUL:
				result = op_mul();
				break;
			case OP_DIV:
				result = op_div();
				break;
			case OP_MOD:
				result = op_mod();
				break;
			case OP_CALL_2S:
				result = op_call_2s();
				break;
			case OP_CALL_2N:
				result = op_call_2n();
				break;
			case OP_SET_COLOUR:
				result = op_set_colour();
				break;
			case OP_THROW:
				result = op_throw();
				break;
			case OP_JZ:
				result = op_jz();
				break;
			case OP_GET_SIBLING:
				result = op_get_sibling();
				break;
			case OP_GET_CHILD:
				result = op_get_child();
				break;
			case OP_GET_PARENT:
				result = op_get_parent();
				break;
			case OP_GET_PROP_LEN:
				result = op_get_prop_len();
				break;
			case OP_INC:
				result = op_inc();
				break;
			case OP_DEC:
				result = op_dec();
				break;
			case OP_PRINT_ADDR:
				result = op_print_addr();
				break;
			case OP_CALL_1S:
				result = op_call_1s();
				break;
			case OP_REMOVE_OBJ:
				result = op_remove_obj();
				break;
			case OP_PRINT_OBJ:
				result = op_print_obj();
				break;
			case OP_RET:
				result = op_ret();
				break;
			case OP_JUMP:
				result = op_jump();
				break;
			case OP_PRINT_PADDR:
				result = op_print_paddr();
				break;
			case OP_LOAD:
				result = op_load();
				break;
			case OP_CALL_1N:
				result = op_call_1n();
				break;
			case OP_RTRUE:
				result = op_rtrue();
				break;
			case OP_RFALSE:
				result = op_rfalse();
				break;
			case OP_PRINT:
				result = op_print();
				break;
			case OP_PRINT_RET:
				result = op_print_ret();
				break;
			case OP_NOP:
				result = op_nop();
				break;
			case OP_OLD_SAVE:
				result = op_illegal();
				break;
			case OP_OLD_RESTORE:
				result = op_illegal();
				break;
			case OP_RESTART:
				result = op_restart();
				break;
			case OP_RET_POPPED:
				result = op_ret_popped();
				break;
			case OP_CATCH:
				result = op_catch();
				break;
			case OP_QUIT:
				result = op_quit();
				break;
			case OP_NEW_LINE:
				result = op_new_line();
				break;
			case OP_OLD_SHOW_STATUS:
				result = op_nop();
				break;
			case OP_VERIFY:
				result = op_verify();
				break;
			case OP_EXTENDED:
				result = op_extended();
				break;
			case OP_PIRACY:
				result = op_piracy();
				break;
			case OP_CALL_VS:
				result = op_call_vs();
				break;
			case OP_STOREW:
				result = op_storew();
				break;
			case OP_STOREB:
				result = op_storeb();
				break;
			case OP_PUT_PROP:
				result = op_put_prop();
				break;
			case OP_AREAD:
				result = op_aread();
				break;
			case OP_PRINT_CHAR:
				result = op_print_char();
				break;
			case OP_PRINT_NUM:
				result = op_print_num();
				break;
			case OP_RANDOM:
				result = op_random();
				break;
			case OP_PUSH:
				result = op_push();
				break;
			case OP_PULL:
				result = op_pull();
				break;
			case OP_SPLIT_WINDOW:
				result = op_split_window();
				break;
			case OP_SET_WINDOW:
				result = op_set_window();
				break;
			case OP_CALL_VS2:
				result = op_call_vs2();
				break;
			case OP_ERASE_WINDOW:
				result = op_erase_window();
				break;
			case OP_ERASE_LINE:
				result = op_erase_line();
				break;
			case OP_SET_CURSOR:
				result = op_set_cursor();
				break;
			case OP_GET_CURSOR:
				result = op_get_cursor();
				break;
			case OP_SET_TEXT_STYLE:
				result = op_set_text_style();
				break;
			case OP_BUFFER_MODE:
				result = op_buffer_mode();
				break;
			case OP_OUTPUT_STREAM:
				result = op_output_stream();
				break;
			case OP_INPUT_STREAM:
				result = op_input_stream();
				break;
			case OP_SOUND_EFFECT:
				result = op_sound_effect();
				break;
			case OP_READ_CHAR:
				result = op_read_char();
				break;
			case OP_SCAN_TABLE:
				result = op_scan_table();
				break;
			case OP_NOT:
				result = op_not();
				break;
			case OP_CALL_VN:
				result = op_call_vn();
				break;
			case OP_CALL_VN2:
				result = op_call_vn2();
				break;
			case OP_TOKENISE:
				result = op_tokenise();
				break;
			case OP_ENCODE_TEXT:
				result = op_encode_text();
				break;
			case OP_COPY_TABLE:
				result = op_copy_table();
				break;
			case OP_PRINT_TABLE:
				result = op_print_table();
				break;
			case OP_CHECK_ARG_COUNT:
				result = op_check_arg_count();
				break;
			case OP_SAVE:
				result = op_save();
				break;
			case OP_RESTORE:
				result = op_restore();
				break;
			case OP_LOG_SHIFT:
				result = op_log_shift();
				break;
			case OP_ART_SHIFT:
				result = op_art_shift();
				break;
			case OP_SET_FONT:
				result = op_set_font();
				break;
			case OP_SAVE_UNDO:
				result = op_save_undo();
				break;
			case OP_RESTORE_UNDO:
				result = op_restore_undo();
				break;
			case OP_PRINT_UNICODE: {
				// FIXME: PRINT and CHECK unicode are standard v1.0 extensions. They are
				// not defined in the original ZPlet. Theoretically, neither should
				// be called, but it happens anyways. In CHECK we state we can't handle
				// unicode, but that doesn't seem to stop games from trying anyways.
			  // Since unicode support is kinda a low priority, we work around this
				// by doing a normal print_char, which will eventually fall through 
				// a \ufffd character. That's not pretty, but at least its safe.
				result = op_print_char();
				break;
			}
			case OP_CHECK_UNICODE: {
				result = 0;
				break;
			}
			default:
				result = op_illegal();
		}

		if (!iscall() && isstore()) {
			zm.set_variable(storevar, result);
		}

		if (isbranch()) {
			if ((result == 0) != branchtype) {
				/*
				 * that is, if result is 0 and branchtype is false, or result is nonzero
				 * and branchtype is true
				 */
				switch (branchoffset) {
					case 0:
						z_ret();
						if (isstore())
							zm.set_variable(storevar, ZFALSE);
						break;
					case 1:
						z_ret();
						if (isstore())
							zm.set_variable(storevar, ZTRUE);
						break;
					default:
						zm.pc += branchoffset - 2;
				}
			}
		}
	}

	protected boolean isbranch() {
		return branch5[opnum];
	}

	protected boolean isstore() {
		if (has_returned)
			return store5[call_opnum];
		else
			return store5[opnum];
	}

	protected boolean iscall() {
		switch (opnum) {
			case OP_CALL_1S:
			case OP_CALL_1N:
			case OP_CALL_2S:
			case OP_CALL_2N:
			case OP_CALL_VS:
			case OP_CALL_VN:
			case OP_CALL_VS2:
			case OP_CALL_VN2:
				return true;
			default:
				return false;
		}
	}

	protected short z_call() {
		int nlocals;
		int i;

		if (operands[0] == 0) {
			/* calls to zero return false */
			/*
			 * Can't just return false because call is not treated as a store in
			 * execute_instruction
			 */
			if (isstore())
				zm.set_variable(storevar, ZFALSE);

			/*
			 * a non-storing call to zero is pretty ridiculous, but hey, it could
			 * happen
			 */
		}
		else {
			zm.zstack.push(new ZFrameBound(isstore()));
			if (isstore())
				zm.zstack.push(new Integer(storevar));
			zm.zstack.push(new Integer(opnum));
			zm.zstack.push(new Integer(zm.pc));
			zm.zstack.push(new Integer(((ZMachine5) zm).argcount));
			zm.zstack.push(zm.locals);
			// System.err.print("From ");
			// System.err.print(Integer.toString(zm.pc, 16));
			zm.pc = zm.routine_address(operands[0]);
			// System.err.print(" calling routine at ");
			// System.err.println(Integer.toString(zm.pc, 16));
			nlocals = zm.get_code_byte();
			((ZMachine5) zm).argcount = (short) (count - 1);
			zm.locals = new short[nlocals];
			for (i = 0; i < nlocals; i++) {
				if (i < (count - 1)) {
					zm.locals[i] = operands[i + 1];
				}
				else {
					zm.locals[i] = 0;
				}
			}
		}
		return ZFALSE;
	}

	protected void z_ret() /* overrides Standard z_ret */
	{
		Object tos;

		do
			tos = zm.zstack.pop();
		while (!(tos instanceof short[]));
		zm.locals = (short[]) tos;
		((ZMachine5) zm).argcount = (short) ((Integer) zm.zstack.pop()).intValue();
		// System.err.print("From ");
		// System.err.print(Integer.toString(zm.pc, 16));
		zm.pc = ((Integer) zm.zstack.pop()).intValue();
		// System.err.print(" returning to ");
		// System.err.println(Integer.toString(zm.pc, 16));
		call_opnum = (short) (((Integer) zm.zstack.pop()).intValue());
		has_returned = true;
		if (isstore()) {
			storevar = (short) (((Integer) zm.zstack.pop()).intValue());
		}
		zm.zstack.pop(); /* stack frame boundary */
	}

	protected short op_call_2s() {
		z_call();
		return ZFALSE;
	}

	protected short op_call_2n() {
		z_call();
		return ZFALSE;
	}

	protected short op_set_colour() {
		int foreground = operands[0];
		int background = operands[1];

		if (foreground == ZColor.Z_DEFAULT) {
			foreground = ((ZHeader5) zm.header).default_foreground_color();
		}

		if (background == ZColor.Z_DEFAULT) {
			background = ((ZHeader5) zm.header).default_background_color();
		}

		zm.current_window.set_color(foreground, background);
		return ZFALSE;
	}

	protected short op_throw() {
		/* TODO */
		return ZNOTDONE;
	}

	protected short op_call_1s() {
		z_call();
		return ZFALSE;
	}

	protected short op_call_1n() {
		z_call();
		return ZFALSE;
	}

	protected short op_catch() {
		/* TODO */
		return ZNOTDONE;
	}

	protected short op_call_vs() {
		z_call();
		return ZFALSE;
	}

	protected short op_aread() {
		byte ch;
		int tsize;
		int tbuf;
		int bufloc;

		tbuf = operands[0] & 0xFFFF;

		// System.err.println("aread " + Integer.toString(tbuf, 16) + " " +
		// Integer.toString(operands[1]&0xFFFF,16));
		zm.current_window.flush();
		zm.current_window.reset_line_count();
		tsize = zm.memory_image[tbuf] & 0xFF;
		// System.err.println("tsize = " + tsize);
		if (tsize < 3)
			zm.fatal("Text Buffer < 3 bytes");
		bufloc = 2;
		/* TODO prime input buffer here */
		ch = zm.get_input_byte(true);
		/* TODO handle terminating character table here */
		while ((tsize != 0) && (ch != 13) && (ch != 10)) {
			// System.err.println("sr_ch @ " + Integer.toString(tbuf+bufloc, 16) +
			// " : " + ch);
			if ((ch >= (byte) 'A') && (ch <= (byte) 'Z')) {
				ch = (byte) (ch - (byte) 'A' + (byte) 'a');
			}
			zm.memory_image[tbuf + bufloc] = ch;
			bufloc++;
			tsize--;
			ch = zm.get_input_byte(true);
		}
		zm.memory_image[tbuf + 1] = (byte) (bufloc - 2);
		if (operands[1] != 0)
			zm.zd.tokenise(tbuf + 2, bufloc - 2, operands[1] & 0xFFFF);
		return (short) (ch & 0xFF);
	}

	protected short op_call_vs2() {
		z_call();
		return ZFALSE;
	}

	protected short op_erase_window() {
		if (operands[0] == SCREEN_UNSPLIT) {
			split_screen(0);
			zm.screen.clear();
			zm.window[LOWER_WINDOW].movecursor(0, 0);
			zm.window[LOWER_WINDOW].reset_line_count();
		}
		else if (operands[0] == SCREEN_NOUNSPLIT) {
			zm.screen.clear();
			zm.window[LOWER_WINDOW].movecursor(0, 0);
			zm.window[LOWER_WINDOW].reset_line_count();
			zm.window[UPPER_WINDOW].movecursor(0, 0);
			zm.window[UPPER_WINDOW].reset_line_count();
		}
		else {
			zm.window[operands[0]].clear();
			zm.window[operands[0]].movecursor(0, 0);
			zm.window[operands[0]].reset_line_count();
		}
		return ZFALSE;
	}

	protected short op_erase_line() {
		zm.current_window.erase_line(operands[0]);
		return ZFALSE;
	}

	protected short op_set_cursor() {
		int x = operands[1] & 0xFFFF;
		int y = operands[0] & 0xFFFF;

		if (zm.current_window == zm.window[UPPER_WINDOW])
			zm.current_window.movecursor(x - 1, y - 1);
		return ZFALSE;
	}

	protected short op_get_cursor() {
		int x, y;

		int table = operands[0] & 0xFFFF;
		zm.current_window.flush();
		x = zm.current_window.getx() + 1;
		y = zm.current_window.gety() + 1;
		zm.memory_image[table] = (byte) ((y >> 8) & 0xFF);
		zm.memory_image[table + 1] = (byte) (y & 0xFF);
		zm.memory_image[table + 2] = (byte) ((x >> 8) & 0xFF);
		zm.memory_image[table + 3] = (byte) (x & 0xFF);
		return ZFALSE;
	}

	protected short op_set_text_style() {
		zm.current_window.set_text_style(operands[0] & 0xFFFF);
		return ZFALSE;
	}

	protected short op_buffer_mode() {
		// zm.window[LOWER_WINDOW].setbuffermode(operands[0] != 0);
		zm.window[LOWER_WINDOW].setwrapmode(operands[0] != 0);
		return ZFALSE;
	}

	protected short op_read_char() {
		short ch;

		zm.current_window.flush();
		zm.current_window.reset_line_count();
		ch = (short) (zm.get_input_byte(false) & 0xFF);
		return ch;
	}

	protected short op_scan_table() {
		int advance = 2;
		int location = operands[1] & 0xFFFF;
		int lastloc;
		int len = operands[2] & 0xFFFF;
		boolean words = true;

		if (count == 4) {
			advance = operands[3] & 0x7F;
			words = (operands[3] & 0x80) == 0x80;
		}
		if (words) {
			lastloc = location + (len << 1);
			while (location < lastloc) {
				if (((zm.memory_image[location] & 0xFF) == ((operands[0] >> 8) & 0xFF))
						&& ((zm.memory_image[location + 1] & 0xFF) == (operands[0] & 0xFF))) {
					return (short) location;
				}
				location += advance;
			}
		}
		else { /* bytes */
			lastloc = location + len;
			while (location < lastloc) {
				if ((zm.memory_image[location] & 0xFF) == (operands[0] & 0xFFFF)) {
					return (short) location;
				}
				location += advance;
			}
		}
		return 0;
	}

	protected short op_call_vn() {
		z_call();
		return ZFALSE;
	}

	protected short op_call_vn2() {
		z_call();
		return ZFALSE;
	}

	protected short op_tokenise() {
		int tbuf, tlen;
		int userdict;
		boolean parseunknown;

		if (count < 3)
			userdict = 0;
		else
			userdict = operands[2];

		parseunknown = (count < 3) || (operands[3] == 0);

		if (userdict != 0)
			System.err.println("tokenise opcode encountered (userdict)");

		tbuf = operands[0] & 0xFFFF;
		tlen = zm.memory_image[tbuf + 1];

		((ZDictionary5) zm.zd).tokenise(tbuf + 2, tlen, operands[1] & 0xFFFF,
				parseunknown);
		return ZNOTDONE;
	}

	protected short op_encode_text() {
		short encword[];
		int ascii_text = operands[0] & 0xFFFF + operands[2];
		int coded_text = operands[3] & 0xFFFF;
		int i;

		encword = zm.encode_word(ascii_text, operands[1], 6);
		for (i = 0; i < 3; i++) {
			zm.memory_image[coded_text + i + i] = (byte) ((encword[i] >> 8) & 0xFF);
			zm.memory_image[coded_text + i + i + 1] = (byte) (encword[i] & 0xFF);
		}
		return ZFALSE;
	}

	protected short op_copy_table() {
		int first = operands[0] & 0xFFFF;
		int second = operands[1] & 0xFFFF;
		int length = operands[2];
		int i;

		if (second == 0) {
			if (length < 0)
				length = -length;
			for (i = first + length - 1; i >= first; i--)
				zm.memory_image[i] = 0;
		}
		else {
			if (length > 0)
				System.arraycopy(zm.memory_image, first, zm.memory_image, second,
						length);
			else {
				length = -length;
				for (i = 0; i < length; i++) {
					zm.memory_image[second + i] = zm.memory_image[first + i];
				}
			}
		}
		return ZFALSE;
	}

	protected short op_print_table() {
		int textpos = operands[0] & 0xFFFF;
		int width = operands[1] & 0xFFFF;
		int height = 1;
		int skip = 0;
		int i, j;
		int x, y;

		if (count > 2)
			height = operands[2] & 0xFFFF;

		if (count > 3)
			skip = operands[3] & 0xFFFF;

		/* TODO */
		zm.current_window.flush();
		x = zm.current_window.getx();
		y = zm.current_window.gety();
		for (j = 0; j < height; j++) {
			zm.current_window.movecursor(x, y + j);
			for (i = 0; i < width; i++) {
				zm.print_ascii_char(zm.memory_image[textpos++]);
			}
			textpos += skip;
		}
		return ZFALSE;
	}

	protected short op_check_arg_count() {
		if (((operands[0] & 0xFFFF) - 1) < ((ZMachine5) zm).argcount)
			return ZTRUE;
		return ZFALSE;
	}

	protected short op_log_shift() {
		if (operands[1] >= 0)
			return (short) ((operands[0] & 0xFFFF) << operands[1]);
		else
			return (short) ((operands[0] & 0xFFFF) >> -operands[1]);
	}

	protected short op_art_shift() {
		if (operands[1] >= 0)
			return (short) (operands[0] << operands[1]);
		else
			return (short) (operands[0] << -operands[1]);
	}

	protected short op_set_font() {
		/* TODO */
		return ZNOTDONE;
	}

	protected short op_save_undo() {
		short result;

		zm.zstack.push(new Integer(storevar));
		result = (short) ((ZMachine5) zm).save_undo();
		if (result == 0)
			zm.zstack.pop();
		return result;
	}

	protected short op_restore_undo() {
		short result;

		result = (short) ((ZMachine5) zm).restore_undo();
		if (result != 0)
			storevar = (short) ((Integer) zm.zstack.pop()).intValue();
		return result;
	}

	protected void setupbs() {
		/* Sets up store and branch instructions */
		branch5[OP_JE] = true;
		branch5[OP_JL] = true;
		branch5[OP_JG] = true;
		branch5[OP_DEC_CHK] = true;
		branch5[OP_INC_CHK] = true;
		branch5[OP_JIN] = true;
		branch5[OP_TEST] = true;
		store5[OP_OR] = true;
		store5[OP_AND] = true;
		branch5[OP_TEST_ATTR] = true;
		store5[OP_LOADW] = true;
		store5[OP_LOADB] = true;
		store5[OP_GET_PROP] = true;
		store5[OP_GET_PROP_ADDR] = true;
		store5[OP_GET_NEXT_PROP] = true;
		store5[OP_ADD] = true;
		store5[OP_SUB] = true;
		store5[OP_MUL] = true;
		store5[OP_DIV] = true;
		store5[OP_MOD] = true;
		store5[OP_CALL_2S] = true;
		branch5[OP_JZ] = true;
		branch5[OP_GET_SIBLING] = true;
		store5[OP_GET_SIBLING] = true;
		branch5[OP_GET_CHILD] = true;
		store5[OP_GET_CHILD] = true;
		store5[OP_GET_PARENT] = true;
		store5[OP_GET_PROP_LEN] = true;
		store5[OP_CALL_1S] = true;
		store5[OP_LOAD] = true;
		store5[OP_CATCH] = true;
		branch5[OP_VERIFY] = true;
		branch5[OP_PIRACY] = true;
		store5[OP_CALL_VS] = true;
		store5[OP_AREAD] = true;
		store5[OP_RANDOM] = true;
		store5[OP_CALL_VS2] = true;
		store5[OP_READ_CHAR] = true;
		store5[OP_SCAN_TABLE] = true;
		branch5[OP_SCAN_TABLE] = true;
		store5[OP_NOT] = true;
		branch5[OP_CHECK_ARG_COUNT] = true;
		store5[OP_SAVE] = true;
		store5[OP_RESTORE] = true;
		store5[OP_LOG_SHIFT] = true;
		store5[OP_ART_SHIFT] = true;
		store5[OP_SET_FONT] = true;
		store5[OP_SAVE_UNDO] = true;
		store5[OP_RESTORE_UNDO] = true;
	}
}
