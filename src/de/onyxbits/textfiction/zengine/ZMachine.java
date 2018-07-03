/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

import java.io.File;
import java.util.EmptyStackException;
import java.util.Random;
import java.util.Stack;

import android.util.Log;
import android.widget.Toast;

public abstract class ZMachine {

	public static final int STATE_INIT = 0;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_WAIT_CMD = 2;
	public static final int STATE_WAIT_CHAR = 3;

	public ZWindow current_window;
	public int pc;
	public ZWindow window[];
	public ZHeader header;
	public ZScreen screen;
	public ZObjectTree objects;
	public ZDictionary zd;
	public ZState restart_state;
	public ZStatus status_line;
	public byte memory_image[];
	public Stack zstack;
	public Random zrandom;
	protected int globals;
	public short locals[];
	protected int inputstream;
	protected boolean outputs[];
	protected int printmemory;
	protected int alphabet;
	protected short build_ascii;
	protected short built_ascii;
	protected short abbrev_mode;
	protected short checksum;
	protected ZInstruction zi;
	protected boolean status_redirect;
	protected String status_location;

	protected final String A2 = "0123456789.,!?_#\'\"/\\-:()";

	public final static int OP_LARGE = 0;
	public final static int OP_SMALL = 1;
	public final static int OP_VARIABLE = 2;
	public final static int OP_OMITTED = 3;

	private int runState = STATE_INIT;

	private char[] inputBuffer;
	private int inputIndex;
	
	public boolean saveCalled;
	public boolean restoreCalled;
	
	/**
	 * Where the save/restore opcode saveto /restore from.
	 */
	private File quickSaveSlot;

	public ZMachine(ZScreen screen, ZStatus status_line, byte[] memory_image) {
		this.screen = screen;
		this.status_line = status_line;
		this.memory_image = memory_image;
		locals = new short[0];
		zstack = new Stack();
		restart_state = new ZState(this);
		restart_state.save_current();
		zrandom = new Random(); /* starts in "random" mode */
		inputstream = 0;
		outputs = new boolean[5];
		outputs[1] = true;
		alphabet = 0;
	}

	public abstract void update_status_line();

	/**
	 * Store keyboard input into the keyboard buffer (will be processed on the
	 * next run()).
	 * 
	 * @param input
	 *          what the user entered. Note: this must either be a single byte or
	 *          a newline terminated string, depending on the runstate.
	 */
	public void fillInputBuffer(char[] input) {
		inputBuffer = input;
		inputIndex = 0;
	}

	/**
	 * Called by from the instructions. The input buffer must first be filled with
	 * fillInputBuffer().
	 * 
	 * @param buffered
	 *          ignored.
	 * @return the next byte in the buffer.
	 */
	public byte get_input_byte(boolean buffered) {
		if (inputIndex < inputBuffer.length) {
			byte ret = 13;
			try {
				if (inputBuffer[inputIndex] > 128 && inputBuffer[inputIndex] < 145
						|| inputBuffer[inputIndex] == 8) {
					// unicode_to_zascii doesn't handle cursor and function keys. 
					// it does handle backspace by transforming it into delete, but we
					// don't want that transformation.
					ret = (byte) inputBuffer[inputIndex];
				}
				else {
					ret = (byte) ZScreen.unicode_to_zascii(inputBuffer[inputIndex]);
				}
			}
			catch (NoSuchKeyException e) {
				e.printStackTrace();
			}
			inputIndex++;
			return ret;
		}
		else {
			fatal("Reading past the keyboard input buffer!!!");
			return 13;
		}
	}

	public void print_ascii_char(short ch) {
		int nchars;
		if (status_redirect) {
			status_location += (char) ch;
		}
		else if (outputs[3]) {
			nchars = ((memory_image[printmemory] << 8) & 0xFF00)
					| (((int) (memory_image[printmemory + 1])) & 0xFF);
			if (ch > 255)
				memory_image[printmemory + nchars + 2] = (byte) '?';
			else if (ch == 10)
				memory_image[printmemory + nchars + 2] = (byte) 13;
			else
				memory_image[printmemory + nchars + 2] = (byte) ch;
			nchars++;
			memory_image[printmemory] = (byte) (nchars >>> 8);
			memory_image[printmemory + 1] = (byte) (nchars & 0xFF);
		}
		else {
			if (outputs[1]) {
				if ((ch == 13) || (ch == 10)) {
					current_window.newline();
				}
				else
					current_window.printzascii(ch);
			}
			outputs[2] = header.transcripting();
		}
	}

	public abstract int string_address(short addr);

	public abstract int routine_address(short addr);

