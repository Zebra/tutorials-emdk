package com.symbol.emdksample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.TextView;

public class BroadcastIntentActivity extends Activity {

	//Declare a variable to store the textViewData
    private TextView textViewData = null; 
    
	//Declare a variable to store our Broadcast Receiver.
	private BroadcastReceiver EMDKReceiver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_broadcast_intent);
		
		//Get the textViewData
		textViewData = (TextView) findViewById(R.id.textViewData);	
	}
	
	@Override
    protected void onResume() {
		// TODO Auto-generated method stub
        super.onResume();
        
        //Create an Intent Filter
        IntentFilter intentFilter = new IntentFilter("com.symbol.emdksample.RECVRBI"); 
        
        //Create a our Broadcast Receiver.
        EMDKReceiver = new BroadcastReceiver() {
        	
        	@Override
            public void onReceive(Context context, Intent intent) {
        		
        		//Get the source of the data
            	String source = intent.getStringExtra("com.motorolasolutions.emdk.datawedge.source");
            	
            	//Check if the data has come from the barcode scanner
            	if(source.equalsIgnoreCase("scanner")){           	
            		
            		//Get the data from the intent
        	        String data = intent.getStringExtra("com.motorolasolutions.emdk.datawedge.data_string");
                	
        	        //Check that we have received data
        	        if(data != null && data.length() > 0){
        	        	
        	        	//Display the data to the text view
        	        	textViewData.setText("Data = " + data);
        	        }
            	}
        	}
        };
        
        //Register our receiver.
        this.registerReceiver(EMDKReceiver, intentFilter);
	}
	
	@Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        
        //Unregister our receiver.
        this.unregisterReceiver(this.EMDKReceiver);
	}
}
