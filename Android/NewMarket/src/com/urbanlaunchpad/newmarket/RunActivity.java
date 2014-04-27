package com.urbanlaunchpad.newmarket;

import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.urbanlaunchpad.newmarket.model.Run;
import com.urbanlaunchpad.newmarket.model.RunsClient;
import com.urbanlaunchpad.newmarket.model.StepsClient;

public class RunActivity extends Activity {
	private Spinner spTextile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_run);

		// Custom color ActionBar
		ActionBar ab = getActionBar();
		ab.setBackgroundDrawable(getResources().getDrawable(
				R.color.orange_background));

		spTextile = (Spinner) findViewById(R.id.spTextile);
		List<String> textileOptions = RunsClient.getInstance()
				.getTextileOptions();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, textileOptions);
		spTextile.setAdapter(adapter);
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
		String textile = spTextile.getSelectedItem().toString();
		Date time_last_update_UTC = new Date();
		Run run = new Run(textile, 1, startActivity, time_last_update_UTC);
		Intent data = new Intent();
		data.putExtra("run", run);
		setResult(RESULT_OK, data);
		finish();
	}
}
