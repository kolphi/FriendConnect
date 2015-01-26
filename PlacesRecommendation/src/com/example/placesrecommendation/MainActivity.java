package com.example.placesrecommendation;

import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	
	
	ArrayList<Place> m_places;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		ListView list = (ListView) findViewById(R.id.listViewWords);
		m_places = initiateSPorts();
		
		
		
		ItemAdapter adapter = new ItemAdapter(this, R.layout.item_placesplaces,
				m_places, getResources().getDrawable(R.drawable.round_borders_item_verb ));
		
		
		list.setAdapter(adapter);
	}

	private ArrayList<Place> initiateSPorts() {
		
		
		ArrayList<Place> places = new ArrayList<Place>();
		
		places.add(new Place("Sports Halle", "Hagenberg Strasse ", "8.00 - 20.00", "8989898989")  ); 
		places.add(new Place("Linz Sports Halle", "Linz Mozart Strasse Nr 8 ", "8.00 - 20.00", "8989898989")  ); 
		places.add(new Place("Austria Tennis Club", "Linz Faust Strasse Nr 8 ", "8.00 - 22.00", "8989898989")  ); 
		
		return places;
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
}



//class ItemAdapter extends ArrayAdapter<String>
class ItemAdapter extends ArrayAdapter<Place>
{
  Activity context; 
  int layoutResourceId;    
  String data[];
  ArrayList<Place> wordsToBeShown;
  Drawable m_drawable;
  
  
  
	 public ItemAdapter(Activity activity, int layoutResourceId,ArrayList<Place> wordsToBeShown, Drawable _drawable) {
		 	super(activity, layoutResourceId, wordsToBeShown);
	        this.layoutResourceId = layoutResourceId;
	        this.context = activity;
	        this.wordsToBeShown = wordsToBeShown;
	        this.m_drawable = _drawable;
	 }
	 @Override
	  public View getView(int i, View convertView, ViewGroup viewGroup) {
		LayoutInflater inflater = context.getLayoutInflater();
		PlaceViews place = new PlaceViews();
		
		
		convertView = inflater.inflate(layoutResourceId, viewGroup, false);
		convertView.setBackground(m_drawable);
		
		
		
		place.nameOrt = (TextView) convertView.findViewById(R.id.CellNameValue);
		place.AddresseKey = (TextView) convertView.findViewById(R.id.CellAddress);
		place.AddresseValue = (TextView) convertView.findViewById(R.id.CellAdresseValue);
		
		
		place.ZeitenKey = (TextView) convertView.findViewById(R.id.CellZeiten);
		place.ZeitenValue = (TextView) convertView.findViewById(R.id.CellZeitenValue);
		
		
		place.TelefonKey = (TextView) convertView.findViewById(R.id.CellTelefon);
		place.TelefonValue = (TextView) convertView.findViewById(R.id.CellTelefonValue);
		

		place.nameOrt.setText(wordsToBeShown.get(i).m_name);
		place.AddresseValue.setText(wordsToBeShown.get(i).m_Addresse);
		place.ZeitenValue.setText(wordsToBeShown.get(i).m_Offszeiten);
		place.TelefonValue.setText(wordsToBeShown.get(i).m_telephoneNummar);
		
		convertView.setTag(place);
		/*

		
		word.header.setTextColor(tab.color);
		word.translation.setTextColor(tab.color);
		
		*/
		
	    return convertView; 

	 }
	 
	 static class PlaceViews
	 {

		 TextView nameOrt;
		 
		 TextView ZeitenKey;
		 
		 TextView AddresseKey;
		 
		 TextView TelefonKey;
		 
		 
		 TextView ZeitenValue;
		 
		 TextView AddresseValue;
		 
		 TextView TelefonValue;
		 
		 

	 }
	 
}

