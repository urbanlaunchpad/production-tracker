package com.urbanlaunchpad.newmarket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.urbanlaunchpad.newmarket.model.Run;

public class RunsActivity extends Activity {
	private static final int REQUEST_CODE_RUN = 1;
	public static final int REQUEST_PERMISSIONS = 2;
	private static final int REQUEST_ACCOUNT_PICKER = 0;
	private ArrayList<Run> runs;
	private RunsAdapter runsAdapter;
	private ListView lvRuns;

	public String fusionTables_Log_ID = "1D51BebQDM4uvsq_Jhe1lPUeuFC3hezbttdwqrDPT";
	public String fusionTables_Cache_ID = "1uC9y-8dd6Kk3kUCCRNtZR9oOSLFEcfGWyClSIaYl";
	public List<List<Object>> responseArray = null;
	public Fusiontables fusiontables = IniconfigActivity.fusiontables;

	String textile[] = null;
	String last_step[] = null;
	Integer run[] = null;
	Integer totalRuns = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// TODO: (subha) update to java 7 so i can use <>
		runs = new ArrayList<Run>();
		runsAdapter = new RunsAdapter(this, runs);
		lvRuns = (ListView) findViewById(R.id.lvRuns);
		lvRuns.setAdapter(runsAdapter);

		getRunInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_PERMISSIONS:
			if (resultCode == RESULT_OK) {
				getRunInfo();
			} else {
				startActivityForResult(
						IniconfigActivity.credential.newChooseAccountIntent(),
						REQUEST_ACCOUNT_PICKER);
				IniconfigActivity.fusiontables = new Fusiontables.Builder(
						IniconfigActivity.HTTP_TRANSPORT,
						IniconfigActivity.JSON_FACTORY,
						IniconfigActivity.credential).setApplicationName(
						"NewMarket").build();
			}
		case REQUEST_CODE_RUN:
			if (resultCode == RESULT_OK) {
				Run shirt = (Run) data.getSerializableExtra("run");
				runsAdapter.add(shirt);
			}
		}
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			launchRunView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void launchRunView() {
		Intent i = new Intent(getApplicationContext(), RunActivity.class);
		startActivityForResult(i, REQUEST_CODE_RUN);
	}

	public boolean getRunsCache() throws UserRecoverableAuthIOException,
			IOException {
		String query = "SELECT run, textile, last_step FROM "
				+ fusionTables_Cache_ID;
		Sql sql = fusiontables.query().sql(query);
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

	public void getRunInfo() {
		// get and parse table
		new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {
				try {
					getRunsCache();
					return true;
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
						totalRuns = responseArray.size();
						run = new Integer[totalRuns];
						textile = new String[totalRuns];
						last_step = new String[totalRuns];
						for (int i = 0; i < totalRuns; i++) {
							run[i] = Integer.parseInt((String) responseArray.get(i).get(
									0));
							textile[i] = (String) responseArray.get(i).get(1);
							last_step[i] = (String) responseArray.get(i).get(2);
						}
						populateListView(totalRuns, run, textile, last_step);
						RelativeLayout loadingAnimationLayout = (RelativeLayout) findViewById(R.id.loadingPanel);
						loadingAnimationLayout.setVisibility(View.GONE);
					}
				} else {
					Log.v("Fusion Tables", "Didn't get the table");
				}
			}

			private void populateListView(Integer totalRuns, Integer[] runs,
					String[] textiles, String[] last_steps) {
				for (int i = 0; i < totalRuns; i++) {
					Run tempRun = new Run(textiles[i].toLowerCase(), runs[i], last_steps[i]);
					runsAdapter.add(tempRun);
				}

			}
		}.execute();
	}

}
