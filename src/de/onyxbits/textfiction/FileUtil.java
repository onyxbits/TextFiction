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
	 * Where the save game files are stored (relative to the HOMEDIR or app data
	 * dir).
	 */
	public static final String SAVEDIR = "savegames";

	/**
	 * Where games store misc data
	 */
	public static final String DATADIR = "gamedata";

	private static final File library;
	private static final File saves;
	private static final File data;

	/**
	 * Just make sure we got all of our directories.
	 */
	static {
		File root = Environment.getExternalStorageDirectory();
		library = new File(new File(root, HOMEDIR), GAMEDIR);
		saves = new File(new File(root, HOMEDIR), SAVEDIR);
		data = new File(new File(root, HOMEDIR), DATADIR);
		library.mkdirs();
		saves.mkdirs();
		data.mkdirs();
	}

	public static File[] listGames() {
		File[] ret = library.listFiles();
		if (ret == null) {
			return new File[0];
		}
		Arrays.sort(ret);

		return ret;
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
		File[] lst = getSaveGameDir(game).listFiles();
		for (File f : lst) {
			f.delete();
		}
		lst = getDataDir(game).listFiles();
		for (File f : lst) {
			f.delete();
		}
		getSaveGameDir(game).delete();
		getDataDir(game).delete();
		game.delete();
	}

	/**
	 * Get the directory where a game keeps its savegames
	 * 
	 * @param game
	 *          the game in question
	 * @return a directory for saving games.
	 */
	public static File getSaveGameDir(File game) {
		File ret = new File(saves, game.getName());
		ret.mkdirs();
		return ret;
	}

	/**
	 * Get the directory where a game may keep various (config) data.
	 * 
	 * @param game
	 *          the game in question.
	 * @return a directory for keeping misc data.
	 */
	public static File getDataDir(File game) {
		File ret = new File(data, game.getName());
		ret.mkdirs();
		return ret;
	}

	/**
	 * Strip filename extension from file name
	 * 
	 * @param file
	 *          the file in question
	 * @return basename
	 */
	public static String basename(File file) {
		String tmp = file.getName();
		int idx = tmp.lastIndexOf('.');
		if (idx > 0) {
			return tmp.substring(0, idx);
		}
		else {
			return tmp;
		}
	}

	@Override
	public int compare(File lhs, File rhs) {
		return Long.valueOf(rhs.lastModified()).compareTo(lhs.lastModified());
	}

}
