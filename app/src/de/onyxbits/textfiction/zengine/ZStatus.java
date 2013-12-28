/* Zplet, a Z-Machine interpreter in Java */
/* Copyright 1996,2001 Matthew T. Russotto */
/* As of 23 February 2001, this code is open source and covered by the */
/* Artistic License, found within this package */

package de.onyxbits.textfiction.zengine;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ZStatus {
	public boolean timegame;
	public String location;
	public int score;
	public int turns;
	public int hours;
	public int minutes;
	
	private StringBuilder buffer;

	public ZStatus() {
		buffer = new StringBuilder();
	}

	public void update_score_line(String location, int score, int turns) {
		this.timegame = false;
		this.location = location;
		this.score = score;
		this.turns = turns;
	}

	public void update_time_line(String location, int hours, int minutes) {
		this.timegame = true;
		this.location = location;
		this.hours = hours;
		this.minutes = minutes;
	}
	
	public String toString() {
		// FIXME: Yes, this should be properly formated and localized, but since
		// there are only a handful Z3 games, this is very low priority.
		buffer.setLength(0);
		buffer.append(location);
		buffer.append("                ");
		if (timegame) {
			if (hours<10) {
				buffer.append("0");
			}
			buffer.append(hours);
			buffer.append(":");
			buffer.append(minutes);
		}
		else {
			buffer.append("Score: ");
			buffer.append(score);
			buffer.append(" Turn: ");
			buffer.append(turns);
		}
		
		return buffer.toString();
	}

}
