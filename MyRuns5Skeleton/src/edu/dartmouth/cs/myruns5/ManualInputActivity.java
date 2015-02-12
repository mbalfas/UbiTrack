/**
 * manualInputActivity.java
 * 
 * Created by Xiaochao Yang on Sep 13, 2011 10:38:16 PM
 * 
 */

package edu.dartmouth.cs.myruns5;

import android.app.DialogFragment;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;

// Extra credit: display distance unit (kilometers or miles) based on the preference 

public class ManualInputActivity extends ListActivity {

	// Exercise entry
	public ExerciseEntryHelper mEntry;
	
	public static final int LIST_ITEM_ID_DATE = 0;
	public static final int LIST_ITEM_ID_TIME = 1;
	public static final int LIST_ITEM_ID_DURATION = 2;
	public static final int LIST_ITEM_ID_DISTANCE = 3;
	public static final int LIST_ITEM_ID_CALORIES = 4;
	public static final int LIST_ITEM_ID_HEARTRATE = 5;
	public static final int LIST_ITEM_ID_COMMENT = 6;

	// skeleton
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Setting the UI layout
		setContentView(R.layout.manualinput);

		// Initialize
		mEntry = new ExerciseEntryHelper();

		// Get the extra information passed from the launching activity
		Bundle extras = getIntent().getExtras();

		mEntry.setInputType(extras.getInt(Globals.KEY_INPUT_TYPE, -1));
		mEntry.setActivityType(extras.getInt(Globals.KEY_ACTIVITY_TYPE, -1));
	}

	//skeleton
	// "Save" button is clicked
	public void onSaveClicked(View v) {

		// Insert the exercise entry into database
		long id = mEntry.insertToDB(this);

		// Pop up a toast
		Toast.makeText(this, "Entry #" + id + " saved.", Toast.LENGTH_SHORT)
				.show();

		// Close the activity
		finish();
	}

	//skeleton
	// "Cancel" button is clicked
	public void onCancelClicked(View v) {
		// Discard the input and close the activity directly
		Toast.makeText(getApplicationContext(), "Entry discarded.",
				Toast.LENGTH_SHORT).show();
		finish();
	}

	//skeleton
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		int dialogId;

		// Figuring out what dialog to show based on the position clicked
		// (more readable, also could use dialogId = position + 2)
		switch (position) {
		case LIST_ITEM_ID_DATE:
			dialogId = MyRunsDialogFragment.DIALOG_ID_MANUAL_INPUT_DATE;
			break;
		case LIST_ITEM_ID_TIME:
			dialogId = MyRunsDialogFragment.DIALOG_ID_MANUAL_INPUT_TIME;
			break;
		case LIST_ITEM_ID_DURATION:
			dialogId = MyRunsDialogFragment.DIALOG_ID_MANUAL_INPUT_DURATION;
			break;
		case LIST_ITEM_ID_DISTANCE:
			dialogId = MyRunsDialogFragment.DIALOG_ID_MANUAL_INPUT_DISTANCE;
			break;
		case LIST_ITEM_ID_CALORIES:
			dialogId = MyRunsDialogFragment.DIALOG_ID_MANUAL_INPUT_CALORIES;
			break;
		case LIST_ITEM_ID_HEARTRATE:
			dialogId = MyRunsDialogFragment.DIALOG_ID_MANUAL_INPUT_HEARTRATE;
			break;
		case LIST_ITEM_ID_COMMENT:
			dialogId = MyRunsDialogFragment.DIALOG_ID_MANUAL_INPUT_COMMENT;
			break;
		default:
			dialogId = MyRunsDialogFragment.DIALOG_ID_ERROR;
		}

		displayDialog(dialogId);
	}

	//skeleton
	// Display dialog based on id. See MyRunsDialogFragment for details 
	public void displayDialog(int id) {
		DialogFragment fragment = MyRunsDialogFragment.newInstance(id);
		fragment.show(getFragmentManager(),
				getString(R.string.dialog_fragment_tag_general));
	}
	
	// ********************************
	// The following are functions called after dialog is clicked.
	// Called from MyRunsDialogFragment side. mEntry is handled here in a  
	// cleaner and more separated way. 
	// value are parsed and set in mEntry for later database insertion.

	public void onDateSet(int year, int monthOfYear, int dayOfMonth) {
		mEntry.setDate(year, monthOfYear, dayOfMonth);
		
	}

	public void onTimeSet(int hourOfDay, int minute) {
		mEntry.setTime(hourOfDay, minute, 0);
		Log.w(ManualInputActivity.class.getName(), "time=entering the onTimeset");
	}

	public void onDurationSet(String strDurationInMinutes) {

		int durationInSeconds;
		try {
			durationInSeconds = (int) (Double.parseDouble(strDurationInMinutes) * 60);
		} catch (NumberFormatException e) {
			durationInSeconds = 0;
		}

		mEntry.setDuration(durationInSeconds);
	}

	public void onDistanceSet(String strDistance) {

		int distanceInMeters;
		try {
			distanceInMeters = (int) (Double.parseDouble(strDistance) * 1000 * Globals.KM2MILE_RATIO);
		} catch (NumberFormatException e) {
			distanceInMeters = 0;
		}
		mEntry.setDistance(distanceInMeters);

	}

	public void onCaloriesSet(String strCalories) {

		int calories;
		try {
			calories = (Integer.parseInt(strCalories));
		} catch (NumberFormatException e) {
			calories = 0;
		}
		mEntry.setCalories(calories);
	}

	public void onHeartrateSet(String strHeartrate) {
		int heartrate;
		try {
			heartrate = (Integer.parseInt(strHeartrate));
		} catch (NumberFormatException e) {
			heartrate = 0;
		}
		mEntry.setHeartrate(heartrate);
	}

	public void onCommentSet(String comment) {
		mEntry.setComment(comment);
	}
}
