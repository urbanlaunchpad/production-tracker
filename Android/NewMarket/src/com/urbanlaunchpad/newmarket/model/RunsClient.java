package com.urbanlaunchpad.newmarket.model;

import java.util.ArrayList;
import java.util.List;

public class RunsClient {
	private static RunsClient client;
	
	private RunsClient() {
		// do initialization here.
	}
	
	public static RunsClient getInstance() {
		if (client == null) {
			client = new RunsClient ();
		}
		
		return client;
	}
	
	public List<String> getTextileOptions() {
		ArrayList<String> textiles = new ArrayList<String>();
		textiles.add("Kotwali");
		textiles.add("Mirpur");
		textiles.add("Dhanmondi");
		textiles.add("Uttara");
		return textiles;
	}
}