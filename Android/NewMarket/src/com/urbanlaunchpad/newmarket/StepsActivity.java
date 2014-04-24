package com.urbanlaunchpad.newmarket;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.model.Sqlresponse;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;

public class StepsActivity extends Activity {
	public static final int REQUEST_PERMISSIONS = 2;
	
	private String runID;
	private RelativeLayout loadingAnimationLayout;
	private List<List<Object>> responseArray;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_steps);
		runID = (String) this.getIntent()
				.getStringExtra(RunsActivity.ARG_RUNID);
		loadingAnimationLayout = (RelativeLayout) findViewById(R.id.loadingPanel);
		Log.v("StepsActivity", "RunID from intent: " + runID);
		getSteps();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.steps, menu);
		return true;
	}

	public boolean getRunStepsFromLog() throws UserRecoverableAuthIOException,
			IOException {
		String query = "SELECT run, textile, step, runID FROM "
				+ RunsActivity.fusionTables_Log_ID + " WHERE runID = '" + runID + "'";
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
					// TODO DP If can't get updated version, use cached survey
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
//						totalRuns = responseArray.size();
//						run = new Integer[totalRuns];
//						textile = new String[totalRuns];
//						last_step = new String[totalRuns];
//						runID = new String[totalRuns];
//						for (int i = 0; i < totalRuns; i++) {
//							run[i] = Integer.parseInt((String) responseArray
//									.get(i).get(0));
//							textile[i] = (String) responseArray.get(i).get(1);
//							last_step[i] = (String) responseArray.get(i).get(2);
//							runID[i] = (String) responseArray.get(i).get(3);
//						}
//						populateListView(totalRuns, run, textile, last_step);
//						loadingAnimationLayout.setVisibility(View.GONE);
					}
				} else {
					Log.v("Fusion Tables", "Didn't get the table");
				}
			}

		}.execute();
	}

}