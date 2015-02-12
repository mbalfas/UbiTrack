package edu.dartmouth.cs.myruns5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;



import android.app.Activity;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

// Extra credit: add a birthday field using DatePicker, can save and load
// You can find sample code here:
// http://developer.android.com/resources/tutorials/views/hello-datepicker.html

public class ProfileActivity extends Activity {

	public static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
	public static final int REQUEST_CODE_SELECT_FROM_GALLERY = 1;
	public static final int REQUEST_CODE_CROP_PHOTO = 2;

	private static final String IMAGE_UNSPECIFIED = "image/*";
	private static final String URI_INSTANCE_STATE_KEY = "saved_uri";

	private Uri mImageCaptureUri;
	private ImageView mImageView;
	private boolean isTakenFromCamera;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);
		mImageView = (ImageView) findViewById(R.id.imageProfile);
		
		if (savedInstanceState != null) {
			mImageCaptureUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);
		}
		
		// Load the previously saved profile if there is one
		loadProfile();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Save the image capture uri before the activity goes into background
		outState.putParcelable(URI_INSTANCE_STATE_KEY, mImageCaptureUri);
	}

	// ****************** button click callbacks ***************************//

	public void onSaveClicked(View v) {
		// Save profile
		saveProifle();
		// Making a "toast" informing the user the profile is saved.
		Toast.makeText(getApplicationContext(),
				getString(R.string.ui_profile_toast_save_text),
				Toast.LENGTH_SHORT).show();
		// Close the activity
		finish();
	}

	public void onCancelClicked(View v) {
		// Making a "toast" informing the user changes are canceled.
		Toast.makeText(getApplicationContext(),
				getString(R.string.ui_profile_toast_cancel_text),
				Toast.LENGTH_SHORT).show();
		// Close the activity
		finish();
	}

	public void onChangePhotoClicked(View v) {
		// changing the profile image, show the dialog asking the user
		// to choose between taking a picture and picking from gallery
		// Go to MyRunsDialogFragment for details.
		displayDialog(MyRunsDialogFragment.DIALOG_ID_PHOTO_PICKER);
	}

	// Handle date after activity returns.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK)
			return;

		switch (requestCode) {
		case REQUEST_CODE_TAKE_FROM_CAMERA:
			// Send image taken from camera for cropping
			cropImage();
			break;

		case REQUEST_CODE_SELECT_FROM_GALLERY:
			// Send selected image from gallery for cropping
			mImageCaptureUri = data.getData();
			cropImage();
			break;

		case REQUEST_CODE_CROP_PHOTO:
			// Update image view after image crop
			Bundle extras = data.getExtras();
			// Set the profile image in UI
			if (extras != null) {
				mImageView
						.setImageBitmap((Bitmap) extras.getParcelable("data"));
			}
			
			// Delete temporary image taken by camera after crop.
			if (isTakenFromCamera){
				File f = new File(mImageCaptureUri.getPath());
				if (f.exists()) f.delete();
			}
			
			break;
		}
	}

	// ******* Photo picker dialog related functions ************//

	public void displayDialog(int id) {
		DialogFragment fragment = MyRunsDialogFragment
				.newInstance(id);
		fragment.show(getFragmentManager(),
				getString(R.string.dialog_fragment_tag_photo_picker));
	}
	
	public void onPhotoPickerItemSelected(int item) {
		Intent intent;
		
		switch (item) {
		
		case MyRunsDialogFragment.ID_PHOTO_PICKER_FROM_CAMERA:
			// Take photo from camera，
			// Construct an intent with action
			// MediaStore.ACTION_IMAGE_CAPTURE
			intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			// Construct temporary image path and name to save the taken
			// photo
			mImageCaptureUri = Uri.fromFile(new File(Environment
					.getExternalStorageDirectory(), "tmp_"
					+ String.valueOf(System.currentTimeMillis()) + ".jpg"));
			intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
					mImageCaptureUri);
			intent.putExtra("return-data", true);
			try {
				// Start a camera capturing activity
				// REQUEST_CODE_TAKE_FROM_CAMERA is an integer tag you
				// defined to identify the activity in onActivityResult()
				// when it returns
				startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
			} catch (ActivityNotFoundException e) {
				e.printStackTrace();
			}
			isTakenFromCamera = true;
			break;
			
		case MyRunsDialogFragment.ID_PHOTO_PICKER_FROM_GALLERY:
			// Select from gallery
			intent = new Intent();
			intent.setType(IMAGE_UNSPECIFIED);
			intent.setAction(Intent.ACTION_GET_CONTENT);
			// Start a gallery choosing activity
			// REQUEST_CODE_SELECT_FROM_GALLERY is an integer tag you
			// defined to identify the activity in onActivityResult()
			// when it returns
			startActivityForResult(intent, REQUEST_CODE_SELECT_FROM_GALLERY);
			break;
			
		default:
			return;
		}

	}

	// ****************** private helper functions ***************************//

	private void loadProfile() {

		String key, str_val;
		int int_val;

		// Load and update all profile views
		key = getString(R.string.preference_name);
		SharedPreferences prefs = getSharedPreferences(key, MODE_PRIVATE);

		// Load user name
		key = getString(R.string.preference_key_profile_name);
		str_val = prefs.getString(key, "");
		((EditText) findViewById(R.id.editName)).setText(str_val);

		// Load user email
		key = getString(R.string.preference_key_profile_email);
		str_val = prefs.getString(key, "");
		((EditText) findViewById(R.id.editEmail)).setText(str_val);

		// Load user phone number
		key = getString(R.string.preference_key_profile_phone);
		str_val = prefs.getString(key, "");
		((EditText) findViewById(R.id.editPhone)).setText(str_val);

		// Load gender info and set radio box
		key = getString(R.string.preference_key_profile_gender);
		int_val = prefs.getInt(key, -1);
		if (int_val >= 0) {
			RadioButton radioBtn = (RadioButton) ((RadioGroup) findViewById(R.id.radioGender))
					.getChildAt(int_val);
			radioBtn.setChecked(true);
		}

		// Load class info
		key = getString(R.string.preference_key_profile_class);
		str_val = prefs.getString(key, "");
		((TextView) findViewById(R.id.editClass)).setText(str_val);

		// Load student major info
		key = getString(R.string.preference_key_profile_major);
		str_val = prefs.getString(key, "");
		((TextView) findViewById(R.id.editMajor)).setText(str_val);

		// Load profile photo from internal storage
		try {
			FileInputStream fis = openFileInput(getString(R.string.profile_photo_file_name));
			Bitmap bmap = BitmapFactory.decodeStream(fis);
			mImageView.setImageBitmap(bmap);
			fis.close();
		} catch (IOException e) {
			// Default profile photo if no photo saved before.
			mImageView.setImageResource(R.drawable.default_profile);
		}
	}

	private void saveProifle() {

		String key, str_val;
		int int_val;

		key = getString(R.string.preference_name);
		SharedPreferences prefs = getSharedPreferences(key, MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		// Write screen contents into corresponding editor fields.
		key = getString(R.string.preference_key_profile_name);
		str_val = ((EditText) findViewById(R.id.editName)).getText().toString();
		editor.putString(key, str_val);

		key = getString(R.string.preference_key_profile_email);
		str_val = ((EditText) findViewById(R.id.editEmail)).getText()
				.toString();
		editor.putString(key, str_val);

		key = getString(R.string.preference_key_profile_phone);
		str_val = ((EditText) findViewById(R.id.editPhone)).getText()
				.toString();
		editor.putString(key, str_val);

		key = getString(R.string.preference_key_profile_gender);
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGender);
		int_val = radioGroup.indexOfChild(findViewById(radioGroup
				.getCheckedRadioButtonId()));
		editor.putInt(key, int_val);

		key = getString(R.string.preference_key_profile_class);
		str_val = ((EditText) findViewById(R.id.editClass)).getText()
				.toString();
		editor.putString(key, str_val);

		key = getString(R.string.preference_key_profile_major);
		str_val = ((EditText) findViewById(R.id.editMajor)).getText()
				.toString();
		editor.putString(key, str_val);

		// Commit all the changes into preference file
		editor.apply();

		// Save profile image into internal storage.
		mImageView.buildDrawingCache();
		Bitmap bmap = mImageView.getDrawingCache();
		try {
			FileOutputStream fos = openFileOutput(
					getString(R.string.profile_photo_file_name), MODE_PRIVATE);
			bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// Crop and resize the image for profile
	private void cropImage() {
		// Use existing crop activity.
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(mImageCaptureUri, IMAGE_UNSPECIFIED);

		// Specify image size
		intent.putExtra("outputX", 100);
		intent.putExtra("outputY", 100);

		// Specify aspect ratio, 1:1
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("scale", true);
		intent.putExtra("return-data", true);
		// REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
		// identify the activity in onActivityResult() when it returns
		startActivityForResult(intent, REQUEST_CODE_CROP_PHOTO);
	}

}