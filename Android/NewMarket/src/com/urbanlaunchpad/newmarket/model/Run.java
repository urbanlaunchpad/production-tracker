package com.urbanlaunchpad.newmarket.model;

import java.io.Serializable;

// Implements Serializable so we can put it in the intent.
// Use parcelable if we need to improve performance.
public class Run implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7457601238102982305L;
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
