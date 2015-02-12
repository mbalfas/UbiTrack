/**
 * ActivityTabStart.java
 * 
 * Created by Xiaochao Yang on Dec 9, 2011 10:51:32 PM
 * 
 */

package edu.dartmouth.cs.myruns5;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;

public class StartTabFragment extends Fragment {
	
	// Context stands for current running activity.
	private Context mContext;

	// View widgets on the screen needs to be programmatically configured 
	private Spinner mSpinnerInputType;
	private Spinner mSpinnerActivityType;
	private Button mButtonStart;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the view defined in start.xml. Return at the end 
		View view = inflater.inflate(R.layout.start, container, false);
		
		// Initialize context 
		mContext = getActivity();
		
		// Initialize view widgets by IDs
		mSpinnerInputType = (Spinner) view.findViewById(
				R.id.spinnerInputType);
		mSpinnerActivityType = (Spinner) view.findViewById(
				R.id.spinnerActivityType);
		mButtonStart = (Button) view.findViewById(R.id.btnStart);
		
		// Due to the problem with the idea of Fragment, it'll be messy to use
		// onClick in xml. The activity will intercept the event, not the
		// Fragment. I decide to setup onClick listener in the Java code. Please
		// read the detail about this issue here:
		// http://stackoverflow.com/questions/6091194/how-to-handle-button-clicks-using-the-xml-onclick-within-fragments
		// http://stackoverflow.com/questions/7570575/onclick-inside-fragment-called-on-activity
		// http://stackoverflow.com/questions/4153517/how-exactly-does-the-androidonclick-xml-attribute-differ-from-setonclicklistene
		
		// Settup the onClick event of the "Start" button
		mButtonStart.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				// Get the index of which input mode is selected. It can be:
				// "Manual" i.e. you type in everything, 
				// "GPS" i.e. using GPS track your route
				// "Automatic" i.e. using GPS plus automatic machine learning algorithm to detect your activity type. 
				int inputType = mSpinnerInputType.getSelectedItemPosition();

				// Create a extras bundle to store the index of item selected
				// and pass to the next activity through an intent.
				Bundle extras = new Bundle();
				
				// New task (versus reading form history database) 
				extras.putInt(Globals.KEY_TASK_TYPE, Globals.TASK_TYPE_NEW);
				// Input type as explained above.
				extras.putInt(Globals.KEY_INPUT_TYPE, inputType);

				Intent i;
				
				// Based on different selection, start different activity with

				switch (inputType) {
				
				// ----------------------Skeleton--------------------------
				case Globals.INPUT_TYPE_GPS:
					
					// GPS automatic tracking
					extras.putInt(Globals.KEY_ACTIVITY_TYPE,
							mSpinnerActivityType.getSelectedItemPosition());
					// Intent to launch MapDisplayActivity
					i = new Intent(mContext, MapDisplayActivity.class);
					break;
				// ----------------------Skeleton--------------------------
				case Globals.INPUT_TYPE_MANUAL:

					// Manual input
					extras.putInt(Globals.KEY_ACTIVITY_TYPE,
							mSpinnerActivityType.getSelectedItemPosition());
					// Intent to launch ManualInputActivity
					i = new Intent(mContext, ManualInputActivity.class);
					break;

				case Globals.INPUT_TYPE_AUTOMATIC:
					// Inference, the activity does not matter
					extras.putInt(Globals.KEY_ACTIVITY_TYPE, -1);
					// Intent to launch MapDisplayActivity
					i = new Intent(mContext, MapDisplayActivity.class);
					break;

				default:
					return;
				}

				// Put extras into the intent
				i.putExtras(extras);
				// Launch activity
				startActivity(i);
			}
		});
		
		return view;
	}

}
