package de.onyxbits.textfiction;


import android.text.SpannableString;

/**
 * A story element to be shown in the story fragment.
 * 
 * @author patrick
 * 
 */
class StoryItem {

	/**
	 * A message from the narrator
	 */
	public static final int NARRATOR = 1;

	/**
	 * A message from the player.
	 */
	public static final int MYSELF = 2;
	
	/**
	 * A "flash" popup
	 */
	public static final int FLASH = 3;

	/**
	 * What to show to the user
	 */
	public SpannableString message;

	/**
	 * Is this a message from the player or the narrator?
	 */
	protected int type;

	public StoryItem(SpannableString message, int type) {
		this.message = message;
		this.type = type;
	}
}
