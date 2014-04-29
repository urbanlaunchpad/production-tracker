package com.urbanlaunchpad.newmarket;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.urbanlaunchpad.newmarket.helpers.typefaceHelper;
import com.urbanlaunchpad.newmarket.model.Step;

public class StepsAdapter extends ArrayAdapter<Step> {
	public StepsAdapter(Context context, List<Step> runs) {
		super(context, 0, runs);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.step_item, null);
		}

		Step step = getItem(position);
		TextView stepView = (TextView) typefaceHelper.setCustomTypeface(
				view.findViewById(R.id.tvStep), getContext());
		TextView timeCompleted = (TextView) typefaceHelper.setCustomTypeface(
				view.findViewById(R.id.tvTimeCompleted), getContext());
		Button updateButton = (Button) typefaceHelper.setCustomTypeface(
				view.findViewById(R.id.update_button), getContext());

		stepView.setText(step.getStep());

		return view;
	}
}
