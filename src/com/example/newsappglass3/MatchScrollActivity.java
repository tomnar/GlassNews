package com.example.newsappglass3;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.glass.widget.CardScrollView;

public class MatchScrollActivity extends Activity {

	private int mScrollIndex = 0;
	private ArrayList<Match> mMatches;	
	private ArrayList<Match> mFollowed;
	
	private final OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mScrollIndex = position;
			openOptionsMenu();
		}
	};
	
	private final OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			mScrollIndex = position;
		}
		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//make sure screen never sleeps, adn therefore activity doesn't stop. Otherwise it will destroy itself after a while....
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		setContentView(R.layout.activity_match_scroller);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("matches");
		mMatches = 
				(ArrayList<Match>) bundle.getSerializable("matches");
		mFollowed = new ArrayList<Match>();
		
		ArrayList<RelativeLayout> cards = new ArrayList<RelativeLayout>();
		for(Match match: mMatches){
			RelativeLayout rl = (RelativeLayout)getLayoutInflater().inflate(R.layout.score_card_layout, null);
			TextView tournamentName = (TextView)rl.findViewById(R.id.tournamentNameTextView);
			TextView homeTeamName = (TextView)rl.findViewById(R.id.homeTeamNameTextView);
			TextView awayTeamName = (TextView)rl.findViewById(R.id.awayTeamNameTextView);
			TextView homeScore = (TextView)rl.findViewById(R.id.homeScoreTextView);
			TextView awayScore = (TextView)rl.findViewById(R.id.awayScoreTextView);
			TextView half = (TextView)rl.findViewById(R.id.halfTextView);
			tournamentName.setText(match.getTournament());
			homeTeamName.setText(match.getHomeTeamName());
			awayTeamName.setText(match.getAwayTeamName());
			homeScore.setText(match.getHomeScore());
			awayScore.setText(match.getAwayScore());
			half.setText(match.getStatus());
			cards.add(rl);
		}
		
		CardScrollView cardScrollView = new CardScrollView(this);
		cardScrollView.setOnItemClickListener(onItemClickListener);
		cardScrollView.setOnItemSelectedListener(onItemSelectedListener);
		RelativeLayoutCardScrollAdapter cardScrollAdapter = new RelativeLayoutCardScrollAdapter(cards);
		cardScrollView.setAdapter(cardScrollAdapter);
		cardScrollView.activate();
		
		setContentView(cardScrollView);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		//read followed matches from internal storage
		try {
			FileInputStream fis = openFileInput(Utils.FILENAME);
			ObjectInputStream objIn = new ObjectInputStream(fis);
			mFollowed = (ArrayList<Match>)objIn.readObject();
			Debugger.log("folled matches were loaded " + mFollowed.size());
			objIn.close();
			fis.close();
			
			//run through remote matches and set follow status based on follow status of matches from internal storage
			for(Match match : mMatches){
				for(Match followedMatch: mFollowed){
					if(match.equals(followedMatch)){
						match.setFollow(followedMatch.getFollow());
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	};

	@Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL) {
          //  getMenuInflater().inflate(R.menu.match_scroll, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.match_scroll, menu);
		return true;
	}
	@Override
	public boolean onPreparePanel(int featureId, View view, Menu menu) {
		if(featureId == Window.FEATURE_OPTIONS_PANEL){
			menu.removeItem(R.id.unfollow_match);
			menu.removeItem(R.id.follow_match);
			menu.removeItem(R.id.match_events);
			getMenuInflater().inflate(R.menu.match_scroll, menu);
			if(mMatches.size() > 0){
				Match match = mMatches.get(mScrollIndex);
				if(match.getFollow() == false){
					menu.removeItem(R.id.unfollow_match);
				}else{
					menu.removeItem(R.id.follow_match);
				}
				ArrayList<Event> events = match.getEvents();
				if(events.size() == 0){
					menu.removeItem(R.id.match_events);
				}
			}else{
				menu.removeItem(R.id.unfollow_match);
				menu.removeItem(R.id.follow_match);
				menu.removeItem(R.id.match_events);
			}
		}
		return super.onPreparePanel(featureId, view, menu);
	};

	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(featureId == Window.FEATURE_OPTIONS_PANEL){
			switch(item.getItemId()){
			case R.id.follow_match:
				Debugger.log("tapped liveticker menu item");
				Match match = mMatches.get(mScrollIndex); 
				match.setFollow(true);
				mFollowed.add(match);
				Intent intent = new Intent(this, ScoreCard.class);
				Bundle bundle = new Bundle();
				bundle.putSerializable("match", match);
				intent.putExtra("match", bundle);
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				startService(intent);
				break;
			case R.id.unfollow_match:
				Debugger.log("tapped liveticker menu item");
				Match match2 = mMatches.get(mScrollIndex); 
				match2.setFollow(false);
				mFollowed.remove(match2);
				Intent intent2 = new Intent(this, ScoreCard.class);
				Bundle bundle2 = new Bundle();
				bundle2.putSerializable("match", match2);
				intent2.putExtra("match", bundle2);
//				intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				intent2.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				startService(intent2);
				break;
			case R.id.match_events:
				Match match3 = mMatches.get(mScrollIndex);
				ArrayList<Event> events = match3.getEvents();
				Intent intent3 = new Intent(this, EventScrollActivity.class);
				Bundle bundle3 = new Bundle();
				bundle3.putSerializable("events", events);
				bundle3.putSerializable("match", match3);
				intent3.putExtra("events", bundle3);
				startActivity(intent3);
				break;
			default:
				return true;
			}
		}
		
		return super.onMenuItemSelected(featureId, item);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            // user tapped touchpad, do something
        	super.openOptionsMenu();
            return true;
        }
    	return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		
		//write followed matches to harddisc
		try{
			FileOutputStream fos = openFileOutput(Utils.FILENAME, Context.MODE_PRIVATE);
			ObjectOutputStream objOut = new ObjectOutputStream(fos);
			objOut.writeObject(mFollowed);
			objOut.close();
			fos.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	@Override
	protected void onDestroy(){
		super.onDestroy();
		Debugger.log("MatchScroller destroyed");
	}
	
}
