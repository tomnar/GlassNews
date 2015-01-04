package com.example.newsappglass3;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

public class StartAppService extends Service {

    private static final String LIVE_CARD_TAG = "LiveCardDemo";
    private static final String URL = "http://ekstrabladet.dk/sport/kampe/kamp/?matchid=5532148";

    private LiveCard mLiveCard;
    private RemoteViews mLiveCardView;
    private Context mContext;



    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debugger.log("start scorecard service");
        
    	if (mLiveCard == null) {
        	mContext = this;
        	boolean voice = intent.getBooleanExtra(LiveCard.EXTRA_FROM_LIVECARD_VOICE, false);
        	Debugger.log("started with voice? " + voice);
            // Get an instance of a live card
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
            
            Intent menuIntent = new Intent(this, MenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            
            mLiveCard.setVoiceActionEnabled(true);
            Debugger.log("hej med dig du");
            // Publish the live card
            mLiveCard.publish(PublishMode.SILENT);
            //set view of live card
            mLiveCard.setViews(mLiveCardView);


        }
        return START_STICKY;
    }
    
    

    @Override
    public void onDestroy() {
    	
        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
      /*
       *  If you need to set up interprocess communication
       * (activity to a service, for instance), return a binder object
       * so that the client can receive and modify data in this service.
       *
       * A typical use is to give a menu activity access to a binder object
       * if it is trying to change a setting that is managed by the live card
       * service. The menu activity in this sample does not require any
       * of these capabilities, so this just returns null.
       */
        return null;
    }
}
