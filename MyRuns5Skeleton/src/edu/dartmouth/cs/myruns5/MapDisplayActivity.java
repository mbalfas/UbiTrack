package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;


public class MapDisplayActivity extends Activity {

	// Menu ID for deletion
	public static final int MENU_ID_DELETE = 0;

	// Map elements:
	public GoogleMap mMap;
	public Marker start;
	public Marker end;
	public TextView typeStats;
	public TextView avespeedStats;
	public TextView curspeedStats;
	public TextView climbStats;
	public TextView caloriesStats;
	public TextView distanceStats;

	// Flags
	public boolean mIsBound;
	public boolean mIsDoneDrawing;

	// GPS Tracking Service
	public TrackingService mSensorService;
	public Intent mServiceIntent;
	public Context mContext;

	// Task Types: New or History.
	public int mTaskType;


	// Reference to array of Locations, 2 formats: Latlng and Location.
	private ArrayList<LatLng> mLatLngList;
	public ArrayList<Location> mLocationList;

	// Exercise entry
	public ExerciseEntryHelper mEntry;

	// First location flag and reference
	public boolean mIsFirstLocUpdate;
	public LatLng firstLatLng;

	// Broadcast receiver for updating new location on map
	private IntentFilter mLocationUpdateFilter;
	private BroadcastReceiver mLocationUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LatLng latlng;
			synchronized (mLocationList) {
				latlng = Utils.fromLocationToLatLng(mLocationList.get(0));
			}

			// Set the first GPS coordinate once get it.
			if (mIsFirstLocUpdate) {
				mIsFirstLocUpdate = false;
				firstLatLng = latlng;
			}

