package com.urbanlaunchpad.newmarket;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.urbanlaunchpad.newmarket.model.Run;

public class MainActivity extends Activity {
	private static int REQUEST_CODE_RUN = 1;
	
	private ArrayList<Run> runs;
	private RunsAdapter runsAdapter;
	private ListView lvRuns;
	
	public String fusionTables_Log_ID = "1D51BebQDM4uvsq_Jhe1lPUeuFC3hezbttdwqrDPT";
	public String fustionTables_Cache_ID = "1uC9y-8dd6Kk3kUCCRNtZR9oOSLFEcfGWyClSIaYl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //TODO: (subha) update to java 7 so i can use <>
        runs = new ArrayList<Run>();
        runsAdapter = new RunsAdapter(this, runs);
        lvRuns = (ListView) findViewById(R.id.lvRuns);
        lvRuns.setAdapter(runsAdapter);
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
    	switch(item.getItemId()) {
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
}
