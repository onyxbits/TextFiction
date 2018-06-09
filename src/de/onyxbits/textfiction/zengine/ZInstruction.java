/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

public class ZInstruction {
	protected final short ZFALSE = (short) 0;
	protected final short ZTRUE = (short) 1;
	protected final short ZNOTDONE = (short) 0;
	protected final short ZSAVE_SUCCESS = (short) 1;
	protected final short ZRESTORE_SUCCESS = (short) 2;
	protected final static int OP_JE = 1;
	protected final static int OP_JL = 2;
	protected final static int OP_JG = 3;
	protected final static int OP_DEC_CHK = 4;
	protected final static int OP_INC_CHK = 5;
	protected final static int OP_JIN = 6;
	protected final static int OP_TEST = 7;
	protected final static int OP_OR = 8;
	protected final static int OP_AND = 9;
	protected final static int OP_TEST_ATTR = 10;
	protected final static int OP_SET_ATTR = 11;
	protected final static int OP_CLEAR_ATTR = 12;
	protected final static int OP_STORE = 13;
	protected final static int OP_INSERT_OBJ = 14;
	protected final static int OP_LOADW = 15;
	protected final static int OP_LOADB = 16;
	protected final static int OP_GET_PROP = 17;
	protected final static int OP_GET_PROP_ADDR = 18;
	protected final static int OP_GET_NEXT_PROP = 19;
	protected final static int OP_ADD = 20;
	protected final static int OP_SUB = 21;
	protected final static int OP_MUL = 22;
	protected final static int OP_DIV = 23;
	protected final static int OP_MOD = 24;
	protected final static int OP_JZ = 128;
	protected final static int OP_GET_SIBLING = 129;
	protected final static int OP_GET_CHILD = 130;
	protected final static int OP_GET_PARENT = 131;
	protected final static int OP_GET_PROP_LEN = 132;
	protected final static int OP_INC = 133;
	protected final static int OP_DEC = 134;
	protected final static int OP_PRINT_ADDR = 135;
	public final static int OP_CALL_1S = 136;
	protected final static int OP_REMOVE_OBJ = 137;
	protected final static int OP_PRINT_OBJ = 138;
	protected final static int OP_RET = 139;
	protected final static int OP_JUMP = 140;
	protected final static int OP_PRINT_PADDR = 141;
	protected final static int OP_LOAD = 142;
	protected final static int OP_NOT = 143;
	protected final static int OP_RTRUE = 176;
	protected final static int OP_RFALSE = 177;
	protected final static int OP_PRINT = 178;
	protected final static int OP_PRINT_RET = 179;
	protected final static int OP_NOP = 180;
	protected final static int OP_SAVE = 181;
	protected final static int OP_RESTORE = 182;
	protected final static int OP_RESTART = 183;
	protected final static int OP_RET_POPPED = 184;
	protected final static int OP_POP = 185;
	protected final static int OP_QUIT = 186;
	protected final static int OP_NEW_LINE = 187;
	protected final static int OP_SHOW_STATUS = 188;
	protected final static int OP_VERIFY = 189;
	protected final static int OP_EXTENDED = 190;
	protected final static int OP_PIRACY = 191;
	protected final static int OP_CALL = 224;
	protected final static int OP_STOREW = 225;
	protected final static int OP_STOREB = 226;
	protected final static int OP_PUT_PROP = 227;
	protected final static int OP_SREAD = 228;
	protected final static int OP_PRINT_CHAR = 229;
	protected final static int OP_PRINT_NUM = 230;
	protected final static int OP_RANDOM = 231;
	protected final static int OP_PUSH = 232;
	protected final static int OP_PULL = 233;
	protected final static int OP_SPLIT_WINDOW = 234;
	protected final static int OP_SET_WINDOW = 235;
	protected final static int OP_CALL_VS2 = 236;
	protected final static int OP_OUTPUT_STREAM = 243;
	protected final static int OP_INPUT_STREAM = 244;
	protected final static int OP_SOUND_EFFECT = 245;
	protected final static int OP_CALL_VN2 = 250;

