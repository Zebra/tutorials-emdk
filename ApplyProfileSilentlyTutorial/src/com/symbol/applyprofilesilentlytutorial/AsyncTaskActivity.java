package com.symbol.applyprofilesilentlytutorial;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

public class AsyncTaskActivity extends Activity implements EMDKListener {
	// Assign the profile name used in EMDKConfig.xml
	private String profileName = "AppManagerProfile";

	// Declare a variable to store ProfileManager object
	private ProfileManager profileManager = null;

	// Declare a variable to store EMDKManager object
	private EMDKManager emdkManager = null;

	// Contains XML while setting a profile programmatically. It can be Null.
	private String[] modifyData;

	// Contains the status of processProfile operation.
	private EMDKResults results;

	// Class extending AsyncTask that runs operations in a background thread
	// without blocking UI thread. It returns result on UI thread once the
	// operation is completed.
	private class ConfigureProfile extends AsyncTask<String, Integer, String> {
		// Operation to be performed on background thread
		@Override
		protected String doInBackground(String... params) {
			modifyData = new String[1];
			// Call processPrfoile with profile name and SET flag to create the
			// profile. The modifyData can be null.
			results = profileManager.processProfile(profileName,
					ProfileManager.PROFILE_FLAG.SET, modifyData);
			return null;
		}

		// Return result to the UI Thread
		@Override
		protected void onPostExecute(String result) {
			// Display Success or Failure Message based on result
			// Check the return status of processProfile
			if (results.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {

				// Success

			} else {

				// Failure
			}

		}

		// Method to implement actions before the AsynTask executes.
		@Override
		protected void onPreExecute() {

		}

		// Method to display updates of operation
		@Override
		protected void onProgressUpdate(Integer... progress) {

		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// The EMDKManager object will be created and returned in the callback.
		EMDKResults results = EMDKManager.getEMDKManager(
				getApplicationContext(), this);

		// Check the return status of getEMDKManager
		if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {

			// EMDKManager object creation success

		} else {

			// EMDKManager object creation failed

		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// Clean up the objects created by EMDK manager
		emdkManager.release();
	}

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub

		// Assign null to the objects
		emdkManager = null;
		profileManager = null;

	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub

		this.emdkManager = emdkManager;

		// Get the ProfileManager object to process the profiles
		profileManager = (ProfileManager) emdkManager
				.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

		if (profileManager != null) {
			// Start EMDK process profile operation on the background thread.
			new ConfigureProfile().execute("");
			finish();
		}

	}
}
