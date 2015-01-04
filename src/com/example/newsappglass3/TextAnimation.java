package com.example.newsappglass3;

import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class TextAnimation {
	
	private String mText;
	private CardWrapper mCard;
	private CardScrollAdapter mCardScrollAdapter;
	
	private final Runnable mAnimationRunnable = new Runnable() {
		@Override
		public void run() {
//			if(mIndex == 0){
				try {
					for(int i = 0; i < 3; i++){
						sendTextToHandler(mText);
						Thread.sleep(300);
						sendTextToHandler("");
						Thread.sleep(300);
					}
					if(mCard.getBookmarked()){
						sendTextToHandler(mCard.getTimeliness()+ "  ★");
					}else{
						sendTextToHandler(mCard.getTimeliness());
					}
				
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//mAnimationHandler.postDelayed(mAnimationRunnable, 3000); //show modes 3 seconds
//			}else if(mIndex == 1){
//				sendTextToHandler(mCard.getTimeliness());
//			}
//			mIndex++;
		}
	};
	
	private final Handler mAnimationHandler = new Handler(){
		public void handleMessage(Message msg) {
			String text =  msg.getData().getString("text"); 
			Object obj = mCard.getCard();
			if(obj instanceof CardBuilder){
				CardBuilder cardBuilder = (CardBuilder)obj;
				cardBuilder.setTimestamp(text);
			}
			mCardScrollAdapter.notifyDataSetChanged();
		}
	};
	
	
	public TextAnimation(String text, CardWrapper card, CardScrollAdapter cardScrollAdapter){
		mText = text;
		mCard = card;
		mCardScrollAdapter = cardScrollAdapter;
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
