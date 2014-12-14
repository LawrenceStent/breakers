package com.example.breakers;

import java.util.ArrayList;
import java.util.List;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseObject;

public class MainActivity extends ActionBarActivity implements OnMapClickListener, OnMarkerClickListener, OnItemSelectedListener {

	//create map, spinner and marker for later use.
	private GoogleMap mMap;
	private Spinner breakNames;
	private Marker myMarker;
	
	//lists for storage purposes
	ArrayList<LatLng> breaks = new ArrayList<LatLng>();
	List<Marker> marks = new ArrayList<Marker>();
	
	//co-ordinates - here I will need to link the maps coordinates of the pin to the name on the database 
	//which will the provide co-ords with name of place. This will then link to server with spot_id,
	//and each photo will have photo_id with a table of comments.
	private static final LatLng MUIZENBERG = new LatLng(-34.108,18.469);
	private static final LatLng LLANDUDNO = new LatLng(-34.007,18.339);
	private static final LatLng KALK_BAY = new LatLng(-34.125, 18.452);
	private static final LatLng GLEN_BEACH = new LatLng(-33.947,18.376);
	private static final LatLng DUNGEONS = new LatLng(-34.062,18.335);
	private static final LatLng HOEK = new LatLng(-34.098,18.322);
	private static final LatLng KOMMETJIE = new LatLng(-34.136,18.322);
	private static final LatLng CAPE_POINT = new LatLng(-34.355, 18.482);
	private static final LatLng DERDE_STEEN = new LatLng(-33.747,18.439);
	private static final LatLng MISTY_CLIFFS = new LatLng(-34.189, 18.364);
	private static final LatLng HOUT_BAY = new LatLng(-34.045,18.353);
	private static final LatLng CAPE_TOWN = new LatLng(-33.925,18.423);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//method call to set up map in app
		setUpMapIfNeeded();
		
		//ParseApp pa = new ParseApp();
		//pa.onCreate();
		//line of coded need to confirm with Parse.com web server that this is a legitimate application using its api
		Parse.initialize(this, "jG9huu1vC5iavn18Pnad6xUhQ0ovKTaEeHWctU1U", "E6upL3s42oNrsmXOHbEjxRSKr1aDUqyyaejcWrIT");
		
		
		//Toast.makeText(getBaseContext(), "Startsssss!!!!!", Toast.LENGTH_LONG).show();
		
		//call method that adds markers for breaks, markers to be in <List><LatLng>
		markerAdd();
		//adding of spinner items to spinner - removed as do not have onclick functionality
		//spinnerAdd();
		
