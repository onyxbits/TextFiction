package de.onyxbits.textfiction.input;

import java.io.File;

/**
 * Master callback interface of the package.
 * 
 * @author patrick
 * 
 */
public interface InputProcessor {

	/**
	 * Make the game engine process user input. Calling this method while the
	 * engine is not idle results in a no-op.
	 * 
	 * @param inputBuffer
	 *          user input to hand over to the engine
	 */
	public void executeCommand(char[] inputBuffer);
	
	/**
	 * Turn highlighting of text in the story on/off.
	 * @param txt the "word" to toggle highlighting for.
	 */
	public void toggleTextHighlight(String txt);
	
	/**
	 * Say something through TTS. This method may be called while utterance is
	 * still going on, in which case the current utterance should be replaced.
	 * @param txt the text to synthesize or null to stop speaking.
	 */
	public void utterText(CharSequence txt);

	
	/**
	 * Query the game that is being played.
	 * @return path to the currently playing game
	 */
	public File getStory();
		
}
