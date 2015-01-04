package com.example.newsappglass3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class GetRequestRunnable implements Runnable {
		
		private String mUrlString;
		public GetRequestRunnable(String urlString){
			mUrlString = urlString;
		}
		@Override
		public void run() {
			makeRequest(mUrlString);
		}
		
		private void makeRequest(String urlString){
			HttpURLConnection conn = null; 
			URL url = null;
			try {
				url = new URL(urlString);
				conn = (HttpURLConnection) url.openConnection();
		    	conn.setReadTimeout(10000 /* milliseconds */);
			    conn.setConnectTimeout(15000 /* milliseconds */);
				conn.setRequestMethod("GET");
				
				//conn.setRequestProperty("Accept", "application/json");
				conn.setDoInput(true);
				// Starts the query
				conn.connect();
				InputStream in = conn.getInputStream();
;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
}
