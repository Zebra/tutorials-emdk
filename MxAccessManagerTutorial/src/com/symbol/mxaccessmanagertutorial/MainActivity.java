package com.symbol.mxaccessmanagertutorial;

import java.io.StringReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

public class MainActivity extends Activity implements EMDKListener {
	// Assign the profile name used in EMDKConfig.xml
	private String profileName = "AccessManagerProfile";

	// Declare a variable to store ProfileManager object
	private ProfileManager profileManager = null;

	// Declare a variable to store EMDKManager object
	private EMDKManager emdkManager = null;

	// Contains the parm-error name (sub-feature that has error)
	private String errorName = "";

	// Contains the characteristic-error type (Root feature that has error)
	private String errorType = "";

	// contains the error description for parm or characteristic error.
	private String errorDescription = "";

	// contains status of the profile operation
	private String status = "";

	// Relative Layout that contains entire view when the single user mode with
	// white list is enabled
	private RelativeLayout whiteListLayout;

	// Boolean that indicates whether single user mode with/without white list
	// (False for without and True for with white list)
	private boolean isWhiteListActive = false;

	// Drop Down that contains options for settings menu of the Access Manager
	private Spinner settingsAccessSpinner;

	// Drop Down that contains options for deleting packages from white list
	private Spinner deletePackageSpinner;

	// Edit Text that contains comma separated package names to be removed from
	// white list
	private EditText deletePackageEditText;

	// Drop Down that contains options for Adding packages to white list
	private Spinner addPackageSpinner;

	// Edit Text that contains comma separated package names to be added in the
	// white list
	private EditText AddPackageEditText;

	// Array Adapter to hold arrays that are used in various drop downs
	private ArrayAdapter<String> dataAdapter;

	// Radio Group for holding Radio Buttons that enable user to select option
	// for single user mode with or without white list
	private RadioGroup radioGroup;

	// Holds the position of user selected option in the settings drop down
	private int settingsSpinnerPosition = 0;

	// Holds the position of user selected option in the delete package drop
	// down
	private int deletePackageSpinnerPosition = 0;

	// Holds the position of user selected option in the add package drop down
	private int addPackageSpinnerPosition = 0;

	// Set Button that is used to implement changes made by user in the
	// Access Manager
	private Button setButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Reference of Set Button
		setButton = (Button) findViewById(R.id.buttonSet);

		// On Click Listener Call for Set Button
		setButton.setOnClickListener(onClickListsner);

		// Reference for White List Layout
		whiteListLayout = (RelativeLayout) findViewById(R.id.whitelist_layout);

		// Reference for Radio Group that has Radio Buttons
		radioGroup = (RadioGroup) findViewById(R.id.radioGroupAccess);

		// On Checked Change Listener for Radio Buttons in a Radio Group
		radioGroup.setOnCheckedChangeListener(onCheckedChangeListener);

		// Reference for Add and Delete Package Edit Texts
		deletePackageEditText = (EditText) findViewById(R.id.et_delete_package);
		AddPackageEditText = (EditText) findViewById(R.id.et_add_package);

		// Reference for settings, Add Package and Delete Package drop downs
		settingsAccessSpinner = (Spinner) findViewById(R.id.settings_access_spinner);
		deletePackageSpinner = (Spinner) findViewById(R.id.delete_package_spinner);
		addPackageSpinner = (Spinner) findViewById(R.id.add_package_spinner);

