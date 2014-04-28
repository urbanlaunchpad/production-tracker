package com.urbanlaunchpad.newmarket;

import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;

public abstract class UlDialogFragment extends DialogFragment {

	/**
	 * Fixes up the dimensions of the dialog.
	 * Only call when the dialog is visible!!
	 * Aka after you have called show and executed pending fragment transactions.
	 * @param dialogFragment
	 */
	public void sizeDialog() {
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		Dialog dialog = getDialog();
		dialog.getWindow().setLayout((4 * width)/ 5, (4 * height)/ 5);
	}
	
	/**
	 * Sets the dialog title. Again only call when visible.
	 */
	public void setDialogTitle(String title) {
		getDialog().setTitle(title);
	}
}