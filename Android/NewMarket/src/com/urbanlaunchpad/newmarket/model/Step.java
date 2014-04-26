package com.urbanlaunchpad.newmarket.model;

import java.io.Serializable;

// Implements Serializable so we can put it in the intent.
// Use parcelable if we need to improve performance.
public class Step implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7457601238102982304L;
//	private static final long serialVersionUID = 7457601238102982305L;
	private String step;	
	
	public Step(String step) {
		this.step = step;
	}
	
	public String getStep() {
		return step;
	}
}
