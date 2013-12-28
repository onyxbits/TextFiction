/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

import de.onyxbits.textfiction.zengine.ZMachine;

public abstract class ZObjectTree {
	protected ZMachine zm;
	protected int object_table;
	protected int object_tree;

	public ZObjectTree(ZMachine zm) {
		this.zm = zm;
		object_table = zm.header.object_table();
		object_tree = object_table + num_properties() * 2;
		/* skip the prop_defaults */
	}

	public short default_property(short property) {
		short result;

		property--;
		result = (short) (((zm.memory_image[object_table + property * 2] << 8) & 0xFF00) | (zm.memory_image[object_table
				+ property * 2 + 1] & 0xFF));
		return result;
	}

	abstract protected int num_properties();

	abstract protected int ptableoffset();

	abstract protected int getentryloc(short objectnum);

	public boolean attribute(short object, short attr_num) {
		int bytenum;
		int bitmask;
		int entryloc;

		// System.err.print("pc ");
		// System.err.print(Integer.toString(zm.pc, 16));
		// System.err.print("object ");
		// System.err.print(object);
		// System.err.print(" attr_num ");
		// System.err.println(attr_num);
		entryloc = getentryloc(object);
		bytenum = attr_num >> 3;
		bitmask = 1 << (7 - (attr_num & 7));
		return ((zm.memory_image[entryloc + bytenum] & bitmask) != 0);
	}

	public void set_attribute(short object, short attr_num) {
		int bytenum;
		int bitmask;
		int entryloc;

		entryloc = getentryloc(object);
		bytenum = attr_num >> 3;
		bitmask = 1 << (7 - (attr_num & 7));
		zm.memory_image[entryloc + bytenum] |= (byte) bitmask;
	}

	public void clear_attribute(short object, short attr_num) {
		int bytenum;
		int bitmask;
		int entryloc;

		entryloc = getentryloc(object);
		bytenum = attr_num >> 3;
		bitmask = 1 << (7 - (attr_num & 7));
		zm.memory_image[entryloc + bytenum] &= (byte) (bitmask ^ 0xFF);
	}

	public abstract short parent(short object);

	public abstract short child(short object);

	public abstract short sibling(short object);

	public abstract void set_parent(short object, short newparent);

	public abstract void set_sibling(short object, short newparent);

	public abstract void set_child(short object, short newparent);

	public int property_table_addr(short object) {
		int entryloc;

		entryloc = getentryloc(object);
		return (((zm.memory_image[entryloc + ptableoffset()] << 8) & 0xFF00) | (zm.memory_image[entryloc
				+ ptableoffset() + 1] & 0xFF));
	}

	public int short_name_addr(short object) {
		return (property_table_addr(object) + 1);
	}

	public abstract int prop_entry_address(short object, short propnum);

	public abstract short next_prop(short object, short propnum);

	public abstract short prop_address(short object, short propnum);

	public abstract short prop_len(short prop_address);

	public short prop(short object, short propnum) {
		int entry_address = prop_entry_address(object, propnum);
		int size;

		// System.err.print("prop: " +
		// Integer.toString(object&0xFFFF, 10) + " " +
		// Integer.toString(propnum&0xFFFF, 10) + " " +
		// Integer.toString(entry_address, 16) + " ");
		if (entry_address == 0) {
			// System.err.println("default " +
			// Integer.toString(default_property(propnum)&0xFFFF, 16));
			return default_property(propnum);
		}
		else {
			size = (zm.memory_image[entry_address] >>> 5) + 1;
			if (size == 1)
				return (short) (zm.memory_image[entry_address + 1] & 0xFF);
			else {
				return (short) (((zm.memory_image[entry_address + 1] << 8) & 0xFF00) | (zm.memory_image[entry_address + 2] & 0xFF));
			}
		}
	}

	public void put_prop(short object, short propnum, short value) {
		int entry_address = prop_entry_address(object, propnum);
		int size;

		if (entry_address == 0) {
			zm.fatal("Tried to set nonexistent property");
		}
		else {
			size = (zm.memory_image[entry_address] >>> 5) + 1;
			if (size == 1)
				zm.memory_image[entry_address + 1] = (byte) (value & 0xFF);
			else {
				zm.memory_image[entry_address + 1] = (byte) (value >>> 8);
				zm.memory_image[entry_address + 2] = (byte) (value & 0xFF);
			}
		}
	}
}
