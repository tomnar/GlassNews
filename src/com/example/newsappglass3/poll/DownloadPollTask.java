package com.example.newsappglass3.poll;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;














import com.example.newsappglass3.CardUtils;
import com.example.newsappglass3.CardWrapper;
import com.example.newsappglass3.Debugger;
import com.example.newsappglass3.NewsItem;
import com.example.newsappglass3.NewsSliderActivity;
import com.example.newsappglass3.R;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class DownloadPollTask extends AsyncTask<String, String, String> {

	private Activity mActivity;
	private ArrayList<CardWrapper> mCards;
	private CardScrollAdapter mCardScrollAdapter;
	private Poll mPoll;
	private ArrayList<NewsItem> mNewsItems;
	public DownloadPollTask(Activity activity, ArrayList<CardWrapper> cards, CardScrollAdapter cardScrollAdapter,
			Poll poll,  ArrayList<NewsItem> newsItems){
		mActivity = activity;
		mCards = cards;
		mCardScrollAdapter = cardScrollAdapter;
		mPoll = poll;
		mNewsItems = newsItems;
	}
	
	@Override
    protected String doInBackground(String... urls) { 	
            return getJSON(urls[0]); 
    }

    @Override
    protected void onPostExecute(String input) {  
    	Debugger.log("in post execute of poll task");
    	try {
			JSONObject jsonObj = new JSONObject(input);
			int ID = jsonObj.getInt("id");
			String question = jsonObj.getString("question");
			String answer1 = jsonObj.getString("answer1");
			String answer2 = jsonObj.getString("answer2");
			int noOfAnswer1 = Integer.parseInt( jsonObj.getString("answer1s") );
			int noOfAnswer2 = Integer.parseInt( jsonObj.getString("answer2s") );
			String imageUrl = jsonObj.getString("image_url");
			String pubDate = jsonObj.getString("pub_date");
			boolean hasVoted = jsonObj.getBoolean("hasVoted");
			int hasVotedAnswer1 = jsonObj.getInt("hasVotedAnswer1");
			int hasVotedAnswer2 = jsonObj.getInt("hasVotedAnswer2");
			
			mPoll.setID(ID);
			mPoll.setQuestion(question);
			mPoll.setAnswer1(answer1);
			mPoll.setAnswer2(answer2);
			mPoll.setNoOfAnswer1(noOfAnswer1);
			mPoll.setNoOfAnswer2(noOfAnswer2);
			mPoll.setImageUrl(imageUrl);
			mPoll.setPubDate(pubDate);
			mPoll.setHasVoted(hasVoted);
			if(hasVoted){
				if(hasVotedAnswer1 == 1){
					mPoll.setVote(1);
				}else if(hasVotedAnswer2 == 1){
					mPoll.setVote(2);
				}
			}else{
				mPoll.setVote(0);
			}
			
			CardWrapper cardWrapper = null;
			if(!mPoll.getHasVoted()){
				CardBuilder card = new CardBuilder(mActivity, CardBuilder.Layout.TEXT);
				card.setText(mPoll.getQuestion());
				card.setFootnote("");
				card.setTimestamp("Today's poll");
				NewsSliderActivity newsSlider = (NewsSliderActivity)mActivity;
				card.setTimestamp(newsSlider.calculateTimeliness(pubDate));
				cardWrapper = new CardWrapper(card, "poll", mPoll.getQuestion()); 
			}else{
//				RelativeLayout rl = (RelativeLayout)mActivity.getLayoutInflater().inflate(R.layout.poll_card_layout, null);
//				TextView pollQuestion = (TextView)rl.findViewById(R.id.pollQuestionTextView);
//				TextView pollAnswer1 = (TextView)rl.findViewById(R.id.pollAnswer1Desc);
//				TextView pollAnswer2 = (TextView)rl.findViewById(R.id.pollAnswer2Desc);
//				TextView pollNoOfAnswer1 = (TextView)rl.findViewById(R.id.pollAnswer1Votes);
//				TextView pollNoOfAnswer2 = (TextView)rl.findViewById(R.id.pollAnswer2Votes);
//				ImageView pollBar1 = (ImageView)rl.findViewById(R.id.pollAnswer1Bar);
//				ImageView pollBar2 = (ImageView)rl.findViewById(R.id.pollAnswer2Bar);
//				
//				pollQuestion.setText(mPoll.getQuestion());
//				pollAnswer1.setText(mPoll.getAnswer1());
//				pollAnswer2.setText(mPoll.getAnswer2());
//				pollNoOfAnswer1.setText(""+mPoll.getNoOfAnswer1());
//				pollNoOfAnswer2.setText(""+mPoll.getNoOfAnswer2());
//				double totalVotes = noOfAnswer1 + noOfAnswer2;
//				int maxBarHeight = 180; //px
//				int barHeight1 = (int) (noOfAnswer1/totalVotes * maxBarHeight); 
//				int barHeight2 = (int) (noOfAnswer2/totalVotes * maxBarHeight);
//				Debugger.log("bar height 1 " + barHeight1 + " bar height 2 " + barHeight2);
//				pollBar1.setLayoutParams(new LinearLayout.LayoutParams(50, barHeight1));
//				pollBar2.setLayoutParams(new LinearLayout.LayoutParams(50, barHeight2));
//				cardWrapper = new CardWrapper(rl, "poll", mPoll.getQuestion()); 
				ArrayList<CardWrapper> cards = Poll.createPollCards(mActivity, mPoll);
				cardWrapper = cards.get(0);
			}
			
			//update currentNo/totalNoOfNews
			mCards.add(cardWrapper);
			int noOfNewsItems = mNewsItems.size();
			for(int i = 0; i < noOfNewsItems; i++){
				CardBuilder card = CardUtils.getCard(mCards.get(i));
				card.setFootnote((i+1) +"/"+ mCards.size() + " - " + mNewsItems.get(i).getSection());
			}
			for(int i = noOfNewsItems; i < mCards.size(); i++ ){
				CardWrapper cardWr = mCards.get(i);
				CardBuilder card = CardUtils.getCard(cardWr);
				if(card != null){
					String type = cardWr.getType();
					if(type.equals("tournament")){
						card.setFootnote((i+1) +"/"+ mCards.size() + " - " + "sports");
					}else if(type.equals("poll")){
						card.setFootnote((i+1) +"/"+ mCards.size() + " - " + "today's poll");
					}
				}
			}
			mCardScrollAdapter.notifyDataSetChanged();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

    }
    
	private String getJSON(String urlString) {	
		Debugger.log("in getJSON of poll task"); 
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

