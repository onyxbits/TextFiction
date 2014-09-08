package de.onyxbits.textfiction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.onyxbits.textfiction.zengine.GrueException;
import de.onyxbits.textfiction.zengine.ZMachine;
import de.onyxbits.textfiction.zengine.ZMachine3;
import de.onyxbits.textfiction.zengine.ZMachine5;
import de.onyxbits.textfiction.zengine.ZMachine8;
import de.onyxbits.textfiction.zengine.ZScreen;
import de.onyxbits.textfiction.zengine.ZStatus;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Loader for z-code games. Supports z3, z5, z8 and zblorb files.
 * 
 * @author patrick
 * 
 */
public class LoaderTask extends AsyncTask<File, Integer, ZMachine> {

	private RetainerFragment retainer;

	/**
	 * 
	 * @param retainer
	 *          where to deliver the loaded engine.
	 */
	public LoaderTask(RetainerFragment retainer) {
		this.retainer = retainer;
	}

	@Override
	protected void onPreExecute() {
		GameActivity gameActivity = (GameActivity)retainer.getActivity();
		if (gameActivity != null) {
			gameActivity.setLoadingVisibility(true);
		}
	}

	@Override
	protected ZMachine doInBackground(File... story) {
		ZMachine engine = null;
		try {
			byte[] memImage = createMemImage(new FileInputStream(story[0]));

			if (isBlorb(memImage)) {
				memImage = extractGame(memImage);
			}

			switch (memImage[0]) {
				case 3: {
					engine = new ZMachine3(new ZScreen(), new ZStatus(), memImage);
					break;
				}
				case 5: {
					engine = new ZMachine5(new ZScreen(), memImage);
					break;
				}
				case 8: {
					engine = new ZMachine8(new ZScreen(), memImage);
					break;
				}
			}
		}
		catch (IOException e) {
			Log.w(getClass().getName(), e);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			Log.w(getClass().getName(), "Empty file");
		}

		if (engine != null) {
			try {
				// NOTE: Don't throw a command save in the same directory as a menu
				// save. It won't work! The menusave captures the state of the machine
				// at rest. The command save captures the state in the middle of
				// execution (while executing the save commmand).
				File qs = new File(FileUtil.getDataDir(story[0]), "quicksave.bin");
				engine.setQuickSaveSlot(qs);
				engine.restart();
				engine.run();
			}
			catch (GrueException e) {
				Log.w(getClass().getName(), e);
				engine = null;
			}
		}
		return engine;
	}

	@Override
	protected void onPostExecute(ZMachine result) {
		retainer.engine = result;
		GameActivity gameActivity = (GameActivity) retainer.getActivity();
		if (gameActivity != null) {
			gameActivity.setLoadingVisibility(false);
			if (result != null) {
				gameActivity.publishResult();
			}
			else {
				retainer.postMortem = new GrueException(
						gameActivity.getString(R.string.msg_corrupt_game_file));
				gameActivity.finish();
			}
		}
	}

	/**
	 * Load a binary file into RAM
	 * 
	 * @param mystream
	 *          where to read from
	 * @return memory image as byte array.
	 * @throws IOException
	 *           in case something goes wrong.
	 */
	private byte[] createMemImage(InputStream mystream) throws IOException {
		byte buffer[];
		byte oldbuffer[];
		int currentbytes = 0;
		int bytesleft;
		int got;
		int buffersize = 1024 * 8;

		buffer = new byte[buffersize];
		bytesleft = buffersize;
		got = 0;
		while (got != -1) {
			bytesleft -= got;
			currentbytes += got;
			if (bytesleft == 0) {
				oldbuffer = buffer;
				buffer = new byte[buffersize + currentbytes];
				System.arraycopy(oldbuffer, 0, buffer, 0, currentbytes);
				oldbuffer = null;
				bytesleft = buffersize;
			}
			got = mystream.read(buffer, currentbytes, bytesleft);
		}
		if (buffer.length != currentbytes) {
			oldbuffer = buffer;
			buffer = new byte[currentbytes];
			System.arraycopy(oldbuffer, 0, buffer, 0, currentbytes);
		}
		return buffer;
	}

	/**
	 * Check if a file is an zblorb archive
	 * 
	 * @param image
	 *          the memory image of the file
	 * @return true if the file starts with a FORM header.
	 */
	public static boolean isBlorb(byte[] image) {
		// detect "FORM" header
		return (image != null && image.length >= 40 && image[0] == 'F' && image[1] == 'O'
				&& image[2] == 'R' && image[3] == 'M');
	}

	/**
	 * 
	 * @param image
	 * @param index
	 * @return
	 */
	static int readInt(byte[] image, int index) {
		return image[index] << 24 | (image[index + 1] & 0xFF) << 16 | (image[index + 2] & 0xFF) << 8
				| (image[index + 3] & 0xFF);
	}

	/**
	 * Pull the z-code out of a zblorb.
	 * 
	 * @param image
	 *          raw memory of the zblorb file
	 * @return either the z-code of the game or an empty array if no z-code was
	 *         found.
	 */
	public static byte[] extractGame(byte[] image) {
		int size = readInt(image, 4);

		// allow for 4 bytes "FORM" and 4 bytes size
		int offset = 8;
		if ((size + offset) != image.length) {
			// size check failed!
			return new byte[0];
		}

		if (image[offset] != 'I' || image[offset + 1] != 'F' || image[offset + 2] != 'R'
				|| image[offset + 3] != 'S') {
			// IFRS chunk header not found!
			return new byte[0];
		}

		offset += 4;
		if (image[offset] != 'R' || image[offset + 1] != 'I' || image[offset + 2] != 'd'
				|| image[offset + 3] != 'x') {
			// RIdx not found
			return new byte[0];
		}

		offset += 4;
		offset += 4;
		int resources = readInt(image, offset);
		offset += 4;

		// iterate through 12-byte long index entries
		int start = -1;
		int i;
		for (i = 0; i < resources; i += 12) {
			if (image[offset] == 'E' && image[offset + 1] == 'x' && image[offset + 2] == 'e'
					&& image[offset + 3] == 'c') {
				int number = readInt(image, offset + 4);
				if (number == 0) {
					// start of ZCOD chunk, hopefully
					start = readInt(image, offset + 8);
				}
			}
		}
		if (start > -1) {
			// check "ZCOD", 4 bytes
			if (image[start] == 'Z' && image[start + 1] == 'C' && image[start + 2] == 'O'
					&& image[start + 3] == 'D') {
				int zlength = readInt(image, start + 4);
				offset = start + 8;
				byte[] result = new byte[zlength];
				for (i = 0; i < zlength; i++) {
					result[i] = image[offset + i];
				}
				return result;
			}
		}
		return new byte[0];
	}
}
