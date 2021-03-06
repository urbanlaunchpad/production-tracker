package com.urbanlaunchpad.newmarket.model;

import java.io.Serializable;
import java.util.Date;

// Implements Serializable so we can put it in the intent.
// Use parcelable if we need to improve performance.
public class Run implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7457601238102982305L;
	private String textile;
	private int run;
	private String step;	
	private Date time_last_update_UTC;
	
	public Run(String textile, int run, String step, Date time_last_update_UTC) {
		this.textile = textile;
		this.run = run;
		this.step = step;
		this.time_last_update_UTC = time_last_update_UTC;
	}

	public String getTextile() {
		return textile;
	}
	
	public int getRun() {
		return run;
	}
	
	public String getStep() {
		return step;
	}

	public Date getTime() {
		return time_last_update_UTC;
	}
}