		// Adapter to hold the list system settings menu options
		dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.system_settings_array));
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Set adapter to settings drop down
		settingsAccessSpinner.setAdapter(dataAdapter);

		// Adapter to hold the list delete package options
		dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.delete_packages_array));
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Set adapter to delete package drop down
		deletePackageSpinner.setAdapter(dataAdapter);

		// Adapter to hold the list add package options
		dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.add_packages_array));
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Set adapter to add package drop down
		addPackageSpinner.setAdapter(dataAdapter);

		// Call to common On Item Selected Listener for all drop downs based on
		// view ID.
		settingsAccessSpinner.setOnItemSelectedListener(onItemSelectedListener);
		deletePackageSpinner.setOnItemSelectedListener(onItemSelectedListener);
		addPackageSpinner.setOnItemSelectedListener(onItemSelectedListener);

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

	// On Click Listener for Set Button
	private OnClickListener onClickListsner = new OnClickListener() {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			int id = view.getId();
			switch (id) {
			case R.id.buttonSet:
				// Call modifyAccessManagerProfileXML method to set the user
				// selected changes
				modifyAccessManagerProfileXML(isWhiteListActive);
				break;
			default:
				break;
			}

		}
	};

	// Method to modify Access Manager settings based on user selection and set
	// them in Profile Manager
	private void modifyAccessManagerProfileXML(boolean isWhiteListActive) {

		// Prepare XML to modify the existing profile settings
		String[] modifyData = new String[1];

		// Check if the user has selected with/without white list option
		// from radio group in order to form modifyData xml accordingly.
		if (isWhiteListActive) {
			// User has selected Single User Mode With White list
			modifyData[0] = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
					+ "<characteristic type=\"Profile\">"
					+ "<parm name=\"ProfileName\" value=\"AccessManagerProfile\"/>"
					+ "<characteristic type=\"AccessMgr\">"
					+ "<parm name=\"OperationMode\" value=\"2\"/>"
					+ "<parm name=\"SystemSettings\" value=\""
					+ settingsSpinnerPosition + "\"/>";

			// Set the delete package settings to the xml based on user
			// selection from
			// delete package drop down stored in
			// deletePackageSpinnerPosition integer
			// deletePackageSpinnerPosition = 0 -> Delete No Package
			// deletePackageSpinnerPosition = 1 -> Delete Specific
			// Packages(s)
			// deletePackageSpinnerPosition = 2 -> Delete All Packages
			if (deletePackageSpinnerPosition == 1) {
				// Get package names to be deleted from white list
				modifyData[0] = modifyData[0]
						+ "<parm name=\"DeletePackagesAction\" value=\"1\"/>"
						+ "<parm name=\"DeletePackageNames\" value=\""
						+ deletePackageEditText.getText().toString() + "\"/>";
			} else {
				// Delete No Package or Delete All Package based on
				// deletePackageSpinnerPosition integer
				modifyData[0] = modifyData[0]
						+ "<parm name=\"DeletePackagesAction\" value=\""
						+ deletePackageSpinnerPosition + "\"/>";
			}

			// Set the Add package settings to the xml based on user
			// selection from
			// Add package drop down stored in
			// addPackageSpinnerPosition integer
			// addPackageSpinnerPosition = 0 -> Add No Package
			// addPackageSpinnerPosition = 1 -> Add Specific
			// Packages(s)
			if (addPackageSpinnerPosition == 1) {
				// Get package names to be added in the white list
				modifyData[0] = modifyData[0]
						+ "<parm name=\"AddPackagesAction\" value=\"1\"/>"
						+ "<parm name=\"AddPackageNames\" value=\""
						+ AddPackageEditText.getText().toString() + "\"/>";
			} else {
				// Add No Package
				modifyData[0] = modifyData[0]
						+ "<parm name=\"AddPackagesAction\" value=\""
						+ addPackageSpinnerPosition + "\"/>";
			}

			modifyData[0] = modifyData[0] + "</characteristic>"
					+ "</characteristic>";
		} else {
			// User has selected Single User Mode Without White list
			modifyData[0] = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
					+ "<characteristic type=\"Profile\">"
					+ "<parm name=\"ProfileName\" value=\"AccessManagerProfile\"/>"
					+ "<characteristic type=\"AccessMgr\">"
					+ "<parm name=\"OperationMode\" value=\"1\"/>"
					+ "</characteristic>" + "</characteristic>";
		}

		// Call process profile to modify the Access Manager Profile of
		// specified profile
		// name based on modifyData XML formed above
		EMDKResults results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.SET, modifyData);

		if (results.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {

			// Method call to handle EMDKResult
			handleEMDKResult(results);

		} else {
			// Show dialog of Failure
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Failure");
			builder.setMessage("Failed to set device clock...")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {

								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	// Method to handle EMDKResult by extracting response and parsing it
	public void handleEMDKResult(EMDKResults results) {
		// Get XML response as a String
		String statusXMLResponse = results.getStatusString();

		try {
			// Create instance of XML Pull Parser to parse the response
			XmlPullParser parser = Xml.newPullParser();
			// Provide the string response to the String Reader that reads
			// for the parser
			parser.setInput(new StringReader(statusXMLResponse));
			// Call method to parse the response
			parseXML(parser);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		// Method call to display results in a dialog
		displayResults();
	}

	// Common On Item Selected Listener for all drop downs based on View ID
	private OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			// Get View Id of user selected drop down
			int spinnerID = parent.getId();
			switch (spinnerID) {
			// System Settings drop down is selected
			case R.id.settings_access_spinner:
				settingsSpinnerPosition = ++position;
				break;
			// Delete Package drop down is selected
			case R.id.delete_package_spinner:
				deletePackageSpinnerPosition = position;
				// Show/Hide delete package Edit Text based on user selection
				if (deletePackageSpinnerPosition == 1)
					// Show the delete package Edit Text
					deletePackageEditText.setVisibility(View.VISIBLE);
				else
					// Hide the delete package Edit Text
					deletePackageEditText.setVisibility(View.GONE);
				break;
			// Add Package drop down is selected
			case R.id.add_package_spinner:
				addPackageSpinnerPosition = position;
				// Show/Hide Add package Edit Text based on user selection
				if (addPackageSpinnerPosition == 1)
					// Show the Add package Edit Text
					AddPackageEditText.setVisibility(View.VISIBLE);
				else
					// Hide the Add package Edit Text
					AddPackageEditText.setVisibility(View.GONE);
				break;
			default:
				break;
			}

		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			// TODO Auto-generated method stub

		}
	};

	// On checked change listener for Radio Buttons of Radio Group
	private OnCheckedChangeListener onCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			// Single User Mode Without White List
			case R.id.radio_without_whitelist:
				// Set the white list flag to false
				isWhiteListActive = false;
				// Clear all the edit texts
				deletePackageEditText.setText("");
				AddPackageEditText.setText("");
				// Hide the White List Layout
				whiteListLayout.setVisibility(View.GONE);
				break;
			// Single User Mode With White List
			case R.id.radio_with_whitelist:
				// Set the white list flag to false
				isWhiteListActive = true;
				// Show the White List Layout
				whiteListLayout.setVisibility(View.VISIBLE);
				break;
			default:
				break;
			}

		}
	};

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		// Clean up the objects created by EMDK manager
		emdkManager.release();
	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub

		// This callback will be issued when the EMDK is ready to use.
		this.emdkManager = emdkManager;

		// Get the ProfileManager object to process the profiles
		profileManager = (ProfileManager) emdkManager
				.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);

		if (profileManager != null) {
			String[] modifyData = new String[1];

			// Call processPrfoile with profile name and SET flag to create the
			// profile. The modifyData can be null.
			EMDKResults results = profileManager.processProfile(profileName,
					ProfileManager.PROFILE_FLAG.SET, modifyData);

			if (results.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {

				// Method call to handle EMDKResult
				handleEMDKResult(results);

			} else {
				// Show dialog of Failure
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Failure");
				builder.setMessage("Failed to apply profile...")
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int id) {

									}
								});
				AlertDialog alert = builder.create();
				alert.show();
			}
		}
	}

	// Method to parse the XML response using XML Pull Parser
	public void parseXML(XmlPullParser myParser) {
		int event;
		try {
			event = myParser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String name = myParser.getName();
				switch (event) {
				case XmlPullParser.START_TAG:
					// Get Status, error name and description in case of
					// parm-error
					if (name.equals("parm-error")) {
						status = "Failure";
						errorName = myParser.getAttributeValue(null, "name");
						errorDescription = myParser.getAttributeValue(null,
								"desc");

						// Get Status, error type and description in case of
						// parm-error
					} else if (name.equals("characteristic-error")) {
						status = "Failure";
						errorType = myParser.getAttributeValue(null, "type");
						errorDescription = myParser.getAttributeValue(null,
								"desc");
					}
					break;
				case XmlPullParser.END_TAG:

					break;
				}
				event = myParser.next();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Method to build failure message that contains name, type and
	// description of respective error (parm, characteristic or both)
	public String buildFailureMessage() {
		String failureMessage = "";
		if (!TextUtils.isEmpty(errorName) && !TextUtils.isEmpty(errorType))
			failureMessage = errorName + " :" + "\n" + errorType + " :" + "\n"
					+ errorDescription;
		else if (!TextUtils.isEmpty(errorName))
			failureMessage = errorName + " :" + "\n" + errorDescription;
		else
			failureMessage = errorType + " :" + "\n" + errorDescription;
		return failureMessage;

	}

	// Method to display results (Status, Error Name, Error Type, Error
	// Description) in a
	// dialog
	public void displayResults() {
		// Alert Dialog to display the status of the Profile creation
		// operation of MX features
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				MainActivity.this);

		if (TextUtils.isEmpty(errorDescription)) {
			alertDialogBuilder.setTitle("Success");
			alertDialogBuilder.setMessage("Profile Successfully Applied...");
		} else {
			// set title
			alertDialogBuilder.setTitle(status);
			// call buildFailureMessage() method to set failure message in
			// dialog
			alertDialogBuilder.setMessage(buildFailureMessage());
		}

		alertDialogBuilder.setCancelable(false).setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
					}
				});
		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}

}
