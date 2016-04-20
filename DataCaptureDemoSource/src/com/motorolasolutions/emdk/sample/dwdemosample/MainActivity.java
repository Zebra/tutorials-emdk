package com.motorolasolutions.emdk.sample.dwdemosample;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.text.Editable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

/*
 * To receive data via intents from DataWedge, the DataWedge intent plug-in will need to be configured.
 * The following steps will help you get started...
 * 1. Launch DataWedge
 * 2. Create a new profile and give it a name such as "dwdemosample"
 * 3. Edit the profile
 * 4. Go into Associated apps, tap the menu button, and add a new app/activity
 * 5. For the application select com.motorolasolutions.emdk.sample.dwdemosample
 * 6. For the activity select com.motorolasolutions.emdk.sample.dwdemosample.MainActivty
 * 7. Go back and disable the keystroke output plug-in
 * 8. Enable the intent output plug-in
 * 9. For the intent action enter com.motorolasolutions.emdk.sample.dwdemosample.RECVR
 * 10. For the intent category enter android.intent.category.DEFAULT
 * 
 * Now when you run this activity and scan a barcode you should see the barcode data
 * preceded with additional info (source, symbology and length); see handleDecodeData below.
 */

public class MainActivity extends Activity {
	// Tag used for logging errors
	private static final String TAG = MainActivity.class.getSimpleName();
	
	// Let's define some intent strings
	// This intent string contains the source of the data as a string
    private static final String SOURCE_TAG = "com.motorolasolutions.emdk.datawedge.source";
    // This intent string contains the barcode symbology as a string
    private static final String LABEL_TYPE_TAG = "com.motorolasolutions.emdk.datawedge.label_type";
    // This intent string contains the barcode data as a byte array list
    private static final String DECODE_DATA_TAG = "com.motorolasolutions.emdk.datawedge.decode_data";

    // This intent string contains the captured data as a string
    // (in the case of MSR this data string contains a concatenation of the track data)
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";

    // Let's define the MSR intent strings (in case we want to use these in the future)
    private static final String MSR_DATA_TAG = "com.motorolasolutions.emdk.datawedge.msr_data";
    private static final String MSR_TRACK1_TAG = "com.motorolasolutions.emdk.datawedge.msr_track1";
    private static final String MSR_TRACK2_TAG = "com.motorolasolutions.emdk.datawedge.msr_track2";
    private static final String MSR_TRACK3_TAG = "com.motorolasolutions.emdk.datawedge.msr_track3";
    private static final String MSR_TRACK1_STATUS_TAG = "com.motorolasolutions.emdk.datawedge.msr_track1_status";
    private static final String MSR_TRACK2_STATUS_TAG = "com.motorolasolutions.emdk.datawedge.msr_track2_status";
    private static final String MSR_TRACK3_STATUS_TAG = "com.motorolasolutions.emdk.datawedge.msr_track3_status";

    // Let's define the API intent strings for the soft scan trigger
    private static final String ACTION_SOFTSCANTRIGGER = "com.motorolasolutions.emdk.datawedge.api.ACTION_SOFTSCANTRIGGER";
    private static final String EXTRA_PARAM = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
	private static final String DWAPI_START_SCANNING = "START_SCANNING";
	private static final String DWAPI_STOP_SCANNING = "STOP_SCANNING";
	private static final String DWAPI_TOGGLE_SCANNING = "TOGGLE_SCANNING";
	
	private static String ourIntentAction = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ourIntentAction = getString(R.string.intentAction);

    	// Let's set the cursor at the end of any text in the editable text field
        EditText et = (EditText)findViewById(R.id.editbox);
        Editable txt = et.getText();
        et.setSelection(txt.length());
        
