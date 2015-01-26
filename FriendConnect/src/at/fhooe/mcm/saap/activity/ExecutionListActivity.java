package at.fhooe.mcm.saap.activity;

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import at.fhooe.mcm.saap.R;
import at.fhooe.mcm.saap.database.SQLiteHelper;
import at.fhooe.mcm.saap.model.ActivityModel;

import com.facebook.android.Util;


/**
 * activity where the user will be asked to log in/ log up
 * @author Vlad Herescu
 *
 */
public class ExecutionListActivity extends ListActivity {
	
	private SQLiteHelper db;
	private List<ActivityModel> activityList;

	 public void onCreate(Bundle icicle) {
		    super.onCreate(icicle);
		 // init database if not initialized
			if (db == null) {
				//add activities
				db = new SQLiteHelper(getApplicationContext());	
			}
			List<ActivityModel> activityList = db.getAllActivities();
			ActivityListAdapter adapter = new ActivityListAdapter(activityList, getApplicationContext());
		    setListAdapter(adapter);
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
	
	
	/**
	 * Represents a transaction list adapter.
	 * 
	 */
	private class ActivityListAdapter extends BaseAdapter {

		private List<ActivityModel> activityItems;
		private Context context;

		/**
		 * Instantiates a new transaction list adapter.
		 * 
		 * @param items
		 *            the transaction items to be displayed.
		 * @param context
		 *            the application context.
		 */
		public ActivityListAdapter(List<ActivityModel> items,
				Context context) {
			this.activityItems = items;
			this.context = context;
		}

		@Override
		public int getCount() {
			return activityItems.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View itemView = convertView;

			if (itemView == null) {
				LayoutInflater vi;
				vi = LayoutInflater.from(context);
				itemView = vi.inflate(R.layout.ui_wallet_item_details_data_view_cell, null);
			}
			
			ActivityModel activityItem = activityItems.get(position);
			
			TextView textItemTitle = (TextView) itemView.findViewById(R.id.text_header_view_title);
			TextView textItemSubtitle = (TextView) itemView.findViewById(R.id.text_header_view_subtitle);
			ImageView ivItemIcon = (ImageView) itemView.findViewById(R.id.image_header_view);
			
			//set view for date
			if(activityItem.getName() == null){
				textItemSubtitle.setVisibility(View.GONE);
			}else{
				textItemTitle.setText(activityItem.getName());
			}
			
			//set view for date
			if(activityItem.getLocation() == null && activityItem.getWeather() == null){
				textItemSubtitle.setVisibility(View.GONE);
			}else{
				//textTransactionDate.setText(transactionItem.getDateString());
				textItemSubtitle.setText("Location: " + activityItem.getLocation() + ", Weather" + activityItem.getWeather());
			}
			
			//setting images
			ivItemIcon.setImageResource(R.drawable.ic_launcher);
			
			
			return itemView;

		}

	}
	
}
