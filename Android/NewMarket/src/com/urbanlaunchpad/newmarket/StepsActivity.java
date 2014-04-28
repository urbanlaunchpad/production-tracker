package com.urbanlaunchpad.newmarket;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.ActionBar;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.urbanlaunchpad.newmarket.model.Step;

public class StepsActivity extends FragmentActivity implements
		StepCreationListener {
	private static final int REQUEST_CODE_STEP = 1;
	public static final int REQUEST_PERMISSIONS = 2;
	private static final int REQUEST_ACCOUNT_PICKER = 0;

	private String runID;
	private RelativeLayout loadingAnimationLayout;
	private List<List<Object>> responseArray;

	protected int totalSteps;

	private ArrayList<Step> steps;
	private StepsAdapter stepsAdapter;
	private ListView lvSteps;
	private String textile;
	private Integer run;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_steps);

		// Custom color ActionBar
		ActionBar ab = getActionBar();
		ab.setBackgroundDrawable(getResources().getDrawable(
				R.color.orange_background));

		runID = (String) this.getIntent()
				.getStringExtra(RunsActivity.ARG_RUNID);
		textile = this.getIntent().getStringExtra(RunsActivity.ARG_TEXTILE);
		run = this.getIntent().getIntExtra(RunsActivity.ARG_RUN, 0);
		Log.v("StepsActivity", "RunID from intent: " + runID);

		steps = new ArrayList<Step>();
		stepsAdapter = new StepsAdapter(this, steps);
		lvSteps = (ListView) findViewById(R.id.lvSteps);
		lvSteps.setAdapter(stepsAdapter);
		lvSteps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.v("Step", "" + position);
			}
		});

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
			launchAddStepView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void launchAddStepView() {
		/*
		 * Intent i = new Intent(getApplicationContext(),
		 * NewStepActivity.class); startActivityForResult(i, REQUEST_CODE_STEP);
		 */

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment prev = getSupportFragmentManager().findFragmentByTag(
				RunDialogFragment.getTagName());
		if (prev != null) {
			ft.remove(prev);
		}
		ft.addToBackStack(null);

		// Create and show the dialog.
		StepDialogFragment newFragment = StepDialogFragment.newInstance();
		newFragment.setStepCreationListener(this);
		newFragment.show(ft, RunDialogFragment.getTagName());
		getSupportFragmentManager().executePendingTransactions();

		newFragment.sizeDialog();

		// TODO (subha) : the title should be colored
		newFragment.setDialogTitle("Add a new step.");
	}

	public boolean getRunStepsFromLog() throws UserRecoverableAuthIOException,
			IOException {
		String query = "SELECT step, start_time_UTC FROM "
				+ RunsActivity.fusionTables_Log_ID + " WHERE runID = '" + runID
				+ "' ORDER BY start_time_UTC";
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
		Log.v("getRunStepsFromLog response", responseArray.toString());
		return true;
	}

	public void getSteps() {
		// get and parse table
		new AsyncTask<Void, Void, Boolean>() {
			private String[] steps;
			private Date[] start_time_UTC;

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
						start_time_UTC = new Date[totalSteps];
						for (int i = 0; i < totalSteps; i++) {
							steps[i] = (String) responseArray.get(i).get(0);
							try {
								start_time_UTC[i] = RunsActivity.uTC_SimpleDateFormat
										.parse((String) responseArray.get(i)
												.get(1));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						populateListView(totalSteps, steps, start_time_UTC);
						loadingAnimationLayout.setVisibility(View.GONE);
					}
				} else {
					Log.v("Fusion Tables", "Didn't get the table");
				}
			}

		}.execute();
	}

	private void populateListView(int totalSteps, String[] steps,
			Date[] start_time_UTC) {
		stepsAdapter.clear();
		for (int i = 0; i < totalSteps; i++) {
			Step tempStep = new Step(steps[i], start_time_UTC[i]);
			stepsAdapter.add(tempStep);
			loadingAnimationLayout.setVisibility(View.GONE);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_PERMISSIONS:
			if (resultCode == RESULT_OK) {
				getSteps();
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
		case REQUEST_CODE_STEP:
			if (resultCode == RESULT_OK) {
				loadingAnimationLayout.setVisibility(View.VISIBLE);
				Step step = (Step) data.getSerializableExtra("step");
				onStepCreated(step);
			}
		}
	}

	public void onStepCreated(Step step) {
		uploadNewStep(step);
		getSteps();
	}

	private void uploadNewStep(Step step) {
		uploadNewStepOnLog(step, RunsActivity.fusionTables_Log_ID);
		uploadNewStepOnCache(step, RunsActivity.fusionTables_Cache_ID, runID);
	}

	private void uploadNewStepOnLog(final Step step,
			final String fusionTables_ID) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				if (step == null) {
					return false;
				}

				try {
					String query = "INSERT INTO "
							+ fusionTables_ID
							+ " (run,step,textile,runID,start_time_UTC)"
							+ " VALUES ('"
							+ run
							+ "','"
							+ step.getStep()
							+ "','"
							+ textile
							+ "','"
							+ runID
							+ "','"
							+ RunsActivity.uTC_SimpleDateFormat.format(step
									.getStart_time_UTCstart_time_UTC()) + "');";
					Sql sql = IniconfigActivity.fusiontables.query().sql(query);
					sql.setKey(IniconfigActivity.API_KEY);
					Sqlresponse response = sql.execute();
					if (response == null || response.getRows() == null) {
						return false;
					}
					Log.v("response", response.toString());
					return true;
				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_PERMISSIONS);
					Log.e("Fusion Tables error",
							"UserRecoverableAuthIOException " + e.toString());
					return false;
				} catch (IOException e) {
					// TODO DP If can't get updated version, use cached survey
					Log.e("Fusion Tables error", "IOException " + e.toString());
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				super.onPostExecute(success);
				if (success) {
				} else {
					Log.v("Fusion Tables", "Couldn't upload to Fusion Tables");
				}
			}

		}.execute();

	}

	private void uploadNewStepOnCache(final Step step,
			final String fusionTables_ID, String runID2) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				if (step == null) {
					return false;
				}

				try {
					String query = "SELECT ROWID FROM " + fusionTables_ID
							+ " WHERE runID = '" + runID + "'";
					Sql ROWIDsql = IniconfigActivity.fusiontables.query().sql(
							query);
					ROWIDsql.setKey(IniconfigActivity.API_KEY);
					Sqlresponse ROWIDresponse = ROWIDsql.execute();
					if (ROWIDresponse == null
							|| ROWIDresponse.getRows() == null) {
						return false;
					} else {
						Log.v("Getting ROWID response",
								ROWIDresponse.toString());
						// TODO code assumes there's only one row with searched
						// id on the table, this element should solve cases of
						// repeated elements.
						List<List<Object>> ROWIDresponseArray = ROWIDresponse
								.getRows();
						Integer ROWID = Integer
								.parseInt((String) ROWIDresponseArray.get(0)
										.get(0));
						String UPDATEquery = "UPDATE "
								+ fusionTables_ID
								+ " SET step = '"
								+ step.getStep()
								+ "', time_last_update_UTC = '"
								+ RunsActivity.uTC_SimpleDateFormat.format(step
										.getStart_time_UTCstart_time_UTC())
								+ "' WHERE ROWID = '" + ROWID + "'";
						Sql UPDATEsql = IniconfigActivity.fusiontables.query()
								.sql(UPDATEquery);
						UPDATEsql.setKey(IniconfigActivity.API_KEY);
						Sqlresponse response = UPDATEsql.execute();
						if (response == null || response.getRows() == null) {
							return false;
						}
						Log.v("Update response", response.toString());
						return true;

					}

				} catch (UserRecoverableAuthIOException e) {
					startActivityForResult(e.getIntent(), REQUEST_PERMISSIONS);
					Log.e("Fusion Tables error",
							"UserRecoverableAuthIOException " + e.toString());
					return false;
				} catch (IOException e) {
					// TODO DP If can't get updated version, use cached survey
					Log.e("Fusion Tables error", "IOException " + e.toString());
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				super.onPostExecute(success);
				if (success) {
					getSteps();
				} else {
					Log.v("Fusion Tables", "Couldn't upload to Fusion Tables");
				}
			}

		}.execute();

	}

}
