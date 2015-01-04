package com.example.newsappglass3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.timeline.LiveCard;


class DownloadJSONFromEBTask2 extends AsyncTask<String, String, String> {

	private Context mContext;
	
	public DownloadJSONFromEBTask2(Context context){
		mContext = context;
	}
	
	
	@Override
    protected String doInBackground(String... urls) { 	
            return getJSON(urls[0]); 
    }

    @Override
    protected void onPostExecute(String input) {  
    	Debugger.log("updating score card... ");
    	try {
			JSONArray matches = new JSONArray(input);
			HashMap<String, ArrayList<Match> > tournamentMap = new HashMap<String, ArrayList<Match> >();
			for(int i = 0; i < matches.length(); i++){
				JSONObject match = matches.getJSONObject(i);
				String tournamentName = match.getString("uniquetournamentname");
				String homeTeamName = match.getString("hometeam");
				String awayTeamName = match.getString("awayteam");
				String homeScoreNow = match.getString("homescore");
				String awayScoreNow = match.getString("awayscore");
				String status = match.getString("statustext");
				JSONArray events = match.getJSONArray("matchevents");
				Match m = new Match(homeTeamName, awayTeamName, homeScoreNow, awayScoreNow, status, tournamentName);
				for(int j = 0; j < events.length(); j++){
					JSONObject event = events.getJSONObject(j);
					String type = event.getString("type");
					String playerName = event.getString("playername");
					String playerShirtNo = event.getString("playershirtnumber"); //not used, try to use it...
					String score = event.getString("score");
					String[] scores = score.split(" - ");
					String homeScore = scores[0];
					String awayScore = scores[1];
					String time = event.getString("time"); //time inn minutes
					String homeOrAway = event.getString("homeoraway");
					String teamName = "";
					if(type.equals("Goal")){
						Event e = null;
						if(homeOrAway.equals("1")){
							teamName = homeTeamName;
							e = new Event(EventType.GOALHOME, playerName, teamName, time, homeScore, awayScore, "");
						}else if(homeOrAway.equals("2")){
							teamName = awayTeamName;
							e = new Event(EventType.GOALAWAY, playerName, teamName, time, homeScore, awayScore, "");
						}
						m.addEvent(e);
					}
				}
				
				if(tournamentMap.containsKey(tournamentName)){
					ArrayList<Match> ms = tournamentMap.get(tournamentName);
					ms.add(m);
				}else{
					ArrayList<Match> ms = new ArrayList<Match>();
					ms.add(m);
					tournamentMap.put(tournamentName, ms);
				}
			}

			ScoreCard scoreCard = (ScoreCard)mContext;
		//	LiveCard liveCard = scoreCard.getLiveCard();
			//RemoteViews liveCardView = scoreCard.getRemoteViews();
			ArrayList<Match> localMatches = scoreCard.getMatches();
			for(Match localMatch : localMatches){
				ArrayList<Match> remoteMatches = tournamentMap.get(localMatch.getTournament());
				for(Match remoteMatch : remoteMatches){
					if(localMatch.equals(remoteMatch)){
						
						//check status of local match and update to status of remote match
						String localMatchStatus = localMatch.getStatus();
						String remoteMatchStatus = remoteMatch.getStatus();
						if( !localMatchStatus.equals(remoteMatchStatus) ){
							Event e = null;
							localMatch.setStatus(remoteMatchStatus);
							if(remoteMatchStatus.equals("1. halvleg")){
								e = new Event(EventType.START, 
										"", "", "", remoteMatch.getHomeScore(), 
										remoteMatch.getAwayScore(), "");
							}else if(remoteMatchStatus.equals("2. halvleg")){
								e = new Event(EventType.SECONDHALF, 
										"", "", "", remoteMatch.getHomeScore(), 
										remoteMatch.getAwayScore(), "");
							}else if(remoteMatchStatus.equals("Pause")){
								e = new Event(EventType.HALFTIME, 
										"", "", "", remoteMatch.getHomeScore(), 
										remoteMatch.getAwayScore(), "");
							}else if(remoteMatchStatus.equals("Slut")){
								e = new Event(EventType.FINISH, 
										"", "", "", remoteMatch.getHomeScore(), 
										remoteMatch.getAwayScore(), "");
							}
							AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
							audio.playSoundEffect(Sounds.SUCCESS);
							
//							Intent intent = new Intent(mContext, EventActivity.class);
//							Bundle bundle = new Bundle();
//							bundle.putSerializable("event", e);
//							bundle.putSerializable("match", remoteMatch);
//							intent.putExtra("event", bundle);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //needed 
//							mContext.startActivity(intent);
							scoreCard.updateLiveCardWithEvent(e, remoteMatch);
							scoreCard.setupPendingIntent(localMatches);
						}
						
						ArrayList<Event> updatedEvents = remoteMatch.getEvents();
						int a = updatedEvents.size();
						int b = localMatch.getEvents().size();
						//if a > b, then events occured before the live ticker was started or new events have occured
						if(a > b){
							for(int j = b; j < a; j++){
								Event event = updatedEvents.get(j);
								localMatch.addEvent(event);
							
								//play sound for every new event
//								AudioManager audio = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
//								audio.playSoundEffect(Sounds.SUCCESS);
								SoundPlayer.playSound(mContext, SoundPlayer.S1);
								
								scoreCard.updateLiveCardWithEvent(event, remoteMatch);
								scoreCard.setupPendingIntent(localMatches);
							}
						}
					}
				}
			}
				
		} catch (JSONException e) {
			e.printStackTrace();
		}

    }
    
	private String getJSON(String urlString) {	
		 HttpURLConnection conn = null; 
		 String jsonString = "";
		try{
			
			URL url = new URL(urlString);
		    conn = (HttpURLConnection) url.openConnection();
	    	conn.setReadTimeout(10000 /* milliseconds */);
		    conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			
			conn.setRequestProperty("Accept", "application/json");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			
		    InputStream in = conn.getInputStream();
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
		    String s = reader.readLine();
	        while(s != null){
	        	//Log.d(ContextualMenuActivity.TAG, "inside"); 
		    	jsonString += s;
		    	s = reader.readLine();
		    }
		    reader.close();
		   conn.disconnect();
		}
	    catch(IOException e){
	       
	    }
		 //Log.d(StartService.TAG, "value " + jsonString);
		return jsonString;
	}
}

