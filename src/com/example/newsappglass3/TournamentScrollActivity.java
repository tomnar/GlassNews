package com.example.newsappglass3;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;

public class TournamentScrollActivity extends Activity {

	private Context mContext;
	private ArrayList<String> mTournaments;
	private int mScrollIndex = 0;
	private HashMap<String, ArrayList<Match> > mTournamentMap;
	
	private final OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mScrollIndex = position;
			Debugger.log("tournament: " + mTournaments.get(mScrollIndex));
			ArrayList<Match> matches = mTournamentMap.get(mTournaments.get(mScrollIndex));
			
			Intent intent = new Intent(mContext, MatchScrollActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("matches", matches);
			intent.putExtra("matches", bundle);
			startActivity(intent);
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
		
		mContext = this;
		setContentView(R.layout.activity_match_scroller);
		
		Intent intent = getIntent();
		boolean stop = intent.getBooleanExtra("stop", false);
	    if(stop)
	    {
	        finish();
	    }else{
			Bundle bundle = intent.getBundleExtra("tournament_matches");
			mTournamentMap = 
					(HashMap<String, ArrayList<Match> >) bundle.getSerializable("tournament_matches");
			
			ArrayList<CardWrapper> cards = new ArrayList<CardWrapper>();
			mTournaments = new ArrayList<String>();
			for(String key: mTournamentMap.keySet()){
				CardBuilder card = new CardBuilder(this, CardBuilder.Layout.CAPTION);
				card.setText(key);
				
				int noMatches = mTournamentMap.get(key).size();
				if(noMatches == 1){
					card.setFootnote(mTournamentMap.get(key).size() + " match");
				}else{
					card.setFootnote(mTournamentMap.get(key).size() + " matches");
				}
				if(key.equals("UEFA Champions League")){
					Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.champions_league);
					card.addImage(background);
				}
				if(key.equals("Premier League")){
					Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.premier_league);
					card.addImage(background);
				}
				CardWrapper cardWrapper = new CardWrapper(card, "tournament", key);
				cards.add(cardWrapper);
				mTournaments.add(key); //use by mScrollIndex for indexing
			}
			
			CardScrollView cardScrollView = new CardScrollView(this);
			cardScrollView.setOnItemClickListener(onItemClickListener);
			cardScrollView.setOnItemSelectedListener(onItemSelectedListener);
			MyCardScrollAdapter cardScrollAdapter = new MyCardScrollAdapter();
			cardScrollAdapter.setCards(cards);
			cardScrollView.setAdapter(cardScrollAdapter);
			cardScrollView.activate();
			
			setContentView(cardScrollView);
		}
	}
	
    @Override
	protected void onNewIntent(Intent intent)
	{
	    super.onNewIntent(intent);
	    Debugger.log("received stop intent");
	    boolean stop = intent.getExtras().getBoolean("stop");
	    if(stop)
	    {
	        finish();
	    }
	}

	@Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.match_scroll, menu);
		return true;
	}

	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if(featureId == Window.FEATURE_OPTIONS_PANEL){
			switch(item.getItemId()){
			}
		}
		
		return super.onMenuItemSelected(featureId, item);
	}

	
}
