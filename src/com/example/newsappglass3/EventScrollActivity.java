package com.example.newsappglass3;

import java.util.ArrayList;

import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EventScrollActivity extends Activity {

	private ArrayList<Event> mEvents;
	private Match mMatch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//extract events from intent
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("events");
		mEvents = (ArrayList<Event>)bundle.get("events");
		mMatch = (Match)bundle.get("match");
		
	}
	
	@Override 
	protected void onStart(){
		super.onStart();
		
//		ArrayList<Event> reverseEvents = new ArrayList<Event>();
//		//reverse the order of events in the list
//		//int j = 0;
//		for(int i = mEvents.size()-1, j = 0; i >= 0 && j < mEvents.size(); i--, j++){
//			reverseEvents.add(mEvents.get(i));
//			Debugger.log("event " + mEvents.get(i));
//		}
		
		CardScrollView cardScrollView = new CardScrollView(this);
		ArrayList<RelativeLayout> views = new ArrayList<RelativeLayout>();
		
		for(Event e : mEvents){
			if(e.getType() == EventType.REDCARD || e.getType() == EventType.YELLOWCARD){
				RelativeLayout rl = (RelativeLayout)getLayoutInflater().inflate(R.layout.event_card_layout, null);
				TextView playerTv = (TextView)rl.findViewById(R.id.player_textview);
				playerTv.setText(e.getPlayer());
				TextView teamTv = (TextView)rl.findViewById(R.id.team_textview);
				teamTv.setText(e.getTeam());
				TextView timeTv = (TextView)rl.findViewById(R.id.time_textview);
				timeTv.setText("");
				ImageView img = (ImageView)rl.findViewById(R.id.event_image);
				img.setImageBitmap(e.getBitmap(this));
				TextView timestampTv = (TextView)rl.findViewById(R.id.timestamp);
				timestampTv.setText(e.getTime()+". min");
				views.add(rl);
			}else if(e.getType() == EventType.GOALHOME || e.getType() == EventType.GOALAWAY){
				RelativeLayout rl = (RelativeLayout)getLayoutInflater().inflate(R.layout.event_card_layout, null);
				TextView playerTv = (TextView)rl.findViewById(R.id.player_textview);
				playerTv.setText(e.getPlayer());
				TextView teamTv = (TextView)rl.findViewById(R.id.team_textview);
				teamTv.setText(e.getTeam());
				TextView timeTv = (TextView)rl.findViewById(R.id.time_textview);
				timeTv.setText("");
				ImageView img = (ImageView)rl.findViewById(R.id.event_image);
				img.setImageBitmap(e.getBitmap(this));
				TextView timestampTv = (TextView)rl.findViewById(R.id.timestamp);
				timestampTv.setText(e.getTime()+". min");
				
				TextView awayteamTv = (TextView)rl.findViewById(R.id.awayteam_textview);
				awayteamTv.setText(mMatch.getAwayTeamName());
				TextView hometeamTv = (TextView)rl.findViewById(R.id.hometeam_textview);
				hometeamTv.setText(mMatch.getHomeTeamName());
				TextView awayscoreTv = (TextView)rl.findViewById(R.id.awayscore_textview);
				awayscoreTv.setText(e.getAwayScore());
				TextView homescoreTv = (TextView)rl.findViewById(R.id.homescore_textview);
				homescoreTv.setText(e.getHomeScore());
				
				views.add(rl);
			}else if(e.getType() == EventType.START || e.getType() == EventType.HALFTIME
					|| e.getType() == EventType.SECONDHALF || e.getType() == EventType.FINISH){
				RelativeLayout rl = (RelativeLayout)getLayoutInflater().inflate(R.layout.event_card_layout2, null);
				TextView statusTv = (TextView)rl.findViewById(R.id.status);
				if(e.getType() == EventType.START){
					statusTv.setText("START");
				}else if(e.getType() == EventType.HALFTIME){
					statusTv.setText("HALFTIME");
				}else if(e.getType() == EventType.SECONDHALF){
					statusTv.setText("2ND HALF");
				}else if(e.getType() == EventType.FINISH){
					statusTv.setText("FINISHED");
				}
			
				ImageView img = (ImageView)rl.findViewById(R.id.event_image);
				img.setImageBitmap(e.getBitmap(this));
				TextView timestampTv = (TextView)rl.findViewById(R.id.timestamp);
				timestampTv.setText("");
				
				TextView awayteamTv = (TextView)rl.findViewById(R.id.awayteam_textview);
				awayteamTv.setText(mMatch.getAwayTeamName());
				TextView hometeamTv = (TextView)rl.findViewById(R.id.hometeam_textview);
				hometeamTv.setText(mMatch.getHomeTeamName());
				TextView awayscoreTv = (TextView)rl.findViewById(R.id.awayscore_textview);
				awayscoreTv.setText(e.getAwayScore());
				TextView homescoreTv = (TextView)rl.findViewById(R.id.homescore_textview);
				homescoreTv.setText(e.getHomeScore());
				
				views.add(rl);
			}
			
		}
		
		RelativeLayoutCardScrollAdapter eventCardScrollAdapter = new RelativeLayoutCardScrollAdapter(views);
		cardScrollView.setAdapter(eventCardScrollAdapter);
		cardScrollView.activate();
		setContentView(cardScrollView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event_scroll, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
