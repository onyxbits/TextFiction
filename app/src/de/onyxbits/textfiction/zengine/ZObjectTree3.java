/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

class ZObjectTree3 extends ZObjectTree {
	public ZObjectTree3(ZMachine zm) {
		super(zm);
	}

	protected int ptableoffset() {
		return 7;
	}

	protected int getentryloc(short object) {
		return object_tree + (((object & 0xFFFF) - 1) * 9);
	}

	protected int num_properties() {
		return 31;
	}

	public short parent(short object) {
		int entryloc;

		entryloc = getentryloc(object);
		return (short) (zm.memory_image[entryloc + 4] & 0xFF);
	}

	public short sibling(short object) {
		int entryloc;

		entryloc = getentryloc(object);
		return (short) (zm.memory_image[entryloc + 5] & 0xFF);
	}

	public short child(short object) {
		int entryloc;

		entryloc = getentryloc(object);
		return (short) (zm.memory_image[entryloc + 6] & 0xFF);
	}

	public void set_parent(short object, short newparent) {
		int entryloc;

		entryloc = getentryloc(object);
		zm.memory_image[entryloc + 4] = (byte) newparent;
	}

	public void set_sibling(short object, short newparent) {
		int entryloc;

		entryloc = getentryloc(object);
		zm.memory_image[entryloc + 5] = (byte) newparent;
	}

	public void set_child(short object, short newparent) {
		int entryloc;

		entryloc = getentryloc(object);
		zm.memory_image[entryloc + 6] = (byte) newparent;
	}

	public int prop_entry_address(short object, short propnum) {
		int entry_address;
		int sizebyte;

		// System.err.println(" prop_entry_address " + object + " " +
		// propnum);
		entry_address = property_table_addr(object);
		entry_address += (zm.memory_image[entry_address] & 0xFF) * 2 + 1;
		sizebyte = zm.memory_image[entry_address] & 0xFF;
		while (sizebyte != 0) {
			// System.err.println("propnum size addr " + (sizebyte&31) +
			// " " + (sizebyte >>5) + " " +
			// Integer.toString(entry_address,16));
			if ((sizebyte & 31) == propnum)
				return entry_address;
			else if ((sizebyte & 31) < propnum)
				return 0;
			entry_address += (sizebyte >> 5) + 2;
			sizebyte = zm.memory_image[entry_address] & 0xFF;
		}
		return 0;
	}

	public short next_prop(short object, short propnum) {
		int entry_address;
		int sizebyte;

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
			entry_address += ((zm.memory_image[entry_address] & 0xFF) >> 5) + 2;
		}

		sizebyte = zm.memory_image[entry_address] & 0xFF;
		return (short) (sizebyte & 31);
	}

	public short prop_address(short object, short propnum) {
		int entry_address = prop_entry_address(object, propnum);

		if (entry_address == 0)
			return 0;
		return (short) (entry_address + 1);
	}

	public short prop_len(short prop_address) {
		int sizebyte;

		if (prop_address == 0) {
			zm.fatal("Tried to find length of missing property");
			return (short) -1;
		}

		sizebyte = zm.memory_image[(prop_address & 0xFFFF) - 1] & 0xFF;
		return (short) ((sizebyte >> 5) + 1);
	}
}
