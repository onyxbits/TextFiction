package de.onyxbits.textfiction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import de.onyxbits.textfiction.zengine.ZMachine;
import de.onyxbits.textfiction.zengine.ZMachine3;
import de.onyxbits.textfiction.zengine.ZMachine5;
import de.onyxbits.textfiction.zengine.ZMachine8;
import de.onyxbits.textfiction.zengine.ZScreen;
import de.onyxbits.textfiction.zengine.ZState;
import de.onyxbits.textfiction.zengine.ZStatus;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

/**
 * Takes care of creating an engine from a story file
 * 
 * @author patrick
 * 
 */
public class LoaderTask extends AsyncTask<File, Integer, ZMachine> {

	private RetainerFragment retainer;

	public LoaderTask(RetainerFragment retainer) {
		this.retainer = retainer;
	}

	@Override
	protected void onPreExecute() {
		Activity gameActivity = retainer.getActivity();
		if (gameActivity != null) {
			gameActivity.setProgressBarIndeterminate(true);
			gameActivity.setProgressBarIndeterminateVisibility(true);
			gameActivity.setProgressBarVisibility(true);
		}
	}

	@Override
	protected ZMachine doInBackground(File... story) {
		ZMachine engine = null;
		try {
			byte[] memImage = createMemImage(new FileInputStream(story[0]));
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
				default: {
					// TODO: create a dummy machine?
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
			engine.restart();
			engine.run();
		}
		return engine;
	}

	@Override
	protected void onPostExecute(ZMachine result) {
		retainer.engine = result;
		GameActivity gameActivity = (GameActivity) retainer.getActivity();
		if (gameActivity != null) {
			gameActivity.setProgressBarIndeterminate(false);
			gameActivity.setProgressBarIndeterminateVisibility(false);
			gameActivity.setProgressBarVisibility(false);
			if (result != null) {
				gameActivity.publishResult();
			}
			else {
				gameActivity.finish();
				Toast.makeText(gameActivity, R.string.msg_corrupt_game_file,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Load the Rom code
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

}
