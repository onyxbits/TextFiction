package de.onyxbits.textfiction.zengine;

public class IFFChunkNotFoundException extends Exception {
	/**
	 * Constructs an IFFChunkNotFoundException with no detail message. A detail
	 * message is a String that describes this particular exception.
	 */
	public IFFChunkNotFoundException() {
		super();
	}

	/**
	 * Constructs an IFFChunkNotFoundException with the specified detail message.
	 * A detail message is a String that describes this particular exception.
	 * 
	 * @param s
	 *          the detail message
	 */
	public IFFChunkNotFoundException(String s) {
		super(s);
	}

}
