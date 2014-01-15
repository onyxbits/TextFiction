package de.onyxbits.textfiction.input;

import org.json.JSONException;
import org.json.JSONObject;

import de.onyxbits.textfiction.R;

/**
 * Container class that stores the info for quick access command buttons.
 * 
 * @author patrick
 * 
 */
public class CmdIcon {


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
			R.drawable.ic_action_user19,
			R.drawable.ic_action_user20,
			R.drawable.ic_action_user21,
			R.drawable.ic_action_user22,
			R.drawable.ic_action_user23,
			R.drawable.ic_action_user24,
			R.drawable.ic_action_user25,
			R.drawable.ic_action_user26,
			R.drawable.ic_action_user27,
			R.drawable.ic_action_user28,
			R.drawable.ic_action_user29,
			R.drawable.ic_action_user30,
			R.drawable.ic_action_user31,
	};

	/**
	 * the (user) command (string) associated with the button.
	 */
	public String cmd;

	/**
	 * Whether or not the command should be executed immediately on the button
	 * press.
	 */
	public boolean atOnce;

	/**
	 * ID number of the drawable (for saving in the preferences storage)
	 */
	public int imgid;

	public CmdIcon(int imgid, String cmd, boolean atOnce) {
		this.imgid = imgid;
		this.cmd = cmd;
		this.atOnce = atOnce;
	}

	public static JSONObject toJSON(CmdIcon ico) {
		JSONObject ret = new JSONObject();
		try {
			ret.put("imgid", ico.imgid);
			ret.put("cmd", ico.cmd);
			ret.put("atonce", ico.atOnce);
		}
		catch (JSONException e) {
			// Can't really see this happening
			throw new RuntimeException(e);
		}
		return ret;
	}

	public static CmdIcon fromJSON(JSONObject in) {
		int imgid = in.optInt("imgid", 0);
		if (imgid > ICONS.length - 1) {
			imgid = 0;
		}
		String cmd = in.optString("cmd", "???");
		boolean atOnce = in.optBoolean("atonce", false);
		return new CmdIcon(imgid, cmd, atOnce);
	}

}