	protected final static int LOWER_WINDOW = 0;
	protected final static int UPPER_WINDOW = 1;

	protected int opnum;
	protected int count;
	protected int save_pc;
	protected short operands[];
	protected short storevar;
	protected short branchoffset;
	protected boolean branchtype;
	protected ZMachine zm;
	static boolean[] store = null;
	static boolean[] branch = null;

	protected ZInstruction() {
	}

	public ZInstruction(ZMachine zm) {
		this.zm = zm;
		if (store == null) {
			store = new boolean[256];
			branch = new boolean[256];
			setupbs();
		}
		operands = new short[4];
	}

	public ZInstruction(int opnum, int count, short[] operands, short storevar,
			short branchoffset, boolean branchtype) {
		this.opnum = opnum;
		this.count = count;
		this.operands = operands;
		this.storevar = storevar;
		this.branchoffset = branchoffset;
		this.branchtype = branchtype;
	}

	public ZInstruction(int opnum, int count, short[] operands) {
		this(opnum, count, operands, (short) 0, (short) 0, false);
	}

	protected boolean isbranch() {
		return branch[opnum];
	}

	protected boolean isstore() {
		return store[opnum];
	}

	protected boolean isret() {
		switch (opnum) {
			case OP_RET:
			case OP_RFALSE:
			case OP_RTRUE:
			case OP_RET_POPPED:
			case OP_PRINT_RET:
				return true;
			default:
				return false;
		}
	}

	public void decode_instruction() {
		decode_first_half();
		save_pc = zm.pc; /* A kludge to support a kludge */
		decode_second_half();
	}

	protected void decode_first_half() {
		int opcode;
		int optype, optypes;
		int optypes2 = 0; /* It takes a kludge to handle a kludge */
		int optypebytes;
		int isextended = 0;

		opcode = zm.get_code_byte() & 0xFF;
		if (opcode == OP_EXTENDED) {
			isextended = 0xC0;
		}

		switch (isextended | opcode & 0xC0) {
			case 0xC0: /* variable form */
				if (isextended != 0xC0) {
					opnum = opcode;
				}
				else {
					opnum = (0x100 | (zm.get_code_byte() & 0xFF));
				}

				if ((opcode & 0x20) == 0) { /* 2OP */
					/*
					 * note that variable-form je can have more than two arguments, though
					 * it is a 2OP
					 */
					opnum = opcode & 0x1F;
				}
				count = 0;

				optypes = zm.get_code_byte();
				if ((opnum == OP_CALL_VS2) || (opnum == OP_CALL_VN2)) {
					optypebytes = 2;
					optypes2 = zm.get_code_byte();
				}
				else
					optypebytes = 1;

				while (optypebytes-- != 0) {
					optype = (optypes & 0xC0) >> 6;
					if (optype == zm.OP_OMITTED)
						break;
					operands[count++] = zm.get_operand(optype);
					optype = (optypes & 0x30) >> 4;
					if (optype == zm.OP_OMITTED)
						break;
					operands[count++] = zm.get_operand(optype);
					optype = (optypes & 0x0C) >> 2;
					if (optype == zm.OP_OMITTED)
						break;
					operands[count++] = zm.get_operand(optype);
					optype = (optypes & 0x03);
					if (optype == zm.OP_OMITTED)
						break;
					operands[count++] = zm.get_operand(optype);
					optypes = optypes2;
				}
				break;

			case 0x80: /* short form */
				optype = (opcode & 0x30) >> 4;
				if (optype == zm.OP_OMITTED) { /* 0OP */
					opnum = opcode;
					count = 0;
				}
				else { /* 1OP */
					opnum = opcode & 0x8F;
					count = 1;
					operands[0] = zm.get_operand(optype);
				}
				break;

			default: /* long form */
				/* always 2OP */
				opnum = opcode & 0x1F;
				count = 2;

				optype = ((opcode & 0x40) >> 6) + 1;
				operands[0] = zm.get_operand(optype);
				optype = ((opcode & 0x20) >> 5) + 1;
				operands[1] = zm.get_operand(optype);
		}
	}

