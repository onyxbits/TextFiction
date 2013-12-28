/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Stack;

public class ZState {
	final static short QUETZAL_PROCEDURE = 0x10;

	ZMachine zm;
	Stack zstack;
	public ZHeader header;
	int pc;
	byte dynamic[];
	short locals[];
	short argcount;

	public ZState(ZMachine zm) {
		this.zm = zm;
	}

	public void save_current() {
		int dyn_size;

		header = new ZStateHeader(zm.memory_image);
		dyn_size = header.static_base();
		/*
		 * clones the stack but not the Integers within. Fortunately they are
		 * immutable. But the arrays aren't, so don't mess with them
		 */
		zstack = (Stack) zm.zstack.clone();
		dynamic = new byte[dyn_size];
		System.arraycopy(zm.memory_image, 0, dynamic, 0, dyn_size);
		locals = new short[zm.locals.length];
		System.arraycopy(zm.locals, 0, locals, 0, locals.length);
		header = new ZStateHeader(dynamic);
		pc = zm.pc;
		if (header.version() > 3)
			argcount = ((ZMachine5) zm).argcount;
	}

	public void restore_saved() {
		System.arraycopy(dynamic, 0, zm.memory_image, 0, dynamic.length);
		zm.locals = new short[locals.length];
		System.arraycopy(locals, 0, zm.locals, 0, locals.length);
		zm.zstack = (Stack) zstack.clone();
		zm.pc = pc;
		if (header.version() > 3)
			((ZMachine5) zm).argcount = argcount;
	}

	public boolean restore_from_disk(String fname) {
		IFFInputFile infile = null;
		IFFChunkInfo chunkinfo;
		String formtype;
		boolean returnvalue = false;
		short release;
		short checksum;
		byte[] serial = new byte[6];
		int version;
		int framepc;
		byte flags;
		byte resultvar;
		byte argmask;
		int evalwords;
		short[] framelocals;
		int numlocals;
		short[] lastlocals;
		byte lastargmask;
		short argcount;
		int i;
		int frameno;
		long ifhdend;

		lastlocals = new short[0];
		lastargmask = 0;

		version = zm.header.version();
		try {
			infile = new IFFInputFile(fname);
			formtype = infile.readFORM();
			if (formtype.equals("IFZS")) {
				/* find the IFHD */
				chunkinfo = infile.skipToChunk("IFhd");
				release = infile.readShort();
				infile.read(serial, 0, 6);
				checksum = infile.readShort();

				/* verify the story file type */

				if (release != zm.header.release())
					throw new IOException("Release # did not match");
				if (checksum != zm.header.checksum())
					throw new IOException("Checksum did not match");
				for (i = 0; i < serial.length; i++) {
					if (zm.memory_image[ZHeader.SERIAL_NUMBER + i] != serial[i])
						throw new IOException("Serial # did not match");
				}

				/* Set the program counter */

				pc = (infile.readByte() & 0xFF) << 16;
				pc = pc | (infile.readShort() & 0xFFFF);

				infile.closeChunk();

				ifhdend = infile.getFilePointer();
				/* read the memory chunk */

				try {
					chunkinfo = infile.skipToChunk("UMem");
					if (chunkinfo.chunklength != zm.header.static_base())
						throw new IOException("Dynamic memory area is "
								+ chunkinfo.chunklength + " expected "
								+ zm.header.static_base());
					dynamic = new byte[chunkinfo.chunklength];
					infile.read(dynamic, 0, chunkinfo.chunklength);
				}
				catch (IFFChunkNotFoundException memnotfound) {
					boolean runmode;
					byte ch;
					int nbytesout;
					int length;

					infile.seek(ifhdend);
					chunkinfo = infile.skipToChunk("CMem");
					dynamic = new byte[zm.header.static_base()];
					System.arraycopy(zm.restart_state.dynamic, 0, dynamic, 0,
							zm.header.static_base());

					length = chunkinfo.chunklength;
					nbytesout = 0;
					runmode = false;
					while (length-- > 0) {
						if (nbytesout >= zm.header.static_base())
							throw new IOException("CMem exceeded dynamic memory size");
						ch = infile.readByte();
						if (runmode) {
							runmode = false;
							nbytesout += (ch & 0xFF) + 1;
						}
						else if (ch != 0) {
							dynamic[nbytesout++] ^= ch;
						}
						else
							runmode = true;
					}
				}
				header = new ZStateHeader(dynamic);

				infile.closeChunk();

				infile.seek(ifhdend);
				/* read the stacks */
				chunkinfo = infile.skipToChunk("Stks");
				zstack = new Stack();

				frameno = 0;
				while (infile.getChunkPointer() < chunkinfo.chunklength) {
					/* read the frame header */
					framepc = (infile.readByte() & 0xFF) << 16;
					framepc = framepc | (infile.readShort() & 0xFFFF);
					flags = infile.readByte();
					resultvar = infile.readByte();
					argmask = infile.readByte();
					evalwords = infile.readShort();
					numlocals = flags & 0xF;
					framelocals = new short[numlocals];
					for (i = 0; i < numlocals; i++)
						framelocals[i] = infile.readShort();
					/* build the internal frame header */

					if (frameno > 0) { /* no frame header for dummy frame */
						if ((flags & QUETZAL_PROCEDURE) == QUETZAL_PROCEDURE) {
							zstack.push(new ZFrameBound(false));
							if (version > 3)
								zstack.push(new Integer(ZInstruction5.OP_CALL_VN)); /*
																																		 * not
																																		 * entirely
																																		 * correct,
																																		 * but close
																																		 * enough
																																		 */
						}
						else {
							zstack.push(new ZFrameBound(true));
							zstack.push(new Integer(resultvar));
							if (version > 3)
								zstack.push(new Integer(ZInstruction.OP_CALL_1S)); /*
																																		 * not
																																		 * entirely
																																		 * correct,
																																		 * but close
																																		 * enough
																																		 */
						}
						if ((lastargmask & (lastargmask + 1)) != 0)
							throw new IOException(
									"This implementation does not support noncontiguous arguments");

						zstack.push(new Integer(framepc));

						if (version > 3) {
							argcount = 0;
							while (lastargmask != 0) {
								argcount++;
								lastargmask = (byte) (lastargmask & (lastargmask - 1));
							}
							zstack.push(new Integer(argcount));
						}

						zstack.push(lastlocals);
					}

					lastargmask = argmask;
					lastlocals = framelocals;

					/* push the evaluation stack */
					for (i = 0; i < evalwords; i++)
						zstack.push(new Integer(infile.readShort()));

					frameno++;
				}
				this.locals = lastlocals;
				if (version > 3) {
					argcount = 0;
					while (lastargmask != 0) {
						argcount++;
						lastargmask = (byte) (lastargmask & (lastargmask - 1));
					}
					this.argcount = argcount;
				}
				infile.closeChunk();
				returnvalue = true;
			}
			infile.close();
		}

		catch (IOException excpt) {
			System.err.println(excpt);
			try {
				if (infile != null)
					infile.close();
			}
			catch (IOException excpt2) {
			}
		}
		catch (IFFChunkNotFoundException cnfexcpt) {
			System.err.println(cnfexcpt);
			try {
				if (infile != null)
					infile.close();
			}
			catch (IOException cnfexcpt2) {
			}
		}
		catch (SecurityException sexcpt) {
			System.err.println(sexcpt);
			try {
				if (infile != null)
					infile.close();
			}
			catch (IOException sexcpt2) {
			}
			catch (SecurityException sexcpt3) {
			}
		}
		finally {
			if (!returnvalue) {
				infile = null;
				dynamic = null;
				zstack = null;
				framelocals = null;
				lastlocals = null;
				header = null;
			}
		}
		return returnvalue;
	}

