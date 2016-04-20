package simulscantutorial.symbol.com.simulscantutorial;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.simulscan.SimulScanConfig;
import com.symbol.emdk.simulscan.SimulScanData;
import com.symbol.emdk.simulscan.SimulScanException;
import com.symbol.emdk.simulscan.SimulScanManager;
import com.symbol.emdk.simulscan.SimulScanMultiTemplate;
import com.symbol.emdk.simulscan.SimulScanReader;
import com.symbol.emdk.simulscan.SimulScanReaderInfo;
import com.symbol.emdk.simulscan.SimulScanStatusData;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity implements EMDKManager.EMDKListener,
        SimulScanReader.DataListerner, SimulScanReader.StatusListerner,
        View.OnClickListener, AdapterView.OnItemSelectedListener {
    // Tag for managing logs
    private final static String TAG = MainActivity.class.getCanonicalName();

    // TextView for displaying status of SimulScan operations
    private TextView textViewStatus = null;
    // Spinner for selecting scanning device for SimulScan operation
    private Spinner deviceSelectionSpinner = null;
    // Button that triggers reading form elements from the template
    private Button readButton = null;
    // Button to stop reading template
    private Button stopReadButton = null;
    // Declare a variable to store EMDKManager object
    private EMDKManager emdkManager = null;
    // Declare a variable to store SimulScanManager object
    private SimulScanManager simulscanManager = null;
    // List of SimulScan supported devices
    private List<SimulScanReaderInfo> readerInfoList = null;
    // object for holding EMDKResults data.
    private EMDKResults results;
    // provides access to physical SimulScan reader device.
    private SimulScanReader selectedSimulScanReader = null;
    // contains a list of SimulScanData, captured through SimulScan operations
    private List<SimulScanData> simulScanDataList = Collections
            .synchronizedList(new ArrayList<SimulScanData>());
    // Catches Exception
    private Exception lastException;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The EMDKManager object will be created and returned in the callback.
        results = EMDKManager.getEMDKManager(getApplicationContext(), this);

        // Get references for UI elements
        textViewStatus = (TextView) findViewById(R.id.status_view);
        deviceSelectionSpinner = (Spinner) findViewById(R.id.devices_spinner);
        readButton = (Button) findViewById(R.id.btn_start_read);
        stopReadButton = (Button) findViewById(R.id.btn_stop_read);

        // Update Status TextView
        textViewStatus.setText("Status: " + " Starting..");

        // Set listeners for Spinner and buttons
        deviceSelectionSpinner.setOnItemSelectedListener(this);
        readButton.setOnClickListener(this);
        stopReadButton.setOnClickListener(this);

        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            textViewStatus.setText("Status: "
                    + "EMDKManager object request failed!");
        }
    }

    @Override
    public void onData(SimulScanData simulScanData) {
        // clear the SimulScanDataList before adding new scanned data
        synchronized (simulScanDataList) {
            simulScanDataList.clear();
        }

        // Add Scanned data to SimulScanDataList
        synchronized (simulScanDataList) {
            simulScanDataList.add(simulScanData);
        }
    }

    @Override
    public void onOpened(EMDKManager emdkManager) {
        // This callback will be issued when the EMDK is ready to use.
        this.emdkManager = emdkManager;

        // Get the SimulScanManager object
        simulscanManager = (SimulScanManager) emdkManager
                .getInstance(EMDKManager.FEATURE_TYPE.SIMULSCAN);

        if (null == simulscanManager) {
            textViewStatus.setText("Status: "
                    + "Get SimulScanManager instance failed!");
            return;
        }

        // Get the SimulScan supported device list
        readerInfoList = simulscanManager.getSupportedDevicesInfo();
        List<String> nameList = new ArrayList<String>();
        for (SimulScanReaderInfo rinfo : readerInfoList) {
            nameList.add(rinfo.getFriendlyName());
        }
        // Add the simulscan supported list to spinner and
        // set item selected listener
        addItemsOnSpinner(deviceSelectionSpinner, nameList);
        deviceSelectionSpinner.setOnItemSelectedListener(this);
    }


    // Add SimulScan supported device list to spinner
    private void addItemsOnSpinner(Spinner spinner, List<String> list) {

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, list);
        dataAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onClosed() {
        textViewStatus.setText("Status: " + "EMDK closed unexpectedly!");
    }

    @Override
    public void onClick(View v) {
        // Start reading template
        if(v.equals(readButton)){
            try {
                readCurrentScanner();
            } catch (Exception e) {
                lastException = e;
                textViewStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewStatus.setText("Status: "
                                + lastException.getMessage());
                    }
                });
                e.printStackTrace();
            }
        }

        // Cancel/Stop reading template
        if(v.equals(stopReadButton)){
            try {
                stopReadCurrentScanner();
            } catch (SimulScanException e) {
                lastException = e;
                textViewStatus.post(new Runnable() {
                    @Override
                    public void run() {
                        textViewStatus.setText("Status: "
                                + lastException.getMessage());
                    }
                });
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // Initialize the selected Simul Scan device
        if (parent.equals(deviceSelectionSpinner)) {
            if (simulscanManager != null) {
                SimulScanReaderInfo readerInfo = readerInfoList.get(position);
                if (readerInfo != null) {
                    try {
                        deinitCurrentScanner();
                        selectedSimulScanReader = simulscanManager
                                .getDevice(readerInfo);
                        initCurrentScanner();
                    } catch (SimulScanException e) {
                        e.printStackTrace();
                        textViewStatus.setText("Status: "
                                + "Error enabling reader");
                    }
                }
            }
        }
    }

    // Initialize Simul scanner by enabling
    // simulscan reader and setting listeners
    private void initCurrentScanner() throws SimulScanException {
        selectedSimulScanReader.addStatusListener(this);
        selectedSimulScanReader.addDataListener(this);
        selectedSimulScanReader.enable();
    }


    // De-Initialize scanner by removing listeners and cancelling pending reads
    private void deinitCurrentScanner() throws SimulScanException {
        if (selectedSimulScanReader != null) {
            if (selectedSimulScanReader.isReadPending())
                selectedSimulScanReader.cancelRead();
            if (selectedSimulScanReader.isEnabled())
                selectedSimulScanReader.disable();
            selectedSimulScanReader.removeDataListener(this);
            selectedSimulScanReader.removeStatusListener(this);
            selectedSimulScanReader = null;
        }
    }


    // Set SimulScan Config settings
    private void setCurrentConfig() throws Exception {
        if (selectedSimulScanReader != null) {
            SimulScanConfig config = selectedSimulScanReader.getConfig();
            if (config != null) {
                // Get the template path from the storage
                String templatePath = Environment.getExternalStorageDirectory().toString()
                        + "/simulscan/templates/MyTemplate.xml";
                File file = new File(templatePath);
                // Get the SimulScan Template
                SimulScanMultiTemplate myTemplate = new SimulScanMultiTemplate(simulscanManager,Uri.fromFile(file));

                // Set the template with SimulScanConfig settings
                if(myTemplate != null)
                    config.multiTemplate = myTemplate;
                // Amount of time in milliseconds to wait before timing out identification.
                config.identificationTimeout = 15000;
                // Amount of time in milliseconds to wait before timing out processing.
                config.processingTimeout = 10000;
                // If userConfirmationOnScan is true, shows UI for user to confirm
                // the scanned data before sending results to application.
                config.userConfirmationOnScan = true;
                // If true, form will be captured automatically when detected
                config.autoCapture = true;
                // If enabled, allows a session to write form capture, region images,
                // region values, and other data to storage.
                config.debugMode = false;
                // Turn on/off audio feedback.
                config.audioFeedback = true;
                // Turn on/off haptic feedback.
                config.hapticFeedback = true;
                // Turn on/off LED feedback.
                config.ledFeedback = true;
                // Set SimulScanConfig
                selectedSimulScanReader.setConfig(config);
            }
        }
    }


    // Start reading template through SimulScanReader
    private void readCurrentScanner() throws Exception {
        setCurrentConfig();
        if (selectedSimulScanReader != null) {
            selectedSimulScanReader.read();
        }
    }


    // Cancel/Stop reading template through SimulScanReader
    private void stopReadCurrentScanner() throws SimulScanException {
        if (selectedSimulScanReader != null)
            selectedSimulScanReader.cancelRead();
    }


    @Override
    public void onStart() {
        super.onStart();
        if (selectedSimulScanReader != null)
            try {
                if (!selectedSimulScanReader.isEnabled())
                    // Enable SimulScan Reader
                    selectedSimulScanReader.enable();
            } catch (SimulScanException e) {
                e.printStackTrace();
                textViewStatus.setText("Status: " + "Error enabling reader");
            }
    }

    @Override
    public void onStop() {
        if (selectedSimulScanReader != null) {
            if (selectedSimulScanReader.isReadPending()) {
                try {
                    // Cancel any pending SimulScan Read
                    selectedSimulScanReader.cancelRead();
                } catch (SimulScanException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (selectedSimulScanReader.isEnabled()) {
                    // Disable SimulScan Reader
                    selectedSimulScanReader.disable();
                }
            } catch (SimulScanException e) {
                e.printStackTrace();
            }
        }
        super.onStop();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // De-initialize device spinner when nothing is selected
        if (parent.equals(deviceSelectionSpinner)) {
            try {
                deinitCurrentScanner();
            } catch (SimulScanException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onStatus(SimulScanStatusData simulScanStatusData) {
        // Update Status View with the updated SimulScan operation status
        textViewStatus.post(new StatusDataRunnable(simulScanStatusData));
    }


    // Update SimulScan operation status on Status View
    private class StatusDataRunnable implements Runnable {
        SimulScanStatusData statusData = null;

        StatusDataRunnable(SimulScanStatusData statusData) {
            this.statusData = statusData;
        }

        @Override
        public void run() {
            if (statusData != null) {
                switch (statusData.getState()) {
                    case DISABLED:
                        textViewStatus.setText("Status: "
                                + statusData.getFriendlyName()
                                + ": Closed reader successfully");
                        break;
                    case ENABLED:
                        textViewStatus.setText("Status: "
                                + statusData.getFriendlyName()
                                + ": Opened reader successfully");
                        break;
                    case SCANNING:
                        textViewStatus.setText("Status: "
                                + statusData.getFriendlyName()
                                + ": Started reader successfully");

                        break;
                    case IDLE:
                        textViewStatus.setText("Status: "
                                + statusData.getFriendlyName()
                                + ": Stopped reader successfully");
                        break;
                    case ERROR:
                        textViewStatus.setText("Status: "
                                + statusData.getFriendlyName() );
                        break;
                    case UNKNOWN:
                    default:
                        break;
                }
            }
        }
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


}
