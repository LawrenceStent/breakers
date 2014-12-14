package com.example.breakers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.parse.ParseFile;
import com.parse.ParseObject;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class cameraClass extends Activity
{
		//reference on using a camera on an android phone:
		//http://www.androidhive.info/2013/09/android-working-with-camera-api/
	
		//Activity request codes
		private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
		public static final int MEDIA_TYPE_IMAGE = 1;

		private Bitmap uploadPhoto;
		
		// directory name to store captured images
		private static final String IMAGE_DIRECTORY_NAME = "Breakers photo's";

		// file url to store image
		private Uri fileUri; 

		private ImageView imgPreview;
		private Button btnRetakePicture;
		private Button btnUploadPhoto;
		private String spotName = "";
		private String description = "";
		
		@Override
		protected void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
			setContentView(R.layout.cam_main);

			 //get action bar    
	        ActionBar actionBar = getActionBar();
	  
	        //Enabling Up / Back navigation 
	        actionBar.setDisplayHomeAsUpEnabled(true);
			
			imgPreview = (ImageView) findViewById(R.id.imgPreview);
			btnRetakePicture = (Button) findViewById(R.id.btnRetakePicture);
			btnUploadPhoto = (Button) findViewById(R.id.btnUploadImage);

			
			//button to recall the capture image method if user unhappy with photo 
			btnRetakePicture.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// capture picture
					captureImage();
				}
			});
			
			//here on button click method is called to upload the photo to parse core
			btnUploadPhoto.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) 
				{
					//commented out loadDialog() until I have a more effective way of passing description data
					//currently it is ineffective as my onClick finishes before the data is collected
					//and read, so null values get passed to Parse. The simple and obvious fix is to have it
					//in my cam_main.xml view, however adjusting and adding to it causes errors in other areas
					//of the application. Will fix this for POE.
					
					//loadDialog();
					onSubmitPhoto();
					finish();
				}
				
			});

			captureImage();

			// Checking camera availability
			if (!isDeviceSupportCamera()) {
				Toast.makeText(getApplicationContext(),
						"Sorry! Your device doesn't support camera",
						Toast.LENGTH_LONG).show();
				// will close the app if the device does't have camera
				finish();
			}
		}
		
		//here the captured photo is taken from the device and sent to be stored on Parse
		public void onSubmitPhoto()
		{
			
			String alertDescription = description;
			Intent intent = getIntent();
			spotName = intent.getStringExtra("SpotName");
			
			ParseObject surfPhoto = new ParseObject("SurfSpot");
			//below here I will add fields as in surfspot name and user description to the fields
			//i.e take edittext field (to be placed in cam_main.xml) read and and upload with image to Parse
			
			//here is taking the photo and uploading it to parse containing just the image -- no extra fields
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			uploadPhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] photo = stream.toByteArray();
			
			ParseFile file = new ParseFile("image.png", photo);
			
			file.saveInBackground();
			surfPhoto.put("spotName", spotName);
			surfPhoto.put("image", file);
			surfPhoto.put("description", alertDescription);
			surfPhoto.saveInBackground();
		}
		
		public void loadDialog()
		{
			//inflate the needed xml view
			LayoutInflater inflate = LayoutInflater.from(this);
			final View comment = inflate.inflate(R.layout.alert_text_entry, null);
			
			final EditText comm = (EditText)comment.findViewById(R.id.editComment);
			comm.setText("");

			//create alert dialog
			final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			
			dialog.setIcon(R.drawable.ic_launcher).setTitle("Comments:").setView(comment).setPositiveButton("Save", 
					new DialogInterface.OnClickListener() 
					{
						@Override
						public void onClick(DialogInterface dia, int button) 
						{
							String commented = comm.getText().toString();
							descLoad(commented);
							description = commented;
						}
					});
			dialog.create().show();
			//return description;
		}
		
		private void descLoad(String comment)
		{
			description = comment;
		}
		
		/**
		 * Checking device has camera hardware or not
		 * */
		private boolean isDeviceSupportCamera() {
			if (getApplicationContext().getPackageManager().hasSystemFeature(
					PackageManager.FEATURE_CAMERA)) {
				// this device has a camera
				return true;
			} else {
				// no camera on this device
				return false;
			}
		}

		
		//Capturing Camera Image will lauch camera app requrest image capture 
		private void captureImage() 
		{
			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

			intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

			// start the image capture Intent
			startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
		}
		
		
		 //Here we store the file url as it will be null after returning from camera
		 //app 
		@Override
		protected void onSaveInstanceState(Bundle outState) 
		{
			super.onSaveInstanceState(outState);

			// save file url in bundle as it will be null on scren orientation
			// changes
			outState.putParcelable("file_uri", fileUri);
		}

		@Override
		protected void onRestoreInstanceState(Bundle savedInstanceState)
		{
			super.onRestoreInstanceState(savedInstanceState);

			// get the file url
			fileUri = savedInstanceState.getParcelable("file_uri");
		}
		
		
		//Receiving activity result method will be called after closing the camera
		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) 
		{
			// if the result is capturing Image
			if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
				if (resultCode == RESULT_OK) {
					// successfully captured the image
					// display it in image view
					previewCapturedImage();
				} else if (resultCode == RESULT_CANCELED) {
					// user cancelled Image capture
					Toast.makeText(getApplicationContext(),
							"User cancelled image capture", Toast.LENGTH_SHORT)
							.show();
				} else {
					// failed to capture image
					Toast.makeText(getApplicationContext(),
							"Sorry! Failed to capture image", Toast.LENGTH_SHORT)
							.show();
				}
			} 
		}
		
		
		//Display image from a path to ImageView
		private void previewCapturedImage() 
		{
			try {
				

				imgPreview.setVisibility(View.VISIBLE);

				// bimatp factory
				BitmapFactory.Options options = new BitmapFactory.Options();

				// downsizing image as it throws OutOfMemory Exception for larger
				// images
				options.inSampleSize = 8;

				final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
						options);

				imgPreview.setImageBitmap(bitmap);
				
				uploadPhoto = bitmap;
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
			
			
		}

		
		//Creating file uri to store image 
		public Uri getOutputMediaFileUri(int type) 
		{
			return Uri.fromFile(getOutputMediaFile(type));
		}

		
		//returning image 
		private static File getOutputMediaFile(int type) 
		{

			// External sdcard location
			File mediaStorageDir = new File(
					Environment
							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
					IMAGE_DIRECTORY_NAME);

			// Create the storage directory if it does not exist
			if (!mediaStorageDir.exists()) {
				if (!mediaStorageDir.mkdirs()) {
					Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
							+ IMAGE_DIRECTORY_NAME + " directory");
					return null;
				}
			}

			// Create a media file name
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
					Locale.getDefault()).format(new Date());
			File mediaFile;
			if (type == MEDIA_TYPE_IMAGE) {
				mediaFile = new File(mediaStorageDir.getPath() + File.separator
						+ "IMG_" + timeStamp + ".jpg");
			} 
			else
			{
				return null;
			}

			return mediaFile;
		}


}
