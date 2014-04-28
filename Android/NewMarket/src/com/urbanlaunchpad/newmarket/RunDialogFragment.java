package com.urbanlaunchpad.newmarket;

import java.util.Date;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.urbanlaunchpad.newmarket.model.Run;
import com.urbanlaunchpad.newmarket.model.RunsClient;
import com.urbanlaunchpad.newmarket.model.StepsClient;

public class RunDialogFragment extends DialogFragment {
    /**
     * Create a new instance of RunDialogFragment.
     */
    static RunDialogFragment newInstance() {
        return new RunDialogFragment();
    }
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        int style = DialogFragment.STYLE_NORMAL;
        int theme = android.R.style.Theme_Holo_Light_Dialog;
        setStyle(style, theme);
    }
    
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_run, container, false);
        
		final Spinner spTextile = getAndInitSpinner(v);

        // When save button is clicked, create a run with the run info,
		// callback the runs activity with the new run
		// dismiss the dialog.
        Button button = (Button)v.findViewById(R.id.btnSave);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // When button is clicked, call up to owning activity.
            	// TODO: subha - put runs activity behind a callback interface instead of assuming type
                ((RunsActivity)getActivity()).runFragmentCallback(getNewRun(spTextile));
                dismiss();
            }
        });

        return v;
    }
	
	/**
	 * Initializes the spinner and returns it.
	 */
	private Spinner getAndInitSpinner(View v) {
		Spinner spTextile = (Spinner) v.findViewById(R.id.spTextile);
		List<String> textileOptions = RunsClient.getInstance()
				.getTextileOptions();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_spinner_dropdown_item, textileOptions);
		spTextile.setAdapter(adapter);
		
		return spTextile;
	}
	
	/**
	 * Creates a new run with the information in spTextile.
	 * @param spTextile
	 */
	private Run getNewRun(Spinner spTextile) {
		String startStep = StepsClient.getInstance().getStart();
		String textile = spTextile.getSelectedItem().toString();
		Date time_last_update_UTC = new Date();
		return new Run(textile, 1, startStep, time_last_update_UTC);
	}
}
