/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

public class ZObjectTree5 extends ZObjectTree {
	public ZObjectTree5(ZMachine zm) {
		super(zm);
	}

	protected int ptableoffset() {
		return 12;
	}

	protected int getentryloc(short object) {
		return object_tree + (((object & 0xFFFF) - 1) * 14);
	}

	protected int num_properties() {
		return 63;
	}

	public short parent(short object) {
		int entryloc;

		entryloc = getentryloc(object);
		return (short) ((zm.memory_image[entryloc + 6] << 8) | (zm.memory_image[entryloc + 7] & 0xFF));
	}

	public short sibling(short object) {
		int entryloc;

		entryloc = getentryloc(object);
		return (short) ((zm.memory_image[entryloc + 8] << 8) | (zm.memory_image[entryloc + 9] & 0xFF));
	}

	public short child(short object) {
		int entryloc;

		entryloc = getentryloc(object);
		return (short) ((zm.memory_image[entryloc + 10] << 8) | (zm.memory_image[entryloc + 11] & 0xFF));
	}

	public void set_parent(short object, short newparent) {
		int entryloc;

		// System.err.println("set_parent " + object + " " + newparent);
		entryloc = getentryloc(object);
		zm.memory_image[entryloc + 6] = (byte) ((newparent >> 8) & 0xFF);
		zm.memory_image[entryloc + 7] = (byte) (newparent & 0xFF);
	}

	public void set_sibling(short object, short newparent) {
		int entryloc;

		// System.err.println("set_sibling " + object + " " + newparent);
		entryloc = getentryloc(object);
		zm.memory_image[entryloc + 8] = (byte) ((newparent >> 8) & 0xFF);
		zm.memory_image[entryloc + 9] = (byte) (newparent & 0xFF);
	}

	public void set_child(short object, short newparent) {
		int entryloc;

		// System.err.println("set_child " + object + " " + newparent);
		entryloc = getentryloc(object);
		zm.memory_image[entryloc + 10] = (byte) ((newparent >> 8) & 0xFF);
		zm.memory_image[entryloc + 11] = (byte) (newparent & 0xFF);
	}

	public int prop_entry_address(short object, short propnum) {
		int entry_address;
		int sizebyte;
		int curpropnum, length; /* length includes size byte(s) */

		// System.err.println(" prop_entry_address " + object + " " +
		// propnum);
		entry_address = property_table_addr(object);
		entry_address += (zm.memory_image[entry_address] & 0xFF) * 2 + 1;
		sizebyte = zm.memory_image[entry_address] & 0xFF;
		while (sizebyte != 0) {
			curpropnum = sizebyte & 0x3F;
			if ((sizebyte & 0x80) == 0x80) {
				length = (zm.memory_image[entry_address + 1] & 0x3F) + 2;
			}
			else
				length = (sizebyte >> 6) + 2;
			// System.err.println("propnum length addr " + curpropnum +
			// " " + length + " " +
			// Integer.toString(entry_address,16));
			if (curpropnum == propnum)
				return entry_address;
			else if (curpropnum < propnum)
				return 0;
			entry_address += length;
			sizebyte = zm.memory_image[entry_address] & 0xFF;
		}
		return 0;
	}

	public short next_prop(short object, short propnum) {
		int entry_address;
		int sizebyte;
		int length;

		if (propnum == 0) {
			entry_address = property_table_addr(object);
			if (entry_address == 0)
				zm.fatal("Tried to get next property for object with no properties");
			entry_address += (zm.memory_image[entry_address] & 0xFF) * 2 + 1;

		}
		else {
			entry_address = prop_entry_address(object, propnum);
			if (entry_address == 0)
				zm.fatal("Tried to get next property for nonexistent property");
			sizebyte = zm.memory_image[entry_address] & 0xFF;
			if ((sizebyte & 0x80) == 0x80) {
				length = (zm.memory_image[entry_address + 1] & 0x3F) + 2;
			}
			else {
				length = (sizebyte >> 6) + 2;
			}
			entry_address += length;
		}

		sizebyte = zm.memory_image[entry_address] & 0xFF;
		return (short) (sizebyte & 0x3F);
	}

	public short prop_address(short object, short propnum) {
		int entry_address = prop_entry_address(object, propnum);

		if (entry_address == 0)
			return 0;
		if ((zm.memory_image[entry_address] & 0x80) == 0x80)
			return (short) (entry_address + 2);
		else
			return (short) (entry_address + 1);
	}

	public short prop_len(short prop_address) {
		int sizebyte;

		if (prop_address == 0) {
			zm.fatal("Tried to find length of missing property");
			return (short) -1;
		}

		sizebyte = zm.memory_image[(prop_address & 0xFFFF) - 1] & 0xFF;
		if ((sizebyte & 0x80) == 0x80)
			return (short) (sizebyte & 0x3F);
		else
			return (short) ((sizebyte >> 6) + 1);
	}
}