	private void write_cmem_chunk(IFFOutputFile outfile) throws IOException {
		int i;
		int runsize;

		outfile.openChunk("CMem");
		runsize = 0;
		for (i = 0; i < zm.header.static_base(); i++) {
			if (zm.memory_image[i] == zm.restart_state.dynamic[i])
				runsize++;
			else {
				while (runsize > 0) {
					outfile.writeByte(0);
					if (runsize >= 256) {
						outfile.writeByte((byte) 255);
						runsize -= 256;
					}
					else {
						outfile.writeByte((byte) (runsize - 1));
						runsize = 0;
					}
				}
				outfile.writeByte(zm.memory_image[i] ^ zm.restart_state.dynamic[i]);
			}
		}
		outfile.closeChunk();
	}

	public boolean disk_save(String fname, int save_pc) {
		Enumeration e, f;
		Object el, el2;
		int i;
		IFFOutputFile outfile = null;
		boolean framestore;
		short storevar;
		short argcount;
		short numlocals;
		short locals[];
		int framepc;
		int version;
		byte frameflags;
		byte argmask;
		long evalstackloc, placeholder;
		boolean returnvalue = false;

		try {
			if (fname.equals("") || fname.equals("nullnull"))
				throw new java.io.IOException("No file picked"); /*
																													 * user didn't pick a
																													 * file
																													 */

			outfile = new IFFOutputFile(fname, "IFZS");
			outfile.openChunk("IFhd");
			outfile.write(zm.memory_image, header.RELEASE, 2);
			outfile.write(zm.memory_image, header.SERIAL_NUMBER, 6);
			outfile.write(zm.memory_image, header.FILE_CHECKSUM, 2);
			outfile.writeByte((save_pc & 0xFF0000) >>> 16);
			outfile.writeShort(save_pc & 0xFFFF);
			outfile.closeChunk();
			// outfile.openChunk("UMem");
			// outfile.write(zm.memory_image, 0, zm.header.static_base());
			// outfile.closeChunk();
			write_cmem_chunk(outfile);
			outfile.openChunk("Stks");
			version = zm.header.version();

			f = zm.zstack.elements(); /*
																 * a horrid kludge to get stuff from the next
																 * stack frame
																 */

			while (f.hasMoreElements()) { /* skip the first framebound */
				el2 = f.nextElement();
				if (el2 instanceof ZFrameBound)
					break;
			}

			e = zm.zstack.elements();
			el = null;
			el2 = null;

			/* write the dummy frame */
			outfile.writeByte(0); /* PC high */
			outfile.writeShort(0); /* PC low */
			outfile.writeByte(0); /* flags */
			outfile.writeByte(0); /* storevar */
			outfile.writeByte(0); /* argmask */

			evalstackloc = outfile.getFilePointer();
			outfile.writeShort((short) 0);
			i = 0;
			while (e.hasMoreElements()) {
				el = e.nextElement();
				if (el instanceof ZFrameBound)
					break;
				outfile.writeShort((short) (((Integer) el).intValue()));
				i++;
			}
			placeholder = outfile.getFilePointer();
			outfile.seek(evalstackloc);
			outfile.writeShort((short) i);
			outfile.seek(placeholder);

			while (e.hasMoreElements()) {
				if (f.hasMoreElements()) {
					do {
						el2 = f.nextElement();
					}
					while (f.hasMoreElements() && !(el2 instanceof ZFrameBound));
				}

				if (f.hasMoreElements()) { /* get the stuff from the next stack frame */
					if (((ZFrameBound) el2).isstore())
						el2 = f.nextElement(); /* skip the storevar */
					if (version > 3) { /* breaks my object-orientation */
						el2 = f.nextElement(); /* skip the opcode number */
					}
					el2 = f.nextElement(); /* skip the frame PC */

					if (version > 3) {
						el2 = f.nextElement();
						argcount = (short) (((Integer) el2).intValue());
					}
					else {
						argcount = 4; /* doesn't really matter for V3 */
					}

					el2 = f.nextElement();
					locals = (short[]) el2;
				}
				else { /* get it from the running Z-machine state */
					if (version > 3) {
						argcount = (short) (((ZMachine5) zm).argcount);
					}
					else {
						argcount = 4; /* doesn't really matter for V3 */
					}
					locals = zm.locals;
				}

				numlocals = (short) locals.length;
				if ((version <= 3) && (numlocals < argcount))
					argcount = numlocals;

				framestore = ((ZFrameBound) el).isstore();
				if (framestore) {
					el = e.nextElement();
					storevar = (short) (((Integer) el).intValue());
				}
				else
					storevar = 0;

				if (version > 3) { /* breaks my object-orientation */
					el = e.nextElement(); /* skip the opcode number */
				}

				el = e.nextElement(); /* get the program counter */
				framepc = ((Integer) el).intValue();

				if (version > 3) {
					el = e.nextElement(); /* skip the argcount */
				}
				el = e.nextElement(); /* skip the locals */

				frameflags = (byte) numlocals;
				if (!framestore)
					frameflags |= QUETZAL_PROCEDURE; /* procedure flag */
				argmask = (byte) ((1 << argcount) - 1);
				outfile.writeByte((framepc & 0xFF0000) >>> 16);
				outfile.writeShort(framepc & 0xFFFF);
				outfile.writeByte(frameflags);
				outfile.writeByte((byte) storevar);
				outfile.writeByte(argmask);
				evalstackloc = outfile.getFilePointer();
				outfile.writeShort((short) 0);
				for (i = 0; i < numlocals; i++) {
					outfile.writeShort(locals[i]);
				}
				i = 0;
				while (e.hasMoreElements()) {
					el = e.nextElement();
					if (el instanceof ZFrameBound)
						break;
					// System.err.println("el = " + el);
					outfile.writeShort((short) (((Integer) el).intValue()));
					i++;
				}
				placeholder = outfile.getFilePointer();
				outfile.seek(evalstackloc);
				outfile.writeShort((short) i);
				outfile.seek(placeholder);
			}
			outfile.closeChunk();
			outfile.close();
			returnvalue = true;
		}
		catch (IOException excpt) {
			try {
				if (outfile != null)
					outfile.close();
			}
			catch (IOException excpt2) {
			}
		}
		catch (SecurityException sexcpt) {
			try {
				if (outfile != null)
					outfile.close();
			}
			catch (IOException sexcpt2) {
			}
			catch (SecurityException sexcpt3) {
			}
		}
		return returnvalue;
	}
}
