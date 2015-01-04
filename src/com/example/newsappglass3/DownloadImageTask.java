package com.example.newsappglass3;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.webkit.WebView.FindListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;


class DownloadImageTask extends AsyncTask<Void, Void, ArrayList<Bitmap>> {
	
	private ArrayList<CardWrapper> mCards;
	private CardScrollAdapter mCardScrollAdapter;
	private ArrayList<NewsItem> mNewsItems;
	private final String imagePath = "";
	public DownloadImageTask(ArrayList<CardWrapper> cards, CardScrollAdapter cardScrollAdapter, ArrayList<NewsItem> newsItems){
		mCards = cards;
		mCardScrollAdapter = cardScrollAdapter;
		mNewsItems = newsItems;
	}
    
    @Override
    protected void onPostExecute(ArrayList<Bitmap> bms) {  
    	int index = 0;
    	for(NewsItem newsItem : mNewsItems){
    		Object obj = mCards.get(index).getCard();
    		Bitmap bm = bms.get(index);
    		if(obj instanceof CardBuilder){
    			CardBuilder cardBuilder = (CardBuilder) obj;
    			cardBuilder.addImage(bm);
    		}
    		
    		index++;
    	}
    	mCardScrollAdapter.notifyDataSetChanged();
    }

	@Override
	protected ArrayList<Bitmap> doInBackground(Void... params) {
		ArrayList<Bitmap> bms = new ArrayList<Bitmap>();
		for(NewsItem newsItem: mNewsItems){
			//first check to see if file exists on internal device storage
			String path = Environment.getExternalStorageDirectory().toString()+imagePath;
			File file = new File(path, "image"+newsItem.getID()+".jpg");
			Debugger.log("file exists when reading on startup " + file.exists() +" path " + file.getAbsolutePath());
			if(file.exists()){
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;
				options.inDither=false;                     //Disable Dithering mode
				options.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
				options.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
				options.inTempStorage=new byte[32 * 1024];
				//causes out of memory error
				Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
				bms.add(bitmap);
				
				newsItem.setExternalImageUrl(file.getAbsolutePath());
			}else{
				//if not download image from the internet, then write it to intrnal storage
				Bitmap bm = null;
				String urlString = newsItem.getImageUrl();
				OutputStream fOut = null;
				try{
					URL url = new URL(urlString);
				    InputStream in = url.openStream();
				    bm = BitmapFactory.decodeStream(in);
				    bms.add(bm);
				    
				  //  then write it to internal storage
					fOut = new FileOutputStream(file);
					bm.compress(Bitmap.CompressFormat.JPEG, 50, fOut);
					newsItem.setExternalImageUrl(file.getAbsolutePath());
					fOut.flush();
					Debugger.log("FILE EXISTS WHEN WRITING " + file.exists() +" path " + file.getAbsolutePath());
				}catch(Exception e){
					e.printStackTrace();
					try {
						fOut.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}
		return bms;
	}
}
