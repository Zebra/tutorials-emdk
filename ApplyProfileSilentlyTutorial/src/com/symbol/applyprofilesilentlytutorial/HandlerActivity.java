package com.symbol.applyprofilesilentlytutorial;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

public class HandlerActivity extends Activity implements EMDKListener {
	// Assign the profile name used in EMDKConfig.xml
	private String profileName = "AppManagerProfile";

	// Declare a variable to store ProfileManager object
	private ProfileManager profileManager = null;

	// Declare a variable to store EMDKManager object
	private EMDKManager emdkManager = null;

	// Handler that receives messages from background thread to populate on UI
	// thread.
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			// Retrieve the result string from message using ((String) msg.obj)
			// and Populate it on UI.
			// Since we don't have UI in this tutorial, we will not use this
			// result string
		};
	};

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

	private String applyProfile(String profileName) {
		try {
			// Call process profile to modify the profile of specified profile
			// name
			EMDKResults results = profileManager.processProfile(profileName,
					ProfileManager.PROFILE_FLAG.SET, (String[]) null);

			// Check the return status of processProfile
			if (results.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {

				return "Applying '" + profileName + "' was successful.";

			} else {
				return "Applying '" + profileName + "' failed.";
			}

		} catch (Exception ex) {

			return ex.getMessage();
		}
	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub
		this.emdkManager = emdkManager;

		// Get the ProfileManager object to process the profiles
		profileManager = (ProfileManager) emdkManager
				.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

		final Message msg = mHandler.obtainMessage();

		// Create Runnable instance to Apply Mx Profile on background thread
		Thread background = new Thread(new Runnable() {

			@Override
			public void run() {
				msg.obj = applyProfile(profileName);
				if (msg.obj != null)
					// Send the result in message from background to UI
					// thread for processing.
					mHandler.sendMessage(msg);

			}
		});
		// Start the background Thread
		background.start();
		// Closes the Activity which in turn closes the Application as the
		// application has only one Activity.
		finish();

	}
}
