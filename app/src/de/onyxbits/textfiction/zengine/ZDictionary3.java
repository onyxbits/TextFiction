/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;


public class ZDictionary3 extends ZDictionary{
	ZMachine zm;
	String separators;
	int nentries;
	int wtable_addr;
	int dict_address;
	int entry_length;
	
	public ZDictionary3(ZMachine zm, int dict_address) {
		int n, i;
		char separray[];

		this.zm = zm;
		this.dict_address = dict_address;
		n = zm.memory_image[dict_address]&0xFF;
		separray = new char[n];

		for (i = 0; i < n; i++)
			separray[i] = (char)zm.memory_image[dict_address + i + 1];

		separators = new String(separray);
//		System.err.println("separators: " + separators);
		entry_length = zm.memory_image[dict_address + n + 1];
		nentries = (zm.memory_image[dict_address + n + 2] << 8)  |
			((zm.memory_image[dict_address + n + 3]) & 0xFF);
		wtable_addr = dict_address+n+4;
	}

	public ZDictionary3 (ZMachine zm) {
		this(zm, zm.header.dictionary());
	}

	public boolean parse_word(int textloc, int wordloc, int wordlength, int parseloc) {
		short encword[];
		int encint;
		int dictint;
		int dictloc;
		int first = 0;
		int last = nentries - 1;
		int middle;
		int parseentry;

//		System.err.println("Entered parse_word");
		if (zm.memory_image[parseloc] == zm.memory_image[parseloc+1])
			return true;

		encword = zm.encode_word(wordloc, wordlength, 2);
		encint =
			((encword[0]&0xFFFF)<<16) |
						((encword[1])&0xFFFF);
		middle = (last + first) / 2;
		while (true) {
//			System.err.print("bsearch " + first + " " + middle + " " + last);
			dictloc = wtable_addr + (middle * entry_length);
			dictint = ((zm.memory_image[dictloc]&0xFF)<<24) |
				((zm.memory_image[dictloc + 1]&0xFF)<<16) |
					((zm.memory_image[dictloc + 2]&0xFF)<<8) |
						(zm.memory_image[dictloc + 3]&0xFF);
//			System.err.println(" " + Integer.toString(encint, 16) + " " +
//							 Integer.toString(dictint, 16));
			if (encint < dictint) {
				if (first == middle) break;
				last = middle - 1;
				middle = (first + middle)/2;
			}
			else if (encint > dictint) {
				if (last == middle) break;
				first = middle + 1;
				middle = (middle + last + 1)/2;
			}
			else
				break;
		}
		if (encint != dictint) {
			dictloc = 0;
		}
//		System.err.println("dictloc " + Integer.toString(dictloc,16));
//		System.err.println("wordlength " + Integer.toString(wordlength,16));
//		System.err.println("wordloc " + Integer.toString(wordloc - textloc,16));
		parseentry = parseloc + ((zm.memory_image[parseloc+1]&0xFF)*4) + 2;
		zm.memory_image[parseentry] = (byte)((dictloc & 0xFF00)>>8);
		zm.memory_image[parseentry + 1] = (byte)(dictloc&0xFF);
		zm.memory_image[parseentry + 2] = (byte)wordlength;
		zm.memory_image[parseentry + 3] = (byte)(wordloc - textloc + 1);
		/* +1 in V3, + 2 in V5 on line above*/
		zm.memory_image[parseloc+1]++;
		if (zm.memory_image[parseloc] == zm.memory_image[parseloc+1])
			return true;
		return false;
	}

	public void tokenise(int textloc, int textlength, int parseloc) {
		int wordloc, wordlength;
		int textleft = textlength;
		char ch;
		boolean pbfull;

		if ((zm.memory_image[parseloc]&0xFF) < 1)
			zm.fatal("Parse buffer less than 1 word (6 bytes)");
		zm.memory_image[parseloc + 1] = (byte)0;
		wordloc = textloc;
		wordlength = 0;
		pbfull = false;
		while (!pbfull && (textleft-- > 0)) {
			ch = (char)zm.memory_image[wordloc + wordlength];
//			System.err.println("ch @ " + Integer.toString(wordloc + wordlength, 16) + " : " + ch);
			if (separators.indexOf(ch) != -1) {
				if (wordlength > 0)
					parse_word(textloc, wordloc, wordlength, parseloc);
				pbfull = parse_word(textloc, wordloc+wordlength, 1, parseloc);
				wordloc = wordloc + wordlength + 1;
				wordlength = 0;
			}
			else if (ch == ' ') {
				if (wordlength > 0)
					pbfull = parse_word(textloc, wordloc, wordlength, parseloc);
				wordloc = wordloc + wordlength + 1;
				wordlength = 0;
			}
			else
				wordlength++;
		}

		if (!pbfull && (wordlength > 0))
			parse_word(textloc, wordloc, wordlength, parseloc);
	}
}

