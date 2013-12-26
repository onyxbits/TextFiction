/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;


public abstract class ZHeader {
	protected byte memory_image[];
	protected final static int VERSION =		 0x00;
	protected final static int FLAGS1 =		 0x01;
	public final static int RELEASE =		 0x02;
	protected final static int HIGH_BASE =	 0x04;
	protected final static int INITIAL_PC =	 0x06;
	protected final static int DICTIONARY =	 0x08;
	protected final static int OBJECT_TABLE =  0x0A;
	protected final static int GLOBAL_TABLE =  0x0C;
	protected final static int STATIC_BASE =	 0x0E;
	protected final static int FLAGS2 	 =	 0x10;
        public final static int SERIAL_NUMBER = 0x12;
	protected final static int ABBREV_TABLE =  0x18;
	protected final static int FILE_LENGTH  =  0x1A;
	public final static int FILE_CHECKSUM = 0x1C;
	protected final static int STD_REVISION  = 0x32;

	public int version()
	{
		return memory_image[VERSION];
	}

	public static int image_version(byte [] memory_image)
	{
		return memory_image[VERSION];
	}

	public int high_base() {
		return (((int)memory_image[HIGH_BASE]<<8)&0xFF00) | 
				(((int)memory_image[HIGH_BASE+1]) & 0x00FF);
	}

	public int initial_pc() {
		return (((int)memory_image[INITIAL_PC]<<8)&0xFF00) |
				(((int)memory_image[INITIAL_PC+1]) & 0x00FF);
	}

	public int dictionary() {
		return (((int)memory_image[DICTIONARY]<<8)&0xFF00) |
				(((int)memory_image[DICTIONARY+1]) & 0x00FF);
	}
	
	public int object_table() {
		return (((int)memory_image[OBJECT_TABLE]<<8)&0xFF00) |
				(((int)memory_image[OBJECT_TABLE+1]) & 0x00FF);
	}	
	
	public int global_table() {
		return (((int)memory_image[GLOBAL_TABLE]<<8)&0xFF00) |
				(((int)memory_image[GLOBAL_TABLE+1]) & 0x00FF);
	}
	
	public int static_base() {
		return (((int)memory_image[STATIC_BASE]<<8)&0xFF00) |
				(((int)memory_image[STATIC_BASE+1]) & 0x00FF);
	}
	
	public boolean transcripting() {
		return ((memory_image[FLAGS2+1]&1) == 1);
	}

	public void set_transcripting (boolean onoff) {
		if (onoff)
			memory_image[FLAGS2+1] |= 1;
		else
			memory_image[FLAGS2+1] &= 0xFE;
	}

	public int abbrev_table() {
		return (((int)memory_image[ABBREV_TABLE]<<8) & 0xFF00) |
				(((int)memory_image[ABBREV_TABLE+1]) & 0x00FF);
	}
	
	public boolean force_fixed() {
		return ((memory_image[FLAGS2+1]&2) == 2);
	}

	public void set_revision(int major, int minor) {
		memory_image[STD_REVISION] = (byte)major;
		memory_image[STD_REVISION + 1] = (byte)minor;
	}

	public short release()
	{
		return (short)(((memory_image[RELEASE]&0xFF)<<8) |
		   (memory_image[RELEASE+1]&0xFF));
	}

	public short checksum() {
		return (short)(((memory_image[FILE_CHECKSUM]&0xFF)<<8) |
		   (memory_image[FILE_CHECKSUM+1]&0xFF));
	}

	public abstract int file_length();
}

