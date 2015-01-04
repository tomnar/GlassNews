package com.example.newsappglass3;

import com.google.android.glass.widget.CardBuilder;
//import com.google.android.gms.auth.GoogleAuthUtil;

import com.google.android.gms.auth.GoogleAuthUtil;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends Activity {
	
	private final String urlEB = "http://ekstrabladet.dk/srtjson/?spec=desktop/matchoverview&sports=1&seasons=seasons&uniquetournaments=460,26,39,436,437,47,7,76,17,18,19,21,346,679,465,357,8,329,35,44,217,37,23,328,34,335,27,1,11,436,16,851,853,18,238,36,65,66,52,347,384,91,53,346,213,20,40,454,453,477,173,334,347,16,799&parameters=filter%3Dtodaysmatches";
	//private final String soundcloudUrl  = "http://ec-media.soundcloud.com/tLWNceptcwJm.128.mp3?f10880d39085a94a0418a7ef69b03d522cd6dfee9399eeb9a522009e6efdbd38b5f473020aeffe1deb704efe59168b726666739624c081358a37786bf0f4332a3064dca0c5&AWSAccessKeyId=AKIAJNIGGLK7XA7YZSNQ&Expires=1412153425&Signature=Q563MH1%2B0eEVV5OfF03EgQLGpyY%3D"; 
	private final String soundcloudUrl = "http://api.soundcloud.com/tracks/166909957/stream?client_id=824c0ea48bfa8bd5fdb5026fa7fd384e";
	public static final String intentMessage = "com.speciale.newsapp";

	
	 protected void onCreate(Bundle bundle) {
	        super.onCreate(bundle);
	        
	        //make sure screen never sleeps, adn therefore activity doesn't stop. Otherwise it will destroy itself after a while....
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        
	        CardBuilder loadScreenCard = new CardBuilder(this, CardBuilder.Layout.TEXT);
	        Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
			loadScreenCard.addImage(background);
	        setContentView(loadScreenCard.getView());
	        
	        //NB we assume that there is only 1 account on the phone
	        AccountManager mAccountManager = AccountManager.get(getApplicationContext());
	    	Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE); 
	    	String name = accounts[0].name;
	    	Utils.setUsername(name);
	       
	        String newsUrl = Utils.NEWS+"&email="+Utils.getUsername();
	        Debugger.log("url to news " + newsUrl);
	        new DownloadJSONTask(this, "Top News", loadScreenCard).execute(newsUrl);
	        
	      
	    }
	 
	 	protected void onStart(){
			super.onStart();
		}
		
		protected void onResume(){
			super.onResume();
		}
		
		protected void onPause(){
			super.onPause();
		}
		
		protected void onStop(){
			super.onStop();
			
		}
		
		protected void onDestroy(){
			super.onDestroy();
		}
		
		

}