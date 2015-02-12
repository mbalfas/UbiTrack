package edu.dartmouth.cs.myruns5;

import java.util.Calendar;




import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

// Ref: http://developer.android.com/reference/android/app/DialogFragment.html
// The difference from the sample code is that we use Items, not OK/Cancel buttons


// Handling all the customized dialog boxes in our project.
// Differentiated by dialog id.
public class MyRunsDialogFragment extends DialogFragment {
	
	// Different dialog IDs
	public static final int DIALOG_ID_ERROR = -1;
	public static final int DIALOG_ID_PHOTO_PICKER = 1;
	public static final int DIALOG_ID_MANUAL_INPUT_DATE = 2;
	public static final int DIALOG_ID_MANUAL_INPUT_TIME = 3;
	public static final int DIALOG_ID_MANUAL_INPUT_DURATION = 4;
	public static final int DIALOG_ID_MANUAL_INPUT_DISTANCE = 5;
	public static final int DIALOG_ID_MANUAL_INPUT_CALORIES = 6;
	public static final int DIALOG_ID_MANUAL_INPUT_HEARTRATE = 7;
	public static final int DIALOG_ID_MANUAL_INPUT_COMMENT = 8;
	
	// For photo picker selection:
	public static final int ID_PHOTO_PICKER_FROM_CAMERA = 0;
	public static final int ID_PHOTO_PICKER_FROM_GALLERY = 1;
	

	private static final String DIALOG_ID_KEY = "dialog_id";

	public static MyRunsDialogFragment newInstance(int dialog_id) {
		MyRunsDialogFragment frag = new MyRunsDialogFragment();
		Bundle args = new Bundle();
		args.putInt(DIALOG_ID_KEY, dialog_id);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int dialog_id = getArguments().getInt(DIALOG_ID_KEY);
		
		final Activity parent = getActivity();

		// For initializing date/time related dialogs
		final Calendar now;
		int hour, minute, year, month, day;
		
		// For text input field
		final EditText textEntryView;
		
		
		// Settup dialog appearance and onClick Listeners
		switch (dialog_id) {
		case DIALOG_ID_PHOTO_PICKER:
			// Build picture picker dialog for choosing from camera or gallery
			AlertDialog.Builder builder = new AlertDialog.Builder(parent);
			builder.setTitle(R.string.ui_profile_photo_picker_title);
			// Set up click listener, firing intents open camera or gallery based on
			// choice.
			builder.setItems(R.array.ui_profile_photo_picker_items,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int item) {
							// Item can be: ID_PHOTO_PICKER_FROM_CAMERA
							// or ID_PHOTO_PICKER_FROM_GALLERY
							((ProfileActivity) parent)
									.onPhotoPickerItemSelected(item);
						}
					});
			return builder.create();

		case DIALOG_ID_MANUAL_INPUT_DATE:

			now = Calendar.getInstance();
			year = now.get(Calendar.YEAR);
			month = now.get(Calendar.MONTH);
			day = now.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(parent,
					new DatePickerDialog.OnDateSetListener() {
						public void onDateSet(DatePicker view, int year,
								int monthOfYear, int dayOfMonth) {
							((ManualInputActivity) parent).onDateSet(
									year, monthOfYear, dayOfMonth);
						}
					}, year, month, day);

		case DIALOG_ID_MANUAL_INPUT_TIME:
			now = Calendar.getInstance();
			hour = now.get(Calendar.HOUR_OF_DAY);
			minute = now.get(Calendar.MINUTE);

			return new TimePickerDialog(parent,
					new TimePickerDialog.OnTimeSetListener() {
						public void onTimeSet(TimePicker view, int hourOfDay,
								int minute) {
							((ManualInputActivity) parent).onTimeSet(
									hourOfDay, minute);
						}
					}, hour, minute, false);

		case DIALOG_ID_MANUAL_INPUT_DURATION:
			textEntryView = new EditText(parent);
			textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			return new AlertDialog.Builder(parent)
					.setTitle(R.string.ui_manual_input_duration_title)
					.setView(textEntryView)
					.setPositiveButton(R.string.ui_button_ok_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((ManualInputActivity) parent).onDurationSet(textEntryView.getText()
											.toString());
								}
							})
					.setNegativeButton(R.string.ui_button_cancel_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									textEntryView.setText("");
								}
							}).create();
			
		case DIALOG_ID_MANUAL_INPUT_DISTANCE:
			textEntryView = new EditText(parent);
			textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
			return new AlertDialog.Builder(parent)
					.setTitle(R.string.ui_manual_input_distance_in_miles_title)
					.setView(textEntryView)
					.setPositiveButton(R.string.ui_button_ok_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									((ManualInputActivity) parent).onDistanceSet(textEntryView.getText()
											.toString());
								}
							})
					.setNegativeButton(R.string.ui_button_cancel_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									textEntryView.setText("");
								}
							}).create();
			
		case DIALOG_ID_MANUAL_INPUT_CALORIES:
			
			textEntryView = new EditText(parent);
			textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER);
			return new AlertDialog.Builder(parent)
					.setTitle(R.string.ui_manual_input_calories_title)
					.setView(textEntryView)
					.setPositiveButton(R.string.ui_button_ok_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((ManualInputActivity) parent).onCaloriesSet(textEntryView.getText()
											.toString());
								}
							})
					.setNegativeButton(R.string.ui_button_cancel_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									textEntryView.setText("");
								}
							}).create();
			
		case DIALOG_ID_MANUAL_INPUT_HEARTRATE:
			
			textEntryView = new EditText(parent);
			textEntryView.setInputType(InputType.TYPE_CLASS_NUMBER);
			return new AlertDialog.Builder(parent)
					.setTitle(R.string.ui_manual_input_heartrate_title)
					.setView(textEntryView)
					.setPositiveButton(R.string.ui_button_ok_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((ManualInputActivity) parent).onHeartrateSet(textEntryView.getText()
											.toString());
								}
							})
					.setNegativeButton(R.string.ui_button_cancel_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									textEntryView.setText("");
								}
							}).create();

		case DIALOG_ID_MANUAL_INPUT_COMMENT:
			
			textEntryView = new EditText(parent);
			textEntryView.setInputType(InputType.TYPE_CLASS_TEXT);
			textEntryView.setHint(R.string.ui_manual_input_comment_hint);
			textEntryView.setLines(4);
			return new AlertDialog.Builder(parent)
					.setTitle(R.string.ui_manual_input_comment_title)
					.setView(textEntryView)
					.setPositiveButton(R.string.ui_button_ok_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									((ManualInputActivity) parent).onCommentSet(textEntryView.getText()
											.toString());

								}
							})
					.setNegativeButton(R.string.ui_button_cancel_title,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									textEntryView.setText("");
								}
							}).create();

		default:
			return null;
		}
	}
}
