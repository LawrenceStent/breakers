package com.example.breakers;

import android.app.Activity;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListAdapter extends BaseAdapter
{
	//variables for this class
	Context con;
	private Activity activity;
	private String data;
	private ArrayList listData;
	private static LayoutInflater layInflate = null;
	public ImageLoader imageLoader;
	private List<SurfInfo> surfInfoList = null;
	private ArrayList<SurfInfo> arrayList;
	private String spotName;
	
	//class constructor
	public CustomListAdapter(Context con, List<SurfInfo> surfInfoList, String spotName)
	{
		
		this.con = con;
		this.surfInfoList = surfInfoList;
		this.spotName = spotName;
		layInflate = LayoutInflater.from(con);
		this.arrayList = new ArrayList<SurfInfo>();
		this.arrayList.addAll(surfInfoList);
		imageLoader = new ImageLoader(con);
		
	}
	//view holder is the place for each item within each row of the listview 
	static class ViewHolder 
	{
		TextView nameText;
		TextView descriptionText;
		//date isn't used yet so is left out
		//TextView dateText;
		ImageView imgView;
	}
	
	//the
	@Override
	public int getCount()
	{
		return surfInfoList.size();
	}
	
	@Override
	public Object getItem(int position)
	{
		return surfInfoList.get(position);
	}
	
	@Override
	public long getItemId(int position)
	{
		return position;
	}
	
	//this method gets the right values for each item in the row.
	//it gets each value and then sets them
	//it then calls the imageLoader class to assign the image to the view holder
	public View getView(int position, View view, ViewGroup parent)
	{
		//View view = convertView;
		final ViewHolder hold;
		if(view == null)
		{
			hold = new ViewHolder();
			view = layInflate.inflate(R.layout.simplerow, null);
			
			hold.nameText = (TextView)view.findViewById(R.id.name);
			hold.descriptionText = (TextView)view.findViewById(R.id.description);
			hold.imgView = (ImageView)view.findViewById(R.id.imageOne);
			
			view.setTag(hold);
		}
		else
		{
			hold = (ViewHolder)view.getTag();
		}
		
		hold.nameText.setText(surfInfoList.get(position).getUser());
		hold.descriptionText.setText(surfInfoList.get(position).getDescription());
		
		imageLoader.DisplayImage(surfInfoList.get(position).getPhoto(), hold.imgView, spotName);
		
		//below is the space where if clicked on a photo a new intent will start showing only the image
		//and the user with description... need to be completed so that comments can be added by other users
		
		//not for this version of app but here as it will be needed
		
		//view.setOnClickListener(new OnClickListener() 
		//{
			
		//});
		
		return view;
	}
	
	

}