        // Since we will be using the image as a soft scan trigger toggle button
        // let's handle the image on onClick event
        ImageView img = (ImageView) findViewById(R.id.logo);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	// the image has been tapped so shoot off the intent to DataWedge
            	// to toggle the soft scan trigger
            	// Create a new intent
    			Intent i = new Intent();
    			// set the intent action using soft scan trigger action string declared earlier
    			i.setAction(ACTION_SOFTSCANTRIGGER);
    			// add a string parameter to tell DW that we want to toggle the soft scan trigger
    			i.putExtra(EXTRA_PARAM, DWAPI_TOGGLE_SCANNING);
    			// now broadcast the intent
    			MainActivity.this.sendBroadcast(i);
    			// provide some feedback to the user that we did something
                Toast.makeText(v.getContext(), "Soft scan trigger toggled.", Toast.LENGTH_SHORT).show();
            }
        });

        // in case we have been launched by the DataWedge intent plug-in
        // using the StartActivity method let's handle the intent
        Intent i = getIntent();
    	handleDecodeData(i);
    }


    // We need to handle any incoming intents, so let override the onNewIntent method
    @Override
    public void onNewIntent(Intent i) {
    	handleDecodeData(i);
    }
   
    // This function is responsible for getting the data from the intent
    // formatting it and adding it to the end of the edit box
    private void handleDecodeData(Intent i) {
    	// check the intent action is for us
        if ( i.getAction().contentEquals(ourIntentAction) ) {
        	// define a string that will hold our output
        	String out = "";
        	// get the source of the data
        	String source = i.getStringExtra(SOURCE_TAG);
        	// save it to use later
        	if (source == null) source = "scanner";
        	// get the data from the intent
	        String data = i.getStringExtra(DATA_STRING_TAG);
	        // let's define a variable for the data length
        	Integer data_len = 0;
        	// and set it to the length of the data
        	if (data != null) data_len = data.length();

        	// check if the data has come from the barcode scanner
        	if (source.equalsIgnoreCase("scanner")) {
        		// check if there is anything in the data
			    if (data != null && data.length() > 0) {
			        // we have some data, so let's get it's symbology
		        	String sLabelType = i.getStringExtra(LABEL_TYPE_TAG);
		        	// check if the string is empty
		        	if (sLabelType != null && sLabelType.length() > 0) {
		        		// format of the label type string is LABEL-TYPE-SYMBOLOGY
		        		// so let's skip the LABEL-TYPE- portion to get just the symbology
		        		sLabelType = sLabelType.substring(11);
		        	}
		        	else {
		        		// the string was empty so let's set it to "Unknown"
		        		sLabelType = "Unknown";
		        	}
		        	// let's construct the beginning of our output string
			        out = "Source: Scanner, " + "Symbology: " + sLabelType + ", Length: " + data_len.toString() + ", Data: ...\r\n";
		        }
	        }
	        
	        // check if the data has come from the MSR
        	if (source.equalsIgnoreCase("msr")) {
        		// construct the beginning of our output string
	        	out = "Source: MSR, Length: " + data_len.toString() + ", Data: ...\r\n"; 
	        }

	        // let's get our edit box view
        	EditText et = (EditText)findViewById(R.id.editbox);
        	// and get it's text into an editable string
	        Editable txt = et.getText();
	        // now because we want format our output
	        // we need to put the edit box text into a spannable string builder
        	SpannableStringBuilder stringbuilder = new SpannableStringBuilder(txt);
        	// add the output string we constructed earlier
        	stringbuilder.append(out);
        	// now let's highlight our output string in bold type
        	stringbuilder.setSpan(new StyleSpan(Typeface.BOLD), txt.length(), stringbuilder.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        	// then add the barcode or msr data, plus a new line, and add it to the string builder
        	stringbuilder.append(data + "\r\n");
        	// now let's update the text in the edit box
        	et.setText(stringbuilder);
        	// we want the text cursor to be at the end of the edit box
        	// so let's get the edit box text again
	        txt = et.getText();
	        // and set the cursor position at the end of the text
	        et.setSelection(txt.length());
	        // and we are done!
        }
    	
    }
    
}
