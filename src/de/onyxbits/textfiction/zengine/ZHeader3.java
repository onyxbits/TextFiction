/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;


class ZHeader3 extends ZHeader
{
	final static int FILE_LENGTH_FACTOR = 2;

	ZHeader3 (byte [] memory_image)
	{
		this.memory_image = memory_image;
	}

	public boolean time_game() /* as opposed to score game */
	{
		return ((memory_image[FLAGS1]&0x02) == 2);
	}
	
	public void set_status_unavailable(boolean unavail) 
	{
		if (unavail) {
			memory_image[FLAGS1] |= 0x10;
		}
		else {
			memory_image[FLAGS1] &= 0xEF;
		}
	}

	public void set_splitting_available(boolean avail)
	{
		if (avail) {
			memory_image[FLAGS1] |= 0x20;
		}
		else {
			memory_image[FLAGS1] &= 0xDF;
		}
	}

	public void set_variable_default(boolean variable)
	{
		if (variable) {
			memory_image[FLAGS1] |= 0x40;
		}
		else {
			memory_image[FLAGS1] &= 0xBF;
		}
	}

	public int file_length() {
		int packed_length;
		
		packed_length = (((memory_image[FILE_LENGTH]&0xFF)<<8) |
					   (memory_image[FILE_LENGTH+1]&0xFF));
		return packed_length * FILE_LENGTH_FACTOR;
	}
}

