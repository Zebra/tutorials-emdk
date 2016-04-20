package com.symbol.mxpowermanagertutorial;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileManager;

public class MainActivity extends Activity implements EMDKListener {
	// Assign the profile name used in EMDKConfig.xml
	private String profileName = "PowerManagerProfile";

	// Declare a variable to store ProfileManager object
	private ProfileManager profileManager = null;

	// Declare a variable to store EMDKManager object
	private EMDKManager emdkManager = null;

	// Radio Group to hold Radio Buttons for Power Manager Options
	private RadioGroup pwrRadioGroup = null;

	// Edit Text that allows user to enter the path of the update package from
	// external SD Card
	private EditText zipFilePathEditText;

	// String that gets the path of the OS Update Package from Edit Text
	private String zipFilePath;

	// Initial Value of the Power Manager options to be executed in the
	// onOpened() method when the EMDK is ready. Default Value set in the wizard
	// is 0.
	// 0 -> Do Nothing
	// 1 -> Sleep Mode
	// 4 -> Reboot
	// 8 -> OS Update
	private int value = 0;

	// Contains the parm-error name (sub-feature that has error)
	private String errorName = "";

	// Contains the characteristic-error type (Root feature that has error)
	private String errorType = "";

	// contains the error description for parm or characteristic error.
	private String errorDescription = "";

	// contains status of the profile operation
	private String status = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		pwrRadioGroup = (RadioGroup) findViewById(R.id.radioGroupPwr);
		zipFilePathEditText = (EditText) findViewById(R.id.et_zip_file_path);

		// Set on Click listener to the set button to execute Power Manager
		// operations
		addSetButtonListener();

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

	// Method to set on click listener on the Set Button
	private void addSetButtonListener() {

		// Get Reference to the Set Button
		Button setButton = (Button) findViewById(R.id.buttonSet);

		// On Click Listener
		setButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				// Get Reference to the Radio Buttons that show various Power
				// Manager Options
				int radioid = pwrRadioGroup.getCheckedRadioButtonId();

				if (radioid == R.id.radioSuspend)
					value = 1; // 1 - Suspend/ Sleep Mode (Set device to the
								// sleep mode)

				if (radioid == R.id.radioReset)
					value = 4; // 4 - Perform Reset/Reboot (Reboot Device)

				if (radioid == R.id.radioOSUpdate)
					value = 8; // 8 - Perform OS Update

				// Apply Settings selected by user
				modifyProfile_XMLString();
			}
		});

	}

	// Method that applies the modified settings to the EMDK Profile based on
	// user selected options of Power Manager feature.
	private void modifyProfile_XMLString() {

		// Prepare XML to modify the existing profile
		String[] modifyData = new String[1];
		if (value == 8) {
			zipFilePath = zipFilePathEditText.getText().toString();
			// If the OS Package path entered by user is empty then display
			// a Toast
			if (TextUtils.isEmpty(zipFilePath)) {
				Toast.makeText(MainActivity.this, "Incorrect File Path...",
						Toast.LENGTH_SHORT).show();
				return;
			}

			// Modified XML input for OS Update feature that contains path
			// to the update package
			modifyData[0] = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
					+ "<characteristic type=\"Profile\">"
					+ "<parm name=\"ProfileName\" value=\"PowerManagerProfile\"/>"
					+ "<characteristic type=\"PowerMgr\">"
					+ "<parm name=\"ResetAction\" value=\"" + value + "\"/>"
					+ "<characteristic type=\"file-details\">"
					+ "<parm name=\"ZipFile\" value=\"" + zipFilePath + "\"/>"
					+ "</characteristic>" + "</characteristic>"
					+ "</characteristic>";
		} else {
			// Modified XML input for Sleep and Reboot feature based on user
			// selected options of radio button
			// value = 1 -> Sleep Mode
			// value = 4 -> Rebbot
			modifyData[0] = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
					+ "<characteristic type=\"Profile\">"
					+ "<parm name=\"ProfileName\" value=\"PowerManagerProfile\"/>"
					+ "<characteristic type=\"PowerMgr\">"
					+ "<parm name=\"ResetAction\" value=\"" + value + "\"/>"
					+ "</characteristic>" + "</characteristic>";
		}

		// Call process profile to modify the profile of specified profile
		// name
		EMDKResults results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.SET, modifyData);

		if (results.statusCode == EMDKResults.STATUS_CODE.CHECK_XML) {
			// Method call to handle EMDKResult
			handleEMDKResult(results);
		} else {
			// Show dialog of Failure
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Failure");
			builder.setMessage("Failed to apply profile...").setPositiveButton(
					"OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {

						}
					});
			AlertDialog alert = builder.create();
			alert.show();
		}
	}

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub

		// This callback will be issued when the EMDK closes abruptly.

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
	// dialog in case of any errors
	public void displayResults() {

		// Display dialog in case of errors else proceed.
		if (!TextUtils.isEmpty(errorDescription)) {
			// Alert Dialog to display the status of the Profile creation
			// operation of MX features
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					MainActivity.this);
			// set title
			alertDialogBuilder.setTitle(status);
			// call buildFailureMessage() method to set failure message in
			// dialog
			alertDialogBuilder.setMessage(buildFailureMessage());

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

}
