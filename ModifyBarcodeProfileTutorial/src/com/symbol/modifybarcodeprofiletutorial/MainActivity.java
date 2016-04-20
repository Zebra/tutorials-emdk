package com.symbol.modifybarcodeprofiletutorial;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.ProfileConfig;
import com.symbol.emdk.ProfileConfig.DEVICETYPES;
import com.symbol.emdk.ProfileConfig.ENABLED_STATE;
import com.symbol.emdk.ProfileManager;

public class MainActivity extends Activity implements EMDKListener {
	// Assign the profile name used in EMDKConfig.xml
	private String profileName = "ModifyBarcodeProfile";

	// Declare a variable to store ProfileManager object
	private ProfileManager profileManager = null;

	// Declare a variable to store EMDKManager object
	private EMDKManager emdkManager = null;

	// CheckBox for Barcode type 128
	private CheckBox checkBoxCode128;
	// CheckBox for Barcode type 39
	private CheckBox checkBoxCode39;
	// CheckBox for Barcode type EAN8
	private CheckBox checkBoxEAN8;
	// CheckBox for Barcode type EAN13
	private CheckBox checkBoxEAN13;
	// CheckBox for Barcode type UPCA
	private CheckBox checkBoxUPCA;
	// CheckBox for Barcode type UPCE0
	private CheckBox checkBoxUPCE0;

	// Drop down that displays a list of available types of scanners
	private Spinner scannerTypeSpinner;

	// Button for updating changes made in the barcode types.
	private Button setButton;
	// Button to specify current status of the Barcode.
	private Button barcodeStatusButton;

	// Profile Config reference for modifying Profiles.
	private ProfileConfig profileConfigObj = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get references of the checkboxes declared in the UI
		checkBoxCode128 = (CheckBox) findViewById(R.id.checkBoxCode128);
		checkBoxCode39 = (CheckBox) findViewById(R.id.checkBoxCode39);
		checkBoxEAN8 = (CheckBox) findViewById(R.id.checkBoxEAN8);
		checkBoxEAN13 = (CheckBox) findViewById(R.id.checkBoxEAN13);
		checkBoxUPCA = (CheckBox) findViewById(R.id.checkBoxUPCA);
		checkBoxUPCE0 = (CheckBox) findViewById(R.id.checkBoxUPCE0);

		// Get references of all the buttons declared in the UI
		setButton = (Button) findViewById(R.id.btn_set);
		barcodeStatusButton = (Button) findViewById(R.id.btn_barcode);

		// Get the reference scanner type spinner
		scannerTypeSpinner = (Spinner) findViewById(R.id.scanner_type_spinner);

		// List that contains supported scanning device types
		List<String> scannerTypeList = new ArrayList<String>();
		scannerTypeList.add("AUTO");
		scannerTypeList.add("INTERNAL_LASER1");
		scannerTypeList.add("INTERNAL_IMAGER1");
		scannerTypeList.add("INTERNAL_CAMERA1");

