package com.symbol.emdksample;

import android.app.Activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.content.Intent;
import android.widget.TextView;

public class MSRCompletedActivity extends Activity {

	//Declare a variable to store the textViewMSRData
    private TextView textViewMSRData = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msrcompleted);
		
		//Get the textViewBarcode
		textViewMSRData = (TextView) findViewById(R.id.textViewMSRData);
		
		//In case we have been launched by the DataWedge intent plug-in
		Intent i = getIntent();
		handleDecodeData(i);
	}
	
	//We need to handle any incoming intents, so let override the onNewIntent method
	@Override
	public void onNewIntent(Intent i) {
		handleDecodeData(i);
	}
	
	//This function is responsible for getting the data from the intent
	private void handleDecodeData(Intent i)
	{
		//Check the intent action is for us
        if (i.getAction().contentEquals("com.symbol.emdksample.RECVRMSR")) 
        {
        	//Get the source of the data
        	String source = i.getStringExtra("com.motorolasolutions.emdk.datawedge.source");
        	
        	//Check if the data has come from the msr
        	if(source.equalsIgnoreCase("msr"))
        	{           	
        		//Get the data from the intent
    	        String data = i.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string");
            	
    	        //Check that we have received data
    	        if(data != null && data.length() > 0)
    	        {
    	        	//Display the data to textViewMSRData
    	        	textViewMSRData.setText("Data = " + data);
    	        }
        	}
        }
	}

}
