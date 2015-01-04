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

public class ScoreCard extends Service {

    private static final String LIVE_CARD_TAG = "LiveCardDemo";
    private static final String URL = "http://ekstrabladet.dk/sport/kampe/kamp/?matchid=5532148";

    private LiveCard mLiveCard;
    private RemoteViews mLiveCardView;
    private RemoteViews mEventCardView;
    private RemoteViews mEventCardView2;
    private Context mContext;
    private ArrayList<Event> mEvents;
    private ArrayList<Event> mEventsForIntent;
    private int mHomeScore, mAwayScore;
    private int mEventIndex = -1;
    private String mStatus = "1ST HALF"; 
    private long mSeconds;
    private Match mMatch;
    private int mNoEvents = 0;
    private ArrayList<Match> mMatches;


    private final Handler mHandler = new Handler();
//    private final UpdateLiveCardRunnable mUpdateLiveCardRunnable =
//        new UpdateLiveCardRunnable();
    private final UpdateScoreCardsRunnable mUpdateScoreCardsRunnable =
    		new UpdateScoreCardsRunnable();
    //use mEventMode as a flag for MenuActivity to prepare menu, 
    //if true, an option for closing event card will be shown
    private boolean mEventMode = false; 
	private HashSet<String> mTournaments;
//	private UpdateScoreCard mUpdateScoreCard;
	
    private static final long DELAY_MILLIS = 5000;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Debugger.log("start scorecard service");
    	if (mLiveCard == null) {
    		mMatches = new ArrayList<Match>();
    		Debugger.log("live card is null");
        	mContext = this;
        	
        	SoundPlayer.initSounds(mContext);
        	//mSeconds = System.currentTimeMillis()/1000;
        	
        	//get match which the liveticker will display information about
        	Bundle bundleMatch = intent.getBundleExtra("match");
        	Bundle bundleMatches = intent.getBundleExtra("matches");
        	if(bundleMatch != null){
	        	mMatch = (Match) bundleMatch.getSerializable("match");
        		if(mMatch.getFollow()){
	        		mMatches.add(mMatch);
	        	}else{
	        		mMatches.remove(mMatch);
	        	}
        	}
        	if(bundleMatches != null){
        		ArrayList<Match> matches = (ArrayList<Match>) bundleMatches.getSerializable("matches");
        		for(Match match : matches){
        			if(match.getFollow()){
        				mMatches.add(match);
        			}else{
        				mMatches.remove(match);
        			}
        		}
        	}
        	writeFollowMatchesToInternalSotrage();
//        	mUpdateScoreCard = new UpdateScoreCard(this);
        //	Debugger.log("match " + mMatch.getHomeTeamName() + " " + mMatch.getAwayTeamName());
        	
            // Get an instance of a live card
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);
           
            // Inflate a layout into a remote views
            mEventCardView = new RemoteViews(getPackageName(),
                    R.layout.event_card_layout);
            mEventCardView2 = new RemoteViews(getPackageName(),
                    R.layout.event_card_layout2);
            
            mLiveCardView = new RemoteViews(getPackageName(),
            		R.layout.service_main);
            
            mTournaments = new HashSet<String>();
            for(Match match : mMatches){
            	mTournaments.add(match.getTournament());
            }
            
            String overview = "";
            if(mTournaments.size() == 1){
    	    	if(mMatches.size() > 1){
    	    		overview = "You are following " + mMatches.size() + " matches in " + mMatches.get(0).getTournament();
    	    	}else{
    	    		overview = "You are following " + mMatches.size() + " match in " + mMatches.get(0).getTournament();
    	    	}
            }else{
            	overview = "You are following " + mMatches.size() + " matches in " + mTournaments.size() + " leagues";
            }
            mLiveCardView.setTextViewText(R.id.overview_textview,
          		  overview);
            
            
            // Set up the live card's action with a pending intent
            // to show a menu when tapped
            setupPendingIntent();
            
            // Publish the live card
            mLiveCard.publish(PublishMode.SILENT);
            //set view of live card
            mLiveCard.setViews(mLiveCardView);


