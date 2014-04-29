package com.urbanlaunchpad.newmarket;

import java.util.Date;

import com.urbanlaunchpad.newmarket.helpers.typefaceHelper;
import com.urbanlaunchpad.newmarket.model.Step;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class StepDialogFragment extends UlDialogFragment {
	private static final String TAG_NAME = "addStepDialog";

	private StepCreationListener stepCreationListener;

	/**
	 * Create a new instance of StepDialogFragment.
	 */
	static StepDialogFragment newInstance() {
		return new StepDialogFragment();
	}

	public void setStepCreationListener(
			StepCreationListener stepCreationListener) {
		this.stepCreationListener = stepCreationListener;
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
		View v = inflater.inflate(R.layout.fragment_step, container, false);

		final EditText etStepName = (EditText) typefaceHelper
				.setCustomTypeface(v.findViewById(R.id.etStepName),
						getActivity());

		// When save button is clicked, create a run with the run info,
		// callback the runs activity with the new run
		// dismiss the dialog.
		Button button = (Button) typefaceHelper.setCustomTypeface(
				v.findViewById(R.id.btnSave), getActivity());
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (stepCreationListener != null) {
					stepCreationListener.onStepCreated(getNewStep(etStepName));
				}
				dismiss();
			}
		});

		return v;
	}

	private Step getNewStep(EditText etStepName) {
		String stepName = etStepName.getText().toString();
		Date start_time_UTC = new Date();
		return new Step(stepName, start_time_UTC);
	}

	/**
	 * Return the tag name of the matching xml file. This is the value in
	 * android:tag of the top level xml layout.
	 */
	public static String getTagName() {
		return TAG_NAME;
	}
}