			if (firstLatLng != null) {
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
						Globals.DEFAULT_MAP_ZOOM_LEVEL));
			}

			mIsDoneDrawing = false;
			try {
				mEntry.updateStats();
			} catch (Exception e) {

				e.printStackTrace();
			}
			synchronized (mLocationList) {
				// ----------------------Skeleton--------------------------
				// Initialization
				if (mLocationList == null || mLocationList.isEmpty())
					return;

				// Convert the mLocationList to mLatLngList
				for (int i = 0; i < mLocationList.size() - 1; i++) {
					Location loc = mLocationList.get(i);
					mLatLngList.add(Utils.fromLocationToLatLng(loc));
				}

				// Draw Polyline using PolylineOptions
				// ----------------------Skeleton--------------------------
				PolylineOptions polylineOptions = new PolylineOptions();

				// Set Polyline's color
				polylineOptions.color(Color.RED);

				// Set Polyline's width
				polylineOptions.width(5);

				// Add all LatLng points into the list
				polylineOptions.addAll(mLatLngList);

				// Draw the list
				mMap.addPolyline(polylineOptions);
				// ----------------------Skeleton--------------------------
				// Draw marker
				// Initialization
				if (start == null)
					start = mMap
							.addMarker(new MarkerOptions()
									.position(firstLatLng)
									.icon(BitmapDescriptorFactory
											.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

				// Update the latest Marker
				if (end != null)
					end.remove();

				end = mMap
						.addMarker(new MarkerOptions()
								.position(
										Utils.fromLocationToLatLng(mLocationList
												.get(mLocationList.size() - 1)))
								.icon(BitmapDescriptorFactory
										.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

				// Get real-time stats from the Exercise Entry
				String[] statDecriptions = mEntry.getStatsDescription(mContext);

				// Draw the stats on the map
				if (statDecriptions.length != 0) {
					typeStats.setText(statDecriptions[0]);
					avespeedStats.setText(statDecriptions[1]);
					curspeedStats.setText(statDecriptions[2]);
					climbStats.setText(statDecriptions[3]);
					caloriesStats.setText(statDecriptions[4]);
					distanceStats.setText(statDecriptions[5]);

				}
				// Clear the mLatLngList
				mLatLngList.removeAll(mLatLngList);
			}
			mIsDoneDrawing = true;

		}
	};

	// Set up the mMotionUpdateIntentFilter broadcast receiver to update
	// activity inference using onReceive(). Gets the classification results
	// from the TrackingService and updates mEntry.updateByInference
	private IntentFilter mMotionUpdateIntentFilter;
	private BroadcastReceiver mMotionUpdateReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			int classifiedValue = intent.getIntExtra(Globals.KEY_CLASSIFICATION_RESULT, -1);
			mEntry.updateByInference(classifiedValue);
		}
	};
	
	

	// ----------------------Skeleton--------------------------
	// Create new ServiceConnection
	private ServiceConnection connection = new ServiceConnection() {
		public void onServiceConnected(ComponentName name, IBinder service) {
			
			// Initialize mSensorService from TrackingService
			mSensorService = ((TrackingService.MyRunsBinder)service).getService();
			
			// Get mLocationList from mSensorService
			mLocationList = mSensorService.mLocationList;
			
			// set Location list for mEntry.
			mEntry.setLocationList(mLocationList);

			// Start logging
			try {
				mEntry.startLogging();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}

		public void onServiceDisconnected(ComponentName name) {
			// Stop the service. This ONLY gets called when crashed.
			stopService(mServiceIntent);
			mSensorService = null;			
			
		}
	};


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		// Inflate the map_display
		setContentView(R.layout.map_display);

		// Initialize the mLatLngList.
		mLatLngList = new ArrayList<LatLng>(Globals.GPS_LOCATION_CACHE_SIZE);

		// ----------------------Skeleton--------------------------
		// Find all 6 TextView widgets using their resource id.
		typeStats = (TextView) findViewById(R.id.type_stats);
		avespeedStats = (TextView) findViewById(R.id.avespeed_stats);
		curspeedStats = (TextView) findViewById(R.id.curspeed_stats);
		climbStats = (TextView) findViewById(R.id.climb_stats);
		caloriesStats = (TextView) findViewById(R.id.calories_stats);
		distanceStats = (TextView) findViewById(R.id.distance_stats);

		// ----------------------Skeleton--------------------------
		// Set context.
		mContext = this;

		// Initialize mEntry.
		mEntry = new ExerciseEntryHelper();

		// Initialize the Bound flag.
		mIsBound = false;

		// ----------------------Skeleton--------------------------
		// Get extras from intent and set the mTaskType,
		// InputType, Row Id and ActivityType
		Bundle extras = getIntent().getExtras();
		mTaskType = extras.getInt(Globals.KEY_TASK_TYPE,
				Globals.TASK_TYPE_ERROR);
		mEntry.setInputType(extras.getInt(Globals.KEY_INPUT_TYPE,
				Globals.INPUT_TYPE_ERROR));
		mEntry.setActivityType(extras.getInt(Globals.KEY_ACTIVITY_TYPE,
				Globals.ACTIVITY_TYPE_ERROR));
		mEntry.setID(extras.getLong(Globals.KEY_ROWID, -1));

		// ----------------------Skeleton--------------------------
		// Get google map from the MapFragment
		mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		switch (mTaskType) {

		case Globals.TASK_TYPE_NEW:

			// ----------------------Skeleton--------------------------
			// Create extras to Intent for TrackingService,
			// put InputType and TaskType to the extras.
			extras = new Bundle();
			extras.putInt(Globals.KEY_TASK_TYPE, mTaskType);
			extras.putInt(Globals.KEY_INPUT_TYPE, mEntry.getInputType());

			// Register the GPS location sensor to receive location update.
			mLocationUpdateFilter = new IntentFilter();
			mLocationUpdateFilter.addAction(Globals.ACTION_LOCATION_UPDATED);

			// Set the mIsFirstLocUpdate flag to handle first location.
			mIsFirstLocUpdate = true;

			// If in the automatic mode, also register the
			// motion sensor intent filter
			if (mEntry.getInputType() == Globals.INPUT_TYPE_AUTOMATIC) {
				mMotionUpdateIntentFilter = new IntentFilter();
				mMotionUpdateIntentFilter
						.addAction(Globals.ACTION_MOTION_UPDATED);
			}

			// Start and bind the tracking service
			mServiceIntent = new Intent(this, TrackingService.class);
			mServiceIntent.putExtras(extras);
			startService(mServiceIntent);
			doBindService();

			break;

		case Globals.TASK_TYPE_HISTORY:
			// ----------------------Skeleton--------------------------

			// No longer need "Save" and "Cancel" button in history mode
			((Button) findViewById(R.id.btnSave)).setVisibility(View.GONE);
			((Button) findViewById(R.id.btnCancel)).setVisibility(View.GONE);

			// ----------------------Skeleton--------------------------

			// Read track from database
			try {
				if (mEntry == null)
					Log.e("Entry", "Entry is null");

				mEntry.readFromDB(mContext);
			} catch (Exception e) {
				e.printStackTrace();
			}

			// ----------------------Skeleton--------------------------

			// Convert the mLocationList to mLatLngList
			// so that you can draw polylines using LatLng objects
			mLocationList = mEntry.getLocationList();
			for (int i = 0; i < mLocationList.size() - 1; i++) {
				Location loc = mLocationList.get(i);
				mLatLngList.add(Utils.fromLocationToLatLng(loc));
			}

			// ----------------------Skeleton--------------------------
			// Draw marker for the start point
			mMap.addMarker(new MarkerOptions().position(mLatLngList.get(0))
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

			// ----------------------Skeleton--------------------------
			// Draw marker for the end point
			mMap.addMarker(new MarkerOptions().position(
					mLatLngList.get(mLatLngList.size() - 1)).icon(
					BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

			// ----------------------Skeleton--------------------------
			// Draw the GPS traces, set the width, color and use addAll to
			// write a Polyline that goes through all the LatLng points
			PolylineOptions polylineOptions = new PolylineOptions();
			polylineOptions.color(Color.RED);
			polylineOptions.width(5);
			polylineOptions.addAll(mLatLngList);
			mMap.addPolyline(polylineOptions);

			// ----------------------Skeleton--------------------------
			// Move map center to the 1st point in the route track.
			if (!mLatLngList.isEmpty()) {
				LatLng latlng = mLatLngList.get(0);
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng,
						Globals.DEFAULT_MAP_ZOOM_LEVEL));

			}
			// ----------------------Skeleton--------------------------
			// Clear the mLatLngList
			mLatLngList.removeAll(mLatLngList);

			// ----------------------Skeleton--------------------------
			// Get previous stats from the ExerciseEntry
			String[] statDecriptions = mEntry.getStatsDescription(mContext);

			// ----------------------Skeleton--------------------------
			// Draw the stats on the map
			if (statDecriptions.length != 0) {
				switch (mEntry.getActivityType()) {
				case Globals.ACTIVITY_TYPE_WALKING:
					typeStats.setText("Type: Walking");
					break;
				case Globals.ACTIVITY_TYPE_RUNNING:
					typeStats.setText("Type: Running");
					break;
				case Globals.ACTIVITY_TYPE_STANDING:
					typeStats.setText("Type: Standing");
					break;
				default:
					break;
				}
				avespeedStats.setText(statDecriptions[1]);
				curspeedStats.setText(statDecriptions[2]);
				climbStats.setText(statDecriptions[3]);
				caloriesStats.setText(statDecriptions[4]);
				distanceStats.setText(statDecriptions[5]);

			}

			break;

		default:
			finish(); // Should never happen.
			return;
		}

	}

	// ----------------------Skeleton--------------------------
	@Override
	protected void onResume() {
		// Register the receiver for receiving the location update broadcast
		// from the service. Logic is the same as in onCreate()

		// If "new" task, need to read sensor data.
		if (mTaskType == Globals.TASK_TYPE_NEW) {
			// Register gps location update receiver
			registerReceiver(mLocationUpdateReceiver, mLocationUpdateFilter);
			// If it's "automatic" mode, also need motion sensor for
			// classification
			if (mEntry.getInputType() == Globals.INPUT_TYPE_AUTOMATIC) {
				registerReceiver(mMotionUpdateReceiver,
						mMotionUpdateIntentFilter);
			}
		}

		super.onResume();
	}

	// ----------------------Skeleton--------------------------
	@Override
	protected void onPause() {
		// Unregister the receiver when the activity is about to go inactive.
		if (mTaskType == Globals.TASK_TYPE_NEW) {
			unregisterReceiver(mLocationUpdateReceiver);
			if (mEntry.getInputType() == Globals.INPUT_TYPE_AUTOMATIC) {
				unregisterReceiver(mMotionUpdateReceiver);
			}
		}
		super.onPause();
	}

	public void onSaveClicked(View v) {

		// The Exercise will be only saved when the drawing is done.
		if (mIsDoneDrawing == true) {
			// We don't want to save duplicate exercise entries
			// so we need to disable the button after first click.
			v.setEnabled(false);

			// Insert the ExerciseEntry to database.
			long id = mEntry.insertToDB(mContext);

			// There are some cases when the entry is not saved. see insertToDB
			// for detail.
			if (id > 0) {
				Toast.makeText(getApplicationContext(),
						"Entry #" + id + " saved.", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), "Entry not saved.",
						Toast.LENGTH_SHORT).show();
			}

			// ----------------------Skeleton--------------------------
			// Stop the service in the foreground, unbind the service and stop
			// it.
			// cancel the fired Notification.
			mSensorService.stopForeground(true);
			doUnbindService();
			stopService(mServiceIntent);
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancelAll();
			finish();
		}
	}

	public void onCancelClicked(View v) {
		// Similar to what happened in onSaveClicked() but without the
		// insertToDB operation.
		v.setEnabled(false);

		// Stop the service in the foreground, unbind the service and stop it.
		// cancel the fired Notification.
		// ----------------------Skeleton--------------------------
		mSensorService.stopForeground(true);
		doUnbindService();
		stopService(mServiceIntent);
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.cancelAll();
		finish();
	}

	@Override
	public void onDestroy() {
		// Stop the service and the notification.
		// ----------------------Skeleton--------------------------
		// Need to check whether the mSensorService is null or not
		// before unbind and stop the service.
		if (mSensorService != null) {
			mSensorService.stopForeground(true);

			doUnbindService();
			stopService(mServiceIntent);
		}
		// Cancel the fired Notification.
		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.cancelAll();
		// Finish the MapDisplayActivity itself.
		finish();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		// Stop service and notification when back is pressed
		if (mTaskType == Globals.TASK_TYPE_NEW) {
			// ----------------------Skeleton--------------------------
			mSensorService.stopForeground(true);
			doUnbindService();
			stopService(mServiceIntent);
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancelAll();
		}

		super.onBackPressed();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// If task type is displaying history, also give a menu button
		// To delete the entry
		MenuItem menuitem;
		if (mTaskType == Globals.TASK_TYPE_HISTORY) {
			menuitem = menu.add(Menu.NONE, MENU_ID_DELETE, MENU_ID_DELETE,
					"Delete");
			menuitem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ID_DELETE:
			// Delete entry in database
			mEntry.deleteEntryInDB(mContext);
			finish();
			return true;
		default:
			finish();
			return false;
		}
	}

	// Bind service and set binding flag.
	private void doBindService() {
		if (!mIsBound) {
			bindService(mServiceIntent, connection, Context.BIND_AUTO_CREATE);
			mIsBound = true;
		}

	}

	// Unbind service and set binding flag.
	private void doUnbindService() {
		if (mIsBound) {
			unbindService(connection);
			mIsBound = false;
		}
	}
}

