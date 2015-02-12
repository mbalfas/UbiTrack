/**
 * LocationService.java
 * 
 * Created by Xiaochao Yang on Sep 11, 2011 4:50:19 PM
 * 
 */

package edu.dartmouth.cs.myruns5;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.meapsoft.FFT;


// The Tracking service will: read and process GPS data.
public class TrackingService extends Service implements LocationListener,
		SensorEventListener {
	
	
	// A buffer list to store all GPS track points
	// It's accessed at different places
	public ArrayList<Location> mLocationList;
	public int mInferenceResult;

	// Sensor manager for accelerometer
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	// Location manager and Notification manager
	private LocationManager mlocationManager;
	private NotificationManager mNotificationManager;

	// Context for "this"
	private Context mContext;

	// Intents for broadcasting location/motion updates
	private Intent mLocationUpdateBroadcast;
	private Intent mActivityClassificationBroadcast;

	// A blocking queue for buffering motion sensor data
	private static ArrayBlockingQueue<Double> mAccBuffer;

	// The AsyncTask running in a different thread all the time to
	// process the motion sensor data and do classification
	private ActivityClassificationTask mActivityClassificationTask;

	// Based on input type, GPS or automatic, do different things
	private int mInputType;

	// Standard service stuff.
	private final IBinder binder = new MyRunsBinder();

	public class MyRunsBinder extends Binder {
		TrackingService getService() {
			return TrackingService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {

		// Initialize mContext, mLocationList, mLocationUpdateBroadcast

		mContext = this;

		mLocationList = new ArrayList<Location>(Globals.GPS_LOCATION_CACHE_SIZE);

		mLocationUpdateBroadcast = new Intent();
		mLocationUpdateBroadcast.setAction(Globals.ACTION_LOCATION_UPDATED);
		
		// Initialize mAccBuffer, mActivityClassificationBroadcast here
		mAccBuffer = new ArrayBlockingQueue<Double>(
				Globals.ACCELEROMETER_BUFFER_CAPACITY);

		mActivityClassificationBroadcast = new Intent();
		mActivityClassificationBroadcast
				.setAction(Globals.ACTION_MOTION_UPDATED);

		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Read inputType, can be GPS or Automatic.
		mInputType = intent.getExtras().getInt(Globals.KEY_INPUT_TYPE);

		// Get LocationManager and set related provider.
		// GPS_PROVIDER recommended.
		mlocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				0, 0, this);

		// If it's automatic mode, registering motion sensor for activity
		// recognition.
		if (mInputType == Globals.INPUT_TYPE_AUTOMATIC) {

//			You code here
//			Set up the SensorManager
//			Instantiate the mActivityClassificationTask AsynTask and start it 
			mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
			mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
			mSensorManager.registerListener(this, mAccelerometer,SensorManager.SENSOR_DELAY_FASTEST);
			
			//TODO Check whether this belongs
//			Bundle extras = intent.getExtras();
//			mLabel=extras.getString(Globals.CLASS_LABEL_KEY);
			
			mActivityClassificationTask =new ActivityClassificationTask();
			mActivityClassificationTask.execute();

		}
		// ----------------------Skeleton--------------------------
		// Setup the intent to create a PendingIntent related to
		// MapDisplayActivity.
		Intent i = new Intent(this, MapDisplayActivity.class);

		// ----------------------Skeleton--------------------------
		// Set flags to avoid re-invent activity.
		// http://developer.android.com/guide/topics/manifest/activity-element.html#lmode
		// IMPORTANT!. no re-create activity
		i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		// ----------------------Skeleton--------------------------
		// Using pending intent to bring back the MapActivity from notification
		// center.
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

		// ----------------------Skeleton--------------------------

		// Use NotificationManager to build notification(icon, content, title,
		// flag and pIntent)
		Notification notification = new Notification.Builder(this)
				.setContentTitle(
						mContext.getString(R.string.ui_maps_display_notification_title))
				.setContentText(
						getResources().getString(
								R.string.ui_maps_display_notification_content))
				.setSmallIcon(R.drawable.icon).setContentIntent(pi).build();
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		notification.flags = notification.flags
				| Notification.FLAG_ONGOING_EVENT;

		mNotificationManager.notify(0, notification);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		// ----------------------Skeleton--------------------------
		// Unregistering location manager,
		// foreground notification,
		// and sensor listener.
		// Cancel the mActivityClassificationTask.
		mlocationManager.removeUpdates(this);
		mNotificationManager.cancelAll();
		if (mInputType == Globals.INPUT_TYPE_AUTOMATIC) {
			//Your code here
			mSensorManager.unregisterListener(this);
			mActivityClassificationTask.cancel(false);
		
		}

		super.onDestroy();
	}

	// Gets called when new GPS location updates
	public void onLocationChanged(Location location) {

		// ----------------------Skeleton--------------------------
		// Check whether location is valid, drop if invalid
		if (location == null || Math.abs(location.getLatitude()) > 90
				|| Math.abs(location.getLongitude()) > 180)
			return;

		// ----------------------Skeleton--------------------------
		// Buffer the new location. mLocation is connected by reference by
		// several other classes. Accessed with "synchronized" lock
		synchronized (mLocationList) {
			mLocationList.add(location);
		}

		// ----------------------Skeleton--------------------------
		// Send broadcast saying new location is updated
		mContext.sendBroadcast(mLocationUpdateBroadcast);

	}

	// You don't need to implement the other three abstract methods of the
	// LocationListener Interface class.:
	// onProviderDisabled, onProviderEnabled, and onStatusChanged.
	// You can leave them as blank.
	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	// ----------------------Skeleton--------------------------
	// An AsyncTask running in a separate thread processing the sensor data.
	// It's an infinite loop, waiting on new sensor event, and uses weka
	// classifier to do activity recognition
	private class ActivityClassificationTask extends
			AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... arg0) {

//			Your code  here. Take a look at collector but do not copy 
//			the code directly. You must understand the collector code
//			and write it in your own way
			
			// Create the feature vector for classification
			ArrayList<Double> featVect = new ArrayList<Double>();
			
			
			int blockSize = 0;
			FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
			double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			double[] re = accBlock;
			double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
			
			double max = Double.MIN_VALUE;
			
			while(true) {
				try{
					if (isCancelled()==true)
					{
						return null;
					}
					
					accBlock[blockSize++]=mAccBuffer.take().doubleValue();
					
					
					if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
						blockSize = 0;
						max=.0;
						for (double val:accBlock){
							if (max < val) {
								max = val;
							}
						}
						
						fft.fft(re, im);
						for (int i = 0; i < re.length; i++) {
							double mag = Math.sqrt(re[i] * re[i] + im[i]
									* im[i]);
							featVect.add(mag);
							im[i] = .0; // Clear the field
						}			
						
						featVect.add(max);
						
						
						//TODO
						int classifiedValue = (int) WekaClassifier.classify(featVect.toArray());											
						mActivityClassificationBroadcast.putExtra(Globals.KEY_CLASSIFICATION_RESULT, classifiedValue);
						sendBroadcast(mActivityClassificationBroadcast);
						featVect.clear();
					}
					
					
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				

				
			}
			
		}
	}
	
	
	
	// ----------------------Skeleton--------------------------
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {			
			
			// Compute m for 3-axis accelerometer input.
			// m=sqrt(x^2+y^2+z^2)
			double x = event.values[0];
			double y = event.values[1];
			double z = event.values[2];
			double m = Math.sqrt(x*x + y*y + z*z);
						
			// Add m to the mAccBuffer one by one.
			try {
				mAccBuffer.add(new Double(m));
			} catch (IllegalStateException e) {

				// Exception happens when reach the capacity.
				// Doubling the buffer. ListBlockingQueue has no such issue,
				// But generally has worse performance
				ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(
						mAccBuffer.size()*2);
				mAccBuffer.drainTo(newBuf);
				mAccBuffer = newBuf;
				mAccBuffer.add(new Double(m));

			}
		}
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}





}