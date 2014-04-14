package com.urbanlaunchpad.newmarket;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.urbanlaunchpad.newmarket.model.Run;

public class RunsAdapter extends ArrayAdapter<Run> {
	public RunsAdapter(Context context, List<Run> runs) {
		super(context, 0, runs);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.run_item, null);
		}

		Run run = getItem(position);
		TextView nameView = (TextView) view.findViewById(R.id.tvRunName);
		nameView.setText(run.getName());

		return view;
	}
}