	protected void decode_second_half() {
		if (isstore())
			storevar = zm.get_code_byte();
		if (isbranch()) {
			branchoffset = zm.get_code_byte();
			branchtype = (branchoffset & 0x80) != 0;
			if ((branchoffset & 0x40) != 0) /* positive 6-bit number */
				branchoffset &= 0x3F;
			else if ((branchoffset & 0x20) != 0) /* negative 14-bit number */
				branchoffset = (short) (0xC000 | ((branchoffset << 8) | (((short) zm
						.get_code_byte()) & 0xFF)));
			else
				/* positive 14-bit number */
				branchoffset = (short) (((branchoffset & 0x3F) << 8) | (((short) zm
						.get_code_byte()) & 0xFF));
		}
	}

	public void execute() {
		short result;

		// System.err.println("Executing instruction " + opnum);
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
			case OP_NOT:
				result = op_not();
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
			case OP_SAVE:
				result = op_save();
				break;
			case OP_RESTORE:
				result = op_restore();
				break;
			case OP_RESTART:
				result = op_restart();
				break;
			case OP_RET_POPPED:
				result = op_ret_popped();
				break;
			case OP_POP:
				result = op_pop();
				break;
			case OP_QUIT:
				result = op_quit();
				break;
			case OP_NEW_LINE:
				result = op_new_line();
				break;
			case OP_SHOW_STATUS:
				result = op_show_status();
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
			case OP_CALL:
				result = op_call();
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
			case OP_SREAD:
				result = op_sread();
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
			case OP_OUTPUT_STREAM:
				result = op_output_stream();
				break;
			case OP_INPUT_STREAM:
				result = op_input_stream();
				break;
			case OP_SOUND_EFFECT:
				result = op_sound_effect();
				break;
			default:
				result = op_illegal();
		}

