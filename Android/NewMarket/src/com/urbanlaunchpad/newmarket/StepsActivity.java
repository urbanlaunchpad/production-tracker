package com.urbanlaunchpad.newmarket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.urbanlaunchpad.newmarket.model.Run;
import com.urbanlaunchpad.newmarket.model.Step;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;

public class StepsActivity extends Activity {
	private static final int REQUEST_CODE_STEP = 1;
	public static final int REQUEST_PERMISSIONS = 2;

	private String runID;
	private RelativeLayout loadingAnimationLayout;
	private List<List<Object>> responseArray;

	protected int totalSteps;

	private ArrayList<Step> steps;
	private StepsAdapter stepsAdapter;
	private ListView lvSteps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_steps);
		runID = (String) this.getIntent()
				.getStringExtra(RunsActivity.ARG_RUNID);
		Log.v("StepsActivity", "RunID from intent: " + runID);

		steps = new ArrayList<Step>();
		stepsAdapter = new StepsAdapter(this, steps);
		lvSteps = (ListView) findViewById(R.id.lvSteps);
		lvSteps.setAdapter(stepsAdapter);

		loadingAnimationLayout = (RelativeLayout) findViewById(R.id.loadingPanel);

		getSteps();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.steps, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			launchStepView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void launchStepView() {
		Intent i = new Intent(getApplicationContext(), NewStepActivity.class);
		startActivityForResult(i, REQUEST_CODE_STEP);
	}

	public boolean getRunStepsFromLog() throws UserRecoverableAuthIOException,
			IOException {
		String query = "SELECT step FROM " + RunsActivity.fusionTables_Log_ID
				+ " WHERE runID = '" + runID + "'";
		Sql sql = IniconfigActivity.fusiontables.query().sql(query);
		sql.setKey(IniconfigActivity.API_KEY);
		Sqlresponse response = sql.execute();
		if (response == null || response.getRows() == null) {
			return false;
		}
		responseArray = response.getRows();
		// TODO DP save this for offline use
		// prefs.edit().putString("jsonSurveyString",
		// jsonSurveyString).commit();
		Log.v("response", responseArray.toString());
		return true;
	}

	public void getSteps() {
		// get and parse table
		new AsyncTask<Void, Void, Boolean>() {
			private String[] steps;

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					return getRunStepsFromLog();
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_PERMISSIONS);
					Log.e("Fusion Tables error",
							"UserRecoverableAuthIOException " + e.toString());
					return false;
				} catch (IOException e) {
					// TODO DP If can't get updated version, use cached steps
					// if
					// (ProjectConfig.get().getProjectName().equals(prefs.getString("lastProject",
					// ""))) {
					// jsonSurveyString = prefs.getString("jsonSurveyString",
					// "");
					// } else {
					// e.printStackTrace();
					// }
					Log.e("Fusion Tables error", "IOException " + e.toString());
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				super.onPostExecute(success);
				if (success) {
					if (responseArray == null) {
						Log.v("Fusion Tables", "Table empty");
					} else {
						totalSteps = responseArray.size();
						steps = new String[totalSteps];
						for (int i = 0; i < totalSteps; i++) {
							steps[i] = (String) responseArray.get(i).get(0);
						}
						populateListView(totalSteps, steps);
						loadingAnimationLayout.setVisibility(View.GONE);
					}
				} else {
					Log.v("Fusion Tables", "Didn't get the table");
				}
			}

			private void populateListView(int totalSteps, String[] steps) {
				for (int i = 0; i < totalSteps; i++) {
					Step tempStep = new Step(steps[i]);
					stepsAdapter.add(tempStep);
				}

			}

		}.execute();
	}

}