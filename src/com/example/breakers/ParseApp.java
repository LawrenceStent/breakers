package com.example.breakers;

import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseUser;
import android.app.Application;


//this class was originally used to initialize my connection to the Parse.com server but removed it as did not need it
// -- remains here as a point of reference
public class ParseApp extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();
		
		Parse.initialize(this, "jG9huu1vC5iavn18Pnad6xUhQ0ovKTaEeHWctU1U", "E6upL3s42oNrsmXOHbEjxRSKr1aDUqyyaejcWrIT");
		
		ParseUser.enableAutomaticUser();
		ParseACL defaultACL = new ParseACL();
		
		defaultACL.setPublicReadAccess(true);
		ParseACL.setDefaultACL(defaultACL, true);
	}

	
}
