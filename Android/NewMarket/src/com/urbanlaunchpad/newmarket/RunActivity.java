package com.urbanlaunchpad.newmarket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

import com.urbanlaunchpad.newmarket.model.Run;

public class RunActivity extends Activity {
	private EditText etRunName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run);
		
		etRunName = (EditText) findViewById(R.id.etRunName);
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
		Run run = new Run();
		run.setName(etRunName.getText().toString());
		Intent data = new Intent();
		data.putExtra("run", run);
		setResult(RESULT_OK, data);
		finish();
	}
}
