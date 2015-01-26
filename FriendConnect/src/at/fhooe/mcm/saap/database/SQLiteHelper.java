/**
 * Copyright (C) 2014 NEXPERTS GmbH
 * 
 * The copyright to the computer program(s) herein is the property of NEXPERTS
 * GmbH, Austria. The program(s) may be used and/or copied only with the written
 * permission of NEXPERTS GmbH or in accordance with the terms and conditions
 * stipulated in the agreement/contract under which the program(s) have been
 * supplied.
 * 
 * @author philipp.koller
 * 
 */

package at.fhooe.mcm.saap.database;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import at.fhooe.mcm.saap.model.ActivityModel;
import at.fhooe.mcm.saap.model.InterestModel;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_ACTIVITIES = "tbl_activities";
	public static final String TABLE_INTERESTS = "tbl_interests";

	// Common column names
	private static final String ACTIVITIES_KEY_ID = "activity_name";
	private static final String ACTIVITIES_KEY_TYPE = "activity_type";
	private static final String ACTIVITIES_KEY_LOCATION = "activity_location";
	private static final String ACTIVITIES_KEY_WEATHER = "activity_weather";

	// columns for PAYMENT_INSTRUMENT table
	private static final String INTERESTS_KEY_ID = "interest_id";
	private static final String INTERESTS_KEY_NAME = "interest_name";
	private static final String INTERESTS_KEY_TYPE = "interest_type";

	private static final String DATABASE_NAME = "friendconnect_data";
	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE_ACT = "CREATE TABLE "
			+ TABLE_ACTIVITIES + "(" + ACTIVITIES_KEY_ID
			+ " TEXT PRIMARY KEY," + ACTIVITIES_KEY_TYPE + " TEXT,"
			+ ACTIVITIES_KEY_LOCATION + " TEXT," + ACTIVITIES_KEY_WEATHER + " TEXT )";

	// database creation sql statement
	private static final String DATABASE_CREATE_INT = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_INTERESTS
			+ "("
			+ INTERESTS_KEY_ID
			+ " INTEGER PRIMARY KEY,"
			+ INTERESTS_KEY_NAME
			+ " TEXT, "
			+ INTERESTS_KEY_TYPE
			 + " TEXT )";

	public SQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE_ACT);
		database.execSQL(DATABASE_CREATE_INT);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// drop older tables on upgrade
		Log.w(SQLiteHelper.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACTIVITIES);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTERESTS);
		// create new tables
		onCreate(db);
	}

	/**
	 * Adds a new Activity
	 */
	public boolean addNewActivity(ActivityModel activityModel) {
		
		if (activityModel == null) {
			return false;
		}
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(ACTIVITIES_KEY_ID, activityModel.getName());
		values.put(ACTIVITIES_KEY_TYPE, activityModel.getType());
		values.put(ACTIVITIES_KEY_LOCATION, activityModel.getLocation());
		values.put(ACTIVITIES_KEY_WEATHER, activityModel.getWeather());

		// insert row
		long id = db.insert(TABLE_ACTIVITIES, null, values);

		if (id == -1) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Adds a new Interest
	 * @param interestModel 
	 * @return 
	 */
	public boolean addNewInterest(InterestModel interestModel) {

		if (interestModel == null) {
			return false;
		}
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put(INTERESTS_KEY_ID, interestModel.getId());
			values.put(INTERESTS_KEY_NAME, interestModel.getName());
			values.put(INTERESTS_KEY_TYPE, interestModel.getType());
			
			//insert row
			long id = db.insert(TABLE_INTERESTS, null, values);

			if (id == -1) {
				return false;
			} else {
				return true;
			}
	}

	/**
	 * @param interestModel
	 * @return
	 */
	public int deleteInterest(InterestModel interestModel) {
		SQLiteDatabase db = this.getReadableDatabase();
		int rowcount = db.delete(TABLE_INTERESTS, INTERESTS_KEY_ID + "=?",
				new String[] { interestModel.getId() });
		return rowcount;
	}
	
	/**
	 * getting all interests
	 * @return 
	 **/
	public List<InterestModel> getAllInterests() {
		List<InterestModel> actItems = new ArrayList<InterestModel>();
		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS;

		Log.e("LOG", selectQuery);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (c.moveToFirst()) {
			do {
				InterestModel td = new InterestModel(
						c.getString(c.getColumnIndex(INTERESTS_KEY_ID)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_NAME)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_TYPE)));
				// adding to transactions list

				actItems.add(td);
			} while (c.moveToNext());
		}

		return actItems;
	}

	/**
	 * getting all transactions
	 * @return 
	 **/
	public List<ActivityModel> getAllActivities() {
		List<ActivityModel> actItems = new ArrayList<ActivityModel>();
		String selectQuery = "SELECT  * FROM " + TABLE_ACTIVITIES;

		Log.e("LOG", selectQuery);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (c.moveToFirst()) {
			do {
				ActivityModel td = new ActivityModel(
						c.getString(c.getColumnIndex(INTERESTS_KEY_NAME)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_TYPE)),
						c.getString(c.getColumnIndex(ACTIVITIES_KEY_WEATHER)),
						c.getString(c.getColumnIndex(ACTIVITIES_KEY_LOCATION)));
				// adding to transactions list

				actItems.add(td);
			} while (c.moveToNext());
		}

		return actItems;
	}

	public void deleteAllActivities() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ACTIVITIES, null, null);
	}

	/**
	 * closing database
	 */
	public void closeDB() {
		SQLiteDatabase db = this.getReadableDatabase();
		if (db != null && db.isOpen())
			db.close();

	}
}
