package de.onyxbits.textfiction;

import java.io.File;
import java.lang.reflect.Field;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

/**
 * From this activity, the player can manage his/her library and start games.
 * 
 * @author patrick
 * 
 */
public class MainActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		try {
			Field field = R.style.class.getField(prefs.getString("theme", ""));
			setTheme(field.getInt(null));
		}
		catch (Exception e) {
			Log.w(getClass().getName(), e);
		}
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_main);
		AppRater.appLaunched(this);
		Uri game = getIntent().getData();
		if (game != null && game.getScheme().equals(ContentResolver.SCHEME_FILE)) {
			LibraryFragment frag = (LibraryFragment) getSupportFragmentManager()
					.findFragmentById(R.id.fragment_library);
			File[] f = { new File(game.getPath()) };
			ImportTask.importGames(frag, f);
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
			case R.id.mi_help: {
				openUri(this,Uri.parse(getString(R.string.url_help)));
				return true;
			}
			case R.id.mi_settings: {
				startActivity(new Intent(this, SettingActivity.class));
				return true;
			}
		}
		return false;
	}

	/**
	 * Open an url in a webbrowser
	 * 
	 * @param ctx
	 *          a context
	 * @param uri
	 *          target
	 */
	public static void openUri(Context ctx, Uri uri) {
		try {
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
			ctx.startActivity(browserIntent);
		}
		catch (ActivityNotFoundException e) {
			// There are actually people who don't have a webbrowser installed
			Toast.makeText(ctx, R.string.msg_no_webbrowser, Toast.LENGTH_SHORT)
					.show();
		}
	}

}