            mHandler.post(mUpdateScoreCardsRunnable);
        }else{
        	mEventMode = false;
        	Debugger.log("livecard is NOT null");
        	//get match which the liveticker will display information about
        	Bundle bundleMatch = intent.getBundleExtra("match");
        	Bundle bundleMatches = intent.getBundleExtra("matches");
        	if(bundleMatch != null){
	        	mMatch = (Match) bundleMatch.getSerializable("match");
        		if(mMatch.getFollow()){
	        		mMatches.add(mMatch);
	        	}else{
	        		mMatches.remove(mMatch);
	        	}
        	}
        	if(bundleMatches != null){
        		ArrayList<Match> matches = (ArrayList<Match>) bundleMatches.getSerializable("matches");
        		for(Match match : matches){
        			if(match.getFollow()){
        				mMatches.add(match);
        			}else{
        				mMatches.remove(match);
        			}
        		}
        	}
        	writeFollowMatchesToInternalSotrage();
        	//Debugger.log("match " + mMatch.getHomeTeamName() + " " + mMatch.getAwayTeamName());
        	
        	
        	mTournaments = new HashSet<String>();
            for(Match match : mMatches){
            	mTournaments.add(match.getTournament());
            }
            String overview = "";
            if(mTournaments.size() == 1){
    	    	if(mMatches.size() > 1){
    	    		overview = "You are following " + mMatches.size() + " matches in " + mMatches.get(0).getTournament();
    	    	}else{
    	    		overview = "You are following " + mMatches.size() + " match in " + mMatches.get(0).getTournament();
    	    	}
            }else{
            	overview = "You are following " + mMatches.size() + " matches in " + mTournaments.size() + " leagues";
            }
            mLiveCardView.setTextViewText(R.id.overview_textview,
          		  overview);
            
        	setupPendingIntent();
        	
             
            mLiveCard.setViews(mLiveCardView); 
        }
        return START_STICKY;
    }
    
    private void createFakeEvents(){
        //create fake events
        mEvents = new ArrayList<Event>();
        mEventsForIntent = new ArrayList<Event>();
        Event eventStart = new Event(EventType.START, "", "", "1st  00:00", "0", "0", 
				  "");
        Event event0 = new Event(EventType.REDCARD, "Nicklas Bendtner", "Denmark", "1st  05:02", "0", "0", 
				  "Stupid red card to Bendtner");
        Event event1 = new Event(EventType.YELLOWCARD, "Kloose", "Germany", "1st  25:14", "0", "0", 
        		 "Klose is furious with the referee");
        Event event2 = new Event(EventType.HALFTIME, "", "", "2nd  46:07", "0", "0", "");
        Event event3 = new Event(EventType.SECONDHALF, "", "", "2nd  46:07", "0", "0", "");
        Event event4 = new Event(EventType.GOALHOME, "M. Krohn-Dehli", "Denmark", "2nd  88:01", "1", "0", 
				  "Krohn-Dehli scores in the final minutes");
        Event event5 = new Event(EventType.FINISH, "", "", "2nd  91:02", "1", "0", "");
        mEvents.add(eventStart);
        mEvents.add(event0);
        mEvents.add(event1);
        mEvents.add(event2);
        mEvents.add(event3);
        mEvents.add(event4);
        mEvents.add(event5);
    }

