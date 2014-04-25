package com.urbanlaunchpad.newmarket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.urbanlaunchpad.newmarket.model.Run;
import com.urbanlaunchpad.newmarket.model.StepsClient;

public class NewStepActivity extends Activity {
	private static final String PLACEHOLDER_START_ACTIVITY = "Placeholder Start"; // replace this with the actual start activity later.

	private EditText etTextileName;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run);
		
		etTextileName = (EditText) findViewById(R.id.etTextileName);
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
	public void saveRun(View view) {
		String startActivity = StepsClient.getInstance().getStart();
		String textile = etTextileName.getText().toString();
		Run run = new Run(textile, 1, startActivity);
		Intent data = new Intent();
		data.putExtra("run", run);
		setResult(RESULT_OK, data);
		finish();
	}
}
