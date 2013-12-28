package de.onyxbits.textfiction;

import java.io.File;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

/**
 * From this activity, the player can manage his/her library and start games.
 * 
 * @author patrick
 * 
 */
public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		AppRater.appLaunched(this);
		Uri game = getIntent().getData();
		if (game!=null && game.getScheme().equals(ContentResolver.SCHEME_FILE)) {
			LibraryFragment frag = (LibraryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.fragment_library);
			File[] f = {new File(game.getPath())};
			ImportTask.importGames(frag,f);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.mi_browse: {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(getString(R.string.catalog_url)));
				startActivity(browserIntent);
				return true;
			}
			case R.id.mi_help: {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(getString(R.string.help_url)));
				startActivity(browserIntent);
				return true;
			}
		}
		return false;
	}

}
