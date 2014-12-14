package com.example.breakers;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class HelpClass extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_class);
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// get action bar    
	    ActionBar actionBar = getActionBar();
	
	    // Enabling Up / Back navigation 
	    actionBar.setDisplayHomeAsUpEnabled(true);
		return true;
	}

}
