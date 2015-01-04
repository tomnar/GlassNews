package com.example.newsappglass3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser.BookmarkColumns;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;

import com.example.newsappglass3.voice.Constants;
import com.example.newsappglass3.voice.VoiceDetection;
import com.example.newsappglass3.voice.VoiceMenuDialogFragment.VoiceMenuListener;
import com.example.newsappglass3.voice.VoiceMenuEss;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
import com.example.newsappglass3.poll.*;

public class NewsSliderActivity extends Activity  implements VoiceDetection.VoiceDetectionListener,
	VoiceMenuListener{


    private VoiceMenuEss mVoiceMenu;
    private String[] mPhrases = new String[] { Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
    		Constants.BOOKMARK, Constants.RELATED, Constants.RADIOSTART, Constants.CLOSEAPP }; 
	
 // declare the dialog as a member field of your activity
     private Thread mSliderThread;
	 private int indexOfCard = 0;
	 private Thread sliderThread;
	 private final int mDuration = 7000;
     private ArrayList<NewsItem> mNewsItems;
	 private Context mContext;
	 private ArrayList<Boolean> mLayoutSwitches;
	 private CardScrollView mCardScrollView;
	 private MyCardScrollAdapter mCardScrollAdapter;
	 private GestureDetector mGestureDetector;
	 private boolean mPaused = false;
	 private boolean mRadio;
	 private int mStopAt;
	 private boolean mUserSelected;
	// private HashMap<Integer, ArrayList<CardWrapper>> mHistoryOfRelatedCards;
	 private HashMap<Integer, ArrayList<NewsItem>> mHistoryOfRelatedNewsItems;
	 private HashMap<Integer, Integer> mHistoryOfCardIndex;
	// private int mIndexOfRelated = 0;
	 private HashMap<String, ArrayList<Match>> mTournaments;
	 private Poll mPoll;
	 ArrayList<Match> mFollows;
	 private boolean readCardActivity = false; //used to check if activity was created previosly
	 private ArrayList<DownloadFileTask> mDownloadFileTasks;
	 private int mIndexOfReadAloudCard;
	 private boolean mInAppStart;
	 private int mTotalNoOfCards;
	 private AudioManager mAudio;
	 private String actionUrlBase;
	 
		private final Handler sliderHandler = new Handler(){
			public void handleMessage(Message msg) {
				int index =  (Integer)msg.getData().getSerializable("index"); 
				//rotate cards in mCards
				
				mCardScrollView.animate(index, CardScrollView.Animation.NAVIGATION);
				StreamPlayer.stopMediaPlayer();
			}
		};
	 
//		private final Runnable sliderRunnable = new Runnable() {
//			@Override
//			public void run() {
//				if(!mPaused){
//					TimeLogic.play();
//				}
//				int i = indexOfCard;
//				try {
//					while(i < mCards.size()){
//						if(TimeLogic.getTime() > mDuration){
//							i = indexOfCard;
//							i++;
//							if(i == mCards.size()){
//								i = 0;
//							}
//							indexOfCard = i;
//							TimeLogic.reset();
//							sendIndexToHandler(indexOfCard);
//						}
//						Thread.sleep(200);
//					}
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		};
		
		private final Runnable sliderRunnable2 = new Runnable() {
			@Override
			public void run() {
				try {
			    Thread.sleep(mDuration);
				int i = indexOfCard;
					while(i < mCards.size()){
						i++;
						if(i == mCards.size()){
							i = 0;
						}
						sendIndexToHandler(i);
						Thread.sleep(mDuration);
					}
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		private ArrayList<CardWrapper> mCards;
		private OnItemSelectedListener mSelectedListener = new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				indexOfCard = position;
				if(!mRadio){
					if(mUserSelected){
						mUserSelected = false;
						if(!mPaused){
							mPaused = true;
							//setTimeStamp();
							updatePausedState(mPaused);
							//TimeLogic.pause();
							stopSlider();
						}
						if(StreamPlayer.isPlaying()){
							StreamPlayer.stopMediaPlayer();
							//if(mPaused){
								//updateReadAloudState(mIndexOfReadAloudCard, "paused");
							//}else{
								updateReadAloudState(mIndexOfReadAloudCard, mCards.get(mIndexOfReadAloudCard).getTimeliness());
							//}
						}
					}
				}else{
					if(mUserSelected){
						mUserSelected = false;
						mRadio = false;
						mPaused = false;
						StreamPlayer.stopMediaPlayer();
						updateRadioState(mRadio);
						
					}
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
			
		};
	
	private void sendIndexToHandler(int index){
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putInt("index", index);
		msg.setData(b);
		sliderHandler.sendMessage(msg);
	}
	
	private final OnCompletionListener mOnCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			setTimeStamp();
		}
	};
	
	private final OnCompletionListener mOnRadioTrackCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			indexOfCard++;
			indexOfCard = indexOfCard % mNewsItems.size();
			Debugger.log("on complete " + indexOfCard + " mStopAt " + mStopAt);
			if(mStopAt != indexOfCard){
				StreamPlayer.stopMediaPlayer();
				mCardScrollView.animate(indexOfCard, CardScrollView.Animation.NAVIGATION);
				mAudio.playSoundEffect(Sounds.SUCCESS);
//				StreamPlayer.streamSoundFile(StreamPlayer.getURL(indexOfCard), mOnRadioTrackCompletionListener,
//						new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
			    File file = new File(Environment.getExternalStorageDirectory().toString(), "audio" + mNewsItems.get(indexOfCard).getID() + ".mp3");
	    	    if(file.exists()){
	    	    	StreamPlayer.streamSoundFile(file.getAbsolutePath(),  mOnRadioTrackCompletionListener, 
	    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
	    	    }else{
	    	    	StreamPlayer.streamSoundFile(mNewsItems.get(indexOfCard).getAudioUrl(),  mOnRadioTrackCompletionListener,
	    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
	    	    }
			}else{
				mRadio = false;
				mPaused = false;
				mCardScrollView.animate(indexOfCard, CardScrollView.Animation.NAVIGATION);
				updateRadioState(mRadio);
				//startSlider();
			}
		}
	};
	
		
	 protected void onCreate(Bundle bundle) {
	        super.onCreate(bundle);
	        Debugger.logCycle("onCreate NewsSlider");
	        mContext = this;
	        //make sure screen never sleeps, and therefore activity doesn't stop.
	        //Otherwise it will destroy itself after a while....
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	        
	        //voice detection
	        mVoiceMenu = new VoiceMenuEss(this, this, Constants.OKGLASS, mPhrases);
	     //   mVoiceMenu.changePhrases(new String[] { Constants.NEXT, Constants.PREVIOUS } );
	        
	        Intent intent = getIntent();
	        Bundle b = intent.getBundleExtra("newsitems");
	        mInAppStart = intent.getBooleanExtra("inappstart", false);
	        ArrayList<NewsItem> newsItems =  (ArrayList<NewsItem>)b.getSerializable("newsitems");
	        mNewsItems = newsItems;
	        
	        mLayoutSwitches = new ArrayList<Boolean>();
	        for(int i = 0; i < mNewsItems.size(); i++){
	        	double random = Math.random();
	        	if(random < 0.5){
	        		mLayoutSwitches.add(true);
	        	}else{
	        		mLayoutSwitches.add(false);
	        	}
	        }
	        
	        //create news item cards. Show only the 10 latest. 
	        ArrayList<CardWrapper> cards = new ArrayList<CardWrapper>(); 
	        for(int i = 0; i < mNewsItems.size() && i < 10; i++){
	        	CardWrapper cardWrapper = new CardWrapper(makeCard(i, false), "news", mNewsItems.get(i).getHeadline());
	        	cardWrapper.setTimeliness( calculateTimeliness( mNewsItems.get(i).getDate()) );
	        	cardWrapper.setBookmarked(mNewsItems.get(i).getBookmarked() );
	        	cards.add(cardWrapper);
	        }
	        mCards = cards;
	
	        mHistoryOfRelatedNewsItems = new HashMap<Integer, ArrayList<NewsItem>>();
	        mHistoryOfCardIndex = new HashMap<Integer, Integer>();
	        
	        mCardScrollView = new CardScrollView(this);
	        mCardScrollView.setOnItemSelectedListener(mSelectedListener);
	        mCardScrollAdapter = new MyCardScrollAdapter();
	        mCardScrollAdapter.setCards(mCards);
	        mCardScrollView.setAdapter(mCardScrollAdapter);
	        Debugger.log("before activate " + mCardScrollView);
	        mCardScrollView.activate();
	        Debugger.log("after activate " + mCardScrollView);
	        
	        setContentView(mCardScrollView);
	        
	        //load images and add sounds
	        for(int i = 0; i < mNewsItems.size(); i++){
	        	Debugger.log("audio URL : " + mNewsItems.get(i).getAudioUrl() );
	        	StreamPlayer.addUrl(mNewsItems.get(i).getAudioUrl()); //implement later
	        }
	        new DownloadImageTask(mCards, mCardScrollAdapter, mNewsItems).execute();
	        
	        
	       //Get audio manager. Enables audio feedback on actions. 
	        mAudio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
	        
	        //detect gestures, such a swipe down
	        mGestureDetector = createGestureDetector(this);
	      
	        mFollows = new ArrayList<Match>();
	        
	        //log url
	        actionUrlBase =  Utils.getBase() + "createLog.php?email="+Utils.getUsername()+"&action=";
	        //only run this code, if news slider is NOT started from ReadCardActivity
	        //that is, onlt run this ocde, if the news slider were started from the OS
	        if( ! mInAppStart){
		        //get soccer tournaments from EB
		        mTournaments = new HashMap<String, ArrayList<Match>>();
		        new DownloadMatchesFromEBTask(mContext, mCards, mCardScrollAdapter, mTournaments, mNewsItems).execute(Utils.EB_URL);
		      
		        //get poll 
		        mPoll = new Poll();
		        new DownloadPollTask(this, mCards, mCardScrollAdapter, mPoll, mNewsItems).execute(Utils.POLL_URL + Utils.getUsername());
		        
		        // download ALL audio file from ALL news items to external storage
		        int index = 0;
		        mDownloadFileTasks = new ArrayList<DownloadFileTask>();
		        for(NewsItem newsItem : mNewsItems){
		        	DownloadFileTask downloadTask = new DownloadFileTask(this, mCards.get(index), mCardScrollAdapter, newsItem);
		        	downloadTask.execute(newsItem.getAudioUrl());
		            mDownloadFileTasks.add(downloadTask);
		        	index++;
		        }
		        String actionUrl = actionUrlBase +"glass_app_opened";
		        new Thread(new GetRequestRunnable(actionUrl)).start();
	        }
	    }
	 
	 	public String calculateTimeliness(String dateString) {
	 		String dayString = dateString.substring(0, 2);
	    	String monthString = dateString.substring(3, 5);
	    	String yearString = dateString.substring(6, 10);
	    	String hourString = dateString.substring(11, 13);
	    	String minuteString = dateString.substring(14, 16);
	    	int year = Integer.parseInt( yearString );
	    	int month = Integer.parseInt( monthString );
	    	int day = Integer.parseInt( dayString );
	    	int hour = Integer.parseInt( hourString );
	    	int minute = Integer.parseInt( minuteString );
	    	GregorianCalendar pubDate = new GregorianCalendar(year, month-1, day, hour, minute);
	    	GregorianCalendar today =  (GregorianCalendar)GregorianCalendar.getInstance();

	    	long pubDateMillis= pubDate.getTimeInMillis();
	    	long todayMillis = today.getTimeInMillis();

	    	
	    	long difference = todayMillis - pubDateMillis;
	    	double days = difference / 1000.0 / 60.0 / 60.0 / 24.0;
	    	if(days < 1){
	    		double hours = days * 24.0;
	    		if(hours < 1){
	    			double minutes = hours * 60.0;
	    			return (int)minutes + " minutes ago";
	    		}else{
	    			return (int)hours + " hours ago";
	    		}
	    	}else{
	    		return (int)days + " days ago";
	    	}
	    	
	    	
	 	}

		protected void onStart(){
			super.onStart();
			 Debugger.logCycle("onStart NewsSlider");
			//if coming back from readCardAcitivty
			Debugger.log("coming back from read card activity " + readCardActivity);
			mPaused = true;
//			if(readCardActivity){
//				readCardActivity = false;
//				mPaused = true;
//			//	stopSlider();
//			}else{
//				mPaused = false;
//			}
			
			//if news slider was started from OS, read follow matches from internal storage and start slider
//			if(! mInAppStart){
//				//read follow matches from storage and update tournament cards accordingly
//				//readFollowMatchesFromInternalStorage();
//				
//				//mSliderThread = new Thread(sliderRunnable2);
//				//mSliderThread.start();
//				mPaused = false;
//			}else{
//				mPaused = true;
//			}
			//updatePausedState(mPaused);
			
		}
		protected void onResume(){
			super.onResume();
		     mVoiceMenu.start();
		     Debugger.log("voice detection started ");
		}
		
		protected void onPause(){
			super.onPause();
		    mVoiceMenu.stop();
		}
		
		protected void onStop(){
			super.onStop();
			Debugger.logCycle("onStop NewsSlider");
			if(! mInAppStart){
				writeFollowMatchesToInternalSotrage();
			}
			stopSlider();
		}
		
		protected void onDestroy(){
			Debugger.logCycle("onDestroy NewsSlider");
			StreamPlayer.releaseMediaPlayer();
			
			//cancel the downloading of audio files upon closing the activity and closing the app
			if(! mInAppStart){
				for(DownloadFileTask downloadFileTask : mDownloadFileTasks){
					downloadFileTask.cancel(true);
				}
				 String actionUrl = actionUrlBase +"glass_app_closed";
			     new Thread(new GetRequestRunnable(actionUrl)).start();
			}
			super.onDestroy();
		}

	    @Override
	    public boolean onCreatePanelMenu(int featureId, Menu menu) {
	    	if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
	            getMenuInflater().inflate(R.menu.news_slider, menu);
	            return true;
	        }
	        // Pass through to super to setup touch menu.
	        return super.onCreatePanelMenu(featureId, menu);
	    }
	    
	    @Override
	    public boolean onPreparePanel(int featureId, View view, Menu menu) {
	    	if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
	    		Debugger.log("in onPrepare");
	        // change menu for news item based on whether it has previously been bookmarked
	        	if(!mPaused && !mRadio){
		        	mPaused = true;
		        	updatePausedState(mPaused);
		        	stopSlider();
	        	}
	        	menu.removeItem(R.id.read_aloud_item);
	        	menu.removeItem(R.id.read_aloud_stop_item);
	        	menu.removeItem(R.id.readtext_item);
	        	menu.removeItem(R.id.bookmark_item);
	        	menu.removeItem(R.id.delete_bookmark_item);
	        	menu.removeItem(R.id.read_on_phone_item);
	        	menu.removeItem(R.id.radio_start_item);
	        	menu.removeItem(R.id.radio_stop_item);
	        	menu.removeItem(R.id.related_item);
	        	menu.removeItem(R.id.follow_league_item);
	        	menu.removeItem(R.id.unfollow_league_item);
	        	menu.removeItem(R.id.yes_item);
	        	menu.removeItem(R.id.no_item);
	        	getMenuInflater().inflate(R.menu.news_slider, menu);
	        	
	        	CardWrapper card = mCards.get(indexOfCard);
	        	if(card.getType().equals("tournament")){
	        		menu.removeItem(R.id.read_aloud_item);
	        		menu.removeItem(R.id.read_aloud_stop_item);
	        		menu.removeItem(R.id.readtext_item);
	        		menu.removeItem(R.id.bookmark_item);
	        		menu.removeItem(R.id.delete_bookmark_item);
		        	menu.removeItem(R.id.read_on_phone_item);
		        	menu.removeItem(R.id.related_item);
		        	menu.removeItem(R.id.radio_start_item);
		        	menu.removeItem(R.id.radio_stop_item);
		        	menu.removeItem(R.id.yes_item);
		        	menu.removeItem(R.id.no_item);
		        	ArrayList<Match> matches = mTournaments.get(mCards.get(indexOfCard).getName());
		        	//see if the user follow any game from the torunament. If the case, then follow = true;
		        	boolean follow = followsMatch(mCards.get(indexOfCard).getName());
		        	if(follow){
		        		menu.removeItem(R.id.follow_league_item);
		        	}else{
		        		menu.removeItem(R.id.unfollow_league_item);
		        	}
	        	}else if(card.getType().equals("news")){
	        		if(mRadio){
		        		menu.removeItem(R.id.radio_start_item);
		        		menu.removeItem(R.id.readtext_item);
		        		menu.removeItem(R.id.bookmark_item);
		        		menu.removeItem(R.id.delete_bookmark_item);
		        		menu.removeItem(R.id.read_aloud_item);
		        		menu.removeItem(R.id.read_aloud_stop_item);
		        		menu.removeItem(R.id.related_item);
			        	menu.removeItem(R.id.read_on_phone_item);
			        	menu.removeItem(R.id.follow_league_item);
			        	menu.removeItem(R.id.unfollow_league_item);
			        	menu.removeItem(R.id.yes_item);
			        	menu.removeItem(R.id.no_item);
		        	}else{
		        		menu.removeItem(R.id.radio_stop_item);
		        		menu.removeItem(R.id.follow_league_item);
			        	menu.removeItem(R.id.unfollow_league_item);
			        	menu.removeItem(R.id.yes_item);
			        	menu.removeItem(R.id.no_item);
			        	if(StreamPlayer.isPlaying()){
			        		menu.removeItem(R.id.read_aloud_item);
			        		menu.removeItem(R.id.radio_start_item);
			        		menu.removeItem(R.id.readtext_item);
			        		menu.removeItem(R.id.read_on_phone_item);
			        		menu.removeItem(R.id.bookmark_item);
			        		menu.removeItem(R.id.delete_bookmark_item);
			        		menu.removeItem(R.id.related_item);
			        	}else{
			        		menu.removeItem(R.id.read_aloud_stop_item);
			        	}
			        	//if news item has no related news items, remove action "related"
			        	if(mNewsItems.get(indexOfCard).getRelated().size() == 0){
			        		menu.removeItem(R.id.related_item);
			        	}
			        	if(mNewsItems.get(indexOfCard).getBookmarked()){
			        		menu.removeItem(R.id.bookmark_item);
			        	}else{
			        		menu.removeItem(R.id.delete_bookmark_item);
			        	}
		        	}
	        	}else if(card.getType().equals("poll") ){
	        		if(!mPoll.getHasVoted()){
		        		menu.removeItem(R.id.read_aloud_item);
		        		menu.removeItem(R.id.read_aloud_stop_item);
			        	menu.removeItem(R.id.readtext_item);
			        	menu.removeItem(R.id.bookmark_item);
			        	menu.removeItem(R.id.delete_bookmark_item);
			        	menu.removeItem(R.id.read_on_phone_item);
			        	menu.removeItem(R.id.radio_start_item);
			        	menu.removeItem(R.id.radio_stop_item);
			        	menu.removeItem(R.id.related_item);
			        	menu.removeItem(R.id.follow_league_item);
			        	menu.removeItem(R.id.unfollow_league_item);
			        	return true;
	        		}else if(mPoll.getHasVoted()){
	        			menu.removeItem(R.id.read_aloud_item);
	        			menu.removeItem(R.id.read_aloud_stop_item);
			        	menu.removeItem(R.id.readtext_item);
			        	menu.removeItem(R.id.bookmark_item);
			        	menu.removeItem(R.id.delete_bookmark_item);
			        	menu.removeItem(R.id.read_on_phone_item);
			        	menu.removeItem(R.id.radio_start_item);
			        	menu.removeItem(R.id.radio_stop_item);
			        	menu.removeItem(R.id.related_item);
			        	menu.removeItem(R.id.follow_league_item);
			        	menu.removeItem(R.id.unfollow_league_item);
			        	menu.removeItem(R.id.yes_item);
			        	menu.removeItem(R.id.no_item);
			        	return false;
	        		}
	        	}
	        	return true;
	        }
	        // Good practice to call through to super for other cases
	        return super.onPreparePanel(featureId, view, menu);
	    }
	    

	    @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.news_slider, menu);
	        return super.onCreateOptionsMenu(menu);
	    }
	    
	    

	    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	        // Good practice to pass through to super if not handled
	    	 if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
	    		 switch (item.getItemId()) {
	             	case R.id.read_aloud_item:
	             		 readAloud();
		 	           	 break;
	             	case R.id.read_aloud_stop_item:
	             		 StreamPlayer.stopMediaPlayer();
	             		 setTimeStamp();
		 	           	 break;
	             	case R.id.readtext_item:
	             		 readText();
		 	           	 break;
	             	case R.id.bookmark_item:
	             		 bookmarkOnPhone(mNewsItems.get(indexOfCard));
		 	           	 break;
	             	case R.id.delete_bookmark_item:
	             		 deleteBookmark(mNewsItems.get(indexOfCard));
		 	           	 break;
		             case R.id.read_on_phone_item:
		            	 readOnPhone();
		 	           	 break;
		             case R.id.radio_start_item:
		 	           	 playRadio();
		            	 break;
		             case R.id.radio_stop_item:
		            	  stopRadio();
			              break;
			         case R.id.follow_league_item:
			        	 followLeague();
				         break;
			         case R.id.related_item:
			        	 openRelated();
			        	 break;
			         case R.id.unfollow_league_item:
			        	 unfollowLeague();
			        	 break;
			         case R.id.yes_item:
			        	 vote(mPoll.getAnswer1());
			        	 break;
			         case R.id.no_item:
			        	 vote(mPoll.getAnswer2());
			        	 break;
	 	           default:
	                    return true;
	             }
	    	 }
	        return super.onMenuItemSelected(featureId, item);
	    }
	    

		private void readAloud(){
	    	mPaused = true;
       	 	//TimeLogic.pause();
	    	stopSlider();
       	 	updateReadAloudState(indexOfCard, "reading aloud");
       	 	Debugger.log("url ? " +StreamPlayer.getURL(indexOfCard));
    	    StreamPlayer.stopMediaPlayer();
    	    File file = new File(Environment.getExternalStorageDirectory().toString(), "audio" + mNewsItems.get(indexOfCard).getID() + ".mp3");
//    	    if(mNewsItems.get(indexOfCard).getExternalAudioUrl() != null){
//    	    	StreamPlayer.streamSoundFile(mNewsItems.get(indexOfCard).getExternalAudioUrl(), mOnCompletionListener, 
//    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
//    	    }else{
//    	    	StreamPlayer.streamSoundFile(mNewsItems.get(indexOfCard).getAudioUrl(), mOnCompletionListener,
//    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
//    	    }
    	    if(file.exists()){
    	    	StreamPlayer.streamSoundFile(file.getAbsolutePath(), mOnCompletionListener, 
    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
    	    }else{
    	    	StreamPlayer.streamSoundFile(mNewsItems.get(indexOfCard).getAudioUrl(), mOnCompletionListener,
    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
    	    }
    	    
    	    String actionUrl = actionUrlBase +"read_aloud";
		    new Thread(new GetRequestRunnable(actionUrl)).start();
	    }
	    
	    private void readText(){
	    	mAudio.playSoundEffect(Sounds.TAP);
		 //    NewsItem newsItem = mNewsItems.get(indexOfCard);
	       	 Intent intent = new Intent(this, ReadCardActivity.class);
	       	 Bundle bundle = new Bundle();
	         bundle.putSerializable("newsitem", mNewsItems.get(indexOfCard));
	       	 intent.putExtra("newsitem", bundle);
	       	 //intent.putExtra("index", indexOfCard);
	       	 startActivity(intent);
	       	 updateState("opening article");
	       	 readCardActivity = true;
	       	 
	       	String actionUrl = actionUrlBase +"show_article";
		    new Thread(new GetRequestRunnable(actionUrl)).start();
	    }
	    
	    private void readOnPhone(){
	    	mAudio.playSoundEffect(Sounds.SUCCESS);
	    	
	    	// Sending the item to the server, which pushes the item to the phone of the Glass wearer
	    	NewsItem newsItem = mNewsItems.get(indexOfCard); 
	    	
	    	List<NameValuePair> postData = new ArrayList<NameValuePair>();
	      	 postData.add(new BasicNameValuePair("newsId", ""+newsItem.getID()));
	  		 postData.add(new BasicNameValuePair("email", Utils.getUsername())); //replace with Utils.getUser..
	  		 postData.add(new BasicNameValuePair("headline", newsItem.getHeadline()));
	  		 postData.add(new BasicNameValuePair("subheadline", newsItem.getSubHeadline()));
	  		 postData.add(new BasicNameValuePair("body", newsItem.getBody()));
	  		 postData.add(new BasicNameValuePair("imageUrl", newsItem.getImageUrl()));
	  		 postData.add(new BasicNameValuePair("thumbnailUrl", newsItem.getThumbnailUrl()));
	  		 postData.add(new BasicNameValuePair("date", newsItem.getDate()));
	  		 postData.add(new BasicNameValuePair("bookmarked", ""+newsItem.getBookmarked()));
	  		 Utils.postData(Utils.getBase() + "pushToPhone.php", postData);
	  		 
	  		 updateState("opening on phone");
	  		 
	  		 String actionUrl = actionUrlBase +"open_on_phone";
			    new Thread(new GetRequestRunnable(actionUrl)).start();
	    }
	    
	    public void bookmarkOnPhone(NewsItem newsItem){
	    	mAudio.playSoundEffect(Sounds.SUCCESS);
	    	
	    	 newsItem.setBookmarked(true);
	    	 mCards.get(indexOfCard).setBookmarked(true);
			// Sending the item to the server
			 List<NameValuePair> postData = new ArrayList<NameValuePair>();
			 postData.add(new BasicNameValuePair("newsId", ""+newsItem.getID()));
			 postData.add(new BasicNameValuePair("email", Utils.getUsername()));
			 postData.add(new BasicNameValuePair("title", newsItem.getHeadline()));
			 postData.add(new BasicNameValuePair("subtitle", newsItem.getSubHeadline()));
			 postData.add(new BasicNameValuePair("body", newsItem.getBody()));
			 postData.add(new BasicNameValuePair("imageUrl", newsItem.getImageUrl()));
			 postData.add(new BasicNameValuePair("thumbnailUrl", newsItem.getThumbnailUrl()));
			 postData.add(new BasicNameValuePair("date", newsItem.getDate()));
			 Utils.postData(Utils.getBase() + "addBookmark.php", postData);
			 
			 updateState("bookmarked ★");
			 
			 String actionUrl = actionUrlBase +"bookmark";
			    new Thread(new GetRequestRunnable(actionUrl)).start();
			 
		 }
	    public void deleteBookmark(NewsItem newsItem){
	    	 mAudio.playSoundEffect(Sounds.SUCCESS);

	    	 newsItem.setBookmarked(false);
	    	 mCards.get(indexOfCard).setBookmarked(false);
			// Sending the item to the server
			 List<NameValuePair> postData = new ArrayList<NameValuePair>();
			 postData.add(new BasicNameValuePair("newsId", ""+newsItem.getID()));
			 postData.add(new BasicNameValuePair("email", Utils.getUsername()));
		
			 String url = Utils.getBase() + "deleteBookmark.php";
			 Utils.postData(url, postData);
			 
			 updateState("bookmark deleted");
			 
			 String actionUrl = actionUrlBase +"delete_bookmark";
			    new Thread(new GetRequestRunnable(actionUrl)).start();
		 }
	    
	    private void playRadio(){
	    	mRadio = true;
	    	mStopAt = indexOfCard ;
	    	updateRadioState(mRadio);
	    	StreamPlayer.stopMediaPlayer();
	    	  File file = new File(Environment.getExternalStorageDirectory().toString(), "audio" + mNewsItems.get(indexOfCard).getID() + ".mp3");
//	    	    if(mNewsItems.get(indexOfCard).getExternalAudioUrl() != null){
//	    	    	StreamPlayer.streamSoundFile(mNewsItems.get(indexOfCard).getExternalAudioUrl(), mOnCompletionListener, 
//	    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
//	    	    }else{
//	    	    	StreamPlayer.streamSoundFile(mNewsItems.get(indexOfCard).getAudioUrl(), mOnCompletionListener,
//	    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
//	    	    }
	    	    if(file.exists()){
	    	    	StreamPlayer.streamSoundFile(file.getAbsolutePath(), mOnRadioTrackCompletionListener, 
	    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
	    	    }else{
	    	    	StreamPlayer.streamSoundFile(mNewsItems.get(indexOfCard).getAudioUrl(), mOnRadioTrackCompletionListener,
	    	    			new StreamOnPreparedListener(mCards.get(indexOfCard), mCardScrollAdapter));
	    	    }
	    	    
	    		
        		String actionUrl = actionUrlBase +"playall_started";
			    new Thread(new GetRequestRunnable(actionUrl)).start();
	    }
	    private void stopRadio(){
	    	mRadio = false; 
	    //	mPaused = false;
	        StreamPlayer.stopMediaPlayer();
	        updateRadioState(mRadio);
	      //  startSlider();
	        
	        String actionUrl = actionUrlBase +"playall_stopped";
	        new Thread(new GetRequestRunnable(actionUrl)).start();
	      //  TimeLogic.play();
	    }
	    
	    private boolean followsMatch(String tournament){
	    	ArrayList<Match> matches = mTournaments.get(tournament);
        	//see if the user follow any game from the torunament. If the case, then follow = true;
        	boolean follow = false;
        	for(Match match : matches){
        		for(Match followMatch : mFollows){
        			if(match.equals(followMatch)){
        				follow = true;
        			}
        		}
        	}
        	return follow;
	    }
	    private void followLeague(){
	    	 mAudio.playSoundEffect(Sounds.SUCCESS);
	    	 CardWrapper cardWrapper = mCards.get(indexOfCard);
	    	 ArrayList<Match> matches = mTournaments.get(cardWrapper.getName());
	    	 cardWrapper.setTimeliness("following " + matches.size() + " matches today");
        	 for(Match match : matches){
        		 match.setFollow(true);
        		 mFollows.add(match);
        	 }
			 Intent intent = new Intent(this, ScoreCard.class);
			 Bundle bundle = new Bundle();
			 bundle.putSerializable("matches", matches);
			 intent.putExtra("matches", bundle);  
			 startService(intent);
			 
			 updateState("following");
			 
			 String actionUrl = actionUrlBase +"follow_league";
			    new Thread(new GetRequestRunnable(actionUrl)).start();
	    }
	    private void unfollowLeague(){
	    	mAudio.playSoundEffect(Sounds.SUCCESS);
	    	CardWrapper cardWrapper = mCards.get(indexOfCard);
	    	ArrayList<Match> matches = mTournaments.get(cardWrapper.getName());
	    	cardWrapper.setTimeliness(matches.size() + " matches today");
        	 for(Match match : matches){
        		 match.setFollow(false);
        		 mFollows.remove(match);
        	 }
			 Intent intent = new Intent(this, ScoreCard.class);
			 Bundle bundle = new Bundle();
			 bundle.putSerializable("matches", matches);
			 intent.putExtra("matches", bundle);  
			 startService(intent);
			 
			 updateState("unfollowing");
			 
			 String actionUrl = actionUrlBase +"unfollow_league";
			    new Thread(new GetRequestRunnable(actionUrl)).start();
	    }
	    
	    private void openRelated(){
	        Debugger.log("historyIndexOfCard in openRelated " + indexOfCard);
	    	NewsItem newsItem = mNewsItems.get(indexOfCard); 
//	    	ArrayList<NewsItem> related = newsItem.getRelated();
//	    	ArrayList<CardWrapper> cards = new ArrayList<CardWrapper>();
//	    	for(int i = 0; i < related.size(); i++){
//	    		NewsItem itemRelated = related.get(i);
//	    		double random = Math.random();
//	    		CardBuilder card;
//	    		if(random < 0.5){
//	    			card = new CardBuilder(this, CardBuilder.Layout.TEXT);
//	    		}else{
//	    			card = new CardBuilder(this, CardBuilder.Layout.COLUMNS);
//	    		}
//		    	
//	    		card.setText(itemRelated.getHeadline());
//		    	card.setFootnote((i+1) +"/"+ related.size() + " - " + itemRelated.getSection());
//		    	
//		    	String dateString = itemRelated.getDate();
//		    	String timeliness = calculateTimeliness(dateString);
//		    	card.setTimestamp( timeliness);
//	    		CardWrapper cardWrapper = new CardWrapper(card, "news", itemRelated.getHeadline());
//	    		cards.add(cardWrapper);
//	    	}
//	    	mHistoryOfRelatedNewsItems.put(mIndexOfRelated, mNewsItems);
//	        mNewsItems = related;
//	        mHistoryOfCardIndex.put(mIndexOfRelated, indexOfCard);
//	     	mIndexOfRelated++;
//	     	indexOfCard = 0;
//	    	mCards = cards;
//	    	mCardScrollAdapter.setCards(mCards);
//	    	mCardScrollAdapter.notifyDataSetChanged();
//	    	
//	        new DownloadImageTask(mCards, mCardScrollAdapter, mNewsItems).execute();
	        
	    	 mAudio.playSoundEffect(Sounds.TAP);
	    	 
	    	 String actionUrl = actionUrlBase +"related";
			    new Thread(new GetRequestRunnable(actionUrl)).start();
			    
			 ArrayList<NewsItem> related = newsItem.getRelated();
			Intent intent = new Intent(this, NewsSliderActivity.class);
		//	intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			Bundle bundle = new Bundle();
			bundle.putSerializable("newsitems", related);
			intent.putExtra("newsitems", bundle);
			intent.putExtra("inappstart", true);
			startActivity(intent);
	    	
			
	    	
	    	//reset indexOfCard to 0
	    }
	    private void vote(String vote) {
	    	mAudio.playSoundEffect(Sounds.SUCCESS);
	    	
	 			mPoll.setHasVoted(true);
	 			String request = Utils.VOTE_URL + Utils.getUsername() + "&id="+mPoll.getID() + "&";
	 			if(vote.equals(mPoll.getAnswer1())){
	 				mPoll.setVote(1);
	 				int noOfAnswer1 = mPoll.getNoOfAnswer1();
	 				noOfAnswer1++;
	 				mPoll.setNoOfAnswer1(noOfAnswer1);
	 				request +="answer1=1&answer2=0";
	 			}else if(vote.equals(mPoll.getAnswer2())){
	 				mPoll.setVote(2);
	 				int noOfAnswer2 = mPoll.getNoOfAnswer2();
	 				noOfAnswer2++;
	 				mPoll.setNoOfAnswer2(noOfAnswer2);
	 				request +="answer1=0&anwser2=1";
	 			}
	 			new Thread(new GetRequestRunnable(request)).start();
	 			ArrayList<CardWrapper> pollCards = Poll.createPollCards(this, mPoll);
	 			for(int i = 0; i < mCards.size(); i++){
	 				CardWrapper card = mCards.get(i);
	 				if(card.getType().equals("poll") && card.getName().equals(mPoll.getQuestion())){
	 					mCardScrollAdapter.removeItem(i);
	 					mCardScrollAdapter.addItem(pollCards.get(0));
	 				}
	 			}
	 			mCardScrollAdapter.notifyDataSetChanged();
	 			
	 	}
	    
	    public void startSlider(){
	    	if(mSliderThread == null){
	    		mSliderThread = new Thread(sliderRunnable2);
	    		mSliderThread.start();
	    	}
	    }
	    public void stopSlider(){
	    	if(mSliderThread != null){
	    		mSliderThread.interrupt();
	    		mSliderThread = null;
	    	}
	    }
	    
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event) {
	        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
	        	String actionUrl = actionUrlBase +"tap";
        		new Thread(new GetRequestRunnable(actionUrl)).start();
	        	openOptionsMenu();
	        	return true;
	        }
	     
	        return super.onKeyDown(keyCode, event);
	    }
	    
	    @Override //this allows me to control what happens when option menu shows
	    public void openOptionsMenu(){
	    	super.openOptionsMenu();
	    }
	    
	    
	    private void updatePausedState(boolean paused){
	    	//for(CardWrapper card : mCards){
	    		if(paused){
	    			new Thread(new TextAnimation("paused", mCards.get(indexOfCard), mCardScrollAdapter).getAnimationRunnable()).start();
	    		}else{
		    		new Thread(new TextAnimation("sliding", mCards.get(indexOfCard), mCardScrollAdapter).getAnimationRunnable()).start();
		    	}
	    }
	    
	    private void updateRadioState(boolean paused){
	    	for(CardWrapper card : mCards){
	    		Object obj = card.getCard();
	    		if(paused){
	    			if(obj instanceof CardBuilder){
	    				CardBuilder cardBuilder = (CardBuilder)obj;
	    				cardBuilder.setTimestamp("play all");
	    			}
		    	}else{
		    		if(obj instanceof CardBuilder){
	    				CardBuilder cardBuilder = (CardBuilder)obj;
	    				cardBuilder.setTimestamp(card.getTimeliness());
	    			}
		    	}
	    	}
	    	mCardScrollAdapter.notifyDataSetChanged();
	    }
	    
	    private void updateReadAloudState(int index, String state){
	    	Object obj = mCards.get(index).getCard();
	    	if(obj instanceof CardBuilder){
	    		CardBuilder card = (CardBuilder)obj;
	    		card.setTimestamp(state);
	    		//save index of card that is being read aloud, so that the state of the card can be altered
	    		mIndexOfReadAloudCard = index;
	    	}
	    	mCardScrollAdapter.notifyDataSetChanged();
	    }
	    
	    private void setTimeStamp(){
	    	CardWrapper cardWrapper = mCards.get(mIndexOfReadAloudCard);
			Object obj = cardWrapper.getCard();
			if(obj instanceof CardBuilder){
				CardBuilder card = (CardBuilder)obj;
				if(mNewsItems.get(mIndexOfReadAloudCard).getBookmarked()){
					card.setTimestamp(cardWrapper.getTimeliness() + "  ★");
				}else{
					card.setTimestamp(cardWrapper.getTimeliness());
				}
				
			}
			mCardScrollAdapter.notifyDataSetChanged();
	    }
	    
	    private void updateState(String state){
	    		new Thread(new TextAnimation(state, mCards.get(indexOfCard), mCardScrollAdapter).getAnimationRunnable()).start();
	    }
	    
	    private CardBuilder makeCard(int index, boolean paused){
	    	CardBuilder card;
	    	if(mLayoutSwitches.get(index)){
	    		card = new CardBuilder(mContext, CardBuilder.Layout.COLUMNS);
	    	}
	    	else{
	    		card = new CardBuilder(mContext, CardBuilder.Layout.TEXT);
	    	}
	    	
	    	NewsItem newsItem = mNewsItems.get(index);
	    	card.setText(newsItem.getHeadline());
	    	card.setFootnote((index+1) +"/"+ mNewsItems.size() + " - " + newsItem.getSection());
	    	
	    	String dateString = newsItem.getDate();
	    	String timeliness = calculateTimeliness(dateString);
	    	if(newsItem.getBookmarked()){
	    		card.setTimestamp(timeliness+ "  ★ ");
	    	}else{
	    		card.setTimestamp(timeliness);
	    	}
	    
    		return card;
	    }
	 
	    
	    
	    private GestureDetector createGestureDetector(Context context) {
	        GestureDetector gestureDetector = new GestureDetector(context);
	            //Create a base listener for generic gestures
	            gestureDetector.setBaseListener( new GestureDetector.BaseListener() {

					@Override
					public boolean onGesture(Gesture gesture) {
						if(gesture == Gesture.LONG_PRESS){
							 // user tapped touchpad, pause news slides
				        	if(!mPaused){
				        		mPaused = true;
				        		updatePausedState(mPaused);
				        		stopSlider();
				        		
				        		String actionUrl = actionUrlBase +"slider_stopped";
							    new Thread(new GetRequestRunnable(actionUrl)).start();
				        		
							    return true;
				        	}else if (mPaused){
				        		mPaused = false;
				        		updatePausedState(mPaused);
				        		startSlider();
				        		
				        		String actionUrl = actionUrlBase +"slider_started";
							    new Thread(new GetRequestRunnable(actionUrl)).start();
				        		
				        		return true;
				        	}
						}
						else if(gesture == Gesture.SWIPE_LEFT || gesture == Gesture.SWIPE_RIGHT){
						    mUserSelected = true;
							return true;
						}	
						else if(gesture == Gesture.SWIPE_DOWN){
							if(StreamPlayer.isPlaying()){
								StreamPlayer.stopMediaPlayer();
							}
							return false;
						}
						return false;
					}
	            });
	           
	            return gestureDetector;
	        }
	        
	    
//	    private void goBackHistory(){
//	    	mIndexOfRelated--;
//			mAudio.playSoundEffect(Sounds.DISMISSED);
//			//ArrayList<CardWrapper> historyCards = mHistoryOfRelatedCards.get(mIndexOfRelated);
//			ArrayList<NewsItem> historyNewsItems = mHistoryOfRelatedNewsItems.get(mIndexOfRelated);
//			int historyIndexOfCard = mHistoryOfCardIndex.get(mIndexOfRelated);
//		//	mCards = historyCards;
//			ArrayList<CardWrapper> historyCards = new ArrayList<CardWrapper>();
//			int index = 0;
//			for(NewsItem newsItem : historyNewsItems){
//				double random = Math.random();
//				CardBuilder card = null;
//				if(random < 0.5){
//					card = new CardBuilder(this, CardBuilder.Layout.TEXT);
//				}else{
//					card = new CardBuilder(this, CardBuilder.Layout.COLUMNS);
//				}
//				card.setText(newsItem.getHeadline());
//				card.setFootnote((index+1) +"/"+ historyNewsItems.size() + " - " + newsItem.getSection());
//				card.setTimestamp( calculateTimeliness( newsItem.getDate() ) );
//				CardWrapper cardWrapper = new CardWrapper(card, "news", newsItem.getHeadline());
//				historyCards.add(cardWrapper);
//				
//				index++;
//			}
//			//if back at the beginning of history
//			mCards = historyCards;
//			ArrayList<CardWrapper> tournamentCards = new ArrayList<CardWrapper>();
//			ArrayList<CardWrapper> pollCards = new ArrayList<CardWrapper>();
//			//if on main news slider
//			if(mIndexOfRelated == 0 && !mInAppStart){
//				tournamentCards = TournamentLogic.createTournamentCards(this, mTournaments);
//				mCards.addAll(tournamentCards);
//				pollCards = Poll.createPollCards(this, mPoll);
//				mCards.addAll(pollCards);
//			}
//			mCardScrollAdapter.setCards(mCards);
//			Debugger.log("historyIndexOfCard " + historyIndexOfCard);
//			mCardScrollView.setSelection(historyIndexOfCard);
//			mCardScrollAdapter.notifyDataSetChanged();
//			mNewsItems = historyNewsItems;
//			new DownloadImageTask(mCards, mCardScrollAdapter, mNewsItems).execute();
//	    }
	    /*
	     * Send generic motion events to the gesture detector
	     */
	    @Override
	    public boolean onGenericMotionEvent(MotionEvent event) {
	        if (mGestureDetector != null) {
	            return mGestureDetector.onMotionEvent(event);
	        }
	        return false;
	    }

	    
	    
	    private void readFollowMatchesFromInternalStorage(){	
	    	try{
	    		FileInputStream fis = openFileInput(Utils.FILENAME);
				ObjectInputStream objIn = new ObjectInputStream(fis);
				mFollows = (ArrayList<Match>)objIn.readObject();
				Debugger.log("follows null? " +mFollows);
				if(mFollows ==null){
					mFollows = new ArrayList<Match>();
				}
				objIn.close();
				fis.close();
			}catch(Exception e){
				e.printStackTrace();
			}
	    }
	    
	    private void writeFollowMatchesToInternalSotrage(){
	    	//when liveticker closes, erase any followed matches form local memory	
	    	try{
				FileOutputStream fos = openFileOutput(Utils.FILENAME, Context.MODE_PRIVATE);
				ObjectOutputStream objOut = new ObjectOutputStream(fos);
				//ArrayList<Match> followed = new ArrayList<Match>();
				objOut.writeObject(mFollows);
				objOut.close();
				fos.close();
			}catch(Exception e){
				e.printStackTrace();
			}
	    }
	    
	    public void setFollowMatches(ArrayList<Match> follows){
	    	mFollows = follows;
	    }

	  //implementation of VoiceDetectionListener
		@Override
		public void onHotwordDetected() {
			Debugger.log("ok glass detected");
			if(!mPaused){
				mPaused = true;
	    		updatePausedState(mPaused);
	    		//TimeLogic.pause();	
	    		stopSlider();
			}
    		//set phrases
    		CardWrapper card = mCards.get(indexOfCard);
        	if(card.getType().equals("tournament")){
        		Debugger.log("set phrase to 'follow league'  /'unfollow league'");
        		boolean follow = followsMatch(mCards.get(indexOfCard).getName());
        		if(follow){
        			mPhrases = new String[]{ Constants.UNFOLLOWLEAGUE, Constants.CLOSEAPP };
        		}else{
        			mPhrases = new String[]{ Constants.FOLLOWLEAGUE, Constants.CLOSEAPP };
        		}
        	}else if(card.getType().equals("news")){
        		if(mRadio){
        			//if in related
        				mPhrases = new String[]{ Constants.RADIOSTOP };
        		}else if(StreamPlayer.isPlaying()){
        			//if read aloud feature is activated
        			mPhrases = new String[]{ Constants.READALOUDSTOP };
        		}else{
        			//if in related
        		//	if(mIndexOfRelated > 0){
        				//if news item has related
        				if(mNewsItems.get(indexOfCard).getRelated().size() > 0){
        					//if news item is bookmarked
        					if(mNewsItems.get(indexOfCard).getBookmarked()){
        						//if activity was starrted from in app, provide GO BACK possibility
        						if(mInAppStart){
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.DELETEBOOKMARK, Constants.RELATED, Constants.RADIOSTART, Constants.GOBACK };
        						}else{
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.DELETEBOOKMARK, Constants.RELATED, Constants.RADIOSTART, Constants.CLOSEAPP };
        						}
        					}else{
        						if(mInAppStart){
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.BOOKMARK, Constants.RELATED, Constants.RADIOSTART, Constants.GOBACK };
        						}else{
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.BOOKMARK, Constants.RELATED, Constants.RADIOSTART, Constants.CLOSEAPP };
        						}
        						 
        					}
        				}else{
        					//if news item is bookmarked
        					if(mNewsItems.get(indexOfCard).getBookmarked()){
        						if(mInAppStart){
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.DELETEBOOKMARK, Constants.RADIOSTART, Constants.GOBACK };
        						}else{
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.DELETEBOOKMARK, Constants.RADIOSTART, Constants.CLOSEAPP }; 
        						}
        					}else{
        						if(mInAppStart){
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.BOOKMARK, Constants.RADIOSTART, Constants.GOBACK };
        						}else{
        							mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
        		        					Constants.BOOKMARK, Constants.RADIOSTART, Constants.CLOSEAPP };
        						}
        					} 
        				}
