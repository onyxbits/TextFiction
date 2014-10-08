package de.onyxbits.textfiction;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;

/**
 * Shows the list of installed games, provides the means to start, delete and
 * import them.
 */
public class LibraryFragment extends Fragment implements
		AbsListView.OnItemClickListener {

	/**
	 * The fragment's ListView/GridView.
	 */
	private AbsListView mListView;

	/**
	 * The Adapter which will be used to populate the ListView/GridView with
	 * Views.
	 */
	private LibraryAdapter mAdapter;

	private ArrayList<File> games;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public LibraryFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		games = new ArrayList<File>();
		mAdapter = new LibraryAdapter(getActivity(), 0, games);
		mAdapter.setStripSuffix(true);
	}

	/**
	 * Drop the current list of games and rebuild it from scratch.
	 */
	public void reScan() {
		File[] stories = FileUtil.listGames();
		games.clear();
		for (File f : stories) {
			games.add(f);
		}
		mAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.library_fragment, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_browse: {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(getString(R.string.url_catalog)));
				startActivity(browserIntent);
				return true;
			}
			case R.id.mi_import: {
				ImportTask.showSelectDialog(this);
				return true;
			}
		}
		return false;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_library, null, false);

		// Set the adapter
		mListView = (AbsListView) view.findViewById(android.R.id.list);
		mListView.setEmptyView(view.findViewById(android.R.id.empty));
		((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

		// Set OnItemClickListener so we can be notified on item clicks
		mListView.setOnItemClickListener(this);
		
		reScan();
		return view;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		File game = (File) mAdapter.getItem(position);
		Intent intent = new Intent(getActivity(), GameActivity.class);
		intent.putExtra(GameActivity.LOADFILE, game.getAbsolutePath());
		startActivity(intent);
	}
}
