package com.example.newsappglass3;

import java.util.ArrayList;

import android.app.Service;
import android.os.Handler;

public class UpdateScoreCard implements Runnable {

	private ScoreCard  mScoreCard;
	 private final Handler mHandler = new Handler();
	//Match mMatch;
	private boolean mStopped = false;
	
	public UpdateScoreCard(ScoreCard service){
		mScoreCard = service;
	//	mMatch = match;
	}
	@Override
	public void run() {
		if(!isStopped()){
			new DownloadJSONFromEBTask(mScoreCard).execute(Utils.EB_URL);
			mHandler.postDelayed(this, 10000);
		}
	}
	
	public boolean isStopped(){
		return mStopped;
	}
	public void setStopped(boolean stopped){
		mStopped = stopped;
	}
	

}
