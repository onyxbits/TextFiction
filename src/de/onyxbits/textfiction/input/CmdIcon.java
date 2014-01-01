package de.onyxbits.textfiction.input;

import de.onyxbits.textfiction.R;
import de.onyxbits.textfiction.R.drawable;
import de.onyxbits.textfiction.R.string;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Container class that stores the info for quick access command buttons.
 * 
 * @author patrick
 * 
 */
public class CmdIcon {

	public static final String PREFSATONCE = "atonce";
	public static final String PREFSIMG = "img";
	public static final String PREFSCMD = "cmd";

	/**
	 * List of all icon resource ids, the user is allowed to assign to a button
	 */
	public static final int[] ICONS = {
			R.drawable.ic_action_user0,
			R.drawable.ic_action_user1,
			R.drawable.ic_action_user2,
			R.drawable.ic_action_user3,
			R.drawable.ic_action_user4,
			R.drawable.ic_action_user5,
			R.drawable.ic_action_user6,
			R.drawable.ic_action_user7,
			R.drawable.ic_action_user8,
			R.drawable.ic_action_user9,
			R.drawable.ic_action_user10,
			R.drawable.ic_action_user11,
			R.drawable.ic_action_user12,
			R.drawable.ic_action_user13,
			R.drawable.ic_action_user14,
			R.drawable.ic_action_user15,
			R.drawable.ic_action_user16,
			R.drawable.ic_action_user17,
			R.drawable.ic_action_user18,
	};

	/**
	 * the (user) command (string) associated with the button-
	 */
	public String cmd;

	/**
	 * Whether or not the command should be executed immediately on the button
	 * press.
	 */
	public boolean atOnce;

	/**
	 * ID number of the cmdicon (for saving in the preferences storage)
	 */
	public int slot;

	/**
	 * ID number of the drawable (for saving in the preferences storage)
	 */
	public int imgid;

	private CmdIcon(int slot, int imgid, String cmd, boolean atOnce) {
		this.slot = slot;
		this.imgid = imgid;
		this.cmd = cmd;
		this.atOnce = atOnce;
	}

