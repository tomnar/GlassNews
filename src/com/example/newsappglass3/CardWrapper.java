package com.example.newsappglass3;

import android.widget.RelativeLayout;

import com.google.android.glass.widget.CardBuilder;

public class CardWrapper {
	
	private CardBuilder mCard;
	private RelativeLayout mView;
	private String mType;
	private String mName;
	private String mTime = "";
	private boolean mBookmarked = false;
	public CardWrapper(CardBuilder card, String type, String name){
		mCard = card;
		mType = type;
		mName = name;
	}
	public CardWrapper(RelativeLayout view, String type, String name){
		mView = view;
		mType = type;
		mName = name;
	}
	
	public Object getCard(){
		if(mCard != null){
			return mCard;
		}
		else if(mView != null){
			return mView;
		}else{
			return null;
		}
	}
	
	public String getType(){
		return mType;
	}
	public String getName(){
		return mName;
	}
	public String getTimeliness(){
		return mTime;
	}
	public void setTimeliness(String time){
		mTime = time;
	}
	public boolean getBookmarked(){
		return mBookmarked;
	}
	public void setBookmarked(boolean bookmarked){
		mBookmarked = bookmarked;
	}
	

	
	
}
