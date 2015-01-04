package com.example.newsappglass3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.example.newsappglass3.voice.Constants;
import com.example.newsappglass3.voice.VoiceDetection;
import com.example.newsappglass3.voice.VoiceMenuEss;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.gms.internal.mn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

public class ReadCardActivity extends Activity implements VoiceDetection.VoiceDetectionListener {

	
    private ArrayList<CardWrapper> mCards;
    private NewsItem newsItem;
    private VoiceMenuEss mVoiceMenu;
    private String[] mPhrases = new String[] { Constants.GOBACK  }; //Constants.CLOSEAPP
	private String mHeadline;
	private int mIndex = 0;
	private final double mWordsPerCard = 25;
	private MyCardScrollAdapter mAdapter;
	private CardScrollView mCardScrollView;
	private String actionUrlBase;
	private OnItemSelectedListener listener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view,
				int position, long id) {
			mIndex = position;
		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {
		}
	};
	private AudioManager mAudio;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		Intent intent = getIntent();
		Bundle bundle = intent.getBundleExtra("newsitem");
		//mNewsItems = (ArrayList<NewsItem>)bundle.getSerializable("newsitems");
		newsItem = (NewsItem)bundle.getSerializable("newsitem");
		//int indexOfCard = intent.getIntExtra("index", 0);
		
		//first get bitmaps of news items from external storage
		//do in task manager, otherwise it takes too loooong!!!!!!!!!
	//	Bitmap bm = readBitmapFromExternalStorage(newsItem);
		//then create text + image content
		createContent(newsItem);
		
		mVoiceMenu = new VoiceMenuEss(this, this, Constants.OKGLASS, mPhrases);
	//	mVoiceMenu.changePhrases(new String[] { Constants.NEXT, Constants.PREVIOUS } );
	
		//Get audio manager. Enables audio feedback on actions. 
        mAudio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        
       //log url
        actionUrlBase =  Utils.getBase() + "createLog.php?email="+Utils.getUsername()+"&action=";
        
		setContentView(mCardScrollView);
	}
	
	protected void onResume(){
		 super.onResume();
	     mVoiceMenu.start();
	}
	protected void onPause(){
		super.onPause();
	    mVoiceMenu.stop();
	}
	