	public static CmdIcon create(Context ctx, int slot) {
		SharedPreferences prefs = ctx.getSharedPreferences(InputFragment.PREFSFILE,
				0);
		int imgid = 0;
		String cmdstr = "";
		boolean instantly = false;
		switch (slot) {
			case 0: {
				imgid = prefs.getInt(PREFSIMG + ".0", R.drawable.ic_action_user0);
				cmdstr = prefs.getString(PREFSCMD + ".0",
						ctx.getString(R.string.cmd_user0));
				instantly = prefs.getBoolean(PREFSATONCE + ".0", true);
				break;
			}
			case 1: {
				imgid = prefs.getInt(PREFSIMG + ".1", R.drawable.ic_action_user1);
				cmdstr = prefs.getString(PREFSCMD + ".1",
						ctx.getString(R.string.cmd_user1));
				instantly = prefs.getBoolean(PREFSATONCE + ".1", false);
				break;
			}
			case 2: {
				imgid = prefs.getInt(PREFSIMG + ".2", R.drawable.ic_action_user2);
				cmdstr = prefs.getString(PREFSCMD + ".2",
						ctx.getString(R.string.cmd_user2));
				instantly = prefs.getBoolean(PREFSATONCE + ".2", false);
				break;
			}
			case 3: {
				imgid = prefs.getInt(PREFSIMG + ".3", R.drawable.ic_action_user3);
				cmdstr = prefs.getString(PREFSCMD + ".3",
						ctx.getString(R.string.cmd_user3));
				instantly = prefs.getBoolean(PREFSATONCE + ".3", true);
				break;
			}
			case 4: {
				imgid = prefs.getInt(PREFSIMG + ".4", R.drawable.ic_action_user4);
				cmdstr = prefs.getString(PREFSCMD + ".4",
						ctx.getString(R.string.cmd_user4));
				instantly = prefs.getBoolean(PREFSATONCE + ".4", true);
				break;
			}
			case 5: {
				imgid = prefs.getInt(PREFSIMG + ".5", R.drawable.ic_action_user5);
				cmdstr = prefs.getString(PREFSCMD + ".5",
						ctx.getString(R.string.cmd_user5));
				instantly = prefs.getBoolean(PREFSATONCE + ".5", false);
				break;
			}
			case 6: {
				imgid = prefs.getInt(PREFSIMG + ".6", R.drawable.ic_action_user6);
				cmdstr = prefs.getString(PREFSCMD + ".6",
						ctx.getString(R.string.cmd_user6));
				instantly = prefs.getBoolean(PREFSATONCE + ".6", false);
				break;
			}
			case 7: {
				imgid = prefs.getInt(PREFSIMG + ".7", R.drawable.ic_action_user7);
				cmdstr = prefs.getString(PREFSCMD + ".7",
						ctx.getString(R.string.cmd_user7));
				instantly = prefs.getBoolean(PREFSATONCE + ".7", true);
				break;
			}
			case 8: {
				imgid = prefs.getInt(PREFSIMG + ".8", R.drawable.ic_action_user8);
				cmdstr = prefs.getString(PREFSCMD + ".8",
						ctx.getString(R.string.cmd_user8));
				instantly = prefs.getBoolean(PREFSATONCE + ".8", false);
				break;
			}
			case 9: {
				imgid = prefs.getInt(PREFSIMG + ".9", R.drawable.ic_action_user9);
				cmdstr = prefs.getString(PREFSCMD + ".9",
						ctx.getString(R.string.cmd_user9));
				instantly = prefs.getBoolean(PREFSATONCE + ".9", false);
				break;
			}
			case 10: {
				imgid = prefs.getInt(PREFSIMG + ".10", R.drawable.ic_action_user10);
				cmdstr = prefs.getString(PREFSCMD + ".10",
						ctx.getString(R.string.cmd_user10));
				instantly = prefs.getBoolean(PREFSATONCE + ".10", true);
				break;
			}
			case 11: {
				imgid = prefs.getInt(PREFSIMG + ".11", R.drawable.ic_action_user11);
				cmdstr = prefs.getString(PREFSCMD + ".11",
						ctx.getString(R.string.cmd_user11));
				instantly = prefs.getBoolean(PREFSATONCE + ".11", true);
				break;
			}
			case 12: {
				imgid = prefs.getInt(PREFSIMG + ".12", R.drawable.ic_action_user12);
				cmdstr = prefs.getString(PREFSCMD + ".12",
						ctx.getString(R.string.cmd_user12));
				instantly = prefs.getBoolean(PREFSATONCE + ".12", false);
				break;
			}
			case 13: {
				imgid = prefs.getInt(PREFSIMG + ".13", R.drawable.ic_action_user13);
				cmdstr = prefs.getString(PREFSCMD + ".13",
						ctx.getString(R.string.cmd_user13));
				instantly = prefs.getBoolean(PREFSATONCE + ".13", true);
				break;
			}
			default: {
				imgid = prefs.getInt(PREFSIMG + "."+slot, R.drawable.ic_action_user0);
				cmdstr = prefs.getString(PREFSCMD + "."+slot,"???");
				instantly = prefs.getBoolean(PREFSATONCE + "."+slot, false);
			}
		}
		return new CmdIcon(slot, imgid, cmdstr, instantly);
	}

	/**
	 * Serialize this icon
	 * 
	 * @param ctx
	 *          context to get the sharedpreferences from
	 */
	public void save(Context ctx) {
		SharedPreferences prefs = ctx.getSharedPreferences(InputFragment.PREFSFILE,
				0);
		Editor editor = prefs.edit();
		editor.putString(PREFSCMD + "." + slot, cmd);
		editor.putInt(PREFSIMG + "." + slot, imgid);
		editor.putBoolean(PREFSATONCE + "." + slot, atOnce);
		editor.apply();
	}

}
