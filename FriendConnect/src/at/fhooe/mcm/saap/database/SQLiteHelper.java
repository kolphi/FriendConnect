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

import com.nexperts.mchipemulation.model.EmvTemplate;
import com.nexperts.mchipemulation.model.PaymentInstrument;
import com.nexperts.mchipemulation.utils.TransactionItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_ACTIVITIES = "tbl_activities";
	public static final String TABLE_INTERESTS = "tbl_interests";

	// Common column names
	private static final String ACTIVITIES_KEY_ID = "activity_id";
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
			+ " INTEGER PRIMARY KEY," + ACTIVITIES_KEY_TYPE + " TEXT,"
			+ ACTIVITIES_KEY_LOCATION + " TEXT," + ACTIVITIES_KEY_WEATHER + " TEXT )";

	// database creation sql statement
	private static final String DATABASE_CREATE_INT = "CREATE TABLE IF NOT EXISTS "
			+ TABLE_INTERESTS
			+ "("
			+ INTERESTS_KEY_ID
			+ " TEXT PRIMARY KEY,"
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

	/*
	 * Creating a transaction
	 */
	public long createTransaction(TransactionItem trans) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(TRANSACTION_KEY_VALUE, trans.getValue());
		values.put(ACTIVITIES_KEY_CURRENCY, trans.getCurrency());
		values.put(ACTIVITIES_KEY_LOCATION, trans.getMerchantName());
		values.put(ACTIVITIES_KEY_WEATHER, trans.getDateString());
		values.put(INTERESTS_KEY_NAME, trans.getDisplayPan());

		// insert row
		long trans_id = db.insert(TABLE_ACTIVITIES, null, values);

		return trans_id;
	}

	/**
	 * 
	 * @param PAYMENT_INSTRUMENT
	 *            the PAYMENT_INSTRUMENT to be added
	 * @return true if the new PAYMENT_INSTRUMENT is added successfully, false
	 *         otherwise
	 */
	public boolean addNewPaymentInstrument(PaymentInstrument paymentInstrument) {

		if (paymentInstrument == null) {
			return false;
		}
		if (instrumentExists(paymentInstrument.getInstrumentId())) {
			updatePaymentInstrument(paymentInstrument);
			return true;
		} else {
			SQLiteDatabase db = this.getWritableDatabase();
			ContentValues values = new ContentValues();

			values.put(INTERESTS_KEY_ID, paymentInstrument.getInstrumentId());
			values.put(INTERESTS_KEY_TYPE, paymentInstrument.getType());
			values.put(PAYMENT_INSTRUMENT_KEY_SECURITY_TYPE,
					paymentInstrument.getSecurityType());
			values.put(PAYMENT_INSTRUMENT_KEY_SECURITY_VALUE,
					paymentInstrument.getSecurityPin());
			values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_CARDHOLDER,
					paymentInstrument.getDisplayHolder());
			values.put(INTERESTS_KEY_NAME, paymentInstrument.getDisplayPan());
			values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_EXPIRY,
					paymentInstrument.getDisplayExpiry());
			values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_CVC,
					paymentInstrument.getDisplayCvc());
			values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_IMAGE,
					paymentInstrument.getDisplayImage());
			values.put(PAYMENT_INSTRUMENT_KEY_EMV,
					paymentInstrument.getEmvDataJsonString());
			values.put(PAYMENT_INSTRUMENT_KEY_LUK,
					paymentInstrument.getLimitedUseKey());
			values.put(PAYMENT_INSTRUMENT_KEY_CMK,
					paymentInstrument.getCardMasterKey());
			values.put(PAYMENT_INSTRUMENT_KEY_SUK_UMD,
					paymentInstrument.getSingleUseKeyUmd());
			values.put(PAYMENT_INSTRUMENT_KEY_SK_MD,
					paymentInstrument.getSessionKeyMd());
			values.put(PAYMENT_INSTRUMENT_KEY_IDN,
					paymentInstrument.getIssuerDynamicNumber());
			values.put(PAYMENT_INSTRUMENT_KEY_PKI,
					paymentInstrument.getPublicKeyIndex());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PKEXP,
					paymentInstrument.getIssuerPublicKeyExponent());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PKREM,
					paymentInstrument.getIssuerPublicKeyRemainder());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PKCERT,
					paymentInstrument.getIssuerPublicKeyCertificate());
			values.put(PAYMENT_INSTRUMENT_KEY_ICC_PKEXP,
					paymentInstrument.getIccPublicKeyExponent());
			values.put(PAYMENT_INSTRUMENT_KEY_ICC_PRK,
					paymentInstrument.getIccPrivateKey());
			values.put(PAYMENT_INSTRUMENT_KEY_ICC_PK_REMAINDER,
					paymentInstrument.getIccPublicKeyRemainder());
			values.put(PAYMENT_INSTRUMENT_KEY_ICC_PKCERT,
					paymentInstrument.getIccPublicKeyCertificate());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_NAME,
					paymentInstrument.getIssuerName());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_WEBSITE,
					paymentInstrument.getIssuerWebsite());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PHONE,
					paymentInstrument.getIssuerPhone());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_TERMS,
					paymentInstrument.getIssuerTerms());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_IMAGE,
					paymentInstrument.getIssuerImage());
			values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_INTENT,
					paymentInstrument.getIssuerPackageName());
			values.put(PAYMENT_INSTRUMENT_KEY_STATE,
					PAYMENT_INSTRUMENT_STATE_AVAILABLE);
			long id = db.insert(TABLE_INTERESTS, null, values);

			if (id == -1) {
				return false;
			} else {
				return true;
			}

		}

	}

	public void updatePaymentInstrument(PaymentInstrument paymentInstrument) {
		if (paymentInstrument == null) {
			return;
		}
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ INTERESTS_KEY_ID + "=?";

		Cursor c = db.rawQuery(selectQuery,
				new String[] { paymentInstrument.getInstrumentId() });
		if (c != null) {
			c.moveToFirst();
		}
		values.put(INTERESTS_KEY_TYPE, paymentInstrument.getType());
		values.put(PAYMENT_INSTRUMENT_KEY_SECURITY_TYPE,
				paymentInstrument.getSecurityType());
		values.put(PAYMENT_INSTRUMENT_KEY_SECURITY_VALUE,
				paymentInstrument.getSecurityPin());
		values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_CARDHOLDER,
				paymentInstrument.getDisplayHolder());
		values.put(INTERESTS_KEY_NAME, paymentInstrument.getDisplayPan());
		values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_EXPIRY,
				paymentInstrument.getDisplayExpiry());
		values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_CVC,
				paymentInstrument.getDisplayCvc());
		values.put(PAYMENT_INSTRUMENT_KEY_DISPLAY_IMAGE,
				paymentInstrument.getDisplayImage());
		values.put(PAYMENT_INSTRUMENT_KEY_EMV,
				paymentInstrument.getEmvDataJsonString());
		values.put(PAYMENT_INSTRUMENT_KEY_LUK,
				paymentInstrument.getLimitedUseKey());
		values.put(PAYMENT_INSTRUMENT_KEY_CMK,
				paymentInstrument.getCardMasterKey());
		values.put(PAYMENT_INSTRUMENT_KEY_SUK_UMD,
				paymentInstrument.getSingleUseKeyUmd());
		values.put(PAYMENT_INSTRUMENT_KEY_SK_MD,
				paymentInstrument.getSessionKeyMd());
		values.put(PAYMENT_INSTRUMENT_KEY_IDN,
				paymentInstrument.getIssuerDynamicNumber());
		values.put(PAYMENT_INSTRUMENT_KEY_PKI,
				paymentInstrument.getPublicKeyIndex());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PKEXP,
				paymentInstrument.getIssuerPublicKeyExponent());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PKREM,
				paymentInstrument.getIssuerPublicKeyRemainder());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PKCERT,
				paymentInstrument.getIssuerPublicKeyCertificate());
		values.put(PAYMENT_INSTRUMENT_KEY_ICC_PKEXP,
				paymentInstrument.getIccPublicKeyExponent());
		values.put(PAYMENT_INSTRUMENT_KEY_ICC_PRK,
				paymentInstrument.getIccPrivateKey());
		values.put(PAYMENT_INSTRUMENT_KEY_ICC_PK_REMAINDER,
				paymentInstrument.getIccPublicKeyRemainder());
		values.put(PAYMENT_INSTRUMENT_KEY_ICC_PKCERT,
				paymentInstrument.getIccPublicKeyCertificate());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_NAME,
				paymentInstrument.getIssuerName());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_WEBSITE,
				paymentInstrument.getIssuerWebsite());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_PHONE,
				paymentInstrument.getIssuerPhone());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_TERMS,
				paymentInstrument.getIssuerTerms());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_IMAGE,
				paymentInstrument.getIssuerImage());
		values.put(PAYMENT_INSTRUMENT_KEY_ISSUER_INTENT,
				paymentInstrument.getIssuerPackageName());
		values.put(PAYMENT_INSTRUMENT_KEY_STATE,
				PAYMENT_INSTRUMENT_STATE_AVAILABLE);
		// updating row
		db.update(TABLE_INTERESTS, values, INTERESTS_KEY_ID + " = ?",
				new String[] { paymentInstrument.getInstrumentId() });
	}

	public boolean instrumentExists(String paymentInstrumentId) {
		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ INTERESTS_KEY_ID + "=?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery,
				new String[] { paymentInstrumentId });
		if (c.getCount() > 0) {
			return true;
		}
		return false;
	}

	public boolean usedInstrumentsAvailable() {
		// String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS
		// + " WHERE " + PAYMENT_INSTRUMENT_KEY_STATE + "=? ORDER BY "
		// + INTERESTS_KEY_ID + " ASC";
		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ PAYMENT_INSTRUMENT_KEY_STATE + "=?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery,
				new String[] { String.valueOf(PAYMENT_INSTRUMENT_STATE_USED) });

		// looping through all rows and adding to list
		if (c.moveToFirst()) {
			return true;
		}
		return false;
	}

	public List<PaymentInstrument> getAvailablePaymentInstruments() {
		List<PaymentInstrument> paymentInstruments = new ArrayList<PaymentInstrument>();
		// String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS
		// + " WHERE " + PAYMENT_INSTRUMENT_KEY_STATE + "=? ORDER BY "
		// + INTERESTS_KEY_ID + " ASC";
		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ PAYMENT_INSTRUMENT_KEY_STATE + "=?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, new String[] { String
				.valueOf(PAYMENT_INSTRUMENT_STATE_AVAILABLE) });

		// looping through all rows and adding to list
		if (c.moveToFirst()) {
			do {
				PaymentInstrument paymentInstrument = new PaymentInstrument(
						c.getString(c.getColumnIndex(INTERESTS_KEY_ID)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_TYPE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SECURITY_TYPE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SECURITY_VALUE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_CARDHOLDER)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_NAME)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_EXPIRY)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_CVC)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_IMAGE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_EMV)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_LUK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_CMK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SUK_UMD)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SK_MD)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_IDN)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_PKI)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKEXP)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKREM)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKCERT)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PKEXP)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PRK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PK_REMAINDER)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PKCERT)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_NAME)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_WEBSITE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PHONE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_TERMS)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_IMAGE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_INTENT)));
				paymentInstruments.add(paymentInstrument);

			} while (c.moveToNext());
		}
		return paymentInstruments;
	}

	public String getNextPaymentInstrumentIdForUse() {
		// String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS
		// + " WHERE " + PAYMENT_INSTRUMENT_KEY_STATE + "=? ORDER BY "
		// + INTERESTS_KEY_ID + " ASC";
		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ PAYMENT_INSTRUMENT_KEY_STATE + "=?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, new String[] { String
				.valueOf(PAYMENT_INSTRUMENT_STATE_AVAILABLE) });

		// looping through all rows and adding to list
		if (c.moveToFirst()) {
			return c.getString(c.getColumnIndex(INTERESTS_KEY_ID));
		}
		return null;
	}

	public PaymentInstrument getNextPaymentInstrumentForUse() {
		// String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS
		// + " WHERE " + PAYMENT_INSTRUMENT_KEY_STATE + "=? ORDER BY "
		// + INTERESTS_KEY_ID + " ASC";
		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ PAYMENT_INSTRUMENT_KEY_STATE + "=?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, new String[] { String
				.valueOf(PAYMENT_INSTRUMENT_STATE_AVAILABLE) });

		if (c != null) {
			if (c.moveToFirst()) {
				PaymentInstrument paymentInstrument = new PaymentInstrument(
						c.getString(c.getColumnIndex(INTERESTS_KEY_ID)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_TYPE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SECURITY_TYPE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SECURITY_VALUE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_CARDHOLDER)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_NAME)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_EXPIRY)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_CVC)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_IMAGE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_EMV)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_LUK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_CMK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SUK_UMD)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SK_MD)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_IDN)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_PKI)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKEXP)),

						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKREM)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKCERT)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PKEXP)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PRK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PK_REMAINDER)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PKCERT)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_NAME)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_WEBSITE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PHONE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_TERMS)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_IMAGE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_INTENT)));

				return paymentInstrument;
			}
		}
		return null;
	}

	public PaymentInstrument getPaymentInstrumentById(String instrumentId) {
		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ INTERESTS_KEY_ID + "=?";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery,
				new String[] { String.valueOf(instrumentId) });

		// looping through all rows and adding to list
		if (c != null) {
			if (c.moveToFirst()) {
				PaymentInstrument paymentInstrument = new PaymentInstrument(
						c.getString(c.getColumnIndex(INTERESTS_KEY_ID)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_TYPE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SECURITY_TYPE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SECURITY_VALUE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_CARDHOLDER)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_NAME)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_EXPIRY)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_CVC)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_DISPLAY_IMAGE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_EMV)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_LUK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_CMK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SUK_UMD)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_SK_MD)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_IDN)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_PKI)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKEXP)),

						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKREM)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PKCERT)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PKEXP)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PRK)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PK_REMAINDER)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ICC_PKCERT)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_NAME)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_WEBSITE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_PHONE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_TERMS)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_IMAGE)),
						c.getString(c
								.getColumnIndex(PAYMENT_INSTRUMENT_KEY_ISSUER_INTENT)));

				return paymentInstrument;
			}
		}
		return null;
	}

	public void updateTransactionLocation(String transactionID, String location) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();

		values.put(ACTIVITIES_KEY_LOCATION, location);
		// updating row
		db.update(TABLE_ACTIVITIES, values, ACTIVITIES_KEY_ID + " = ?",
				new String[] { transactionID });
	}

	public void updateTransactionCounter(String paymentInstrumentId,
			String transactionCounter) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();

		String selectQuery = "SELECT  * FROM " + TABLE_INTERESTS + " WHERE "
				+ INTERESTS_KEY_ID + "=?";

		Cursor c = db.rawQuery(selectQuery,
				new String[] { paymentInstrumentId });
		if (c != null) {
			c.moveToFirst();
		}
		EmvTemplate emv = new EmvTemplate(c.getString(c
				.getColumnIndex(PAYMENT_INSTRUMENT_KEY_EMV)));
		emv.setApplicationTransactionCounter(String.valueOf(transactionCounter));

		values.put(PAYMENT_INSTRUMENT_KEY_EMV, emv.getJsonEncodedString());
		// updating row
		db.update(TABLE_INTERESTS, values, INTERESTS_KEY_ID + " = ?",
				new String[] { paymentInstrumentId });
	}

	public void refereshPaymentInstruments() {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();

		values.put(PAYMENT_INSTRUMENT_KEY_STATE, 0);
		// updating row
		db.update(TABLE_INTERESTS, values, PAYMENT_INSTRUMENT_KEY_STATE
				+ " = ?",
				new String[] { String.valueOf(PAYMENT_INSTRUMENT_STATE_USED) });
	}

	public void setUsedPaymentInstrumentState(
			PaymentInstrument paymentInstrument) {
		SQLiteDatabase db = this.getReadableDatabase();
		ContentValues values = new ContentValues();

		values.put(PAYMENT_INSTRUMENT_KEY_STATE, PAYMENT_INSTRUMENT_STATE_USED);
		// updating row
		db.update(TABLE_INTERESTS, values, INTERESTS_KEY_ID + "=?",
				new String[] { paymentInstrument.getInstrumentId() });
	}

	public int deletePaymentInstrument(PaymentInstrument paymentInstrument) {
		SQLiteDatabase db = this.getReadableDatabase();
		int rowcount = db.delete(TABLE_INTERESTS, INTERESTS_KEY_ID + "=?",
				new String[] { paymentInstrument.getInstrumentId() });
		return rowcount;
	}

	/**
	 * getting all transactions
	 **/
	public List<TransactionItem> getAllTransactions() {
		List<TransactionItem> transItems = new ArrayList<TransactionItem>();
		String selectQuery = "SELECT  * FROM " + TABLE_ACTIVITIES;

		Log.e("LOG", selectQuery);

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (c.moveToFirst()) {
			do {
				TransactionItem td = new TransactionItem(c.getLong(c
						.getColumnIndex(TRANSACTION_KEY_VALUE)), c.getString(c
						.getColumnIndex(ACTIVITIES_KEY_CURRENCY)),
						c.getString(c.getColumnIndex(ACTIVITIES_KEY_LOCATION)),
						c.getString(c.getColumnIndex(ACTIVITIES_KEY_WEATHER)),
						c.getString(c.getColumnIndex(INTERESTS_KEY_NAME)));
				// adding to transactions list

				transItems.add(td);
			} while (c.moveToNext());
		}

		return transItems;
	}

	public void deleteAllTransactions() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_ACTIVITIES, null, null);
	}

	// closing database
	public void closeDB() {
		SQLiteDatabase db = this.getReadableDatabase();
		if (db != null && db.isOpen())
			db.close();

	}
}
