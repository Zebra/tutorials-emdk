package com.symbol.emdksample;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.symbol.emdk.*;  
import com.symbol.emdk.EMDKManager.EMDKListener;
import com.symbol.emdk.EMDKResults.STATUS_CODE;

public class MainActivity extends Activity implements EMDKListener {

    //Assign the profile name used in EMDKConfig.xml  
    private String profileName = "DataCaptureProfile";  
      
    //Declare a variable to store ProfileManager object  
    private ProfileManager mProfileManager = null;  
      
    //Declare a variable to store EMDKManager object  
    private EMDKManager emdkManager = null;  
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //The EMDKManager object will be created and returned in the callback.  
        EMDKResults results = EMDKManager.getEMDKManager(getApplicationContext(), this); 
        
        //Check the return status of getEMDKManager
        if(results.statusCode == STATUS_CODE.FAILURE)
        {
        	//Failed to create EMDKManager object         	
        }
    }
    
    @Override  
    protected void onDestroy() {  
    	// TODO Auto-generated method stub  
    	super.onDestroy();  
         
    	//Clean up the objects created by EMDK manager  
    	emdkManager.release();  
    }

	@Override
	public void onClosed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onOpened(EMDKManager emdkManager) {
		// TODO Auto-generated method stub
		//EMDK opened.
		
		this.emdkManager = emdkManager;  
		
		//Get the ProfileManager object to process the profiles  
		mProfileManager = (ProfileManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.PROFILE); 
		
		if(mProfileManager != null)
		{
			String[] modifyData = new String[1];
			
			//Call processPrfoile with profile name and SET flag to create the profile. The modifyData can be null.  
			EMDKResults results = mProfileManager.processProfile(profileName, ProfileManager.PROFILE_FLAG.SET, modifyData);  
			
			if(results.statusCode == STATUS_CODE.FAILURE)
	        {
	        	//Failed to set profile        	
	        }
		}
		
	}
}
