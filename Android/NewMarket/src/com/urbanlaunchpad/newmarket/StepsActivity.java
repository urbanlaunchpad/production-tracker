package com.urbanlaunchpad.newmarket;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class StepsActivity extends Activity {
	private String runID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_steps);
		
		runID = (String) this.getIntent().getStringExtra(RunsActivity.ARG_RUNID);
		Log.v("StepsActivity", "RunID from intent: " + runID);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.steps, menu);
		return true;
	}

}