package com.urbanlaunchpad.newmarket;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.api.services.fusiontables.model.Sqlresponse;
import com.urbanlaunchpad.newmarket.model.Run;

public class RunsActivity extends Activity {
	private static int REQUEST_CODE_RUN = 1;

	private ArrayList<Run> runs;
	private RunsAdapter runsAdapter;
	private ListView lvRuns;

	public String fusionTables_Log_ID = "1D51BebQDM4uvsq_Jhe1lPUeuFC3hezbttdwqrDPT";
	public String fusionTables_Cache_ID = "1uC9y-8dd6Kk3kUCCRNtZR9oOSLFEcfGWyClSIaYl";
	public String responseString = null;
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private static final JsonFactory JSON_FACTORY = new JacksonFactory();
	public static Fusiontables fusiontables;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// TODO: (subha) update to java 7 so i can use <>
		runs = new ArrayList<Run>();
		runsAdapter = new RunsAdapter(this, runs);
		lvRuns = (ListView) findViewById(R.id.lvRuns);
		lvRuns.setAdapter(runsAdapter);


		fusiontables = new Fusiontables.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				IniconfigActivity.credential).setApplicationName("NewMarket").build();
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
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_RUN) {
			Run shirt = (Run) data.getSerializableExtra("run");
			runsAdapter.add(shirt);
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
		String query = "GET https://www.googleapis.com/fusiontables/v1/tables/"
				+ fusionTables_Cache_ID; // + "?key=" + IniconfigActivity.API_KEY;
		Sql sql = fusiontables.query().sql(query);
//		 Sql sql = fusiontables.query().sql(
//		 "SELECT run FROM " + fusionTables_Cache_ID
//		 + " WHERE table_id = '" + tableId + "'"
//		 );
		sql.setKey(IniconfigActivity.API_KEY);
		Sqlresponse response = sql.execute();
		if (response == null || response.getRows() == null) {
			return false;
		}
		responseString = response.getRows().get(0).get(0).toString();

		// TODO DP save this for offline use
		// prefs.edit().putString("jsonSurveyString",
		// jsonSurveyString).commit();
		Log.v("response", responseString);
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
					// startActivityForResult(e.getIntent(),
					// REQUEST_PERMISSIONS);
					Log.e("Fusion Tables error",
							"UserRecoverableAuthIOException "
									+ e.toString());
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
					Log.e("Fusion Tables error",
							"IOException "
									+ e.toString());
					return false;
				}
			}

			@Override
			protected void onPostExecute(Boolean success) {
				super.onPostExecute(success);
				if (success) {
					try {
						if (responseString == null) {
							throw new JSONException("Didn't get the table");
						}
						// Try to parse
						new JSONObject(responseString);
					} catch (JSONException e) {
						Log.e("JSON Parser",
								"Error parsing data of cache Table"
										+ e.toString());
					}
				}
			}
		}.execute();
	}

}
