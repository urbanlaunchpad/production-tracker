package com.urbanlaunchpad.newmarket;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

import com.urbanlaunchpad.newmarket.model.Run;

public class StepsActivity extends Activity {
	private Run run;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_steps);
		
		this.run = (Run) getIntent().getSerializableExtra("run");
		//Toast.makeText(getApplicationContext(), "run textile is " + run.getTextile(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.steps, menu);
		return true;
	}

}