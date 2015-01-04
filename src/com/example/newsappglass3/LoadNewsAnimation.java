package com.example.newsappglass3;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LoadNewsAnimation {
	
	private CardBuilder mCard;
	private Activity mActivity;
	private final int SLEEP = 200;
	
	private final Runnable mAnimationRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				while(true){
					for(int i = 0; i < 3; i++){
						sendTextToHandler("loading news");
						Thread.sleep(SLEEP);
						sendTextToHandler("loading news.");
						Thread.sleep(SLEEP);
						sendTextToHandler("loading news..");
						Thread.sleep(SLEEP);
						sendTextToHandler("loading news...");
						Thread.sleep(SLEEP);
						sendTextToHandler("loading news....");
						Thread.sleep(SLEEP);
						sendTextToHandler("loading news.....");
						Thread.sleep(SLEEP);
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	};
	
	private final Handler mAnimationHandler = new Handler(){
		public void handleMessage(Message msg) {
			String text =  msg.getData().getString("text"); 
			mCard.setFootnote(text);
			mActivity.setContentView(mCard.getView());
			//mCardScrollAdapter.notifyDataSetChanged();
		}
	};

	
	
	public LoadNewsAnimation(Activity activity, CardBuilder card){
		mActivity = activity;
		mCard = card;
	}
	
	private void sendTextToHandler(String text){
		Message msg = new Message();
		Bundle b = new Bundle();
		b.putString("text", text);
		msg.setData(b);
		mAnimationHandler.sendMessage(msg);
	}
	
	public Runnable getAnimationRunnable(){
		return mAnimationRunnable;
	}
		
	

}