		if (((opnum != OP_CALL) && isstore()) || isret()) {
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
						zm.set_variable(storevar, ZFALSE);
						break;
					case 1:
						z_ret();
						zm.set_variable(storevar, ZTRUE);
						break;
					default:
						zm.pc += branchoffset - 2;
				}
			}
		}
	}

	protected short op_illegal() {
		zm.fatal("Unknown opcode: " + opnum);
		return ZFALSE;
	}

	protected short op_je() {
		int i;
		for (i = 1; i < count; i++) {
			if (operands[0] == operands[i])
				return ZTRUE;
		}
		return ZFALSE;
	}

	protected short op_jl() {
		if (operands[0] < operands[1])
			return ZTRUE;
		return 0;
	}

	protected short op_jg() {
		if (operands[0] > operands[1])
			return ZTRUE;
		return ZFALSE;
	}

	protected short op_dec_chk() {
		short vval;

		vval = zm.get_variable(operands[0]);
		vval--;
		zm.set_variable(operands[0], vval);
		if (vval < operands[1])
			return ZTRUE;
		return ZFALSE;
	}

	protected short op_inc_chk() {
		short vval;

		vval = zm.get_variable(operands[0]);
		vval++;
		zm.set_variable(operands[0], vval);
		if (vval > operands[1])
			return ZTRUE;
		return ZFALSE;
	}

	protected short op_jin() {
		if (zm.objects.parent(operands[0]) == operands[1])
			return ZTRUE;
		else
			return ZFALSE;
	}

	protected short op_test() {
		if ((operands[0] & operands[1]) == operands[1])
			return ZTRUE;
		return ZFALSE;
	}

	protected short op_or() {
		return (short) ((operands[0] | operands[1]) & 0xFFFF);
	}

	protected short op_and() {
		return (short) ((operands[0] & operands[1]) & 0xFFFF);
	}

	protected short op_test_attr() {
		if (zm.objects.attribute(operands[0], operands[1]))
			return ZTRUE;
		return ZFALSE;
	}

	protected short op_set_attr() {
		zm.objects.set_attribute(operands[0], operands[1]);
		return ZFALSE;
	}

	protected short op_clear_attr() {
		zm.objects.clear_attribute(operands[0], operands[1]);
		return ZFALSE;
	}

	protected short op_store() {
		zm.set_variable(operands[0], operands[1]);
		return ZFALSE;
	}

	protected short op_insert_obj() {
		short dchild;

		// System.err.println("insert_object " + operands[0] + " " + operands[1]);
		// zm.print_ascii_string("   @insert_obj ");
		// zm.print_string(zm.objects.short_name_addr(operands[0]));
		// zm.print_ascii_string(" ");
		// zm.print_string(zm.objects.short_name_addr(operands[1]));
		// zm.print_ascii_string("\n");
		detach_obj(operands[0]);
		dchild = zm.objects.child(operands[1]);
		zm.objects.set_child(operands[1], operands[0]);
		zm.objects.set_sibling(operands[0], dchild);
		zm.objects.set_parent(operands[0], operands[1]);
		return ZFALSE;
	}

	protected short op_loadw() {
		int byte_index = (operands[0] & 0xFFFF) + ((operands[1] & 0xFFFF) * 2);
		short result;

		// System.err.print("loadw " + Integer.toString(zm.pc, 16) + " ");
		// System.err.print(Integer.toString(operands[0]&0xFFFF, 16) + " " +
		// Integer.toString(operands[1], 16) + " ");
		result = (short) (((zm.memory_image[byte_index] << 8) & 0xFF00) | (zm.memory_image[byte_index + 1] & 0xFF));
		// System.err.println(Integer.toString(result&0xFFFF, 16));
		return result;
	}

	protected short op_loadb() {
		int byte_index = (operands[0] & 0xFFFF) + (operands[1] & 0xFFFF);
		return (short) (zm.memory_image[byte_index] & 0xFF);
	}

	protected short op_get_prop() {
		return zm.objects.prop(operands[0], operands[1]);
	}

	protected short op_get_prop_addr() {
		return zm.objects.prop_address(operands[0], operands[1]);
	}

	protected short op_get_next_prop() {
		return zm.objects.next_prop(operands[0], operands[1]);
	}

	protected short op_add() {
		return (short) (operands[0] + operands[1]);
	}

	protected short op_sub() {
		return (short) (operands[0] - operands[1]);
	}

	protected short op_mul() {
		return (short) (operands[0] * operands[1]);
	}

	protected short op_div() {
		int dividend = operands[0];
		int divisor = operands[1];

		if (divisor == 0) {
			zm.fatal("Remainder from division by zero");
		}
		return (short) (dividend / divisor);
	}

	protected short op_mod() {
		int dividend = operands[0];
		int divisor = operands[1];

		if (divisor == 0) {
			zm.fatal("Remainder from division by zero");
		}
		return (short) (dividend % divisor);
	}

	protected short op_jz() {
		if (operands[0] == 0)
			return ZTRUE;
		return ZFALSE;
	}

	protected short op_get_sibling() {
		return zm.objects.sibling(operands[0]);
	}

	protected short op_get_child() {
		return zm.objects.child(operands[0]);
	}

	protected short op_get_parent() {
		return zm.objects.parent(operands[0]);
	}

	protected short op_get_prop_len() {
		return zm.objects.prop_len(operands[0]);
	}

	protected short op_inc() {
		short vval;
		vval = zm.get_variable(operands[0]);
		vval++;
		zm.set_variable(operands[0], vval);
		return ZFALSE;
	}

	protected short op_dec() {
		short vval;
		vval = zm.get_variable(operands[0]);
		vval--;
		zm.set_variable(operands[0], vval);
		return ZFALSE;
	}

	protected short op_print_addr() {
		zm.print_string((int) operands[0] & 0xFFFF);
		return ZFALSE;
	}

	protected short op_call_1s() {
		/* TODO */
		/* V4+ */
		return ZNOTDONE;
	}

	void detach_obj(short object) {
		/* note: does not leave object tree well-formed */
		short parent;
		short cursor;

		// System.err.println("detach_obj " + object);
		parent = zm.objects.parent(object);
		if (parent != 0) {
			cursor = zm.objects.child(parent);
			if (cursor == object) {
				zm.objects.set_child(parent, zm.objects.sibling(object));
			}
			else {
				while (zm.objects.sibling(cursor) != object) {
					cursor = zm.objects.sibling(cursor);
					if (cursor == 0)
						zm.fatal("Malformed object tree");
				}
				zm.objects.set_sibling(cursor, zm.objects.sibling(object));
			}
		}
	}

	protected short op_remove_obj() {
		// System.err.println("remove_object " + operands[0]);

		// zm.print_ascii_string("   @remove_obj ");
		// zm.print_string(zm.objects.short_name_addr(operands[0]));
		// zm.print_ascii_string("\n");
		detach_obj(operands[0]);
		zm.objects.set_parent(operands[0], (short) 0);
		zm.objects.set_sibling(operands[0], (short) 0);
		return ZFALSE;
	}

	protected short op_print_obj() {
		zm.print_string(zm.objects.short_name_addr(operands[0]));
		return ZTRUE;
	}

	protected void z_ret() {
		Object tos;

		do
			tos = zm.zstack.pop();
		while (!(tos instanceof short[]));
		zm.locals = (short[]) tos;
		// System.err.print("From ");
		// System.err.print(Integer.toString(zm.pc, 16));
		zm.pc = ((Integer) zm.zstack.pop()).intValue();
		// System.err.print(" returning to ");
		// System.err.println(Integer.toString(zm.pc, 16));
		storevar = (short) (((Integer) zm.zstack.pop()).intValue());
		zm.zstack.pop(); /* stack frame boundary */
	}

	protected short op_ret() {
		z_ret();
		return operands[0];
	}

	protected short op_jump() {
		zm.pc += operands[0] - 2;
		return ZFALSE;
	}

	protected short op_print_paddr() {
		zm.print_string(zm.string_address(operands[0]));
		return ZFALSE;
	}

	protected short op_load() {
		return zm.get_variable(operands[0]);
	}

	protected short op_not() {
		return (short) (~operands[0]);
	}

	protected short op_rtrue() {
		z_ret();
		return ZTRUE;
	}

	protected short op_rfalse() {
		z_ret();
		return ZFALSE;
	}

	protected short op_print() {
		int nchars;
		nchars = zm.print_string(zm.pc);
		zm.pc += nchars;
		return ZFALSE;
	}

	protected short op_print_ret() {
		zm.print_string(zm.pc);
		zm.print_ascii_char((short) 13);
		z_ret();
		return ZTRUE;
	}

	protected short op_nop() {
		return ZFALSE;
	}

	protected short op_save() {
		/*
		if (zm.getQuickSaveSlot()==null) {
			zm.fatal("Don't know where to save to!");
		}
		if ((new ZState(zm)).disk_save(zm.getQuickSaveSlot().getPath(), save_pc))
			return ZSAVE_SUCCESS;*/
		zm.saveCalled=true;
		return ZFALSE;
	}

	protected short op_restore() {
		/*
		ZState restore_state;
		if (zm.getQuickSaveSlot()==null) {
			zm.fatal("Don't know where to restore from!");
		}

		restore_state = new ZState(zm);
		if (restore_state.restore_from_disk(zm.getQuickSaveSlot().getPath())) {
			zm.restore(restore_state);
			decode_second_half();
			return ZRESTORE_SUCCESS;
		}*/
		zm.restoreCalled=true;
		return ZFALSE;

	}

	protected short op_restart() {
		zm.restart();
		return ZFALSE;
	}

	protected short op_ret_popped() {
		short returnvalue;

		returnvalue = zm.get_variable((short) 0);
		z_ret();
		return returnvalue;
	}

	protected short op_pop() {
		zm.get_variable((short) 0); /* pop the stack and toss the result */
		return ZFALSE;
	}

	protected short op_quit() {
		zm.print_ascii_char((short) '*');
		zm.print_ascii_char((short) '*');
		zm.print_ascii_char((short) '*');
		zm.print_ascii_char((short) 'E');
		zm.print_ascii_char((short) 'N');
		zm.print_ascii_char((short) 'D');
		zm.print_ascii_char((short) ' ');
		zm.print_ascii_char((short) 'O');
		zm.print_ascii_char((short) 'F');
		zm.print_ascii_char((short) ' ');
		zm.print_ascii_char((short) 'S');
		zm.print_ascii_char((short) 'E');
		zm.print_ascii_char((short) 'S');
		zm.print_ascii_char((short) 'S');
		zm.print_ascii_char((short) 'I');
		zm.print_ascii_char((short) 'O');
		zm.print_ascii_char((short) 'N');
		zm.print_ascii_char((short) '*');
		zm.print_ascii_char((short) '*');
		zm.print_ascii_char((short) '*');
		zm.print_ascii_char((short) 13);
		// zm.stop();
		return ZFALSE;
	}

	protected short op_new_line() {
		zm.print_ascii_char((short) 13);
		return ZFALSE;
	}

	protected short op_show_status() {
		zm.update_status_line();
		return ZFALSE;
	}

	protected short op_verify() {
		int filesize;

		filesize = zm.header.file_length();
		if ((filesize > zm.memory_image.length)
				|| (zm.header.checksum() != zm.checksum)) {
			System.err.println("VERIFY failed: ");
			System.err.println("\texpected\tactual");
			System.err.println("length\t" + filesize + "\t" + zm.memory_image.length);
			System.err.println("checksum\t"
					+ Integer.toString(zm.header.checksum() & 0xFFFF, 16) + "\t"
					+ Integer.toString(zm.checksum, 16));
			return ZFALSE;
		}
		return ZTRUE;
	}

	protected short op_extended() {
		/* TODO */
		/* V5+ only */
		return ZNOTDONE;
	}

	protected short op_piracy() {
		return ZFALSE; /* heh heh */
	}

	protected short op_call() {
		int nlocals;
		int i;
		short thislocal;

		if (operands[0] == 0) {
			/* calls to zero return false */
			/*
			 * Can't just return false because call is not treated as a store in
			 * execute_instruction
			 */
			zm.set_variable(storevar, ZFALSE);
		}
		else {
			zm.zstack.push(new ZFrameBound(isstore()));
			zm.zstack.push(new Integer(storevar));
			zm.zstack.push(new Integer(zm.pc));
			zm.zstack.push(zm.locals);
			// System.err.print("From ");
			// System.err.print(Integer.toString(zm.pc, 16));
			zm.pc = zm.routine_address(operands[0]);
			// System.err.print(" calling routine at ");
			// System.err.println(Integer.toString(zm.pc, 16));
			nlocals = zm.get_code_byte();
			zm.locals = new short[nlocals];
			for (i = 0; i < nlocals; i++) {
				thislocal = (short) (((zm.get_code_byte() << 8) & 0xFF00) | (zm
						.get_code_byte() & 0xFF));
				if (i < (count - 1)) {
					zm.locals[i] = operands[i + 1];
				}
				else {
					zm.locals[i] = thislocal;
				}
			}
		}
		return ZFALSE;
	}

	protected short op_storew() {
		int byte_index = (operands[0] & 0xFFFF) + ((operands[1] & 0xFFFF) * 2);

		// System.err.print("storew " + Integer.toString(zm.pc, 16) + " ");
		// System.err.print(Integer.toString(operands[0]&0xFFFF, 16) + " " +
		// Integer.toString(operands[1]&0xFFFF, 16) + " ");
		// System.err.println(Integer.toString(operands[2]&0xFFFF, 16));
		zm.memory_image[byte_index] = (byte) (operands[2] >>> 8);
		zm.memory_image[byte_index + 1] = (byte) (operands[2] & 0xFF);

		if (byte_index == ZHeader.FLAGS2 || byte_index == ZHeader.FLAGS2 + 1) {
			zm.current_window.set_text_style(zm.header.force_fixed() ? ZWindow.FIXED : ZWindow.ROMAN);
		}

		return ZFALSE;
	}

	protected short op_storeb() {
		int byte_index = (operands[0] & 0xFFFF) + (operands[1] & 0xFFFF);

		zm.memory_image[byte_index] = (byte) (operands[2] & 0xFF);

		if (byte_index == ZHeader.FLAGS2 + 1) {
			zm.current_window.set_text_style(zm.header.force_fixed() ? ZWindow.FIXED : ZWindow.ROMAN);
		}

		return ZFALSE;
	}

	protected short op_put_prop() {
		zm.objects.put_prop(operands[0], operands[1], operands[2]);
		return ZFALSE;
	}

	protected short op_sread() {
		byte ch;
		int tsize;
		int tbuf;
		int bufloc;

		tbuf = operands[0] & 0xFFFF;
		zm.update_status_line();
		zm.current_window.flush();
		zm.current_window.reset_line_count();
		// System.err.println("textbuf = " + Integer.toString(tbuf, 16));
		tsize = zm.memory_image[tbuf] & 0xFF;
		if (tsize < 2)
			zm.fatal("Text Buffer < 3 bytes");
		tsize--; /* reserve a byte for the terminator */
		bufloc = 1;
		while ((tsize != 0) && ((ch = zm.get_input_byte(true)) != 13) && (ch != 10)) {
			// System.err.println("sr_ch @ " + Integer.toString(tbuf+bufloc, 16) +
			// " : " + ch);
			if ((ch >= (byte) 'A') && (ch <= (byte) 'Z')) {
				ch = (byte) (ch - (byte) 'A' + (byte) 'a');
			}
			zm.memory_image[tbuf + bufloc] = ch;
			bufloc++;
			tsize--;
		}
		zm.memory_image[tbuf + bufloc] = 0;
		zm.zd.tokenise(tbuf + 1, bufloc - 1, operands[1] & 0xFFFF);
		return ZFALSE;
	}

	protected short op_print_char() {
		zm.print_ascii_char(operands[0]);
		return ZFALSE;
	}

	protected short op_print_num() {
		char[] mychars;
		int i;

		mychars = Integer.toString((int) operands[0], 10).toCharArray();
		for (i = 0; i < mychars.length; i++) {
			zm.print_ascii_char((short) mychars[i]);
		}
		return ZFALSE;
	}

	protected short op_random() {
		if (operands[0] == 0) {
			zm.zrandom.setSeed(System.currentTimeMillis()); /* back to random mode */
			return (short) 0;
		}
		else if (operands[0] < 0) {
			zm.zrandom.setSeed(-operands[0]);
			return (short) 0;
		}
		else {
			return (short) (((zm.zrandom.nextInt() & 0x7FFF) % operands[0]) + 1);
		}
	}

	protected short op_push() {
		zm.set_variable((short) 0, operands[0]);
		return ZFALSE;
	}

	protected short op_pull() {
		zm.set_variable(operands[0], zm.get_variable((short) 0));
		return ZFALSE;
	}

	protected void split_screen(int lines) {
		int cx, cy;

		zm.window[UPPER_WINDOW].flush();
		zm.window[LOWER_WINDOW].flush();
		// System.err.println("split screen " + lines);
		cx = zm.window[LOWER_WINDOW].getx();
		cy = zm.window[LOWER_WINDOW].gety() + zm.window[UPPER_WINDOW].getlines();
		cy -= lines;
		if (cy < 0)
			cy = 0;
		// System.err.println("cx cy" + cx + cy);
		zm.window[LOWER_WINDOW].moveto(0, lines);
		zm.window[UPPER_WINDOW].moveto(0, 0);
		zm.window[LOWER_WINDOW].resize(zm.screen.getchars(), zm.screen.getlines()
				- lines);
		zm.window[UPPER_WINDOW].resize(zm.screen.getchars(), lines);
		if (cy >= (zm.screen.getlines() - lines)) {
			cy = zm.screen.getlines() - lines - 1;
		}
		zm.window[LOWER_WINDOW].movecursor(cx, cy);
		// System.err.println("cx cy" + cx + cy);
	}

	protected short op_split_window() {
		split_screen(operands[0]);
		return ZFALSE;
	}

	protected short op_set_window() {
		zm.current_window.flush();
		// System.err.println("set_window " + operands[0]);
		zm.current_window = zm.window[operands[0]];
		if (operands[0] == UPPER_WINDOW)
			zm.current_window.movecursor(0, 0);
		return ZFALSE;
	}

	protected short op_output_stream() {
		zm.current_window.flush();
		if (operands[0] == 3) {
			zm.printmemory = operands[1] & 0xFFFF;
			zm.memory_image[zm.printmemory] = (byte) 0;
			zm.memory_image[zm.printmemory + 1] = (byte) 0;
		}
		else if (operands[0] == 2)
			zm.header.set_transcripting(true);
		else if (operands[0] == -2)
			zm.header.set_transcripting(false);

		if (operands[0] > 0)
			zm.outputs[operands[0]] = true;
		else
			zm.outputs[-operands[0]] = false;
		return ZFALSE;
	}

	protected short op_input_stream() {
		zm.inputstream = operands[0];
		return ZFALSE;
	}

	protected short op_sound_effect() {
		/* TODO */
		return ZNOTDONE;
	}

	protected void setupbs() {
		/* Sets up store and branch instructions */
		branch[OP_JE] = true;
		branch[OP_JL] = true;
		branch[OP_JG] = true;
		branch[OP_DEC_CHK] = true;
		branch[OP_INC_CHK] = true;
		branch[OP_JIN] = true;
		branch[OP_TEST] = true;
		store[OP_OR] = true;
		store[OP_AND] = true;
		branch[OP_TEST_ATTR] = true;
		store[OP_LOADW] = true;
		store[OP_LOADB] = true;
		store[OP_GET_PROP] = true;
		store[OP_GET_PROP_ADDR] = true;
		store[OP_GET_NEXT_PROP] = true;
		store[OP_ADD] = true;
		store[OP_SUB] = true;
		store[OP_MUL] = true;
		store[OP_DIV] = true;
		store[OP_MOD] = true;
		branch[OP_JZ] = true;
		branch[OP_GET_SIBLING] = true;
		store[OP_GET_SIBLING] = true;
		branch[OP_GET_CHILD] = true;
		store[OP_GET_CHILD] = true;
		store[OP_GET_PARENT] = true;
		store[OP_GET_PROP_LEN] = true;
		store[OP_LOAD] = true;
		store[OP_NOT] = true;
		branch[OP_SAVE] = true;
		branch[OP_RESTORE] = true;
		branch[OP_VERIFY] = true;
		branch[OP_PIRACY] = true;
		store[OP_CALL] = true;
		store[OP_RANDOM] = true;
	}
}