		ParseObject testObject = new ParseObject("TestObject");
		testObject.put("foo", "bar");
		testObject.saveInBackground();
		//breakNames.setOnItemSelectedListener(this);
		
	}

	protected void onResume(Bundle savedInstanceState) 
	{
		super.onResume();
		
		setUpMapIfNeeded();
		markerAdd();
		//spinnerAdd();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.activity_main_actions, menu);
		//getMenuInflater().inflate(R.menu.main, menu);
		
		//this isnt needed in the main map screen
		//CreateMenu(menu);
		return true;
	}
	
	public void CreateMenu(Menu menu)
	{
		MenuItem mnu1 = menu.add(0,0,0,"Camera");
		{
			mnu1.setIcon(R.drawable.ic_action_camera);
			mnu1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		}
	}
	
	private boolean MenuChoice(MenuItem item)
	{
		switch(item.getItemId())
		{
		case 0 :
			return true;
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//show the map in the map fragment on first activity
	private void setUpMapIfNeeded() 
	{
		
		//add a marker listener to this so that the marker pin can be clicked
		//mMap.setOnMapClickListener(this);
	    // Do a null check to confirm that we have not already instantiated the map.
	    if (mMap == null) {
	        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
	                            .getMap();
	        
	        // Check if we were successful in obtaining the map.
	        if (mMap != null) {
	            // The Map is verified. It is now safe to manipulate the map.

	        }
	    }
	    //postions the maps camera on create
	    mMap.moveCamera(CameraUpdateFactory.newLatLng(CAPE_TOWN));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
	  
	}
	
	public void onMapClick(LatLng location) 
	{
		int count = 1;
		
		Log.v("onMapClick" + count, location.toString());
		pinClicked(null, location);
		
		count++;
	}
	
	public void pinClicked(View v, LatLng locate) 
	{
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locate, 16));

	    // You can customize the marker image using images bundled with
	    // your app, or dynamically generated bitmaps.
		
		//do not need to add a pin - that is in 'Add Spot'.
	    /*mMap.addMarker(new MarkerOptions()
	            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_surfing))
	            .anchor(0.0f, 1.0f) // Anchors the marker on the bottom left
	            .position(locate));*/
	}
	
	//this method adds all the markers to the map. Will make it more efficient
	//for now i wanted to add it to a List to use them dynamically across the application
	public void markerAdd()
	{
		//add a marker listener to this so that the marker pin can be clicked
		mMap.setOnMarkerClickListener(this);
		 
		//breakNames = (Spinner)findViewById(R.id.breakNames);
		
		breaks.add(CAPE_POINT);
		breaks.add(DERDE_STEEN);
		breaks.add(DUNGEONS);
		breaks.add(GLEN_BEACH);
		breaks.add(HOEK);
		breaks.add(HOUT_BAY);
		breaks.add(KALK_BAY);
		breaks.add(KOMMETJIE);
		breaks.add(LLANDUDNO);
		breaks.add(MISTY_CLIFFS);
		breaks.add(MUIZENBERG);
		
		myMarker= mMap.addMarker(new MarkerOptions().position(breaks.get(0)).title("CAPE POINT"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(DERDE_STEEN).title("DERDE STEEN"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(DUNGEONS).title("DUNGEONS"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(GLEN_BEACH).title("GLEN BEACH"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(HOEK).title("HOEK"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(HOUT_BAY).title("HOUT BAY"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(KALK_BAY).title("KALK BAY"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(KOMMETJIE).title("KOMMETJIE"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(LLANDUDNO).title("LLANDUDNO"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(MISTY_CLIFFS).title("MISTY CLIFFS"));
		marks.add(myMarker);
		myMarker= mMap.addMarker(new MarkerOptions().position(MUIZENBERG).title("MUIZENBERG"));
		marks.add(myMarker);
		
		
	}
	
	//on the marker click the new activity is opened.
	@Override 
    public boolean onMarkerClick(final Marker marker) 
	{
 
		Marker markyMark = null;
		
		String pin = "";
    	//on marker click retrieve the given title of the marker
    	pin = marker.getTitle().toString();
    	
    	//int position = 0;
		
    	//loops through to find the matching marker to the clicked pin's title
    	for(int i = 0; i < marks.size(); i++)
    	{
    		markyMark = marks.get(i);
    		markyMark.getTitle().toString();
    		
    		if(markyMark.getTitle().toString().equals(pin))
    		{
    			//sets the local markers title
    			markyMark.setTitle(marks.get(i).getTitle().toString());
    			//ends the loop when the correct marker is found in the list
    			i = marks.size();
    		}
    	}
    	
    	
        if (marker.equals(markyMark)) 
        { 
        	//use of intent to pass data between classes and start activities
        	Intent myIntent = new Intent(MainActivity.this, breakInfoClass.class);
        	myIntent.putExtra("Spot", pin); //Optional parameters
        	MainActivity.this.startActivity(myIntent);
        	Toast.makeText(getBaseContext(), "Gets to here", Toast.LENGTH_LONG).show();
        	Log.d(null, pin);
        }
        return false;
    } 
	
	//method to add all markers in List to spinner item
	/*public void spinnerAdd()
	{
		//breakNames = (Spinner)findViewById(R.id.breakNames);
		Marker markyMark = null;
		String spinName = "";
		List<String> list = new ArrayList<String>();
		
		//loop through markers adding each one to spinner
		for(int i = 0; i < marks.size(); i++)
    	{
    		markyMark = marks.get(i);
    		spinName = markyMark.getTitle().toString();
    		list.add(spinName);
    	}
		
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,list);
          
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
          
		breakNames.setAdapter(dataAdapter);

		// Spinner item selection Listener   
		//addListenerOnSpinnerItemSelection(); 
	}*/
	
	public void helpButton(View view)
	{
		Intent myIntent = new Intent(MainActivity.this, HelpClass.class);
    	MainActivity.this.startActivity(myIntent);
	}
	

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) 
	{
		// TODO Auto-generated method stub
		// On selecting a spinner item 
        String item = arg0.getItemAtPosition(arg2).toString(); 
        Intent myIntent = new Intent(MainActivity.this, breakInfoClass.class);
    	myIntent.putExtra("Spot", item); //Optional parameters
    	MainActivity.this.startActivity(myIntent);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) 
	{
		// TODO Auto-generated method stub
	}
	
	
	}