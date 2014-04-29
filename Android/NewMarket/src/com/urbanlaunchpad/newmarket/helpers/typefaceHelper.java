package com.urbanlaunchpad.newmarket.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class typefaceHelper {
	public static View setCustomTypeface(View view, Context context) {
		Typeface tf = Typeface.createFromAsset(context.getAssets(),
				"fonts/raleway-regular.otf");
		if (view instanceof TextView) {
			TextView tv = (TextView) view;
			tv.setTypeface(tf);
			view = tv;
		} else if (view instanceof Button) {
			Button button = (Button) view;
			button.setTypeface(tf);
			view = button;
		} else if (view instanceof EditText) {
			EditText et = (EditText) view;
			et.setTypeface(tf);
			view = et;
		}
		return view;
	}
}
