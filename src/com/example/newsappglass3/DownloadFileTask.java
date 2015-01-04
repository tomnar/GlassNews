package com.example.newsappglass3;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.glass.android.os.PowerManager;

public class DownloadFileTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private CardWrapper mCard;
	private MyCardScrollAdapter mCardScrollAdapter;
	private NewsItem mNewsItem;
    public DownloadFileTask(Context context, CardWrapper card, MyCardScrollAdapter cardScrollAdapter, NewsItem newsItem) {
        this.context = context;
        mCard = card;
        mCardScrollAdapter = cardScrollAdapter;
        mNewsItem = newsItem;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        HttpURLConnection redirectConnection = null;
        File file = null;
        try {
	    	 file = new File(Environment.getExternalStorageDirectory().toString(), "audio" + mNewsItem.getID() + ".mp3");
	         //if file exists in storage already, do not bother to connect to the internet and download
	    	if(file.exists()){
	    		mNewsItem.setExternalAudioUrl(file.getAbsolutePath());
	         	return null;
	        }
             
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            
            String redirectLocation = connection.getHeaderField("Location");
            URL redirectUrl = new URL(redirectLocation);
            redirectConnection = (HttpURLConnection) redirectUrl.openConnection();
            redirectConnection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (redirectConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + redirectConnection.getResponseCode()
                        + " " + redirectConnection.getResponseMessage();
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            int fileLength = redirectConnection.getContentLength();

            // download the file
            input = redirectConnection.getInputStream();
            output = new FileOutputStream(file);
            
            byte data[] = new byte[4096];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    //if download of file never finished, delete file
                    file.delete();
                    return null;
                }
                total += count;
                // publishing the progress....
               // if (fileLength > 0) // only if total length is known
                   // publishProgress((int) (total * 100 / fileLength));
                output.write(data, 0, count);
            }
            //Ensure that file has been completely downloaded before setting external audio url on news item
            //Otherwise we run the risk that playback will be performed on an uncomplete soundfile. 
            mNewsItem.setExternalAudioUrl(file.getAbsolutePath());
        } catch (Exception e) {
        	e.printStackTrace();
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null){
                connection.disconnect();
            }
            if(redirectConnection != null){
            	redirectConnection.disconnect();
            }
            
        }
        return null;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

    }
   

    @Override
    protected void onPostExecute(String result) {
//        if (result != null){
//        }
//        else{
//        	///new Thread(new TextAnimation("audio downloaded", mCard, mCardScrollAdapter).getAnimationRunnable()).start();
//        }
        mCardScrollAdapter.notifyDataSetChanged();
    }
    
    
    public CardBuilder getCard(Object obj){
    	CardBuilder card = null;
    	if(obj instanceof CardBuilder){
    		card = (CardBuilder)obj;
    	}
    	return card;
    }
}