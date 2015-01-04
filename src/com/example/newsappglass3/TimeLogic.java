package com.example.newsappglass3;

import android.os.SystemClock;

public class TimeLogic {
	
    //time logic for slider (should be moved to own class)
    private static long mPlay = 0;
    private static long mPause = 0;
    private static boolean mPaused = false;
    private static long mCurrent = 0;	  
    
    public static void reset(){
    	mPaused = false;
    	mCurrent = 0;
    	mPlay = SystemClock.elapsedRealtime();
    	mPause = 0;
    }
    public static void play(){
    	mPaused = false;
    	mPlay = SystemClock.elapsedRealtime();
    }
    public static long getTime(){
    	if(!mPaused){
    		mCurrent = (SystemClock.elapsedRealtime() - mPlay) + mPause;
    		return mCurrent;
    	}else{
    		return mCurrent;
    	}
    };
    public static void pause(){
    	mPaused = true;
    	mPause = mCurrent;
    }

}
