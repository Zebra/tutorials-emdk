package scanandpairtutorial.symbol.com.scanandpairtutorial;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.scanandpair.ScanAndPairConfig;
import com.symbol.emdk.scanandpair.ScanAndPairManager;
import com.symbol.emdk.scanandpair.ScanAndPairResults;
import com.symbol.emdk.scanandpair.StatusData;

import java.util.ArrayList;


public class MainActivity extends Activity implements EMDKManager.EMDKListener,
        ScanAndPairManager.StatusListener {

    // Text View to hold Bluetooth Name of Remote Scanning device to pair with.
    private EditText btName = null;

    // Text View to hold Bluetooth Address of Remote Scanning Device to pair with.
    private EditText btAddress = null;

    // Select whether to use Hard Scan or Soft Scan option to Scan
    // Bluetooth Name/Address of Remote Scanning device to pair with.
    private CheckBox checkboxHardTrigger = null;

    // CheckBox to indicate whether to perform a scan to get Bluetooth Name/Address of
    // Remote Scanning device or allow user to enter Bluetooth Name/Address of Remote
    // Scanning device to pair with.
    private CheckBox checkBoxAlwaysScan = null;

    // Button to Pair the client application device with Remote Scanning device.
    private Button scanAndPairButton = null;

    // Button to Unpair the client application device with Remote Scanning device.
    private Button scanAndUnpairButton = null;

    // Spinner to display type of data (Bluetooth Name/Address) of Remote Scanning
    // device will be used to pair.
    private Spinner scandataType = null;

    // Text view to display status of Scan and Pair or Unpair Operations
    private TextView statusView = null;

    // Declare a variable to store EMDKManager object
    private EMDKManager emdkManager = null;

    // Declare a variable to store ScanAndPair object
    ScanAndPairManager scanAndPairMgr = null;

    // An interface for notifying client applications to notify scan and pair or unpair events.
    com.symbol.emdk.scanandpair.ScanAndPairManager.StatusListener statusCallbackObj = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // References for UI elements
        btName = (EditText) findViewById(R.id.name);
        btAddress = (EditText) findViewById(R.id.address);
        checkBoxAlwaysScan = (CheckBox) findViewById(R.id.alwaysscan);
        checkboxHardTrigger = (CheckBox) findViewById(R.id.triggerType);
        scanAndPairButton = (Button) findViewById(R.id.scanandpair);
        scanAndUnpairButton = (Button) findViewById(R.id.scanandunpair);
        statusView = (TextView) findViewById(R.id.logs);
        scandataType = (Spinner)findViewById(R.id.scanDataType);
        statusView.setText("\n");

        // Initially disable both Bluetooth Name and Address Checkboxes
        btName.setEnabled(false);
        btAddress.setEnabled(false);

        // The EMDKManager object creation and object will be returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        // Check the return status of getEMDKManager ()
        if (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS) {
            statusView.setText("Please wait, initialization in progress...");
        } else {
            statusView.setText("Initialization failed!");
        }

        // Add supported scan types to an ArrayList
        ArrayList<ScanAndPairConfig.ScanDataType> scanDataTypes =
                new ArrayList<ScanAndPairConfig.ScanDataType>();
        scanDataTypes.add(ScanAndPairConfig.ScanDataType.MAC_ADDRESS);
        scanDataTypes.add(ScanAndPairConfig.ScanDataType.DEVICE_NAME);
        scanDataTypes.add(ScanAndPairConfig.ScanDataType.UNSPECIFIED);

        // Set the scan types list to an Array Adapter and set that Adapter to the Spinner
        ArrayAdapter<ScanAndPairConfig.ScanDataType> arrayAdapter =
                new ArrayAdapter<ScanAndPairConfig.ScanDataType>(getApplicationContext(),
                        R.layout.simple_spinner_item, scanDataTypes);
        scandataType.setAdapter(arrayAdapter);

        // Method call to register onClick listeners of both Pair and Unpair buttons
        registerForButtonEvents ();

        // Method call to register onChecked listener of both checkboxes.
        addCheckBoxListener();
    }

    // Listener for Always Scan checkbox
    private void addCheckBoxListener() {

        checkBoxAlwaysScan.setOnCheckedChangeListener
                (new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btName.setEnabled(false);
                    btAddress.setEnabled(false);
                }
                else {
                    btName.setEnabled(true);
                    btAddress.setEnabled(true);
                }
            }
        });
    }

    // Method that calls onClick listener methods for Pair and Unpair buttons.
    private void registerForButtonEvents() {
        addScanAndPairButtonEvents();
        addScanAndUnpairButtonEvents();
    }

    // Method to perform ScanAndPair Operation
    private void addScanAndPairButtonEvents() {
        scanAndPairButton = (Button) findViewById(R.id.scanandpair);
        scanAndPairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    statusView.setText("ScanAndPair started..."+ "\n");

                    if(scanAndPairMgr == null) {
                        // Get reference to ScanAndPair Manager
                        scanAndPairMgr = (ScanAndPairManager) emdkManager.getInstance
                                (EMDKManager.FEATURE_TYPE.SCANANDPAIR);

                        if(scanAndPairMgr != null) {
                            // Add Status Listener on ScanAndPair Manager
                            scanAndPairMgr.addStatusListener(statusCallbackObj);
                        }
                    }

                    if(scanAndPairMgr != null) {
                        scanAndPairMgr.config.alwaysScan = checkBoxAlwaysScan.isChecked();
                        // Set Notification to Beeper
                        scanAndPairMgr.config.notificationType =
                                ScanAndPairConfig.NotificationType.BEEPER;
                        // If always scan checkbox is unchecked, get details from EditText
                        if(!checkBoxAlwaysScan.isChecked()) {
                            scanAndPairMgr.config.bluetoothInfo.macAddress =
                                    btAddress.getText().toString().trim();
                            scanAndPairMgr.config.bluetoothInfo.deviceName =
                                    btName.getText().toString().trim();
                        }
                        else {
                            // Else scan these details (Bluetooth Name/Address)
                            scanAndPairMgr.config.scanInfo.scanTimeout = 5000;
                            //Set Trigger Type for Scanning
                            if(checkboxHardTrigger.isChecked()) {
                                scanAndPairMgr.config.scanInfo.triggerType =
                                        ScanAndPairConfig.TriggerType.HARD;
                            } else {
                                scanAndPairMgr.config.scanInfo.triggerType =
                                        ScanAndPairConfig.TriggerType.SOFT;
                            }
                            // Set Scan data type the user has selected from spinner
                            scanAndPairMgr.config.scanInfo.scanDataType =
                                    (ScanAndPairConfig.ScanDataType)scandataType.getSelectedItem();
                        }
                        // Perform ScanAndPair operation
                        ScanAndPairResults resultCode = scanAndPairMgr.scanAndPair("0000");
                        // Update the status on StatusView.
                        if(!resultCode.equals(ScanAndPairResults.SUCCESS))
                            statusView.append(resultCode.toString()+ "\n\n");

                    } else {
                        statusView.append("ScanAndPairmanager intialization failed!");
                    }
                } catch (Exception e) {
                    statusView.setText("ScanAndUnpair Error:"+ e.getMessage() + "\n");
                }
            }
        });
    }


    // Method to perform ScanAndUnpair Operation
    private void addScanAndUnpairButtonEvents() {
        scanAndUnpairButton = (Button) findViewById(R.id.scanandunpair);
        scanAndUnpairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    statusView.setText("ScanAndUnpair started..."+ "\n");
                    if(scanAndPairMgr == null) {
                        // Get reference to ScanAndPair Manager
                        scanAndPairMgr = (ScanAndPairManager)
                                emdkManager.getInstance(EMDKManager.FEATURE_TYPE.SCANANDPAIR);

                        if(scanAndPairMgr != null) {
                            // Add Status Listener on ScanAndPair Manager
                            scanAndPairMgr.addStatusListener(statusCallbackObj);
                        }
                    }

                    if(scanAndPairMgr != null) {
                        scanAndPairMgr.config.alwaysScan = checkBoxAlwaysScan.isChecked();
                        // Set Notification to Beeper
                        scanAndPairMgr.config.notificationType =
                                ScanAndPairConfig.NotificationType.BEEPER;
                        // If always scan checkbox is unchecked, get details from EditText
                        if(!checkBoxAlwaysScan.isChecked()) {
                            scanAndPairMgr.config.bluetoothInfo.macAddress =
                                    btAddress.getText().toString().trim();
                            scanAndPairMgr.config.bluetoothInfo.deviceName =
                                    btName.getText().toString().trim();
                        }
                        else {
                            // Else scan these details (Bluetooth Name/Address)
                            scanAndPairMgr.config.scanInfo.scanTimeout = 5000;
                            //Set Trigger Type for Scanning
                            if(checkboxHardTrigger.isChecked()) {
                                scanAndPairMgr.config.scanInfo.triggerType =
                                        ScanAndPairConfig.TriggerType.HARD;
                            } else {
                                scanAndPairMgr.config.scanInfo.triggerType =
                                        ScanAndPairConfig.TriggerType.SOFT;
                            }
                            // Set Scan data type the user has selected from spinner
                            scanAndPairMgr.config.scanInfo.scanDataType =
                                    (ScanAndPairConfig.ScanDataType)scandataType.getSelectedItem();
                        }
                        // Perform ScanAndUnpair operation
                        ScanAndPairResults resultCode = scanAndPairMgr.scanAndUnpair();
                        // Update the status on StatusView.
                        if(!resultCode.equals(ScanAndPairResults.SUCCESS))
                            statusView.append(resultCode.toString()+ "\n\n");

                    } else {
                        statusView.append("ScanAndPairmanager intialization failed!");
                    }
                } catch (Exception e) {
                    statusView.setText("ScanAndUnpair Error:"+ e.getMessage() + "\n");
                }

            }
        });
    }


    // Method to display status during ScanAndPair and ScanAndUnpair operations.
    @Override
    public void onStatus(StatusData statusData) {

        final StringBuilder text= new StringBuilder();

        boolean isUpdateAddress = false;
        // Get the status and check with the supported status to display message.
        switch (statusData.getState()) {
            // Status Waiting
            case WAITING:
                text.append("Waiting for trigger press to scan the barcode");
                break;
            // Status Scanning
            case SCANNING:
                text.append("Scanner Beam is on, aim at the barcode.");
                break;
            // Status Discovering
            case DISCOVERING:
                text.append("Discovering for the Bluetooth device");
                isUpdateAddress = true;
                break;
            // Status Paired
            case PAIRED:
                text.append("Bluetooth device is paired successfully");
                break;
            // Status Unpaired
            case UNPAIRED:
                text.append("Bluetooth device is un-paired successfully");
                break;
            // Status Error
            default:
            case ERROR:
                text.append("\n"+ statusData.getState().toString()+": " +
                        statusData.getResult());
                break;
        }

        // Update Status View with the status on main thread
        final boolean isUpdateUI = isUpdateAddress;
        runOnUiThread(new Runnable() {
            public void run() {
                statusView.setText(text + "\n");
                // Update Bluetooth Name and Address EditTexts
                if(isUpdateUI) {
                    btName.setText(scanAndPairMgr.config.bluetoothInfo.deviceName);
                    btAddress.setText(scanAndPairMgr.config.bluetoothInfo.macAddress);
                }
            }
        });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        // Get EMDK Manager
        this.emdkManager = emdkManager;
        // Update StatusView TextView with a message on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setText("Application Initialized.");
            }
        });
    }

    @Override
    public void onClosed() {
        // Update StatusView TextView with an error message on UI thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusView.setText("Error!! Restart the application!!");
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (emdkManager != null) {
            // Clean up the objects created by EMDK manager
            emdkManager.release();
            emdkManager=null;
        }
    }


}
