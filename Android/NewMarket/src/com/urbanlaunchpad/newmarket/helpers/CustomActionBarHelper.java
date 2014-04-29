package com.urbanlaunchpad.newmarket.helpers;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.urbanlaunchpad.newmarket.R;

public class CustomActionBarHelper {
	public static void customizeActionBar(Context context, Activity activity){
		// Custom color ActionBar
		ActionBar ab = activity.getActionBar();
		ab.setBackgroundDrawable(context.getResources().getDrawable(
				R.color.orange_background));
		// Custom typeface for ActionBar
		int titleId = context.getResources().getIdentifier("action_bar_title", "id",
	            "android");
	    TextView actionBar = (TextView) typefaceHelper.setCustomTypeface(
	    		activity.findViewById(titleId), context);
	}
}
