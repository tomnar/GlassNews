package com.example.newsappglass3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

public class Utils {

	private static String mUsername;
	
	// Finals
	protected static final String FILENAME = "ENTER HERE";
	protected static final String TAG = "ENTER HERE";
	protected static final String PROPERTY_REG_ID = "ENTER HERE";
	private static final String BASE = "ENTER HERE";
	
	protected static final String PROPERTY_APP_VERSION = "ENTER HERE";
	protected static final String SENDER_ID = "ENTER HERE";
	
	protected static final String NEWS = "ENTER HERE";
	protected static final String TOP_NEWS = "ENTER HERE";
    protected static final String DENMARK = "ENTER HERE";
    protected static final String WORLD = "ENTER HERE";
    protected static final String BUSINESS = "ENTER HERE";
    protected static final String SPORT = "ENTER HERE";
    protected static final String POLITICS = "ENTER HERE";
    protected static final String HEALTH = "ENTER HERE";
    protected static final String ENTERTAINMENT = "ENTER HERE";
    protected static final String TECHNOLOGY = "ENTER HERE";
    protected static final String BOOKMARKS = "ENTER HERE";
    protected static final String CHECKBOOKMARKED = "ENTER HERE";
    protected static final String EB_URL = "ENTER HERE";
	public static final String POLL_URL = "ENTER HERE";
	public static final String VOTE_URL = "ENTER HERE";
	
	//Method used for sending data via POST, NB if a result should be shown use an Async-task instead
	public static void postData(final String url, final List<NameValuePair> postData) {		
		Thread thread = new Thread(){
		    public void run(){
		    	// Create a new HttpClient and Post Header
			    HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost(url);
			    try {
			        // Add your data
			        httppost.setEntity(new UrlEncodedFormEntity(postData, "UTF-8"));
			        // Execute HTTP Post Request
			        httpclient.execute(httppost);
			    } catch (ClientProtocolException e) {
			        e.printStackTrace();
			    } catch (IOException e) {
			    	e.printStackTrace();
			    }    
			} 
		};
		thread.start();
	}
	
	public static String getContentFromUrl(String url){
        StringBuilder a = new StringBuilder();
        try{
        URL mUrl = new URL(url);
        URLConnection urlCon = mUrl.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(urlCon.getInputStream(), "UTF-8"));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            a.append(inputLine);
        in.close();
        }
        catch (IOException e){
                e.printStackTrace();
        }
        return a.toString();
}
		
	// Getters and setters for varaibles
	public static void setUsername(String username){
		mUsername = username;
	}	
	public static String getUsername(){
		return mUsername;
	}
	public static String getBase(){
		return BASE;
	}
	
	
	
	
	
	
	
}