	public short[] encode_word(int wordloc, int wordlen, int nwords) {
		short encword[] = new short[nwords];
		int zchars[] = new int[nwords * 3];
		int i;
		int zi;
		int ch;
		int a2index;

		zi = 0;
		for (i = 0; i < wordlen; i++) {
			ch = (int) memory_image[wordloc + i];
			if ((ch >= (int) 'a') && (ch <= (int) 'z')) {
				zchars[zi] = ch - (int) 'a' + 6;
				if ((++zi) == (nwords * 3))
					break;
			}
			else if ((ch >= (int) 'A') && (ch <= (int) 'Z')) {
				/* encode upper as lower. Legal? */
				System.err.println("Tried to encode uppercase dictionary word");
				zchars[zi] = ch - (int) 'A' + 6;
				if ((++zi) == (nwords * 3))
					break;
			}
			else if ((a2index = A2.indexOf(ch)) != -1) {
				/* From A2 */
				zchars[zi] = 5;
				if ((++zi) == (nwords * 3))
					break;
				zchars[zi] = a2index + 8;
				if ((++zi) == (nwords * 3))
					break;
			}
			else { /* gotta do ascii */
				zchars[zi] = 5;
				if ((++zi) == (nwords * 3))
					break;
				zchars[zi] = 6;
				if ((++zi) == (nwords * 3))
					break;
				zchars[zi] = ch >> 5;
				if ((++zi) == (nwords * 3))
					break;
				zchars[zi] = ch & 0x1F;
				if ((++zi) == (nwords * 3))
					break;
			}
		}
		while (zi < (nwords * 3)) {
			zchars[zi++] = 5;
		}
		zi = 0;
		for (i = 0; i < nwords; i++) {
			encword[i] = (short) (zchars[zi++] << 10);
			encword[i] |= (short) (zchars[zi++] << 5);
			encword[i] |= (short) (zchars[zi++]);
		}
		encword[nwords - 1] |= (short) 0x8000;
		return encword;
	}

	protected short alphabet_lookup(byte zchar) { /* JL: zchar to zscii (zcode) ? */
		int alphabet_table_address;
		short zscii;

		alphabet_table_address = (((int)memory_image[0x34]<<8)&0xFF00) | (((int)memory_image[0x34+1]) & 0x00FF);
		if (alphabet_table_address == 0) { // 0x34 is h_alphabet
				switch (alphabet) {
				case 0:
					return (short)((short)'a' + zchar - 6);
				case 1:
					return (short)((short)'A' + zchar - 6);
				case 2:
					if (zchar == 7) 
								return 13;
					else {
								return (short)(A2.charAt(zchar-8));
					}
				}
		} else { /* JL: game uses its own alphabet */
			if ((alphabet >= 0) && (alphabet <=2)) {
				zscii = (short) memory_image[alphabet_table_address + alphabet * 26 + zchar -6];
				if (zscii == '^') return 13;
				if (zscii >= 0) return zscii;
				else return (short) (zscii +  256); // because of a bug in Inform (or somewhere else)
			}
		}
		fatal("Bad Alphabet");
		return -1;
	}

	void print_abbrev(int abbr_num) {
		int abbrev_index;
		int string_addr;

		abbrev_mode = -1;
		abbrev_index = header.abbrev_table() + 2 * abbr_num;
		string_addr = (((memory_image[abbrev_index] << 8) & 0xFF00) | (((int) memory_image[abbrev_index + 1]) & 0xFF)) * 2;
		print_string(string_addr);
	}

	public void print_zchar(byte zchar) {
		if (build_ascii > 0) {
			// System.err.print("building ascii stage ");
			// System.err.println(build_ascii);
			built_ascii = (short) ((built_ascii << 5) | zchar);
			build_ascii++;
			if (build_ascii == 3) {
				// System.err.println("built ascii: " + built_ascii);
				print_ascii_char(built_ascii);
				build_ascii = 0;
				built_ascii = 0;
			}
			alphabet = 0;
		}
		else if (abbrev_mode > 0) {
			print_abbrev(32 * (abbrev_mode - 1) + zchar);
			abbrev_mode = 0;
			build_ascii = 0;
			alphabet = 0;
		}
		else {
			switch (zchar) {
				case 0:
					print_ascii_char((short) ' ');
					break;
				case 1:
				case 2:
				case 3:
					if (abbrev_mode != 0)
						fatal("Abbreviation in abbreviation");
					abbrev_mode = zchar;
					alphabet = 0;
					break;
				case 4:
					alphabet = (alphabet + 1) % 3;
					break;
				case 5:
					alphabet = (alphabet + 2) % 3;
					break;
				case 6:
					if (alphabet == 2) {
						build_ascii = 1;
						alphabet = 0;
						break;
					}
				default:
					print_ascii_char(alphabet_lookup(zchar));
					alphabet = 0;
					break;
			}
		}
	}

	public int print_string(int addr) {
		/* returns # bytes processed. ADDR is a byte address (hence an int) */
		int nbytes;
		byte zchars[];
		int zseq;
		int i;

		nbytes = 0;
		build_ascii = 0;
		alphabet = 0;
		abbrev_mode = 0;
		zchars = new byte[3];
		do {
			zseq = ((memory_image[addr++] << 8) & 0xFF00)
					| (((int) memory_image[addr++]) & 0xFF);
			zchars[0] = (byte) ((zseq >> 10) & 0x1F);
			zchars[1] = (byte) ((zseq >> 5) & 0x1F);
			zchars[2] = (byte) (zseq & 0x001F);
			for (i = 0; i < 3; i++)
				print_zchar(zchars[i]);
			nbytes += 2;
		}
		while ((zseq & 0x8000) == 0);
		return nbytes;
	}

