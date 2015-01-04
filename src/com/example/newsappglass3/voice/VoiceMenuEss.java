package com.example.newsappglass3.voice;

import com.example.newsappglass3.Debugger;

import android.app.Activity;
import android.app.FragmentManager;
/**
 * Created by phil on 09.05.14.
 */
public class VoiceMenuEss  extends VoiceMenuDialogFragment  implements VoiceDetection.VoiceDetectionListener{
    protected final VoiceDetection mVoiceDetection;
    private String[] mPhrases;
    private final Activity mContext;
    private final String mHotword;
    private final VoiceDetection.VoiceDetectionListener mListener;
    private VoiceMenuDialogFragment mVoiceMenu;

    public VoiceMenuEss(Activity context, VoiceDetection.VoiceDetectionListener listener, String hotword, String ...cmds) {
        mContext = context;
        mPhrases = cmds;
        mHotword = hotword;
        mListener = listener;
        mVoiceDetection = new VoiceDetection(context, hotword, this);
    }

    @Override
    public void onHotwordDetected() {
        FragmentManager fm = mContext.getFragmentManager();
        mListener.onHotwordDetected();
        mVoiceMenu = VoiceMenuDialogFragment.getInstance(fm,mHotword, mVoiceDetection, mPhrases);
        Debugger.log("is added? " + mVoiceMenu.isAdded());
        if(!mVoiceMenu.isAdded()){
        	mVoiceMenu.show(fm, VoiceMenuDialogFragment.FRAGMENT_TAG);
        }
    }

    @Override
    public void onPhraseDetected(int index, String phrase) {
       if(mVoiceMenu != null){
	    	if (mVoiceMenu.isVisible())
	            mVoiceMenu.dismiss();
	        }	

        mListener.onPhraseDetected(index,phrase);
    }
    
    public void changePhrases(String... phrases){
    	mPhrases = phrases;
    	mVoiceDetection.changePhrases(phrases);
    }

    public void start() {
        mVoiceDetection.start();
    }

    public void stop() {
        mVoiceDetection.stop();
    }
}