//        			}else{
//        				//if news item has related
//        				if(mNewsItems.get(indexOfCard).getRelated().size() > 0){
//        					//if news item is bookmarked
//        					if(mNewsItems.get(indexOfCard).getBookmarked()){
//		        				mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
//			        					Constants.DELETEBOOKMARK, Constants.RELATED, Constants.RADIOSTART, Constants.CLOSEAPP }; 
//        					}else{
//        						mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
//			        					Constants.BOOKMARK, Constants.RELATED, Constants.RADIOSTART, Constants.CLOSEAPP }; 
//        					}
//        				}else{
//        					//if news item is bookmarked
//        					if(mNewsItems.get(indexOfCard).getBookmarked()){
//	        					mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
//			        					Constants.DELETEBOOKMARK, Constants.RADIOSTART, Constants.CLOSEAPP }; 
//        					}else{
//        						mPhrases = new String[]{ Constants.READALOUD, Constants.READTEXT, Constants.READONPHONE, 
//			        					Constants.BOOKMARK, Constants.RADIOSTART, Constants.CLOSEAPP }; 
//        					}
//        				}
//        			}
        		}
        	}else if(card.getType().equals("poll")){
        		if(mPoll.getHasVoted()){
        			mPhrases = new String[]{Constants.CLOSEAPP}; 
        		}else{
        			mPhrases = new String[]{ Constants.YES, Constants.NO, Constants.CLOSEAPP };
        		}
        	}
        	mVoiceMenu.changePhrases(mPhrases);
    		
		}

		@Override
		public void onPhraseDetected(int index, String phrase) {
			 String actionUrl = actionUrlBase +"on_hotword";
		     new Thread(new GetRequestRunnable(actionUrl)).start();
		     
			Debugger.log("phrase " + phrase + " detected");
			mVoiceMenu.changePhrases(new String[]{});
			if(phrase.equals(Constants.READALOUD)){
				readAloud();
			}
			else if(phrase.equals(Constants.READALOUDSTOP)){
				StreamPlayer.stopMediaPlayer();
				setTimeStamp();
			}
			else if(phrase.equals(Constants.READTEXT)){
				readText();
			}
			else if(phrase.equals(Constants.READONPHONE)){
				readOnPhone();
			}
			else if(phrase.equals(Constants.BOOKMARK)){
				bookmarkOnPhone(mNewsItems.get(indexOfCard));
			}
			else if(phrase.equals(Constants.DELETEBOOKMARK)){
				deleteBookmark(mNewsItems.get(indexOfCard));
			}
			else if(phrase.equals(Constants.RADIOSTART)){
				playRadio();
			}
			else if(phrase.equals(Constants.RADIOSTOP)){
				stopRadio();
			}
			else if(phrase.equals(Constants.FOLLOWLEAGUE)){
				followLeague();
			}
			else if(phrase.equals(Constants.UNFOLLOWLEAGUE)){
				unfollowLeague();
			}
			else if(phrase.equals(Constants.RELATED)){
				openRelated();
			}
			else if(phrase.equals(Constants.GOBACK)){
			//	goBackHistory();
				finish();
			}
			else if(phrase.equals(Constants.CLOSEAPP)){
				finish();
			}
			else if(phrase.equals(Constants.NEXT)){
				int i = indexOfCard + 1;
				i = i % mCards.size();
				mCardScrollView.animate(i, CardScrollView.Animation.NAVIGATION);
			}
			else if(phrase.equals(Constants.PREVIOUS)){
				int i = indexOfCard-1;
				if(i >= 0){
					mCardScrollView.animate(i, CardScrollView.Animation.NAVIGATION);
				}else{
					i = mCards.size()-1;
					mCardScrollView.animate(i, CardScrollView.Animation.NAVIGATION);
				}
			}
		}

		//implementation of VoiceMenulistener
		@Override
		public void onPhraseSelected(String phrase) {
			Debugger.log("phrase selected " + phrase);
		}
	    
	    
}
