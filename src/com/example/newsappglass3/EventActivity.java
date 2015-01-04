package com.example.newsappglass3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EventActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("event");
    	Event e = (Event) bundle.getSerializable("event");
    	Match match = (Match) bundle.getSerializable("match");
    	
    	if(e.getType() == EventType.GOALAWAY  || e.getType() == EventType.GOALHOME ){
	    	RelativeLayout rl = (RelativeLayout)getLayoutInflater().inflate(R.layout.event_card_layout, null);
	    	TextView playerTv = (TextView)rl.findViewById(R.id.player_textview);
			playerTv.setText(e.getPlayer());
			TextView teamTv = (TextView)rl.findViewById(R.id.team_textview);
			teamTv.setText(e.getTeam());
			TextView timeTv = (TextView)rl.findViewById(R.id.time_textview);
			timeTv.setText(e.getTime());
			ImageView img = (ImageView)rl.findViewById(R.id.event_image);
			img.setImageBitmap(e.getBitmap(this));
			TextView timestampTv = (TextView)rl.findViewById(R.id.timestamp);
			timestampTv.setText(e.getTime() + ". min");
			
			TextView awayteamTv = (TextView)rl.findViewById(R.id.awayteam_textview);
			awayteamTv.setText(match.getAwayTeamName());
			TextView hometeamTv = (TextView)rl.findViewById(R.id.hometeam_textview);
			hometeamTv.setText(match.getHomeTeamName());
			TextView awayscoreTv = (TextView)rl.findViewById(R.id.awayscore_textview);
			awayscoreTv.setText(e.getAwayScore());
			TextView homescoreTv = (TextView)rl.findViewById(R.id.homescore_textview);
			homescoreTv.setText(e.getHomeScore());
			setContentView(rl);
    	}else if(e.getType() == EventType.START || e.getType() == EventType.HALFTIME
				|| e.getType() == EventType.SECONDHALF || e.getType() == EventType.FINISH){
			RelativeLayout rl = (RelativeLayout)getLayoutInflater().inflate(R.layout.event_card_layout2, null);
			TextView statusTv = (TextView)rl.findViewById(R.id.status);
			if(e.getType() == EventType.START){
				statusTv.setText(match.getStatus());
			}else if(e.getType() == EventType.HALFTIME){
				statusTv.setText(match.getStatus());
			}else if(e.getType() == EventType.SECONDHALF){
				statusTv.setText(match.getStatus());
			}else if(e.getType() == EventType.FINISH){
				statusTv.setText(match.getStatus());
			}
		
			ImageView img = (ImageView)rl.findViewById(R.id.event_image);
			img.setImageBitmap(e.getBitmap(this));
			TextView timestampTv = (TextView)rl.findViewById(R.id.timestamp);
			timestampTv.setText("");
			
			TextView awayteamTv = (TextView)rl.findViewById(R.id.awayteam_textview);
			awayteamTv.setText(match.getAwayTeamName());
			TextView hometeamTv = (TextView)rl.findViewById(R.id.hometeam_textview);
			hometeamTv.setText(match.getHomeTeamName());
			TextView awayscoreTv = (TextView)rl.findViewById(R.id.awayscore_textview);
			awayscoreTv.setText(e.getAwayScore());
			TextView homescoreTv = (TextView)rl.findViewById(R.id.homescore_textview);
			homescoreTv.setText(e.getHomeScore());
			setContentView(rl);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.event, menu);
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
