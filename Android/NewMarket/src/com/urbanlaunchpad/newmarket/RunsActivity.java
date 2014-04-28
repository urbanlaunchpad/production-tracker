package com.urbanlaunchpad.newmarket;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
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
import com.urbanlaunchpad.newmarket.model.Run;

public class RunsActivity extends FragmentActivity implements RunCreationListener {
	private static final int REQUEST_CODE_RUN = 1;
	public static final int REQUEST_PERMISSIONS = 2;
	private static final int REQUEST_ACCOUNT_PICKER = 0;

	public static final String ARG_RUNID = "Run id";
	public static final String ARG_TEXTILE = "Textile";
	public static final String ARG_RUN = "Run";

	private ArrayList<Run> runsArrayList;
	private RunsAdapter runsAdapter;
	private ListView lvRuns;

	public static String fusionTables_Log_ID = "1D51BebQDM4uvsq_Jhe1lPUeuFC3hezbttdwqrDPT";
	public static String fusionTables_Cache_ID = "1uC9y-8dd6Kk3kUCCRNtZR9oOSLFEcfGWyClSIaYl";
	public List<List<Object>> responseArray = null;
	public Fusiontables fusiontables = IniconfigActivity.fusiontables;

	// Time parsing format.
	static SimpleDateFormat uTC_SimpleDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	String textile[] = null;
	String last_step[] = null;
	Integer run[] = null;
	String runID[] = null;
	Date time_last_update_UTC[] = null;
	Integer totalRuns = null;

	Sqlresponse response = null;

	RelativeLayout loadingAnimationLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Custom color ActionBar
		ActionBar ab = getActionBar();
		ab.setBackgroundDrawable(getResources().getDrawable(
				R.color.orange_background));

		// Timezones adjusting.
		uTC_SimpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		// TODO: (subha) update to java 7 so i can use <>
		runsArrayList = new ArrayList<Run>();
		runsAdapter = new RunsAdapter(this, runsArrayList);
		lvRuns = (ListView) findViewById(R.id.lvRuns);
		lvRuns.setAdapter(runsAdapter);
		lvRuns.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(getApplicationContext(),
						StepsActivity.class);

				i.putExtra(ARG_RUNID, runID[position]);
				i.putExtra(ARG_RUN, run[position]);
				i.putExtra(ARG_TEXTILE, textile[position]);

				startActivity(i);
			}
		});
		loadingAnimationLayout = (RelativeLayout) findViewById(R.id.loadingPanel);
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
				loadingAnimationLayout.setVisibility(View.VISIBLE);
				Run run = (Run) data.getSerializableExtra("run");
				uploadNewRun(run);
			}
		}
	}
	
	public void onRunCreated(Run run) {
		uploadNewRun(run);
	}
	
	private void uploadNewRun(Run run) {
		String runIDString = "NM" + createRunID();
		uploadNewRunToLog(run, fusionTables_Log_ID, runIDString);
		uploadNewRunToCache(run, fusionTables_Cache_ID, runIDString);
	}

	private void uploadNewRunToLog(final Run run, final String fusionTables_ID,
			final String runID) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				if (run == null) {
					return false;
				}

				try {
					String query = "INSERT INTO " + fusionTables_ID
							+ " (run,step,textile,runID,start_time_UTC)"
							+ " VALUES ('" + run.getRun() + "','"
							+ run.getStep() + "','" + run.getTextile() + "','"
							+ runID + "','"
							+ uTC_SimpleDateFormat.format(run.getTime())
							+ "');";
					Sql sql = fusiontables.query().sql(query);
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
				if (fusionTables_ID.equals(fusionTables_Cache_ID)) {
				}
				if (success) {
				} else {
					Log.v("Fusion Tables", "Couldn't upload to Fusion Tables");
				}
			}

		}.execute();

	}

	private void uploadNewRunToCache(final Run run,
			final String fusionTables_ID, final String runID) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				if (run == null) {
					return false;
				}

				try {
					String query = "INSERT INTO " + fusionTables_ID
							+ " (run,step,textile,runID,time_last_update_UTC)"
							+ " VALUES ('" + run.getRun() + "','"
							+ run.getStep() + "','" + run.getTextile() + "','"
							+ runID + "','"
							+ uTC_SimpleDateFormat.format(run.getTime())
							+ "');";
					Sql sql = fusiontables.query().sql(query);
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
				if (fusionTables_ID.equals(fusionTables_Cache_ID)) {
					getRunInfo();
				}
				if (success) {
				} else {
					Log.v("Fusion Tables", "Couldn't upload to Fusion Tables");
				}
			}

		}.execute();

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_add:
			launchAddRunView();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void launchAddRunView() {
		// The commented out code will launch a run activity
		/*Intent i = new Intent(getApplicationContext(), RunActivity.class);
		startActivityForResult(i, REQUEST_CODE_RUN);*/
		
	    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
	    Fragment prev = getSupportFragmentManager().findFragmentByTag(RunDialogFragment.getTagName());
	    if (prev != null) {
	        ft.remove(prev);
	    }
	    ft.addToBackStack(null);

	    // Create and show the dialog.
	    RunDialogFragment newFragment = RunDialogFragment.newInstance();
	    newFragment.setRunCreationListener(this);
	    newFragment.show(ft, RunDialogFragment.getTagName());
	    getSupportFragmentManager().executePendingTransactions();
	    
	    newFragment.sizeDialog();
	    
	    // TODO (subha) : the title should be colored
	    newFragment.setDialogTitle("Add a new run.");	    
	}
	


	public boolean getRunsCache() throws UserRecoverableAuthIOException,
			IOException {
		String query = "SELECT run, textile, step, runID, time_last_update_UTC FROM "
				+ fusionTables_Cache_ID + " ORDER BY time_last_update_UTC";
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
					return getRunsCache();
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
						runID = new String[totalRuns];
						time_last_update_UTC = new Date[totalRuns];

						JSONObject submission = new JSONObject();
						for (int i = 0; i < totalRuns; i++) {
							run[i] = Integer.parseInt((String) responseArray
									.get(i).get(0));
							textile[i] = (String) responseArray.get(i).get(1);
							last_step[i] = (String) responseArray.get(i).get(2);
							runID[i] = (String) responseArray.get(i).get(3);
							// Parsing time
							try {
								time_last_update_UTC[i] = uTC_SimpleDateFormat
										.parse((String) responseArray.get(i)
												.get(4));
							} catch (ParseException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Log.v("Time",
							// uTC_SimpleDateFormat.format(time_last_update_UTC[i]));

						}
						populateListView(totalRuns, run, textile, last_step);
						loadingAnimationLayout.setVisibility(View.GONE);
					}
				} else {
					Log.v("Fusion Tables", "Didn't get the table");
				}
			}

		}.execute();
	}

	private void populateListView(Integer totalRuns, Integer[] runs,
			String[] textiles, String[] last_steps) {
		runsAdapter.clear();
		for (int i = 0; i < totalRuns; i++) {
			Run tempRun = new Run(textiles[i].toLowerCase(Locale.ENGLISH),
					runs[i], last_steps[i], time_last_update_UTC[i]);
			runsAdapter.add(tempRun);
		}

	}

	public String createRunID() {
		String ID = null;
		Integer randy;
		for (int i = 0; i < 10; ++i) {
			randy = (int) (Math.random() * ((9) + 1));
			if (i == 0) {
				ID = randy.toString();
			} else {
				ID = ID + randy.toString();
			}
		}
		return ID;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadingAnimationLayout.setVisibility(View.VISIBLE);
		getRunInfo();
	}

}