		// Adapter to hold the list of scanning device types.
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, scannerTypeList);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// Set the dapater to the spinner
		scannerTypeSpinner.setAdapter(dataAdapter);

		// On Item Selected Listener of Spinner items.
		scannerTypeSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {

						// Set the user selected device type as scanning device.
						setDeviceType(parent.getItemAtPosition(position)
								.toString());
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

						// TODO Auto-generated method stub
					}
				});

		// Set the on click listeners on buttons
		setButton.setOnClickListener(onClickListener);
		barcodeStatusButton.setOnClickListener(onClickListener);

		// The EMDKManager object creation
		// The EMDKManager object will be returned in the callback.
		EMDKResults results = EMDKManager.getEMDKManager(
				getApplicationContext(), this);

		// Check the return status of processProfile
		if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {

			// EMDKManager object creation success

		} else {

			// EMDKManager object creation failed

		}
	}

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub

	}

	// Sets the status of the Barcode (Enable/Disable)
	public void setBarcodeStatus() {
		// Create the ProfileConfig object
		profileConfigObj = new ProfileConfig();

		// Get the ProfileConfig from the profile XML EMDKResults
		EMDKResults results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.GET, profileConfigObj);
		// Check the return status of processProfile
		if (results.statusCode == EMDKResults.STATUS_CODE.FAILURE) {
			Toast.makeText(MainActivity.this, "Failed to get Profile",
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (barcodeStatusButton.getText().toString()
				.equalsIgnoreCase("Enable Barcode")) {
			// Barcode is enabled so disable it.
			profileConfigObj.dataCapture.barcode.scanner_input_enabled = ENABLED_STATE.TRUE;
			barcodeStatusButton.setText("Disable Barcode");
		} else {
			// Barcode is disabled so enable it.
			profileConfigObj.dataCapture.barcode.scanner_input_enabled = ENABLED_STATE.FALSE;
			barcodeStatusButton.setText("Enable Barcode");
		}

		// Call processPrfoile with profile name, SET flag and config data
		// to update the profile
		results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.SET, profileConfigObj);
		// Check the return status of processProfile
		if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
			Toast.makeText(MainActivity.this, "Barcode Status updated",
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.this, "Barcode status update failed",
					Toast.LENGTH_SHORT).show();
		}
	}

	// Updates the profile based on user selected attributes
	public void updateProfile() {
		String resultString = "";
		try {
			// Create the ProfileConfig object
			profileConfigObj = new ProfileConfig();
			// Get the ProfileConfig from the profile XML EMDKResults
			EMDKResults results = profileManager.processProfile(profileName,
					ProfileManager.PROFILE_FLAG.GET, profileConfigObj);

			// Check the return status of processProfile
			if (results.statusCode == EMDKResults.STATUS_CODE.FAILURE) {
				Toast.makeText(MainActivity.this, "Failed to get Profile",
						Toast.LENGTH_SHORT).show();
				return;
			}

			// Set the code128
			if (checkBoxCode128.isChecked()) {
				profileConfigObj.dataCapture.barcode.decoders.code128 = ENABLED_STATE.TRUE;
			} else {
				profileConfigObj.dataCapture.barcode.decoders.code128 = ENABLED_STATE.FALSE;
			}

			// set code39
			if (checkBoxCode39.isChecked()) {
				profileConfigObj.dataCapture.barcode.decoders.code39 = ENABLED_STATE.TRUE;
			} else {
				profileConfigObj.dataCapture.barcode.decoders.code39 = ENABLED_STATE.FALSE;
			}

			// set EAN8
			if (checkBoxEAN8.isChecked()) {
				profileConfigObj.dataCapture.barcode.decoders.ean8 = ENABLED_STATE.TRUE;
			} else {
				profileConfigObj.dataCapture.barcode.decoders.ean8 = ENABLED_STATE.FALSE;
			}

			// set ENA13
			if (checkBoxEAN13.isChecked()) {
				profileConfigObj.dataCapture.barcode.decoders.ean13 = ENABLED_STATE.TRUE;
			} else {
				profileConfigObj.dataCapture.barcode.decoders.ean13 = ENABLED_STATE.FALSE;
			}

			// set upca
			if (checkBoxUPCA.isChecked()) {
				profileConfigObj.dataCapture.barcode.decoders.upca = ENABLED_STATE.TRUE;
			} else {
				profileConfigObj.dataCapture.barcode.decoders.upca = ENABLED_STATE.FALSE;
			}

			// set upce0
			if (checkBoxUPCE0.isChecked()) {
				profileConfigObj.dataCapture.barcode.decoders.upce0 = ENABLED_STATE.TRUE;
			} else {
				profileConfigObj.dataCapture.barcode.decoders.upce0 = ENABLED_STATE.FALSE;
			}

			// Call processPrfoile with profile name, SET flag and config data
			// to update the profile
			results = profileManager.processProfile(profileName,
					ProfileManager.PROFILE_FLAG.SET, profileConfigObj);

			// Check the return status of processProfile
			if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
				Toast.makeText(MainActivity.this,
						"Profile successfully updated", Toast.LENGTH_SHORT)
						.show();
			} else {
				Toast.makeText(MainActivity.this, "Profile update failed",
						Toast.LENGTH_SHORT).show();
			}
		} catch (Exception ex) {
			resultString = ex.getMessage();
			Toast.makeText(MainActivity.this, resultString, Toast.LENGTH_SHORT)
					.show();
		}
	}

	// Common on Click Listener for all buttons
	private OnClickListener onClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			int id = view.getId();
			switch (id) {
			// On Click Listener for Set Button
			case R.id.btn_set:

				// Create the ProfileConfig object
				if (profileConfigObj.dataCapture.barcode.scanner_input_enabled == ENABLED_STATE.FALSE)
					Toast.makeText(MainActivity.this,
							"Please Enable Barcode to update settings...",
							Toast.LENGTH_SHORT).show();
				else
					updateProfile();

				break;
			// On Click Listener for Barcode Button
			case R.id.btn_barcode:

				setBarcodeStatus();
				break;
			default:
				break;
			}
		}
	};

	// Sets the scanner device type selected by user from the spinner
	public void setDeviceType(String deviceType) {
		// Create the ProfileConfig object
		profileConfigObj = new ProfileConfig();
		// Get the ProfileConfig from the profile XML EMDKResults
		EMDKResults results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.GET, profileConfigObj);
		// Check the return status of processProfile
		if (results.statusCode == EMDKResults.STATUS_CODE.FAILURE) {
			Toast.makeText(MainActivity.this, "Failed to get Profile",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// Set the profile config object to the user selected device type from
		// the spinner
		if (deviceType.equalsIgnoreCase("AUTO")) {
			profileConfigObj.dataCapture.barcode.scannerSelection = DEVICETYPES.AUTO;
		} else if (deviceType.equalsIgnoreCase("INTERNAL_LASER1")) {
			profileConfigObj.dataCapture.barcode.scannerSelection = DEVICETYPES.INTERNAL_LASER1;
		} else if (deviceType.equalsIgnoreCase("INTERNAL_CAMERA1")) {
			profileConfigObj.dataCapture.barcode.scannerSelection = DEVICETYPES.INTERNAL_CAMERA1;
		} else {
			profileConfigObj.dataCapture.barcode.scannerSelection = DEVICETYPES.INTERNAL_IMAGER1;
		}

		// Call processPrfoile with profile name, SET flag and config data
		// to update the profile
		results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.SET, profileConfigObj);
		// Check the return status of processProfile
		if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
			// Device type selected
		} else {
			// Failed to select device type
		}
	}

	@Override
	protected void onDestroy() {

		// TODO Auto-generated method stub
		super.onDestroy();

		// Clean up the objects created by EMDK manager
		emdkManager.release();
	}

	// Check and display the status of the Barcode on the button
	// (Enable/Disable)
	public void checkBarcodeStatus() {

		// Create the ProfileConfig object
		profileConfigObj = new ProfileConfig();

		// Get the ProfileConfig from the profile XML EMDKResults
		EMDKResults results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.GET, profileConfigObj);

		// Check the return status of processProfile
		if (results.statusCode == EMDKResults.STATUS_CODE.FAILURE) {
			Toast.makeText(MainActivity.this, "Failed to get Profile",
					Toast.LENGTH_SHORT).show();
			return;
		}

		if (profileConfigObj.dataCapture.barcode.scanner_input_enabled == ENABLED_STATE.FALSE) {
			barcodeStatusButton.setText("Enable Barcode");
		} else {
			barcodeStatusButton.setText("Disable Barcode");
		}

	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub
		this.emdkManager = emdkManager;

		// Get the ProfileManager object to process the profiles
		profileManager = (ProfileManager) emdkManager
				.getInstance(EMDKManager.FEATURE_TYPE.PROFILE);
		String[] modifyData = new String[1];

		// Call processPrfoile with profile name and SET flag to create the
		// profile
		// The modifyData can be null.
		EMDKResults results = profileManager.processProfile(profileName,
				ProfileManager.PROFILE_FLAG.SET, modifyData);

		// Check the return status of processProfile
		if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
			Toast.makeText(MainActivity.this, "Profile initilization Success",
					Toast.LENGTH_SHORT).show();
			checkBarcodeStatus();
		} else {
			Toast.makeText(MainActivity.this, "Profile initilization failed",
					Toast.LENGTH_SHORT).show();
		}

	}
}
