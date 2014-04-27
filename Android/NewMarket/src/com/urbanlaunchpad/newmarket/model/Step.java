package com.urbanlaunchpad.newmarket.model;

import java.io.Serializable;
import java.util.Date;

// Implements Serializable so we can put it in the intent.
// Use parcelable if we need to improve performance.
public class Step implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7457601238102982304L;
	private String step;
	private Date start_time_UTCstart_time_UTC;
	
	public Step(String step, Date start_time_UTCstart_time_UTC) {
		this.step = step;
		this.start_time_UTCstart_time_UTC = start_time_UTCstart_time_UTC;
	}
	
	public String getStep() {
		return step;
	}
	
	public Date getStart_time_UTCstart_time_UTC(){
		return start_time_UTCstart_time_UTC;
	}
}