	public int getRunState() {
		return runState;
	}

	/**
	 * Execute the Z-Program till it requests user input. Note: restart() must be
	 * called before this method is called the first time.
	 * 
	 * @return the reason (runstate) why execution stopped.
	 */
	public int run() {
		try {
			if (runState != STATE_RUNNING && runState != STATE_INIT) {
				// Picking up from where we left last in the assumption that the user
				// has provided the requested information.
				runState = STATE_RUNNING;
				zi.execute();
			}
			while (true) {
				zi.decode_instruction();
				switch (zi.opnum) {
				// case ZInstruction5.OP_AREAD: // Its the same!
					case ZInstruction.OP_SREAD: {
						runState = STATE_WAIT_CMD;
						return runState;
					}
					case ZInstruction5.OP_READ_CHAR: {
						runState = STATE_WAIT_CHAR;
						return runState;
					}
					default: {
						runState = STATE_RUNNING;
						zi.execute();
					}
				}
			}
		}
		catch (ArrayIndexOutOfBoundsException booga) {
			System.err.print("pc = ");
			System.err.println(Integer.toString(pc, 16));
			fatal("Memory address corruption");
		}
		catch (ClassCastException booga) {
			System.err.print("pc = ");
			System.err.println(Integer.toString(pc, 16));
			fatal("Story corruption");
		}

		return STATE_RUNNING; // We actually never get here
	}

	void calculate_checksum() {
		int filesize = header.file_length();
		int i;

		checksum = 0;
		if (filesize <= memory_image.length) {
			for (i = 0x40; i < filesize; i++) {
				checksum += memory_image[i] & 0xFF;
			}
		}
	}

	public void restart() {
		runState = STATE_INIT;
		restart_state.header.set_transcripting(header.transcripting());
		restart_state.restore_saved();
		set_header_flags();
		pc = header.initial_pc();
		calculate_checksum();
	}

	public void restore(ZState zs) {
		zs.header.set_transcripting(header.transcripting());
		restart();
		zs.restore_saved();
		// NOTE: the z-machine standard only specifies saving/restoring via
		// a command. This implies that a commandline was visible when the
		// player typed the "save" command and therefore we can hard hardcode this
		// here for the sake of simplicity under the condition that saving (from
		// a menu) is *disabled* while the interpreter is in char input mode.
		runState = STATE_WAIT_CMD;
	}

	public void set_header_flags() { /* at start, restart, restore */
		header.set_revision(0, 2);
	}

	public void fatal(String s) {
		throw new GrueException(s);
	}

	public short get_variable(short varnum) {
		short result;

		varnum &= 0xFF;
		if (varnum == 0) { /* stack */
			try {
				result = (short) (((Integer) zstack.pop()).intValue() & 0xFFFF);
			}
			catch (EmptyStackException booga) {
				fatal("Empty Stack");
				result = -1; /* not reached */
			}
		}
		else if (varnum >= 0x10) { /* globals */
			result = (short) (((memory_image[globals + ((varnum - 0x10) << 1)] << 8) & 0xFF00) | (memory_image[globals
					+ ((varnum - 0x10) << 1) + 1] & 0xFF));
			// if (varnum == 0xa1)
			// System.err.println("Got global # " + Integer.toString(varnum-0x10, 16)
			// +
			// " = " + Integer.toString(result, 16));
		}
		else { /* locals */
			result = locals[varnum - 1];
		}
		return result;
	}

	public void set_variable(short varnum, short value) {

		varnum &= 0xFF;
		if (varnum == 0) { /* stack */
			zstack.push(new Integer(value));
		}
		else if (varnum >= 0x10) { /* globals */
			memory_image[globals + ((varnum - 0x10) << 1)] = (byte) (value >>> 8);
			memory_image[globals + ((varnum - 0x10) << 1) + 1] = (byte) (value & 0xFF);
		}
		else { /* locals */
			locals[varnum - 1] = value;
		}
	}

	public byte get_code_byte() {
		return memory_image[pc++];
	}

	public short get_operand(int optype) {
		short operand;
		switch (optype) {
			case OP_SMALL:
				return (short) (get_code_byte() & 0xFF);
			case OP_LARGE:
				return (short) (((get_code_byte() << 8) & 0xFF00) | (get_code_byte() & 0xFF));
			case OP_VARIABLE:
				return get_variable(get_code_byte());
		}
		/* crash */
		return -1;
	}

	/**
	 * Query the filename of the quicksave slot (the one used by the save/restore
	 * opcode)
	 * @return absolute path name
	 */
	public File getQuickSaveSlot() {
		return quickSaveSlot;
	}
	
	public void setQuickSaveSlot(File file) {
		this.quickSaveSlot=file;
	}

}
