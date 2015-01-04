package com.example.newsappglass3;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.gms.auth.firstparty.dataservice.m;


class DownloadMatchesFromEBTask extends AsyncTask<String, String, String> {

	
	private Context mContext;
	private ArrayList<CardWrapper> mCards;
	private CardScrollAdapter mCardScrollAdapter;
	private HashMap<String, ArrayList<Match> > mTournaments;
	private ArrayList<NewsItem> mNewsItems;
	
	public DownloadMatchesFromEBTask(Context context, ArrayList<CardWrapper> cards, 
			CardScrollAdapter cardScrollAdapter, HashMap<String, ArrayList<Match> > tournaments, ArrayList<NewsItem> newsItems ){
		mContext = context;
		mCards = cards;
		mCardScrollAdapter = cardScrollAdapter;
		mTournaments = tournaments;
		mNewsItems = newsItems;
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
			//HashMap<String, ArrayList<Match> > tournamentMap = new HashMap<String, ArrayList<Match> >();
			for(int i = 0; i < matches.length(); i++){
				JSONObject match = matches.getJSONObject(i);
				String tournamentName = match.getString("uniquetournamentname");
				if(!tournamentName.equals("Super Liga") && 
						!tournamentName.equals("UEFA Champions League") && 
						!tournamentName.equals("Premier League") &&
						!tournamentName.equals("Europa League") ){
					continue;
				}
				String homeTeamName = match.getString("hometeam");
				String awayTeamName = match.getString("awayteam");
				String homeScoreNow = match.getString("homescore");
				String awayScoreNow = match.getString("awayscore");
				String status = match.getString("statustext");
//				if(status.equals("Ikke startet")){
//					status = "Not started";
//				}
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
				
				if(mTournaments.containsKey(tournamentName)){
					ArrayList<Match> ms = mTournaments.get(tournamentName);
					ms.add(m);
				}else{
					ArrayList<Match> ms = new ArrayList<Match>();
					ms.add(m);
					mTournaments.put(tournamentName, ms);
				}
			}
			
			
			for(String key : mTournaments.keySet()){
				CardBuilder card = new CardBuilder(mContext, CardBuilder.Layout.CAPTION);
				card.setText(key);
				if(key.equals("UEFA Champions League")){
					Bitmap background = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.champions_league);
					card.addImage(background);
				}
				else if(key.equals("Premier League")){
					Bitmap background = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.premier_league);
					card.addImage(background);
				}
				else if(key.equals("Super Liga")){
					Bitmap background = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.premier_league);
					card.addImage(background);
				}
				else if(key.equals("Europa League")){
					Bitmap background = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.europa_league);
					card.addImage(background);
				}
				
				ArrayList<Match> follows = readFollowMatchesFromInternalStorage();
				NewsSliderActivity newsSlider = (NewsSliderActivity)mContext;
				newsSlider.setFollowMatches(follows);
				boolean following = followsMatch(key, follows);
				String msg = "";
				if(following){
					msg = "following ";
				}
			    String timeStamp = msg + mTournaments.get(key).size() + " matches today";
				card.setTimestamp(timeStamp);
				CardWrapper cardWrapper = new CardWrapper(card, "tournament", key);
				cardWrapper.setTimeliness(timeStamp);
				mCards.add(cardWrapper);
			}
			//update currentNo/totalNoOfNews
			int noOfNewsItems = mNewsItems.size();
			for(int i = 0; i < noOfNewsItems; i++){
				CardBuilder card = CardUtils.getCard(mCards.get(i));
				card.setFootnote((i+1) +"/"+ mCards.size() + " - " + mNewsItems.get(i).getSection());
			}
			for(int i = noOfNewsItems; i < mCards.size(); i++ ){
				CardBuilder card = CardUtils.getCard(mCards.get(i));
				if(card != null){
					card.setFootnote((i+1) +"/"+ mCards.size() + " - " + "sports");
				}
			}
			mCardScrollAdapter.notifyDataSetChanged();
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
	
	 private ArrayList<Match> readFollowMatchesFromInternalStorage(){	
	    	ArrayList<Match> follows = new ArrayList<Match>();	
		 	try{
	    		FileInputStream fis = mContext.openFileInput(Utils.FILENAME);
				ObjectInputStream objIn = new ObjectInputStream(fis);
				follows = (ArrayList<Match>)objIn.readObject();
				if(follows ==null){
					follows = new ArrayList<Match>();
				}
				objIn.close();
				fis.close();
			}catch(Exception e){
				e.printStackTrace();
			}
	    	return follows;
	    }
	 
	 private boolean followsMatch(String tournament, ArrayList<Match> follows){
	    	ArrayList<Match> matches = mTournaments.get(tournament);
     	//see if the user follow any game from the torunament. If the case, then follow = true;
     	boolean follow = false;
     	for(Match match : matches){
     		for(Match followMatch : follows){
     			if(match.equals(followMatch)){
     				follow = true;
     			}
     		}
     	}
     	return follow;
	    }
}

