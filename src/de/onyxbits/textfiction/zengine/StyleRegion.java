package de.onyxbits.textfiction.zengine;

/**
 * A simple datacontainer for applying textstyles (bold, italic, etc) to
 * text printed in a window
 * @author patrick
 *
 */
public class StyleRegion {

	public int style= ZWindow.ROMAN;
	public int start=0;
	public int end=0;
	
	public StyleRegion next;
	
	public StyleRegion() {
		// TODO Auto-generated constructor stub
	}

}
