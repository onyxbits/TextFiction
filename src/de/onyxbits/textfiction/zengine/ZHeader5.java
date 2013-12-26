/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;


class ZHeader5 extends ZHeader
{
		final static int INTERP_NUMBER = 0x1E;
		final static int INTERP_VERSION = 0x1F; 
		final static int SCREEN_HEIGHT_LINES = 0x20;
		final static int SCREEN_WIDTH_CHARACTERS = 0x21;
		final static int SCREEN_WIDTH_UNITS = 0x22;
		final static int SCREEN_HEIGHT_UNITS = 0x24; 
		final static int FONT_HEIGHT_UNITS = 0x26;
		final static int FONT_WIDTH_UNITS = 0x27;
		final static int DEFAULT_BACKGROUND_COLOR = 0x2C;
		final static int DEFAULT_FOREGROUND_COLOR = 0x2D;

		final static int FILE_LENGTH_FACTOR = 4;

		/* interpreter numbers */

		final static int INTERP_DEC 		=  1;
		final static int INTERP_APPLEIIE	=  2;
		final static int INTERP_MAC			=  3;
		final static int INTERP_AMIGA		=  4;
		final static int INTERP_ATARIST		=  5;
		final static int INTERP_MSDOS		=  6;
		final static int INTERP_C128		=  7;
		final static int INTERP_C64 		=  8;
		final static int INTERP_APPLEIIC	=  9;
		final static int INTERP_APPLEIIGS	= 10;
		final static int INTERP_COCO		= 11;

		public ZHeader5 (byte [] memory_image)
		{
				this.memory_image = memory_image;
		}

		public void set_colors_available(boolean avail) {
				if (avail)
						memory_image[FLAGS1] |= 0x01;
				else {
						memory_image[FLAGS1] &= 0xFE;
				}
		}

		public void set_bold_available(boolean avail) {
				if (avail)
						memory_image[FLAGS1] |= 0x04;
				else
						memory_image[FLAGS1] &= 0xFB;
		}

		public void set_italic_available(boolean avail) {
				if (avail)
						memory_image[FLAGS1] |= 0x08;
				else
						memory_image[FLAGS1] &= 0xF7;
		}

		public void set_fixed_font_available(boolean avail) {
				if (avail)
						memory_image[FLAGS1] |= 0x10;
				else
						memory_image[FLAGS1] &= 0xEF;
		}

		public void set_timed_input_available(boolean avail) {
				if (avail)
						memory_image[FLAGS1] |= 0x80;
				else
						memory_image[FLAGS1] &= 0x7F;
		}

		public boolean graphics_font_wanted() { /* Called pictures in spec */
				return (memory_image[FLAGS2+1] & 0x08) != 0;
		}

		public void set_graphics_font_available(boolean avail) {
				if (!avail)
						memory_image[FLAGS2+1] &= 0xF7;
		}

		public boolean undo_wanted() {
				return (memory_image[FLAGS2+1] & 0x10) != 0;
		}

		void set_undo_available(boolean avail) {
				if (!avail)
						memory_image[FLAGS2+1] &= 0xEF;
		}

		public boolean mouse_wanted() {
				return (memory_image[FLAGS2+1] & 0x20) != 0;
		}

		public void set_mouse_available(boolean avail) {
				if (!avail)
						memory_image[FLAGS2+1] &= 0xDF;
		}

		public boolean colors_wanted() {
				return (memory_image[FLAGS2+1] & 0x40) != 0;
		}

		public boolean sound_wanted() {
				return (memory_image[FLAGS2+1] & 0x80) != 0;
		}

		public void set_sound_available(boolean avail) {
				if (!avail)
						memory_image[FLAGS2+1] &= 0x7F;
		}

		public void set_interpreter_number(int number)
		{
				memory_image[INTERP_NUMBER] = (byte)number;
		}

		public void set_interpreter_version(int version)
		{
				memory_image[INTERP_VERSION] = (byte)version;
		}

		public void set_screen_height_lines(int lines)
		{
				memory_image[SCREEN_HEIGHT_LINES] = (byte)lines;
		}

		public void set_screen_width_characters(int characters)
		{
				memory_image[SCREEN_WIDTH_CHARACTERS] = (byte)characters;
		}

		public void set_screen_height_units(int units)
		{
				memory_image[SCREEN_HEIGHT_UNITS  ] = (byte)(units>>8);
				memory_image[SCREEN_HEIGHT_UNITS+1] = (byte)(units&0xFF);
		}

		public void set_screen_width_units(int units)
		{
				memory_image[SCREEN_WIDTH_UNITS  ] = (byte)(units>>8);
				memory_image[SCREEN_WIDTH_UNITS+1] = (byte)(units&0xFF);
		}

		public void set_font_height_units(int units)
		{
				memory_image[FONT_HEIGHT_UNITS] = (byte)units;
		}

		public void set_font_width_units(int units)
		{
				memory_image[FONT_WIDTH_UNITS] = (byte)units;
		}

		public int default_background_color()
		{
				return memory_image[DEFAULT_BACKGROUND_COLOR];
		}

		public int default_foreground_color()
		{
				return memory_image[DEFAULT_FOREGROUND_COLOR];
		}

		public void set_default_background_color(int color)
		{
				memory_image[DEFAULT_BACKGROUND_COLOR] = (byte)color;
		}

		public void set_default_foreground_color(int color)
		{
				memory_image[DEFAULT_FOREGROUND_COLOR] = (byte)color;
		}

		public int file_length() {
				int packed_length;
				
				packed_length = (((memory_image[FILE_LENGTH]&0xFF)<<8) |
										   (memory_image[FILE_LENGTH+1]&0xFF));
				return packed_length * FILE_LENGTH_FACTOR;
		}
}

