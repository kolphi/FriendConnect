/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.fhooe.mcm.saap.facebook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import at.fhooe.mcm.saap.R;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.FriendPickerFragment;
import com.facebook.widget.LoginButton;
import com.facebook.widget.PickerFragment;
import com.facebook.widget.PlacePickerFragment;
import com.facebook.widget.ProfilePictureView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;

/**
 * 
 * @author Philipp
 * 
 */
public class HelloFacebookSampleActivity extends FragmentActivity implements
		LocationListener, GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private Location currentLocation;
	private LocationClient locationClient;

	private static final String PERMISSION = "publish_actions";

	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

	private Button postStatusUpdateButton;
	private Button postPhotoButton;
	private Button pickFriendsButton;
	private Button pickPlaceButton;
	private LoginButton loginButton;

	private TextView userTextView;

	private ProfilePictureView profilePictureView;
	private TextView greeting;
	private PendingAction pendingAction = PendingAction.NONE;
	private ViewGroup controlsContainer;
	private GraphUser user;
	private GraphPlace place;
	private List<GraphUser> tags;
	private boolean canPresentShareDialog;
	private boolean canPresentShareDialogWithPhotos;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d("HelloFacebook", "Success!");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		locationClient = new LocationClient(this, this, this);

		if (savedInstanceState != null) {
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.main);

		loginButton = (LoginButton) findViewById(R.id.login_button);
		// loginButton.setFragment(this);
		loginButton.setReadPermissions(Arrays.asList("user_location",
				"user_birthday", "user_likes,", "user_interests",
				"user_activities", "user_friends", "user_actions.books"));
		loginButton
				.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
					@Override
					public void onUserInfoFetched(GraphUser user) {
						HelloFacebookSampleActivity.this.user = user;
						updateUI();
						// It's possible that we were waiting for this.user to
						// be populated in order to post a
						// status update.
						handlePendingAction();
					}
				});

		profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
		greeting = (TextView) findViewById(R.id.greeting);
		userTextView = (TextView) findViewById(R.id.userTextView);

		postStatusUpdateButton = (Button) findViewById(R.id.postStatusUpdateButton);
		postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				onClickPostStatusUpdate();
			}
		});

		postPhotoButton = (Button) findViewById(R.id.postPhotoButton);
		postPhotoButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				onClickPostPhoto();
			}
		});

		pickFriendsButton = (Button) findViewById(R.id.pickFriendsButton);
		pickFriendsButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				onClickPickFriends();
			}
		});

		pickPlaceButton = (Button) findViewById(R.id.pickPlaceButton);
		pickPlaceButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				onClickPickPlace();
			}
		});

		controlsContainer = (ViewGroup) findViewById(R.id.main_ui_container);

		final FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragment_container);
		if (fragment != null) {
			// If we're being re-created and have a fragment, we need to a) hide
			// the main UI controls and
			// b) hook up its listeners again.
			controlsContainer.setVisibility(View.GONE);
			if (fragment instanceof FriendPickerFragment) {
				setFriendPickerListeners((FriendPickerFragment) fragment);
			} else if (fragment instanceof PlacePickerFragment) {
				setPlacePickerListeners((PlacePickerFragment) fragment);
			}
		}

		// Listen for changes in the back stack so we know if a fragment got
		// popped off because the user
		// clicked the back button.
		fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
			@Override
			public void onBackStackChanged() {
				if (fm.getBackStackEntryCount() == 0) {
					// We need to re-show our UI.
					controlsContainer.setVisibility(View.VISIBLE);
				}
			}
		});

		// Can we present the share dialog for regular links?
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG);
		// Can we present the share dialog for photos?
		canPresentShareDialogWithPhotos = FacebookDialog.canPresentShareDialog(
				this, FacebookDialog.ShareDialogFeature.PHOTOS);
	}

	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();

		// Call the 'activateApp' method to log an app event for use in
		// analytics and advertising reporting. Do so in
		// the onResume methods of the primary Activities that an app may be
		// launched into.
		AppEventsLogger.activateApp(this);

		updateUI();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();

		// Call the 'deactivateApp' method to log an app event for use in
		// analytics and advertising
		// reporting. Do so in the onPause methods of the primary Activities
		// that an app may be launched into.
		AppEventsLogger.deactivateApp(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(HelloFacebookSampleActivity.this)
					.setTitle(R.string.cancelled)
					.setMessage(R.string.permission_not_granted)
					.setPositiveButton(R.string.ok, null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		postStatusUpdateButton.setEnabled(enableButtons
				|| canPresentShareDialog);
		postPhotoButton.setEnabled(enableButtons
				|| canPresentShareDialogWithPhotos);
		pickFriendsButton.setEnabled(enableButtons);
		pickPlaceButton.setEnabled(enableButtons);

		if (enableButtons && user != null) {
			profilePictureView.setProfileId(user.getId());
			greeting.setText(getString(R.string.hello_user, user.getFirstName()));
		} else {
			profilePictureView.setProfileId(null);
			greeting.setText(null);
		}
	}

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but
		// we assume they
		// will succeed.
		pendingAction = PendingAction.NONE;

		switch (previouslyPendingAction) {
		case POST_PHOTO:
			postPhoto();
			break;
		case POST_STATUS_UPDATE:
			postStatusUpdate();
			break;
		}
	}

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		String title = null;
		String alertMessage = null;
		if (error == null) {
			title = getString(R.string.success);
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = getString(R.string.successfully_posted_post,
					message, id);
		} else {
			title = getString(R.string.error);
			alertMessage = error.getErrorMessage();
		}

		new AlertDialog.Builder(this).setTitle(title).setMessage(alertMessage)
				.setPositiveButton(R.string.ok, null).show();
	}

	private void onClickPostStatusUpdate() {
		querySportInterests();
		// queryUserData();
		// performPublish(PendingAction.POST_STATUS_UPDATE,
		// canPresentShareDialog);
	}

	private FacebookDialog.ShareDialogBuilder createShareDialogBuilderForLink() {
		return new FacebookDialog.ShareDialogBuilder(this)
				.setName("Hello Facebook")
				.setDescription(
						"The 'Hello Facebook' sample application showcases simple Facebook integration")
				.setLink("http://developers.facebook.com/android");
	}

	private String buildUserInfoDisplay(GraphUser user) {
		StringBuilder userInfo = new StringBuilder("");

		// Example: typed access (name)
		// - no special permissions required
		userInfo.append(String.format("Name: %s\n\n", user.getName()));

		// Example: typed access (birthday)
		// - requires user_birthday permission
		userInfo.append(String.format("Birthday: %s\n\n", user.getBirthday()));

		// Example: partially typed access, to location field,
		// name key (location)
		// - requires user_location permission
		userInfo.append(String.format("Location: %s\n\n", user.getLocation()
				.getProperty("name")));

		// Example: access via property name (locale)
		// - no special permissions required
		userInfo.append(String.format("Locale: %s\n\n",
				user.getProperty("locale")));

		// Example: access via property name (locale)
		// - no special permissions required
		if (user.getProperty("sports") != null) {
			userInfo.append(String.format("favorite_teams: %s\n\n",
					user.getProperty("sports")));
		}
		// Example: access via property name (locale)
		// - no special permissions required
		if (user.getProperty("favorite_teams") != null) {
			userInfo.append(String.format("favorite_teams: %s\n\n",
					user.getProperty("favorite_teams")));
		}
		if (user.getProperty("favorite_athletes") != null) {
			userInfo.append(String.format("favorite_athletes: %s\n\n",
					user.getProperty("favorite_athletes")));
		}

		// Example: access via key for array (languages)
		// - requires user_likes permission
		JSONArray languages = (JSONArray) user.getProperty("languages");
		if (languages != null) {
			if (languages.length() > 0) {
				ArrayList<String> languageNames = new ArrayList<String>();
				for (int i = 0; i < languages.length(); i++) {
					JSONObject language = languages.optJSONObject(i);
					// Add the language name to a list. Use JSON
					// methods to get access to the name field.
					languageNames.add(language.optString("name"));
				}
				userInfo.append(String.format("Languages: %s\n\n",
						languageNames.toString()));
			}
		}

		return userInfo.toString();
	}

	private void querySportInterests() {
		/* make the API call */

		new Request(Session.getActiveSession(), "/me/activities", null,
				HttpMethod.GET, new Request.Callback() {
					public void onCompleted(Response response) {
						/* handle the result */
						response.toString();
						GraphObject g = response.getGraphObject();
						Log.i("INFO", g.toString());
						JSONObject json = g.getInnerJSONObject();
						JSONArray dataArray = null;
						StringBuffer sb = new StringBuffer();
						
						try {
							dataArray = json.getJSONArray("data");
						
						
						for(int i = 0; i< dataArray.length(); i++){
							sb.append(dataArray.getJSONObject(i).getString("name"));
						}
						
						} catch (JSONException e) {
							Log.e("EXC", "JSON-Exception on parsing");
							e.printStackTrace();
						}

						userTextView.setText(sb.toString());
					}
				}).executeAsync();
	}

	private void postStatusUpdate() {
		if (canPresentShareDialog) {
			FacebookDialog shareDialog = createShareDialogBuilderForLink()
					.build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		} else if (user != null && hasPublishPermission()) {
			final String message = getString(R.string.status_update,
					user.getFirstName(), (new Date().toString()));
			Request request = Request.newStatusUpdateRequest(
					Session.getActiveSession(), message, place, tags,
					new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(message,
									response.getGraphObject(),
									response.getError());
						}
					});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_STATUS_UPDATE;
		}
	}

	private void onClickPostPhoto() {
		performPublish(PendingAction.POST_PHOTO,
				canPresentShareDialogWithPhotos);
	}

	private FacebookDialog.PhotoShareDialogBuilder createShareDialogBuilderForPhoto(
			Bitmap... photos) {
		return new FacebookDialog.PhotoShareDialogBuilder(this)
				.addPhotos(Arrays.asList(photos));
	}

	private void postPhoto() {
		Bitmap image = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.ic_launcher);
		if (canPresentShareDialogWithPhotos) {
			FacebookDialog shareDialog = createShareDialogBuilderForPhoto(image)
					.build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		} else if (hasPublishPermission()) {
			Request request = Request.newUploadPhotoRequest(
					Session.getActiveSession(), image, new Request.Callback() {
						@Override
						public void onCompleted(Response response) {
							showPublishResult(getString(R.string.photo_post),
									response.getGraphObject(),
									response.getError());
						}
					});
			request.executeAsync();
		} else {
			pendingAction = PendingAction.POST_PHOTO;
		}
	}

	private void showPickerFragment(PickerFragment<?> fragment) {
		fragment.setOnErrorListener(new PickerFragment.OnErrorListener() {
			@Override
			public void onError(PickerFragment<?> pickerFragment,
					FacebookException error) {
				String text = getString(R.string.exception, error.getMessage());
				Toast toast = Toast.makeText(HelloFacebookSampleActivity.this,
						text, Toast.LENGTH_SHORT);
				toast.show();
			}
		});

		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().replace(R.id.fragment_container, fragment)
				.addToBackStack(null).commit();

		controlsContainer.setVisibility(View.GONE);

		// We want the fragment fully created so we can use it immediately.
		fm.executePendingTransactions();

		fragment.loadData(true);
	}

	private void onClickPickFriends() {
		final FriendPickerFragment fragment = new FriendPickerFragment();

		setFriendPickerListeners(fragment);

		showPickerFragment(fragment);
	}

	private void setFriendPickerListeners(final FriendPickerFragment fragment) {
		fragment.setOnDoneButtonClickedListener(new FriendPickerFragment.OnDoneButtonClickedListener() {
			@Override
			public void onDoneButtonClicked(PickerFragment<?> pickerFragment) {
				onFriendPickerDone(fragment);
			}
		});
	}

	private void onFriendPickerDone(FriendPickerFragment fragment) {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack();

		String results = "";

		List<GraphUser> selection = fragment.getSelection();
		tags = selection;
		if (selection != null && selection.size() > 0) {
			ArrayList<String> names = new ArrayList<String>();
			for (GraphUser user : selection) {
				names.add(user.getName());
			}
			results = TextUtils.join(", ", names);
		} else {
			results = getString(R.string.no_friends_selected);
		}

		showAlert(getString(R.string.you_picked), results);
	}

	private void onPlacePickerDone(PlacePickerFragment fragment) {
		FragmentManager fm = getSupportFragmentManager();
		fm.popBackStack();

		String result = "";

		GraphPlace selection = fragment.getSelection();
		if (selection != null) {
			result = selection.getName();
		} else {
			result = getString(R.string.no_place_selected);
		}

		place = selection;

		showAlert(getString(R.string.you_picked), result);
	}

	private void onClickPickPlace() {
		final PlacePickerFragment fragment = new PlacePickerFragment();
		fragment.setLocation(currentLocation);
		fragment.setTitleText(getString(R.string.pick_seattle_place));

		setPlacePickerListeners(fragment);

		showPickerFragment(fragment);
	}

	private void setPlacePickerListeners(final PlacePickerFragment fragment) {
		fragment.setOnDoneButtonClickedListener(new PlacePickerFragment.OnDoneButtonClickedListener() {
			@Override
			public void onDoneButtonClicked(PickerFragment<?> pickerFragment) {
				onPlacePickerDone(fragment);
			}
		});
		fragment.setOnSelectionChangedListener(new PlacePickerFragment.OnSelectionChangedListener() {
			@Override
			public void onSelectionChanged(PickerFragment<?> pickerFragment) {
				if (fragment.getSelection() != null) {
					onPlacePickerDone(fragment);
				}
			}
		});
	}

	private void showAlert(String title, String message) {
		new AlertDialog.Builder(this).setTitle(title).setMessage(message)
				.setPositiveButton(R.string.ok, null).show();
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	private void performPublish(PendingAction action, boolean allowNoSession) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
				return;
			} else if (session.isOpened()) {
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSION));
				return;
			}
		}

		if (allowNoSession) {
			pendingAction = action;
			handlePendingAction();
		}
	}

	// ////////
	// LocationStuff
	// ///////
	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		locationClient.connect();

	}

	@Override
	protected void onStop() {
		// Disconnecting the client invalidates it.
		locationClient.disconnect();
		super.onStop();
	}

	@Override
	public void onConnected(Bundle arg0) {
		currentLocation = locationClient.getLastLocation();
		if (currentLocation != null) {
			new GetAddressTask(getApplicationContext())
					.execute(currentLocation);
		}

	}

	@Override
	public void onDisconnected() {
		//
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		//

	}

	private class GetAddressTask extends AsyncTask<Location, Void, String> {
		Context context;

		public GetAddressTask(Context context) {
			super();
			this.context = context;
		}

		@Override
		protected String doInBackground(Location... params) {
			Location location = params[0];
			List<Address> addresses = null;
			Geocoder geocoder = new Geocoder(context, Locale.getDefault());
			/*
			 * Return 1 address.
			 */
			try {
				addresses = geocoder.getFromLocation(location.getLatitude(),
						location.getLongitude(), 1);

			} catch (IOException e) {
				e.printStackTrace();
			}
			if (addresses != null && addresses.size() > 0) {
				Address address = addresses.get(0);

				StringBuffer addressText = new StringBuffer();
				addressText.append(address.getAddressLine(0));
				addressText.append(", " + address.getAddressLine(1));

				return new String(addressText);
			} else {
				return "";
			}
		}

		@Override
		protected void onPostExecute(String addressText) {
			// update the address to last transaction in the database
			// db.updateTransactionLocation(transactionID, addressText);
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		if (currentLocation != null) {
			new GetAddressTask(getApplicationContext())
					.execute(currentLocation);
		}
	}

}
