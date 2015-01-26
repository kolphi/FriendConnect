package at.fhooe.mcm.saap.activity;

import java.util.regex.Pattern;

import at.fhooe.mcm.saap.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import at.fhooe.mcm.saap.database.SQLiteHelper;
import at.fhooe.mcm.saap.facebook.HelloFacebookSampleActivity;
import at.fhooe.mcm.saap.model.ActivityModel;
import at.fhooe.mcm.saap.util.ApplicationConstants;

/**
 * Activity for registering a user
 * @author Philipp Koller
 * 
 */
public class RegistrationActivity extends Activity implements OnClickListener {

	// UI Stuff
	private EditText mClubnumber, mFirstName, mLastName, mEmail;
	private CheckBox mCheckbox;
	private Button mAboButton;
	private SQLiteHelper db;
	private ProgressDialog mProgressDialog;
	
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mPrefEditor;
	public final static Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,15}" + ")+");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newsletter);

		// ActionBar actionBar = getActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);
		//
		mClubnumber = new EditText(this);
		mFirstName = new EditText(this);
		mLastName = new EditText(this);
		mEmail = new EditText(this);
		mCheckbox = new CheckBox(this);
		mAboButton = new Button(this);

		mFirstName = (EditText) findViewById(R.id.activity_newsletter_firstname);
		mLastName = (EditText) findViewById(R.id.activity_newsletter_lastname);
		mEmail = (EditText) findViewById(R.id.activity_newsletter_email);
		mCheckbox = (CheckBox) findViewById(R.id.activity_newsletter_checkbox);
		mAboButton = (Button) findViewById(R.id.activity_newsletter_button);

		mAboButton.setOnClickListener(this);

		// shared prefs editor
		mPrefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		mPrefEditor = mPrefs.edit();
		mFirstName.setText(mPrefs.getString(
				ApplicationConstants.PREFERENCE_USERDATA_FIRSTNAME, ""));
		mLastName.setText(mPrefs.getString(
				ApplicationConstants.PREFERENCE_USERDATA_LASTNAME, ""));
		mEmail.setText(mPrefs.getString(
				ApplicationConstants.PREFERENCE_USERDATA_EMAIL, ""));
		

		// init database if not initialized
		if (db == null) {
			//add activities
			db = new SQLiteHelper(getApplicationContext());
			if(db.getAllActivities().isEmpty()){
				db.addNewActivity(new ActivityModel("Soccerfive Indoor", "Soccer", ApplicationConstants.WEATHER_EXCELLENT,"Linz"));
				db.addNewActivity(new ActivityModel("Hagenberg ", "Soccer", ApplicationConstants.WEATHER_OK,"Hagenberg"));
				db.addNewActivity(new ActivityModel("Pregarten Soccer Court", "Soccer", ApplicationConstants.WEATHER_BAD,"Pregarten"));
				db.addNewActivity(new ActivityModel("Badminton Center Linz", "Badminton", ApplicationConstants.WEATHER_EXCELLENT,"Linz"));
				db.addNewActivity(new ActivityModel("Soccerfive", "Soccer", ApplicationConstants.WEATHER_OK,"Linz"));
			}
		}
	}

	/**
	 * Checks if the email address is valid
	 * 
	 * @param String email, the email address to validate
	 * @return boolean, if the email is valid
	 */
	public boolean checkEmail(String email) {
		return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		switch (item.getItemId()) {
//		// Respond to the action bar's Up/Home button
//		case android.R.id.home:
//
//			NavUtils.navigateUpFromSameTask(this);
//			return true;
//		}
//		return super.onOptionsItemSelected(item);
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.activity_newsletter_button:
			// do webservice request

		//	String clubnumber = mClubnumber.getText().toString();
			String firstname = mFirstName.getText().toString();
			String lastname = mLastName.getText().toString();
			String email = mEmail.getText().toString();

			if (checkEmail(email)) {
				if (firstname.length() > 0 && lastname.length() > 0
						&& email.length() > 0) {
					if (mCheckbox.isChecked()) {

						mPrefEditor.putString(
								ApplicationConstants.PREFERENCE_USERDATA_FIRSTNAME, mFirstName
										.getText().toString());
						mPrefEditor.putString(
								ApplicationConstants.PREFERENCE_USERDATA_LASTNAME, mLastName
										.getText().toString());
						mPrefEditor.putString(ApplicationConstants.PREFERENCE_USERDATA_EMAIL,
								mEmail.getText().toString());
						mPrefEditor.commit();
						
						Intent intent = new Intent(this, HelloFacebookSampleActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
								| Intent.FLAG_ACTIVITY_NEW_TASK);

						startActivity(intent);
						//finish();

					} else {
						new AlertDialog.Builder(this)
								.setTitle(getString(R.string.error_agree_title))
								.setMessage(
										getString(R.string.error_agree_message_registration))
								.setPositiveButton(
										getString(R.string.dialog_ok),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												mCheckbox.setChecked(true);
											}
										})
								.setNegativeButton(
										getString(R.string.dialog_no),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												// do nothing
											}
										}).show();
					}
				} else {
					// error type message
					final AlertDialog alertDialog = new AlertDialog.Builder(
							RegistrationActivity.this).create();

					// Setting Dialog Title
					alertDialog
							.setTitle(getString(R.string.error_dialog_title));
					alertDialog
							.setMessage(getString(R.string.error_dialog_nomember_message));
					// Setting OK Button
					alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
							getString(R.string.dialog_ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									alertDialog.cancel();
								}
							});

					alertDialog.show();
				}

			} else {
				final AlertDialog alertDialog = new AlertDialog.Builder(
						RegistrationActivity.this).create();
				// Setting Dialog Title
				alertDialog.setTitle(getString(R.string.error_dialog_title));
				alertDialog
						.setMessage(getString(R.string.error_dialog_nomember_message)
								+ " (Mail)");
				// Setting OK Button
				alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
						getString(R.string.dialog_ok),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								alertDialog.cancel();
							}
						});

				alertDialog.show();
			}

			break;
		}

	}
}