//	private Bitmap readBitmapFromExternalStorage(NewsItem newsItem) {
//	//	int noOfNewsItems = newsItems.size();
//		//ArrayList<Bitmap> bms = new ArrayList<Bitmap>(); 
//		Bitmap bm = null;
//	//	for(int i = 0; i < noOfNewsItems; i++){
//			//read bitmaps from internal storage
//			try {
//				//String path = Environment.getExternalStorageDirectory().toString();
//				String path = Environment.getExternalStorageDirectory().toString();
//				File file = new File(path, "image"+newsItem.getID()+".jpg");
//				Debugger.log("file exists when reading in readcard activity " + file.exists() +" path " + file.getAbsolutePath());
//				if(file.exists()){
//					BitmapFactory.Options options = new BitmapFactory.Options();
//					options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//					Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
//					//bms.add(bitmap);
//					bm = bitmap;
//				}
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		//}
//		return bm;
//	}


	private void createContent(NewsItem newsItem){
		
		mAdapter = new MyCardScrollAdapter();
    	mCardScrollView = new CardScrollView(this);
        mCardScrollView.setAdapter(mAdapter);
        mCardScrollView.setOnItemSelectedListener(listener );
        mCardScrollView.activate();
        mCards = new ArrayList<CardWrapper>();
        
        String body = newsItem.getBody();
		createBodyText(body, newsItem, mCards);
		
        mAdapter.setCards(mCards);
        mAdapter.notifyDataSetChanged();
	}



	private void createBodyText(String body, NewsItem newsItem, ArrayList<CardWrapper> cards) {
		String[] words = body.split(" ");
		int noOfCards = (int)Math.ceil(words.length / mWordsPerCard);
        
        for(int i = 0; i < noOfCards; i++){
        	CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT_FIXED);
        	StringBuilder textOfCard = new StringBuilder();
        	for(int j = (int)mWordsPerCard*i; j < (int)mWordsPerCard*(i+1); j++){
        		if(j < words.length){
        			textOfCard.append(words[j]+ " ");
        		}
        	}
        	card.setText(textOfCard.toString());
        	card.setFootnote( (i+1) +  "/" + noOfCards );
        	CardWrapper cardWrapper = new CardWrapper(card, "bodytext", mHeadline);
        	cards.add(cardWrapper);
        }
        createRelatedContent(newsItem, cards);
		
	}
	
	private void createRelatedContent(NewsItem newsItem, ArrayList<CardWrapper> cards){
		ArrayList<NewsItem> related = newsItem.getRelated();
		CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
		card.setText("Related");
		card.setFootnote(related.size() + " related articles");
		new DownloadRelatedImageTask(card, mAdapter, related).execute();

		CardWrapper cardWrapper = new CardWrapper(card, "related", newsItem.getHeadline());
		cards.add(cardWrapper);
	}
	
	@Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
    	if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.read_text, menu);
            return true;
        }
        // Pass through to super to setup touch menu.
        return super.onCreatePanelMenu(featureId, menu);
    }
	
	 public boolean onPreparePanel(int featureId, View view, Menu menu) {
	    	if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId == Window.FEATURE_OPTIONS_PANEL) {
	    		return true;
	    	}
	    	// Good practice to call through to super for other cases
	        return super.onPreparePanel(featureId, view, menu);
	   }
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        getMenuInflater().inflate(R.menu.read_text, menu);
	        return super.onCreateOptionsMenu(menu);
	    }
	
	 @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
        	CardWrapper cardWrapper = mCards.get(mIndex);
        	if(cardWrapper.getType().equals("related")){
        		String actionUrl = actionUrlBase +"tap_read_card";
        		new Thread(new GetRequestRunnable(actionUrl)).start();
    			if( newsItem.getHeadline().equals(cardWrapper.getName()) ){
    				openRelated(newsItem);
    			}
        	}
        	return true;
        }
     
        return super.onKeyDown(keyCode, event);
    }
	 
	 private void openRelated(NewsItem newsItem){
		 String actionUrl = actionUrlBase +"related_read_card";
	     new Thread(new GetRequestRunnable(actionUrl)).start();
		 
		 mAudio.playSoundEffect(Sounds.TAP);
		 ArrayList<NewsItem> related = newsItem.getRelated();
		Intent intent = new Intent(this, NewsSliderActivity.class);
	//	intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
		Bundle bundle = new Bundle();
		bundle.putSerializable("newsitems", related);
		intent.putExtra("newsitems", bundle);
		intent.putExtra("inappstart", true);
		startActivity(intent);
	 }

	@Override
	public void onHotwordDetected() {
		 String actionUrl = actionUrlBase +"on_hotword_read_card";
	     new Thread(new GetRequestRunnable(actionUrl)).start();
		//set phrases
    	CardWrapper cardWrapper = mCards.get(mIndex);
    	if(cardWrapper.getType().equals("related")){
    		mVoiceMenu.changePhrases(new String[]{Constants.RELATED, Constants.GOBACK});//Constants.CLOSEAPP
    	}else{
    	 	mVoiceMenu.changePhrases(mPhrases);
    	}
		
	}


	@Override
	public void onPhraseDetected(int index, String phrase) {
		if(phrase.equals(Constants.GOBACK)){
			finish();
		}
		else if(phrase.equals(Constants.NEXT)){
			int indexOfCard = mIndex+1;
			indexOfCard = indexOfCard % mCards.size();
			mCardScrollView.animate(indexOfCard, CardScrollView.Animation.NAVIGATION);
		}
		else if(phrase.equals(Constants.RELATED)){
			CardWrapper cardWrapper = mCards.get(mIndex);
        	if(cardWrapper.getType().equals("related")){
    			if( newsItem.getHeadline().equals(cardWrapper.getName()) ){
    				openRelated(newsItem);
    			}
        	}
		}
		else if(phrase.equals(Constants.PREVIOUS)){
			int indexOfCard = mIndex-1;
			if(indexOfCard >= 0){
				mCardScrollView.animate(indexOfCard, CardScrollView.Animation.NAVIGATION);
			}else{
				indexOfCard = mCards.size()-1;
				mCardScrollView.animate(indexOfCard, CardScrollView.Animation.NAVIGATION);
			}
		}
	}
		
}
