package com.example.newsappglass3.voice;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.newsappglass3.Debugger;
import com.example.newsappglass3.HeadListView;
import com.example.newsappglass3.R;
import com.example.newsappglass3.R.id;
import com.example.newsappglass3.R.layout;
import com.google.android.glass.media.Sounds;
import com.google.glass.voice.VoiceConfig;

/**
 * Created by Ramon on 22.04.2014.
 */
public class VoiceMenuDialogFragment extends DialogFragment {

	public static final String HOTWORD = Constants.HOTWORD;
	public static final String PHRASES = Constants.PHRASES;
	public static final String FRAGMENT_TAG = VoiceMenuDialogFragment.class.getSimpleName();
//    private static final String NEXT = "Next";
//    private static final String PREVIOUS = "Previous";

	private String mActivationWord;
	//private VoiceConfig mVoiceConfig;
	private static VoiceDetection mVoiceDetection;
	private static boolean activeFragment = false; //aded this to see if fragment is open or not. 
	private TextView mName;
	private HeadListView mScroll;
	private AudioManager mAudio;
	private String[] mItems;

	public static VoiceMenuDialogFragment getInstance(FragmentManager fm, String hotword, VoiceDetection voiceDetection, String... phrases) {
		VoiceMenuDialogFragment f = (VoiceMenuDialogFragment) fm.findFragmentByTag(VoiceMenuDialogFragment.FRAGMENT_TAG);
		mVoiceDetection = voiceDetection;
		if (f == null)
			f = new VoiceMenuDialogFragment();
		if(! activeFragment){
			Bundle args = new Bundle();
			args.putString(VoiceMenuDialogFragment.HOTWORD, hotword);
			args.putStringArray(VoiceMenuDialogFragment.PHRASES, phrases);
			f.setArguments(args); //throws illagel state error
			activeFragment = true;
		}

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		Bundle args = getArguments();
		mItems = args.getStringArray(PHRASES);

		mActivationWord = args.getString(HOTWORD);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.voice_menu, container, false);
		mName = (TextView) layout.findViewById(R.id.hotword_text);
		mName.setText(mActivationWord + ",");
		mScroll = (HeadListView) layout.findViewById(R.id.hotword_chooser);

		mScroll.setOnKeyListener(new View.OnKeyListener() {
			@Override // catch the TAP event
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Activity activity = getActivity();
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && activity != null && mScroll != null) {
					String str = ((String) mScroll.getSelectedItem());
					((VoiceMenuListener)activity).onPhraseSelected(str);
					if (mAudio != null)
						mAudio.playSoundEffect(Sounds.TAP);
					return true;
				}
				return false;
			}
		});

		return layout;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity activity = getActivity();
		activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mAudio = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
		mScroll.setAdapter(new ArrayAdapter<String>(activity, R.layout.voice_menu_item, mItems));
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		return dialog;
	}

	@Override
	public void onStart() {
		super.onStart();

		mScroll.activate();
		mScroll.requestFocus();
	}

	@Override
	public void onStop() {

		mScroll.deactivate();
	//	mVoiceDetection.changePhrases(new String[] { Constants.NEXT, Constants.PREVIOUS } ); //remove phrases when closing voice menu fragment
		mVoiceDetection.changePhrases(new String[] { } );
		activeFragment = false;
		Debugger.log("stop voice menu fragment");
		super.onStop();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Debugger.log("deteched voice menu");
		mScroll.setAdapter(null);
		mAudio = null;
	}

    public interface VoiceMenuListener {
		public void onPhraseSelected(String phrase);
	}
}