package com.example.newsappglass3;

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



import com.google.android.glass.widget.CardBuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;


class DownloadJSONTask extends AsyncTask<String, String, String> {

	private Activity mActivity;
	String mSection;
	private Thread mAnimationThead;
	private CardBuilder mCard;
	public DownloadJSONTask(Activity activity, String section, CardBuilder card){
		mActivity = activity;
		mSection = section;
		mCard = card;
		mAnimationThead = new Thread(new LoadNewsAnimation(mActivity, mCard).getAnimationRunnable());
		mAnimationThead.start();
	}
	
	
	@Override
    protected String doInBackground(String... urls) { 	
            return getJSON(urls[0]); 
    }

    @Override
    protected void onPostExecute(String input) {  
    	mAnimationThead.interrupt();
    	int related1 = -1;
    	int related2 = -1;
    	int related3 = -1;
    	try {
			JSONObject jsonObj = new JSONObject(input);
			JSONArray response = jsonObj.getJSONArray("response");
			ArrayList<NewsItem> resultsArray= new ArrayList<NewsItem>();
			for(int i = 0; i < response.length(); i++){
				JSONObject result = response.getJSONObject(i);
				int id = result.getInt("id");
				String headline = result.getString("title");
				String subtitle = result.getString("subtitle");
				String body = result.getString("body");
				String date = result.getString("date");
				Debugger.log(date);
				String imageUrl = result.getString("image");
				String audioUrl = result.getString("audio");
				int bookmark_id = result.getInt("bookmark_id");
				String category = result.getString("category");
				
				related1 = result.getInt("related1");
				related2 = result.getInt("related2");
				related3 = result.getInt("related3");
				
				NewsItem newsItem = new NewsItem(id, headline, subtitle, body, date, imageUrl, "", category, audioUrl); //replace later!!!
				if(bookmark_id == -1){
					newsItem.setBookmarked(false);
				}else{
					newsItem.setBookmarked(true);
				}
				newsItem.addRelatedIDs(related1);
				newsItem.addRelatedIDs(related2);
				newsItem.addRelatedIDs(related3);
				
				resultsArray.add(newsItem);
			}
			//add related news items to news items in resultsArray
			for(int i = 0; i < resultsArray.size(); i++){
				NewsItem newsItem1 = resultsArray.get(i);
				ArrayList<Integer> relatedIDs = newsItem1.getRelatedIDs();
				for(int j = 0; j < resultsArray.size(); j++){
					NewsItem newsItem2 = resultsArray.get(j);
					for(int ID : relatedIDs){
						if(newsItem2.getID() == ID){
							newsItem1.addRelated(newsItem2);
						}
					}
				}
			}
			Intent intent = new Intent(mActivity, NewsSliderActivity.class);
			Bundle bundle = new Bundle();
			bundle.putSerializable("newsitems", resultsArray);
			intent.putExtra("newsitems", bundle);
			intent.putExtra("inappstart", false);
			
			mActivity.startActivity(intent);
			mActivity.finish(); //When new activity is started there is no need for the LoadNewsActvity
			
		} catch (JSONException e) {
			e.printStackTrace();
			mCard.setFootnote("something went wrong with the internet connection");
			mActivity.setContentView(mCard.getView());
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
	       mCard.setFootnote("something went wrong with the internet connection");
	       mActivity.setContentView(mCard.getView());
	    }
		 //Log.d(StartService.TAG, "value " + jsonString);
		return jsonString;
	}
}

