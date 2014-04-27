package com.urbanlaunchpad.newmarket;

import java.util.Date;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.urbanlaunchpad.newmarket.model.Step;

public class NewStepActivity extends Activity {

	private EditText etStepName;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_step);
		
		// Custom color ActionBar
		ActionBar ab = getActionBar();  
        ab.setBackgroundDrawable(getResources().getDrawable(R.color.orange_background));
		
		etStepName = (EditText) findViewById(R.id.etStepName);
 	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.run, menu);
		return true;
	}

	
	/** 
	 * Called when a user chooses the 'save' button.
	 * */
	public void saveStep(View view) {
		String step = etStepName.getText().toString();
		Date start_time_UTC = new Date();
		Step stepStep = new Step(step, start_time_UTC);
		Intent data = new Intent();
		data.putExtra("step", stepStep);
		setResult(RESULT_OK, data);
		finish();
	}
}
