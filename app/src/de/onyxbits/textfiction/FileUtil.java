package de.onyxbits.textfiction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import android.os.Environment;

public class FileUtil implements Comparator<File> {

	/**
	 * Datadir on the external storage
	 */
	public static final String HOMEDIR = "TextFiction";

	/**
	 * Where the games are stored (relative to the HOMEDIR or app data dir).
	 */
	public static final String GAMEDIR = "games";

	/**
	 * Where to keep the auto save files
	 */
	public static final String AUTOSAVEDIR = "autosave";

	/**
	 * Where the save game files are stored (relative to the HOMEDIR or app data
	 * dir).
	 */
	public static final String SAVEDIR = "savegames";

	private static final File library;
	private static final File autosaves;
	private static final File saves;

	/**
	 * Just make sure we got all of our directories.
	 */
	static {
		File root = Environment.getExternalStorageDirectory();
		library = new File(new File(root, HOMEDIR), GAMEDIR);
		autosaves = new File(new File(root, HOMEDIR), AUTOSAVEDIR);
		saves = new File(new File(root, HOMEDIR), SAVEDIR);
		library.mkdirs();
		autosaves.mkdirs();
		saves.mkdirs();
	}

	public static File[] listGames() {

		return library.listFiles();
	}

	/**
	 * List all the save files for a game
	 * 
	 * @param game
	 *          the game in question
	 * @return list of files in the savegamedir
	 */
	public static File[] listSaveGames(File game) {
		File f = getSaveGameDir(game);
		File[] ret = f.listFiles();
		if (ret == null) {
			return new File[0];
		}
		Arrays.sort(ret, new FileUtil());
		return ret;
	}

	/**
	 * List all the saves for a game
	 * 
	 * @param game
	 *          the game in question
	 * @return the filenames of the save games.
	 */
	public static String[] listSaveName(File game) {
		File[] f = listSaveGames(game);
		String ret[] = new String[f.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = f[i].getName();
		}
		return ret;
	}

	/**
	 * 
	 */
	public FileUtil() {
	}

	/**
	 * Copies files to the library
	 * 
	 * @param src
	 *          the file to copy
	 * @throws IOException
	 *           if something goes wrong
	 */
	public static void importGame(File src) throws IOException {
		File dst = new File(library, src.getName());
		byte[] buf = new byte[1024];
		FileInputStream fin = new FileInputStream(src);
		FileOutputStream fout = new FileOutputStream(dst);

		int len;
		while ((len = fin.read(buf)) > 0) {
			fout.write(buf, 0, len);
		}
		getSaveGameDir(dst).mkdirs();
		fin.close();
		fout.close();
	}

	/**
	 * Delete a game and all other files belonging to it.
	 * 
	 * @param game
	 *          the game file
	 */
	public static void deleteGame(File game) {
		getAutosave(game).delete();
		File[] lst = getSaveGameDir(game).listFiles();
		for (File f : lst) {
			f.delete();
		}
		getSaveGameDir(game).delete();
		game.delete();
	}

	/**
	 * Query the autosave for a game
	 * 
	 * @param game
	 *          the game file
	 * @return the autosave file for the game.
	 */
	public static File getAutosave(File game) {
		return new File(autosaves, game.getName());
	}

	/**
	 * Get the directory where a game keeps its savegames
	 * 
	 * @param game
	 *          the game in question
	 * @return a directory for saving games.
	 */
	public static File getSaveGameDir(File game) {
		File ret =new File(saves, game.getName());
		ret.mkdirs();
		return ret;
	}

	@Override
	public int compare(File lhs, File rhs) {
		return Long.valueOf(rhs.lastModified()).compareTo(lhs.lastModified());
	}

}
