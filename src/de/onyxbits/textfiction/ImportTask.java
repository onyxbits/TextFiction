package de.onyxbits.textfiction;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Helper task for the LibraryFragment that takes care of copying games from the
 * download directory to the library directory.
 */
class ImportTask extends AsyncTask<Object, Integer, Exception> implements
		DialogInterface.OnClickListener,
		DialogInterface.OnMultiChoiceClickListener, FilenameFilter {

	/**
	 * Supported filename suffixes
	 */
	public static final String[] SUFFIXES = { "Z3", "Z5", "Z8", "ZBLORB" };

	/**
	 * The import candidates from which the user may choose.
	 */
	private File[] toImport;

	/**
	 * Flags the files in "toImport" that are to be downloaded
	 */
	private boolean[] selected;
	
	/**
	 * Set if we actually did import something
	 */
	private boolean didImport=false;

	/**
	 * The library fragment, we are working for.
	 */
	private LibraryFragment master;

	private ImportTask() {
	}

	@Override
	public void onPreExecute() {
		Activity context = master.getActivity();
		if (context != null) {
			context.setProgressBarIndeterminate(false);
			context.setProgressBarIndeterminateVisibility(false);
			context.setProgressBarVisibility(false);
		}
	}

	@Override
	protected Exception doInBackground(Object... params) {
		for (int i = 0; i < toImport.length; i++) {
			if (selected[i]) {
				try {
					FileUtil.importGame(toImport[i]);
				}
				catch (Exception e) {
					Log.w(getClass().getName(), e);
					return e;
				}
			}
		}
		didImport=true;
		return null;
	}

	@Override
	public void onPostExecute(Exception result) {
		Activity context = master.getActivity();
		if (result != null) {
			Toast.makeText(context, R.string.msg_failure_import, Toast.LENGTH_SHORT)
					.show();
		}
		if (context != null) {
			context.setProgressBarIndeterminate(false);
			context.setProgressBarIndeterminateVisibility(false);
			context.setProgressBarVisibility(false);
		}
		if (result==null && context!=null && didImport) {
			Toast.makeText(context,R.string.msg_file_copied_you_may_delete_the_original_now,Toast.LENGTH_LONG).show();
		}
		master.reScan();
	}

	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		selected[which] = isChecked;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			execute(new Object());
		}
	}

	/**
	 * Silently import games without showing a selection dialog
	 * 
	 * @param master
	 *          callback
	 * @param files
	 *          files to import
	 */
	public static void importGames(LibraryFragment master, File[] files) {
		ImportTask task = new ImportTask();
		task.master = master;
		task.toImport = files;
		task.selected = new boolean[files.length];
		Arrays.fill(task.selected, true);
		task.execute(new Object());
	}

	/**
	 * Shows a list of games that are available in the public downloads directory,
	 * allowing the user to select which ones to import.
	 * 
	 * @param master
	 *          the fragment to report back to in case something was imported.
	 */
	public static void showSelectDialog(LibraryFragment master) {

		ImportTask task = new ImportTask();
		task.toImport = Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_DOWNLOADS).listFiles(task);

		if (task.toImport == null || task.toImport.length == 0) {
			Toast.makeText(master.getActivity(), R.string.msg_nothing_to_import,
					Toast.LENGTH_SHORT).show();
			return;
		}

		Arrays.sort(task.toImport);

		String[] names = new String[task.toImport.length];
		task.selected = new boolean[task.toImport.length];
		task.master = master;

		for (int i = 0; i < task.toImport.length; i++) {
			names[i] = task.toImport[i].getName();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(master.getActivity());
		builder.setTitle(R.string.title_select_import)
				.setMultiChoiceItems(names, null, task)
				.setPositiveButton(android.R.string.ok, task).create().show();
	}

	@Override
	public boolean accept(File dir, String filename) {
		boolean ret = false;
		for (String suffix : SUFFIXES) {
			if (filename.toUpperCase().endsWith(suffix)) {
				ret = true;
				break;
			}
		}
		return ret;
	}
}