//    private void setupPendingIntent(){
//    	// Set up the live card's action with a pending intent
//        // to show a menu when tapped
//        Intent menuIntent = new Intent(this, MenuActivity.class);
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("events", mEventsForIntent);
//        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        menuIntent.putExtra("events", bundle);
//        mLiveCard.setAction(PendingIntent.getActivity(
//            this, 0, menuIntent, PendingIntent.FLAG_UPDATE_CURRENT));
//    }
//    
    public void setupPendingIntent(){
    	// Set up the live card's action with a pending intent
        // to show a menu when tapped
        Intent menuIntent = new Intent(this, MenuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("matches", mMatches);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        menuIntent.putExtra("matches", bundle);
        menuIntent.putExtra("eventmode", mEventMode);
        mLiveCard.setAction(PendingIntent.getActivity(
            this, 0, menuIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
    
    public void setupPendingIntent(ArrayList<Match> matches){
    	// Set up the live card's action with a pending intent
        // to show a menu when tapped
        Intent menuIntent = new Intent(this, MenuActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("matches", matches);
        menuIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        menuIntent.putExtra("matches", bundle);
        menuIntent.putExtra("eventmode", mEventMode);
        mLiveCard.setAction(PendingIntent.getActivity(
            this, 0, menuIntent, PendingIntent.FLAG_UPDATE_CURRENT));
    }
    
    public void updateLiveCardWithEvent(Event event, Match match){
//IMPLEMENT THIS INSTEAD OF EVENTACTIVITY
    	mEventMode = true;
    	if(event.getType() == EventType.GOALAWAY  || event.getType() == EventType.GOALHOME ){
		  	  mEventCardView.setTextViewText(R.id.player_textview, event.getPlayer());
		  	  mEventCardView.setTextViewText(R.id.team_textview, event.getTeam());
		  	  mEventCardView.setTextViewText(R.id.time_textview, "");
		  	  mEventCardView.setImageViewBitmap(R.id.event_image, event.getBitmap(mContext));
		  	  
		  	  mEventCardView.setTextViewText(R.id.timestamp, "");
		  	  
		  	  mEventCardView.setTextViewText(R.id.awayteam_textview, match.getAwayTeamName());
		  	  mEventCardView.setTextViewText(R.id.hometeam_textview, match.getHomeTeamName());
		  	  mEventCardView.setTextViewText(R.id.awayscore_textview, ""+event.getAwayScore());
		  	  mEventCardView.setTextViewText(R.id.homescore_textview, ""+event.getHomeScore());
		  	  
		  	  mLiveCard.setViews(mEventCardView);
   		}
    	else if(event.getType() == EventType.START || event.getType() == EventType.HALFTIME
				|| event.getType() == EventType.SECONDHALF || event.getType() == EventType.FINISH){
    		mEventCardView2.setTextViewText(R.id.status, match.getStatus());
    		mEventCardView2.setImageViewBitmap(R.id.event_image, event.getBitmap(mContext));
    		mEventCardView2.setTextViewText(R.id.timestamp, "");
      	  
    		mEventCardView2.setTextViewText(R.id.awayteam_textview, match.getAwayTeamName());
    		mEventCardView2.setTextViewText(R.id.hometeam_textview, match.getHomeTeamName());
    		mEventCardView2.setTextViewText(R.id.awayscore_textview, ""+event.getAwayScore());
    		mEventCardView2.setTextViewText(R.id.homescore_textview, ""+event.getHomeScore());
  
    		mLiveCard.setViews(mEventCardView2);
    	}
    }
    
    public void removeEventFromLiveCard(){
    	//IMPLEMENT THIS INSTEAD OF EVENTACTIVITY
    	    mEventMode = false;
    	    String overview = "";
    	    if(mTournaments.size() == 1){
    	    	if(mMatches.size() > 1){
    	    		overview = "You are following " + mMatches.size() + " matches in " + mMatches.get(0).getTournament();
    	    	}else{
    	    		overview = "You are following " + mMatches.size() + " match in " + mMatches.get(0).getTournament();
    	    	}
            }else{
            	overview = "You are following " + mMatches.size() + " matches in " + mTournaments.size() + " leagues";
            }
            mLiveCardView.setTextViewText(R.id.overview_textview,
          		  overview);
    	    mLiveCard.setViews(mLiveCardView);	
    }
    
    public LiveCard getLiveCard(){
    	return mLiveCard;
    }
    public RemoteViews getRemoteViews(){
    	return mLiveCardView;
    }
    public ArrayList<Match> getMatches(){
    	return mMatches;
    }
    public Match getMatch(){
    	return mMatch;
    }
	
    private void writeFollowMatchesToInternalSotrage(){
    	//when liveticker closes, erase any followed matches form local memory	
    	try{
			FileOutputStream fos = openFileOutput(Utils.FILENAME, Context.MODE_PRIVATE);
			ObjectOutputStream objOut = new ObjectOutputStream(fos);
			//ArrayList<Match> followed = new ArrayList<Match>();
			objOut.writeObject(mMatches);
			objOut.close();
			fos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
    }

    @Override
    public void onDestroy() {
//     //when liveticker closes, erase any followed matches form local memory	
    	try{
			FileOutputStream fos = openFileOutput(Utils.FILENAME, Context.MODE_PRIVATE);
			ObjectOutputStream objOut = new ObjectOutputStream(fos);
			ArrayList<Match> followed = new ArrayList<Match>();
			objOut.writeObject(followed);
			objOut.close();
			fos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
        if (mLiveCard != null && mLiveCard.isPublished()) {
          //Stop the handler from queuing more Runnable jobs
//            mUpdateLiveCardRunnable.setStop(true);
//            mUpdateScoreCard.setStopped(true);
            mUpdateScoreCardsRunnable.setStop(true);
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        super.onDestroy();
    }

    private class UpdateScoreCardsRunnable implements Runnable{
    	private boolean mIsStopped = false;
    	public void run(){
    		if(!isStopped()){
    			new DownloadJSONFromEBTask2(mContext).execute(Utils.EB_URL);
    			mHandler.postDelayed(mUpdateScoreCardsRunnable, DELAY_MILLIS);
    		}
    	}
    	public boolean isStopped(){
    		return mIsStopped;
    	}
    	public void setStop(boolean stop){
    		mIsStopped = stop;
    	}
    }
    /**
     * Runnable that updates live card contents
     */
//    private class UpdateLiveCardRunnable implements Runnable{
//
//        private boolean mIsStopped = false;
//        /*
//         * Updates the card with a fake score every 30 seconds as a demonstration.
//         * You also probably want to display something useful in your live card.
//         *
//         * If you are executing a long running task to get data to update a
//         * live card(e.g, making a web call), do this in another thread or
//         * AsyncTask.
//         */
//        public void run(){
//        	long diff = System.currentTimeMillis()/1000 - mSeconds;
//        	Debugger.log("diff " +diff);
//        	if(diff > 7){ //after 10 seconds
//        		mSeconds = System.currentTimeMillis()/1000;
//        		mEventIndex++;
//        		if(mEventIndex == mEvents.size()){
//            		setStop(true);
//            	}
//        		if(!isStopped()){
//               	 //udioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//                   //    audio.playSoundEffect(Sounds.SUCCESS);
//                       
//               	  //Use fake event
//                     Event event =  mEvents.get(mEventIndex); 
//                     if(event.getType() == EventType.REDCARD || event.getType() == EventType.YELLOWCARD){
//                    	 AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//                         audio.playSoundEffect(Sounds.ERROR); 
//                    	 //populateViewCardEvent(event);
//                   	  	  mEventsForIntent.add(event);
//                   	  	  setupPendingIntent();
//                   	  	 // SoundPlayer.playSound(mContext, SoundPlayer.S2);
//                   	  	  startEventScrollActivity();
//                     } else if(event.getType() == EventType.GOALHOME || event.getType() == EventType.GOALAWAY){
//                   	  	  //populateViewGoalEvent(event);
//                    	 AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//                         audio.playSoundEffect(Sounds.SUCCESS);
//                    	 if(event.getType() == EventType.GOALHOME){
//                    		 mHomeScore++;
//                    	 }else if(event.getType() == EventType.GOALAWAY){
//                    		 mAwayScore++;
//                    	 }
//                    	  updateLiveCard();
//                   	      mEventsForIntent.add(event);
//                   	      setupPendingIntent();
//                   	    //  SoundPlayer.playSound(mContext, SoundPlayer.S1);
//                   	      startEventScrollActivity();
//                     }else if(event.getType() == EventType.START){
//                    	 AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//                         audio.playSoundEffect(Sounds.SUCCESS);
//                      	  mStatus = "START";
//                      	  updateLiveCard();
//                       	  //populateViewStatusEvent(event, mStatus);
//                       	  mEventsForIntent.add(event);
//                       	  setupPendingIntent();
//                       	  startEventScrollActivity();
//                      }else if(event.getType() == EventType.HALFTIME){
//                    	  AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//                          audio.playSoundEffect(Sounds.SUCCESS);
//                    	  mStatus = "HALFTIME";
//                    	  updateLiveCard();
//                    	  //populateViewStatusEvent(event, mStatus);
//                    	  mEventsForIntent.add(event);
//                    	  setupPendingIntent();
//                    	  startEventScrollActivity();
//                      }else if(event.getType() == EventType.SECONDHALF){
//                    	  AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//                          audio.playSoundEffect(Sounds.SUCCESS);
//                      	  mStatus = "2ND HALF";
//                      	  updateLiveCard();
//                       	  //populateViewStatusEvent(event, mStatus);
//                          mEventsForIntent.add(event);
//                          setupPendingIntent();
//                          startEventScrollActivity();
//                      } else if(event.getType() == EventType.FINISH){
//                    	  AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//                          audio.playSoundEffect(Sounds.SUCCESS);
//                    	  mStatus = "FINISHED";
//                    	  updateLiveCard();
//                    	  //populateViewStatusEvent(event, mStatus);
//                    	  mEventsForIntent.add(event);
//                    	  setupPendingIntent();
//                    	  startEventScrollActivity();
//                     }
//                   	  
//                   // Queue another score update in 30 seconds.
//                      mHandler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILLIS);
//                 }
//                   
//        	}else{
//        		// Queue another score update in 30 seconds.
//        		updateLiveCard();
//                mHandler.postDelayed(mUpdateLiveCardRunnable, DELAY_MILLIS);
//        	}
//        }
        
//        private void updateLiveCard(){
//        	mLiveCardView = new RemoteViews(getPackageName(),
//            		R.layout.score_card_layout);
//            mLiveCardView.setTextViewText(R.id.homeTeamNameTextView,
//        		  getString(R.string.home_team));
//            mLiveCardView.setTextViewText(R.id.awayTeamNameTextView,
//                   getString(R.string.away_team));
//            mLiveCardView.setTextViewText(R.id.homeScoreTextView,
//          		  ""+mHomeScore);
//              mLiveCardView.setTextViewText(R.id.awayScoreTextView,
//                     ""+mAwayScore);
//              if(mStatus.equals("START")){
//            	  mStatus = "1ST HALF";
//              }
//            mLiveCardView.setTextViewText(R.id.halfTextView, mStatus);
//            mLiveCard.setViews(mLiveCardView);
//        }
//        
//        private void startEventScrollActivity(){
//        	Intent intent = new Intent(mContext, EventScrollActivity.class);
//            Bundle bundle = new Bundle();
//            bundle.putSerializable("events", mEventsForIntent);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
//                    Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
//            intent.putExtra("events", bundle);
//            startActivity(intent);
//        }
//        
//        private void populateViewCardEvent(Event event){
//        	  mEventCardView.setTextViewText(R.id.player_textview, event.getPlayer());
//        	  mEventCardView.setTextViewText(R.id.team_textview, event.getTeam());
//        	  mEventCardView.setTextViewText(R.id.time_textview, event.getTime());
//        	  mEventCardView.setImageViewBitmap(R.id.event_image, event.getBitmap(mContext));
//        	  
//        	  mEventCardView.setTextViewText(R.id.timestamp, "just now");
//        	  
//        	  mEventCardView.setTextViewText(R.id.awayteam_textview, "");
//        	  mEventCardView.setTextViewText(R.id.hometeam_textview, "");
//        	  mEventCardView.setTextViewText(R.id.awayscore_textview, "");
//        	  mEventCardView.setTextViewText(R.id.homescore_textview, "");
//        	  
//        	  mLiveCard.setViews(mEventCardView); 
//        }
//        private void populateViewGoalEvent(Event event){
//        	if(event.getType() == EventType.GOALHOME){
//       		 mHomeScore++; 
//       	  }else if(event.getType() == EventType.GOALAWAY){
//       		 mAwayScore++;
//       	  }
//       	  mEventCardView.setTextViewText(R.id.player_textview, event.getPlayer());
//       	  mEventCardView.setTextViewText(R.id.team_textview, event.getTeam());
//       	  mEventCardView.setTextViewText(R.id.time_textview, event.getTime());
//       	  mEventCardView.setImageViewBitmap(R.id.event_image, event.getBitmap(mContext));
//       	  
//       	  mEventCardView.setTextViewText(R.id.timestamp, "just now");
//       	  
//       	  mEventCardView.setTextViewText(R.id.awayteam_textview, getString(R.string.away_team));
//       	  mEventCardView.setTextViewText(R.id.hometeam_textview, getString(R.string.home_team));
//       	  mEventCardView.setTextViewText(R.id.awayscore_textview, ""+mAwayScore);
//       	  mEventCardView.setTextViewText(R.id.homescore_textview, ""+mHomeScore);
//       	  
//       	  mLiveCard.setViews(mEventCardView);
//      }
//      private void populateViewStatusEvent(Event event, String status){
//    	  mEventCardView2.setTextViewText(R.id.status, status);
//    	  mEventCardView2.setImageViewBitmap(R.id.event_image, event.getBitmap(mContext));
//    	  mEventCardView2.setTextViewText(R.id.timestamp, "just now");
//    	  
//    	  mEventCardView2.setTextViewText(R.id.awayteam_textview, getString(R.string.away_team));
//          mEventCardView2.setTextViewText(R.id.hometeam_textview, getString(R.string.home_team));
//          mEventCardView2.setTextViewText(R.id.awayscore_textview, ""+mAwayScore);
//          mEventCardView2.setTextViewText(R.id.homescore_textview, ""+mHomeScore);
//
//    	  mLiveCard.setViews(mEventCardView2);
//      }
//
//        public boolean isStopped() {
//            return mIsStopped;
//        }
//
//        public void setStop(boolean isStopped) {
//            this.mIsStopped = isStopped;
//        }
//    }
    
//    public int getNoOfEvents(){
//		return mNoEvents;
//	}
//	public void setNoOfEvents(int noEvents){
//		mNoEvents = noEvents;
//	}

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
