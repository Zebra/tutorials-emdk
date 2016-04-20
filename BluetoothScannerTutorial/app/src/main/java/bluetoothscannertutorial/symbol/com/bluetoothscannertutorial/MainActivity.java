package bluetoothscannertutorial.symbol.com.bluetoothscannertutorial;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerInfo;

import java.util.List;


public class MainActivity extends Activity implements EMDKManager.EMDKListener,
        BarcodeManager.ScannerConnectionListener{
    // Text View to display status during pairing operation
    private TextView statusView = null;

    // Declare a variable to store EMDKManager object
    private EMDKManager emdkManager = null;

    // Declare a variable to store Barcode Manager object
    private BarcodeManager barcodeManager = null;

    // Declare a variable to hold scanner device to scan
    private Scanner scanner = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Reference to UI elements
        statusView = (TextView) findViewById(R.id.textViewStatus);

        // The EMDKManager object will be created and returned in the callback.
        EMDKResults results = EMDKManager.getEMDKManager(
                getApplicationContext(), this);

        // Check the return status of getEMDKManager and update the status Text
        // View accordingly
        if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
            statusView.setText("Status: "
                    + "EMDKManager object request failed!");
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

    @Override
    public void onOpened(EMDKManager emdkManager) {
        // Update status view with EMDK Open Success message
        statusView.setText("Status: " + "EMDK open success!");

        this.emdkManager = emdkManager;
        // Get the Barcode Manager Instance
        barcodeManager = (BarcodeManager) emdkManager
                .getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
        // Add the Scanner Connection Listener to receive Connected/Disconnected events
        if (barcodeManager != null) {
            barcodeManager.addConnectionListener(this);
        }

        // Initialize Scanner
        initScanner();
    }


    // Initialize Bluetooth Scanner
    private void initScanner() {

        if (scanner == null) {
            // Get a list of supported scanner devices
            List<ScannerInfo> deviceList = barcodeManager
                    .getSupportedDevicesInfo();

            // Iterate through Scanner devices and check if it supports Bluetooth Scanner
            for (ScannerInfo scannerInfo : deviceList){
                if(scannerInfo.getFriendlyName().equalsIgnoreCase("Bluetooth Scanner"))
                    scanner = barcodeManager.getDevice(scannerInfo);
            }
            // If null, then your device does not support Bluetooth Scanner
            if(scanner == null) {
                statusView.setText("Bluetooth Scanner not supported!!!");
                return;
            }else{
                // Supports Bluetooth Scanner
                try {
                    // Enable the Scanner
                    scanner.enable();
                } catch (ScannerException e) {
                    statusView.setText("Status: " + e.getMessage());
                }
            }
        }
    }


    // DeInitialize Scanner
    private void deInitScanner() {
        if (scanner != null) {
            try {
                // Cancel pending reads
                scanner.cancelRead();
                // Disable Scanner
                scanner.disable();
                // Release Scanner
                scanner.release();

            } catch (ScannerException e) {
                statusView.setText("Status: " + e.getMessage());
            }

            scanner = null;
        }
    }

    // AsyncTask for Updating Status in statusView during pairing operation
    private class AsyncStatusUpdate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return params[0];
        }

        @Override
        protected void onPostExecute(String result) {
            // Update Status View
            statusView.setText("Status: " + result);
        }
    }


    @Override
    public void onClosed() {
        if (emdkManager != null) {
            // Remove the connection listener
            if (barcodeManager != null) {
                barcodeManager.removeConnectionListener(this);
            }
            // Release EMDK Manager
            emdkManager.release();
            emdkManager = null;
        }
        statusView
                .setText("Status: "
                        + "EMDK closed unexpectedly! Please close and restart the application.");
    }

    @Override
    public void onConnectionChange(ConnectionStatus connectionStatus) {
        String status = "";
        String scannerName = "";

        // Returns the Connection State for Bluetooth Scanner through callback
        String statusBT = connectionStatus.getConnectionState().toString();
        // Returns the Friendly Name of the Scanner through callback
        String scannerNameBT = connectionStatus.getScannerInfo()
                .getFriendlyName();
        // Get the friendly name of our device's Scanner
        scannerName = scanner.getScannerInfo().getFriendlyName();

        // Check for the Bluetooth Scanner
        if (scannerName.equalsIgnoreCase(scannerNameBT)) {
            // If Bluetooth Scanner, update the status view
            status = scannerNameBT + ":" + statusBT;
            new AsyncStatusUpdate().execute(status);
            // Initialize or De-Initialize Bluetooth Scanner
            // device based on Connection State
            switch (connectionStatus.getConnectionState()) {
                case CONNECTED:
                    // Initialize Scanner
                    initScanner();
                    break;
                case DISCONNECTED:
                    // De-Initialize Scanner
                    deInitScanner();
                    break;
            }
        }
    }

}
