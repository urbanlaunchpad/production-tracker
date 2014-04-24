package com.urbanlaunchpad.newmarket.model;

public class StepsClient {
	private static final String PLACEHOLDER_START_ACTIVITY = "placeholder start";
	private static StepsClient client;
	
	private StepsClient() {
		// do initialization here.
	}
	
	public static StepsClient getInstance() {
		if (client == null) {
			client = new StepsClient();
		}
		
		return client;
	}
	
	public String getStart() {
		return PLACEHOLDER_START_ACTIVITY;
	}
}
