package com.symbol.advancedscanningtutorial;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKManager.FEATURE_TYPE;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.ScanDataCollection.LabelType;
import com.symbol.emdk.barcode.ScanDataCollection.ScanData;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.Scanner.DataListener;
import com.symbol.emdk.barcode.Scanner.StatusListener;
import com.symbol.emdk.barcode.Scanner.TriggerType;
import com.symbol.emdk.barcode.ScannerConfig;
import com.symbol.emdk.barcode.ScannerConfig.AudioStreamType;
import com.symbol.emdk.barcode.ScannerConfig.IlluminationMode;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;
import com.symbol.emdk.barcode.StatusData.ScannerStates;

public class MainActivity extends Activity implements EMDKListener,
		StatusListener, DataListener, OnCheckedChangeListener {

	// Declare a variable to store EMDKManager object
	private EMDKManager emdkManager = null;

	// Declare a variable to store Barcode Manager object
	private BarcodeManager barcodeManager = null;

	// Declare a variable to hold scanner device to scan
	private Scanner scanner = null;

	// Text view to display status of EMDK and Barcode Scanning Operations
	private TextView statusTextView = null;

	// Edit Text that is used to display scanned barcode data
	private EditText dataView = null;

	// CheckBox to set Decoder Param Code 11;
	private CheckBox checkBoxCode11;

	// CheckBox to set Decoder Param Code 39;
	private CheckBox checkBoxCode39;

	// CheckBox to set Decoder Param Code 128;
	private CheckBox checkBoxCode128;

	// CheckBox to set Decoder Param Code UPCA;
	private CheckBox checkBoxCodeUPCA;

	// CheckBox to set Decoder Param EAN 8;
	private CheckBox checkBoxEAN8;

	// CheckBox to set Decoder Param EAN 13;
	private CheckBox checkBoxEAN13;

	// CheckBox to set Reader Param Illumination Mode;
	private CheckBox checkBoxIlluminationMode;

	// CheckBox to set Scan Param Vibration Mode (decodeHapticFeedback);
	private CheckBox checkBoxVibrationMode;

	// Drop Down for selecting scanner devices
	private Spinner deviceSelectionSpinner;

	// Drop Down for selecting the type of streaming on which the scan beep
	// should
	// be played
	private Spinner scanToneSpinner;

	// Boolean to explain whether the scanning is in progress or not at any
	// specific point of time
	boolean isScanning = false;

	// Array Adapter to hold arrays that are used in various drop downs
	private ArrayAdapter<String> spinnerDataAdapter;

	// List of supported scanner devices
	private List<ScannerInfo> deviceList;

	// Provides current scanner index in the device Selection Spinner
	private int scannerIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Reference to UI elements
		statusTextView = (TextView) findViewById(R.id.textViewStatus);
		dataView = (EditText) findViewById(R.id.editText1);
		checkBoxCode11 = (CheckBox) findViewById(R.id.checkBoxCode11);
		checkBoxCode39 = (CheckBox) findViewById(R.id.checkBoxCode39);
		checkBoxCode128 = (CheckBox) findViewById(R.id.checkBoxCode128);
		checkBoxCodeUPCA = (CheckBox) findViewById(R.id.checkBoxUPCA);
		checkBoxEAN8 = (CheckBox) findViewById(R.id.checkBoxEan8);
		checkBoxEAN13 = (CheckBox) findViewById(R.id.checkBoxEan13);

		checkBoxIlluminationMode = (CheckBox) findViewById(R.id.illumination);
		checkBoxVibrationMode = (CheckBox) findViewById(R.id.vibration);

		checkBoxCode11.setOnCheckedChangeListener(this);
		checkBoxCode39.setOnCheckedChangeListener(this);
		checkBoxCode128.setOnCheckedChangeListener(this);
		checkBoxCodeUPCA.setOnCheckedChangeListener(this);
		checkBoxEAN8.setOnCheckedChangeListener(this);
		checkBoxEAN13.setOnCheckedChangeListener(this);
		checkBoxIlluminationMode.setOnCheckedChangeListener(this);
		checkBoxVibrationMode.setOnCheckedChangeListener(this);

		deviceSelectionSpinner = (Spinner) findViewById(R.id.device_selection_spinner);
		scanToneSpinner = (Spinner) findViewById(R.id.scan_tone_spinner);

		// Adapter to hold the list of scan tone options
		spinnerDataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, getResources()
						.getStringArray(R.array.scan_tone_array));
		spinnerDataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Set adapter to scan tone drop down
		scanToneSpinner.setAdapter(spinnerDataAdapter);

		// On Item Click Listener of Scanner Devices Spinner
		addSpinnerScannerDevicesListener();

		// On Item Click Listener of Scan Tone Spinner
		addSpinnerScanToneListener();

		// The EMDKManager object will be created and returned in the callback.
		EMDKResults results = EMDKManager.getEMDKManager(
				getApplicationContext(), this);
		// Check the return status of getEMDKManager and update the status Text
		// View accordingly
		if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
			statusTextView.setText("EMDKManager Request Failed");
		}
	}

	// Listener for Scanner Device Spinner
	private void addSpinnerScannerDevicesListener() {

		deviceSelectionSpinner
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View arg1, int position, long arg3) {

						scannerIndex = position;
						try {
							deInitScanner();
							initializeScanner();
							setProfile();
						} catch (ScannerException e) {
							e.printStackTrace();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						// TODO Auto-generated method stub
					}
				});
	}

	// Listener for Scan Tone Spinner
	private void addSpinnerScanToneListener() {

		scanToneSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int position, long arg3) {

				setProfile();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

	// Disable the scanner instance
	private void deInitScanner() {

		if (scanner != null) {
			try {
				scanner.cancelRead();

				scanner.removeDataListener(this);
				scanner.removeStatusListener(this);
				scanner.disable();

			} catch (ScannerException e) {
				// TODO Auto-generated catch block
				statusTextView.setText("Status: " + e.getMessage());
			}
			scanner = null;
		}
	}

	// Method to initialize and enable Scanner and its listeners
	private void initializeScanner() throws ScannerException {

		if (deviceList.size() != 0) {
			scanner = barcodeManager.getDevice(deviceList.get(scannerIndex));
		} else {
			statusTextView
					.setText("Status: "
							+ "Failed to get the specified scanner device! Please close and restart the application.");
		}

		if (scanner != null) {

			// Add data and status listeners
			scanner.addDataListener(this);
			scanner.addStatusListener(this);

			// The trigger type is set to HARD by default and HARD is not
			// implemented in this release.
			// So set to SOFT_ALWAYS
			scanner.triggerType = TriggerType.SOFT_ALWAYS;

			try {
				// Enable the scanner
				scanner.enable();
			} catch (ScannerException e) {
				// TODO Auto-generated catch block
				statusTextView.setText("Status: " + e.getMessage());
			}

		}

	}

	// Sets the user selected Profile
	public void setProfile() {
		try {

			ScannerConfig config = scanner.getConfig();

			// Set code11
			if (checkBoxCode11.isChecked()) {
				config.decoderParams.code11.enabled = true;
			} else {
				config.decoderParams.code11.enabled = false;
			}

			// Set code39
			if (checkBoxCode39.isChecked()) {
				config.decoderParams.code39.enabled = true;
			} else {
				config.decoderParams.code39.enabled = false;
			}

			// Set code128
			if (checkBoxCode128.isChecked()) {
				config.decoderParams.code128.enabled = true;
			} else {
				config.decoderParams.code128.enabled = false;
			}

			// set codeUPCA
			if (checkBoxCodeUPCA.isChecked()) {
				config.decoderParams.upca.enabled = true;
			} else {
				config.decoderParams.upca.enabled = false;
			}

			// set EAN8
			if (checkBoxEAN8.isChecked()) {
				config.decoderParams.ean8.enabled = true;
			} else {
				config.decoderParams.ean8.enabled = false;
			}

			// set EAN13
			if (checkBoxEAN13.isChecked()) {
				config.decoderParams.ean13.enabled = true;
			} else {
				config.decoderParams.ean13.enabled = false;
			}

			// set Illumination Mode, which is available only for
			// INTERNAL_CAMERA1 device type
			if (checkBoxIlluminationMode.isChecked()
					&& deviceSelectionSpinner.getSelectedItem().toString()
							.contains("Camera")) {
				config.readerParams.readerSpecific.cameraSpecific.illuminationMode = IlluminationMode.ON;
			} else {
				config.readerParams.readerSpecific.cameraSpecific.illuminationMode = IlluminationMode.OFF;
			}

			// set Vibration Mode (decodeHapticFeedback)
			if (checkBoxVibrationMode.isChecked()) {
				config.scanParams.decodeHapticFeedback = true;
			} else {
				config.scanParams.decodeHapticFeedback = false;
			}

			// Set the Scan Tone selected from the Scan Tone Spinner
			config.scanParams.audioStreamType = AudioStreamType.RINGER;
			String scanTone = scanToneSpinner.getSelectedItem().toString();
			if (scanTone.contains("NONE"))
				// Silent Mode (No scan tone will be played)
				config.scanParams.decodeAudioFeedbackUri = "";
			else
				// Other selected scan tones from the drop-down
				config.scanParams.decodeAudioFeedbackUri = "system/media/audio/notifications/"
						+ scanTone;

			scanner.setConfig(config);

			Toast.makeText(
					MainActivity.this,
					"Changes Appplied. Press Hard Scan Button to start scanning...",
					Toast.LENGTH_SHORT).show();

		} catch (Exception e) {
			statusTextView.setText(e.toString());
		}
	}

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub
		// The EMDK closed abruptly. // Clean up the objects created by EMDK
		// manager
		if (this.emdkManager != null) {

			this.emdkManager.release();
			this.emdkManager = null;
		}

	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub
		this.emdkManager = emdkManager;

		// Get the Barcode Manager object
		barcodeManager = (BarcodeManager) this.emdkManager
				.getInstance(FEATURE_TYPE.BARCODE);

		try {
			// Get the supported scanner devices
			enumerateScannerDevices();
			initializeScanner();
			setProfile();
		} catch (ScannerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Go through and get the available scanner devices
	private void enumerateScannerDevices() {

		if (barcodeManager != null) {

			List<String> friendlyNameList = new ArrayList<String>();
			int spinnerIndex = 0;
			// Set the default selection in the spinner
			int defaultIndex = 0;

			deviceList = barcodeManager.getSupportedDevicesInfo();

			if (deviceList.size() != 0) {

				Iterator<ScannerInfo> it = deviceList.iterator();
				while (it.hasNext()) {
					ScannerInfo scnInfo = it.next();
					friendlyNameList.add(scnInfo.getFriendlyName());
					if (scnInfo.isDefaultScanner()) {
						defaultIndex = spinnerIndex;
					}
					++spinnerIndex;
				}
			} else {
				statusTextView
						.setText("Status: "
								+ "Failed to get the list of supported scanner devices! Please close and restart the application.");
			}

			spinnerDataAdapter = new ArrayAdapter<String>(MainActivity.this,
					android.R.layout.simple_spinner_item, friendlyNameList);
			spinnerDataAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			deviceSelectionSpinner.setAdapter(spinnerDataAdapter);
			deviceSelectionSpinner.setSelection(defaultIndex);

		}
	}

	// This is a callback method when user presses any hardware button on the
	// device
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		// check for scanner hard key press.
		if ((keyCode == KeyEvent.KEYCODE_BUTTON_L1)
				|| (keyCode == KeyEvent.KEYCODE_BUTTON_R1)) {

			// Skip the key press if the repeat count is not zero.
			if (event.getRepeatCount() != 0) {
				return true;
			}

			try {
				if (scanner == null) {
					initializeScanner();
				}

				if ((scanner != null) && (isScanning == false)) {
					// Starts an asynchronous Scan. The method will not turn on
					// the scanner. It will, however, put the scanner in a state
					// in which the scanner can be turned ON either by pressing
					// a hardware trigger or can be turned ON automatically.
					scanner.read();
				}

			} catch (Exception e) {
				// Display if there is any exception while performing operation
				statusTextView.setText(e.getMessage());
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// This is a callback method when user releases any hardware button on the
	// device
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		// check for scanner trigger key press.
		if ((keyCode == KeyEvent.KEYCODE_BUTTON_L1)
				|| (keyCode == KeyEvent.KEYCODE_BUTTON_R1)) {

			// Skip the key press if the repeat count is not zero.
			if (event.getRepeatCount() != 0) {
				return true;
			}

			try {
				if ((scanner != null) && (isScanning == true)) {
					// This Cancels any pending asynchronous read() calls
					scanner.cancelRead();
				}
			} catch (Exception e) {
				statusTextView.setText(e.getMessage());
			}
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onData(ScanDataCollection scanDataCollection) {
		// TODO Auto-generated method stub
		// Use the scanned data, process it on background thread using AsyncTask
		// and update the UI thread with the scanned results
		new AsyncDataUpdate().execute(scanDataCollection);
	}

	// Update the scan data on UI
	int dataLength = 0;

	// AsyncTask that configures the scanned data on background
	// thread and updated the result on UI thread with scanned data and type of
	// label
	private class AsyncDataUpdate extends
			AsyncTask<ScanDataCollection, Void, String> {

		@Override
		protected String doInBackground(ScanDataCollection... params) {
			ScanDataCollection scanDataCollection = params[0];

			// Status string that contains both barcode data and type of barcode
			// that is being scanned
			String statusStr = "";

			// The ScanDataCollection object gives scanning result and the
			// collection of ScanData. So check the data and its status
			if (scanDataCollection != null
					&& scanDataCollection.getResult() == ScannerResults.SUCCESS) {

				ArrayList<ScanData> scanData = scanDataCollection.getScanData();

				// Iterate through scanned data and prepare the statusStr
				for (ScanData data : scanData) {
					// Get the scanned data
					String barcodeDate = data.getData();
					// Get the type of label being scanned
					LabelType labelType = data.getLabelType();
					// Concatenate barcode data and label type
					statusStr = barcodeDate + " " + labelType;
				}
			}

			// Return result to populate on UI thread
			return statusStr;
		}

		@Override
		protected void onPostExecute(String result) {
			// Update the dataView EditText on UI thread with barcode data and
			// its label type
			if (dataLength++ > 50) {
				// Clear the cache after 50 scans
				dataView.getText().clear();
				dataLength = 0;
			}
			dataView.append(result + "\n");
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	@Override
	public void onStatus(StatusData statusData) {
		// TODO Auto-generated method stub
		// process the scan status event on the background thread using
		// AsyncTask and update the UI thread with current scanner state
		new AsyncStatusUpdate().execute(statusData);

	}

	// AsyncTask that configures the current state of scanner on background
	// thread and updates the result on UI thread
	private class AsyncStatusUpdate extends AsyncTask<StatusData, Void, String> {

		@Override
		protected String doInBackground(StatusData... params) {
			// Get the current state of scanner in background
			StatusData statusData = params[0];
			String statusStr = "";
			ScannerStates state = statusData.getState();
			// Different states of Scanner
			switch (state) {
			// Scanner is IDLE
			case IDLE:
				statusStr = "The scanner enabled and its idle";
				isScanning = false;
				break;
			// Scanner is SCANNING
			case SCANNING:
				statusStr = "Scanning..";
				isScanning = true;
				break;
			// Scanner is waiting for trigger press
			case WAITING:
				statusStr = "Waiting for trigger press..";
				break;
			default:
				break;
			}
			// Return result to populate on UI thread
			return statusStr;
		}

		@Override
		protected void onPostExecute(String result) {
			// Update the status text view on UI thread with current scanner
			// state
			statusTextView.setText(result);
		}

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected void onProgressUpdate(Void... values) {
		}
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub

		super.onStop();
		deInitScanner();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (barcodeManager != null)
			barcodeManager = null;

		if (emdkManager != null) {

			// Clean up the objects created by EMDK manager
			emdkManager.release();
			emdkManager = null;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		// TODO Auto-generated method stub
		setProfile();
	}
}
