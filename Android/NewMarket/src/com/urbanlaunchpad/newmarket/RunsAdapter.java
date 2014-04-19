package com.urbanlaunchpad.newmarket;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
		TextView textileNameView = (TextView) view.findViewById(R.id.tvTextileName);
		TextView runNumberView = (TextView) view.findViewById(R.id.tvRun);
		TextView stepView = (TextView) view.findViewById(R.id.tvStep);
		ImageView textileImageView = (ImageView) view.findViewById(R.id.ivTextile);

		textileNameView.setText(run.getTextile());
		runNumberView.setText(run.getRun() + "");
		stepView.setText(run.getStep());
		
		// Please note that this code assumes the image names are identical to the textile name.
		// e.g. if textile is "kotwali", drawable is called "kotwali.png"
		// you can refer to this kotwali.png drawable in the drawables folder as "@drawable/kotwali". note the lack of file extension.
		// Clean this up once we have assets; then we should have a spinner of the textile names.
		String uri = "@drawable/" + run.getTextile();
		int resource = super.getContext().getResources().getIdentifier(uri, null, super.getContext().getPackageName());
		Drawable drawable = super.getContext().getResources().getDrawable(resource);
		textileImageView.setImageDrawable(drawable);
		
		return view;
	}
}
