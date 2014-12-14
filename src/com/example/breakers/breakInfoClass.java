package com.example.breakers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import android.os.AsyncTask;

//--In this class I use an adapter to access the database. The beach name is pulled from the pinClicked
//or from the Spinner. From the database I pull beach information i.e. photos and comments. 
//These are then displayed in the listview
//-- Basis of my Adapter to ListView code: http://developer.android.com/guide/topics/ui/layout/listview.html
//-- As that loads then the API call is made Asynchronously.
//Once I retrieve api info I display it in a textview at the top of the activity in beach_view.xml

//Using the above source code on listItem click, start new activity for Class C... 
public class breakInfoClass extends Activity
{
	//variables used in this class
	String spotName = "";
	String spotApi = "";
	TextView name;
	Context con;
	ImageView imageView;
	View v;
	MainActivity main;
	cameraClass cam = new cameraClass();
	
	private ListView mainListView;
	List<ParseObject> obj;
	ProgressDialog mProgress;
	CustomListAdapter adapter;
	private List<SurfInfo> surfInfoList = null;
	ImageLoader imageLoader = new ImageLoader(this);
	String text;
	TextView apiInfo;
	
	//reference of adding textview dynamically to listview
	//from: http://windrealm.org/tutorials/android/android-listview.php
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.beach_view);
		
		//this gets the name of the spot that was selected in the map screen
		Intent intent = getIntent();
		spotName = intent.getStringExtra("Spot");
		
		new SurfDataTask().execute();
		
		ActionBar action = getActionBar();
		action.setTitle(spotName);
		
		//this button isn't shown but will add it to the bottom actionbar so images can be refreshed
		Button b = (Button)findViewById(R.id.btnRefresh);
		b.setOnClickListener(listener);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		//here the msw api will be called and the necessary info will be sent as a parameter 
		//to the actionbar 
		
		//side note: sending api info to the actionbar didn't work as intended as async takes too long
		//before the action bar is created with the necessary information. so api info is displayed  
		//in a textview via the api's async class
		
		Intent intent = getIntent();
		spotApi = intent.getStringExtra("Spot");
		
		//calling of the api - doesn't affect the application whether it is here or in the onCreate()
		//both are called at the same time.
		apiCall(spotApi);
		
		 // get action bar    
        ActionBar actionBar = getActionBar();
  
        // Enabling Up / Back navigation 
        actionBar.setDisplayHomeAsUpEnabled(true);
		
		//adds items to the 
		CreateMenu(menu);
		return true;
	}
	
	//adding items to the action bar. .png items can be found in the drawables folder
	public void CreateMenu(Menu menu)
	{
		MenuItem mnu1 = menu.add(0,0,0,"Camera");
		{
			mnu1.setIcon(R.drawable.ic_action_camera);
			mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
		

		MenuItem mnu2 = menu.add(0,1,1, text);
		{
			mnu2.setIcon(R.drawable.msw_powered_by);
			mnu2.setTitle(text);
			mnu2.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		}
	}
	
	//on action bar item select
	private boolean MenuChoice(MenuItem item)
	{
		//on select of the camera icon in the action bar it creates an instance of the
		//camera class. this instance automatically opens the capture image
		//once the image is captured the user is taken to a preview screen
		//preview screen can either select upload, retake image or go home
		switch(item.getItemId())
		{
		case 0 : 
			Toast.makeText(getBaseContext(), "Hits this", Toast.LENGTH_SHORT).show();
			Intent newIntent = new Intent(breakInfoClass.this, cameraClass.class);
			newIntent.putExtra("SpotName", spotApi);
			breakInfoClass.this.startActivityForResult(newIntent, 1);
			
			return true;
		}
		return false;
	}
	//method that reads item selected in action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		return MenuChoice(item);
	}
	
	@Override
	public void onDestroy()
	{
		mainListView.setAdapter(null);
		super.onDestroy();
	}
	
	public OnClickListener listener = new OnClickListener()
	{
		@Override
		public void onClick(View arg0)
		{
			adapter.imageLoader.clearCache();
			adapter.notifyDataSetChanged();
		}
	};
	
	
	//below I will attach a class that will first load the latest 10 photos from the beach - 
	//this will also accept photo select, identify the correct photo in the database, open in new view/activity with comments
	//will show number of comments
	
	//photo comments functionality must still be added
	
	public void apiCall(String surfSpot)
	{
		//method call as a switch case for cape town surf spots - name goes in - matches with name and selects spotid!
		int spotId = spotNameToId(surfSpot);
		
		//the url of the api which I place the spotId of the required surf spot 
		//parameterised by adding in only the specific info that I require at the end after the 'fields' area.
		String apiUrl = "http://magicseaweed.com/api/ooS48ZuI0L9qe97beR49q2KEeFu38wG7/forecast/?spot_id="+spotId+"&fields=swell.minBreakingHeight,swell.maxBreakingHeight,wind.compassDirection";
		
		//calling of the api async to read the JSON data
		new ReadJSONFeedTask().execute(apiUrl);
	}
	
	//method that converts spotNames to their required id by the api
	//note - not all spots in my app are tracked by the api - so in long run may have to find a better way to pull
	//surf and wave data as I require more spots to make application work well
	public int spotNameToId(String name)
	{
		int spotId = 0;
		
		switch(name)
		{
		case "CAPE POINT": spotId = 848;//multiple spot num use - not accurate
			break;
		case "DERDE STEEN": spotId = 873;
			break;
		case "DUNGEONS": spotId = 1203;
			break;
		case "GLEN BEACH": spotId = 4425;//multiple spot num use - not accurate
			break;
		case "HOEK": spotId = 3796;
			break;
		case "HOUT BAY": spotId = 1203; //multiple spot num use - not accurate
			break;
		case "KALK BAY": spotId = 847;//multiple spot num use - not accurate
			break;
		case "KOMMETJIE": spotId = 848;
			break;
		case "LLANDUDNO": spotId = 4425;
			break;
		case "MISTY CLIFFS": spotId = 848;//multiple spot num use - not accurate
			break;
		case "MUIZENBERG": spotId = 847;
			break;
		default:
			return 0;
			
		
		}
		return spotId;
	}
	//this is a method which connects to the internet client to pull the JSON data
	public String readJSONFeed(String url)
	{
		StringBuilder build = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		
		try
		{
			//reads the api url to get a response
			HttpResponse response = client.execute(httpGet);
			StatusLine status = response.getStatusLine();
			int statusCode = status.getStatusCode();
			
			//if all ok! pull information and create json data
			if(statusCode == 200)
			{
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				
				String line;
				while((line = reader.readLine()) != null)
				{
					build.append(line);
				}
			}
			else
			{
				Log.e("JSON", "Failed to download file");
			}
		}
		catch(ClientProtocolException e)
		{
			e.printStackTrace();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return build.toString();
	}
	//Async class that pulls JSON info then iterates through it to get the desired data
	private class ReadJSONFeedTask extends AsyncTask<String, Void, String>
	{
		protected String doInBackground(String... urls)
		{
			return readJSONFeed(urls[0]);
		}
		
		protected void onPostExecute(String result)
		{	
			String min = "";
			String max = "";
			String direction = "";
			try
			{
				//JSON array of all the data
				JSONArray infoArray = new JSONArray(result);
				//JSONObject for whole JSON structure
				JSONObject jObjAll = infoArray.getJSONObject(0);
				
				//Get swell block and return min and max wave height info
				JSONObject jObjSwell = jObjAll.getJSONObject("swell");
				min = jObjSwell.getString("minBreakingHeight");
				
				max = jObjSwell.getString("maxBreakingHeight");
				
				//get wind block and return wind direction
				JSONObject jObjWind = jObjAll.getJSONObject("wind");
				
				direction = jObjWind.getString("compassDirection");
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			//assigning the pulled values into single String
			text = min + " - " + max +"ft" + " wind:" + direction;
			//Toast.makeText(getBaseContext(), text, Toast.LENGTH_LONG).show();
			//adding text string with surf data to textview
			apiInfo = (TextView)findViewById(R.id.forecast);
			apiInfo.setText("Surf info: " + text);
		}
	}
	
	//async class that pulls the required photos from the Parse.com website

	private class SurfDataTask extends AsyncTask<Void, Void, Void>
	{
		//preExecute method shows a progress dialog while it loads
		@Override
		protected void onPreExecute()
		{
			super.onPreExecute();
			
			mProgress = new ProgressDialog(breakInfoClass.this);
			
			mProgress.setTitle("Breakers Spot Loading");
			
			mProgress.setMessage("Running...");
			mProgress.setIndeterminate(false);
			
			mProgress.show();
		}
		//this pulls the images and description from the Parse website via their built in query function
		@Override
		protected Void doInBackground(Void... params)
		{
			//creates a new instance of the SurfInfo class which gets and sets values
			surfInfoList = new ArrayList<SurfInfo>();
			try
			{
				//this relates the query to the class "SurfSpot" in the online Parse database 
				ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("SurfSpot");
				
				//this orders them by time
				query.orderByAscending("createdAt");
				//this is a select clause for the query that only returns the images for a given spot name
				query.whereEqualTo("spotName", spotName);
				obj = query.find();
				
				//this is adds each listview items details  for each instance of the object
				for(ParseObject surf : obj)
				{
					//this pulls the image
					ParseFile image = (ParseFile)surf.get("image");
					
					//ParseObject com = (ParseObject)surf.get("description");
					//String desc = com.toString();
					
					//new instance of the class "SurfInfo" which sets the data
					SurfInfo cruising = new SurfInfo();
					cruising.setDescription((String)surf.get("description"));
					cruising.setUser((String)surf.get("user"));
					cruising.setPhoto(image.getUrl());
					//this adds the details to an arraylist which references the SurfInfo class
					surfInfoList.add(cruising);
				}
			}
			catch(ParseException e)
			{
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			} catch (com.parse.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		//once the async task is done it adds the data and images to the listview 
		@Override
		protected void onPostExecute(Void result)
		{
			mainListView = (ListView)findViewById(R.id.mainListView);
			//this creates a new instance of the CustomListAdapter class which enables the
			//imageLoader class to work which pulls the images from Parse and enables them
			// to be added to the list view
			adapter = new CustomListAdapter(breakInfoClass.this, surfInfoList, spotName);
			
			mainListView.setAdapter(adapter);
			//destroys the progress dialog.
			mProgress.dismiss();
		}
	}
}
