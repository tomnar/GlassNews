package com.example.newsappglass3.voice;

import android.content.Context;
import android.util.Log;

import com.google.glass.input.VoiceInputHelper;
import com.google.glass.voice.VoiceCommand;
import com.google.glass.voice.VoiceConfig;

public class VoiceDetection extends StubVoiceListener {

	private static final String THIS = VoiceDetection.class.getSimpleName();

	private final VoiceConfig mVoiceConfig;
	private String[] mPhrases;
	private VoiceInputHelper mVoiceInputHelper;
	private VoiceDetectionListener mListener;
	private boolean mRunning = true;

	public VoiceDetection(Context context, String hotword, VoiceDetectionListener listener, String... phrases) {
		mVoiceInputHelper = new VoiceInputHelper(context, this);

		mPhrases = assemblePhrases(hotword, phrases);

	    mVoiceConfig = new VoiceConfig(mPhrases);
		mVoiceConfig.setShouldSaveAudio(false);

		mListener = listener;
	}

	private String[] assemblePhrases(String hotword, String[] phrases) {
		String[] res = new String[phrases.length+1];
		res[0] = hotword;
		for (int i=0; i<phrases.length; ++i)
			res[i+1] = phrases[i];
		return res;
	}

	public void changePhrases(String... phrases) {
		mPhrases = assemblePhrases(mPhrases[0], phrases); //keeps "ok glass" as hot word by setting mPhrases[0] = hotword
	   mVoiceConfig.setCustomPhrases(mPhrases);
		mVoiceInputHelper.setVoiceConfig(mVoiceConfig);
	}

	/**
	 * If the VoiceService is ready, refresh our Config
	 *
	@Override
	public void onVoiceServiceConnected() {
		super.onVoiceServiceConnected();
		mVoiceInputHelper.setVoiceConfig(mVoiceConfig);
	}*/

	@Override
	public VoiceConfig onVoiceCommand(VoiceCommand vc) {
		String literal = vc.getLiteral();

//		if (mListener == null) {
//			mVoiceInputHelper.removeVoiceServiceListener();
//			return null;
//		}

		if (literal.equals(mPhrases[0])) { // Hotword

			Log.i(THIS, "Hotword detected");
			mListener.onHotwordDetected();
		}

		for (int i=1; i< mPhrases.length; ++i) {
			String item = mPhrases[i];
			if (item.equals(literal)) {
				Log.i(THIS, String.format("command %s", literal));
				mListener.onPhraseDetected(i-1, literal);
				return null;
			}
		}

		return null;
	}

	public void start() {
		mRunning = true;
       mVoiceInputHelper.setVoiceConfig(mVoiceConfig);
	}

	public void stop() {
		mRunning = false;
        mVoiceInputHelper.setVoiceConfig(null);
	}

	@Override
	public boolean isRunning() {
		return mRunning;
	}

	public interface VoiceDetectionListener {
		public void onHotwordDetected();
		public void onPhraseDetected(int index, String phrase);
	}
}