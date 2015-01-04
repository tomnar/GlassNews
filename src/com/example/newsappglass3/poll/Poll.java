package com.example.newsappglass3.poll;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.graphics.Typeface;

import com.example.newsappglass3.CardWrapper;
import com.example.newsappglass3.Debugger;
import com.example.newsappglass3.NewsSliderActivity;
import com.example.newsappglass3.R;
import com.google.android.glass.widget.CardBuilder;

public class Poll {
	
	private int mID;
	private String mQuestion;
	private String mAnswer1;
	private String mAnswer2;
	private int mNoOfAnswer1;
	private int mNoOfAnswer2;
	private String mImageUrl;
	private String mPubDate;
	private boolean mHasVoted;
	private int mVote;
	
	public Poll(int ID, String question, String answer1, String answer2, int noOfAnswer1, int noOfAnswer2, 
			String imageUrl,  String pubDate, boolean hasVoted, int vote){
		mID = ID;
		mQuestion = question;
		mAnswer1 = answer1;
		mAnswer2 = answer2;
		mNoOfAnswer1 = noOfAnswer1;
		mNoOfAnswer2 = noOfAnswer2;
		mImageUrl = imageUrl;
		mPubDate = pubDate;
		mHasVoted = hasVoted;
		mVote = vote;
	}

	public Poll(){
		mVote = 0;
	}
	
	public int getID(){
		return mID;
	}
	public void setID(int ID){
		mID = ID;
	}
	public String getQuestion() {
		return mQuestion;
	}
	public void setQuestion(String question) {
		mQuestion = question;
	} 
	
	public String getAnswer1() {
		return mAnswer1;
	}
	public String setAnswer1(String answer1) {
		return mAnswer1  = answer1;
	}
	
	public String getAnswer2() {
		return mAnswer2;
	}
	public void setAnswer2(String answer2){
		mAnswer2 = answer2; 
	}
	
	public int getNoOfAnswer1() {
		return mNoOfAnswer1;
	}
	public void setNoOfAnswer1(int noOfAnswer1){
		mNoOfAnswer1 = noOfAnswer1;
	}
	
	public int getNoOfAnswer2() {
		return mNoOfAnswer2;
	}
	public void setNoOfAnswer2(int noOfAnswer2){
		mNoOfAnswer2 = noOfAnswer2;
	}
	
	public String getImageUrl() {
		return mImageUrl;
	}
	public void setImageUrl(String imageUrl) {
		mImageUrl = imageUrl;
	}
	
	public String getPubDate() {
		return mPubDate;
	}
	public void setPubDate(String pubDate) {
		mPubDate = pubDate;
	}
	public boolean getHasVoted(){
		return mHasVoted;
	}
	public void setHasVoted(boolean hasVoted){
		mHasVoted = hasVoted;
	}
	public int getVote(){
		return mVote;
	}
	public void setVote(int vote){
		mVote = vote;
	}
	

	public static ArrayList<CardWrapper> createPollCards(Activity activity, Poll poll){
		CardWrapper cardWrapper = null;
		if(!poll.getHasVoted()){
			CardBuilder card = new CardBuilder(activity, CardBuilder.Layout.TEXT);
			card.setText(poll.getQuestion());
			card.setFootnote("Today's poll");
			NewsSliderActivity newsSlider = (NewsSliderActivity)activity;
			card.setTimestamp(newsSlider.calculateTimeliness(poll.getPubDate()));
			cardWrapper = new CardWrapper(card, "poll", poll.getQuestion()); 
		}else{
			RelativeLayout rl = (RelativeLayout)activity.getLayoutInflater().inflate(R.layout.poll_card_layout, null);
			TextView pollQuestion = (TextView)rl.findViewById(R.id.pollQuestionTextView);
			TextView pollAnswer1 = (TextView)rl.findViewById(R.id.pollAnswer1Desc);
			TextView pollAnswer2 = (TextView)rl.findViewById(R.id.pollAnswer2Desc);
			TextView pollNoOfAnswer1 = (TextView)rl.findViewById(R.id.pollAnswer1Votes);
			TextView pollNoOfAnswer2 = (TextView)rl.findViewById(R.id.pollAnswer2Votes);
			ImageView pollBar1 = (ImageView)rl.findViewById(R.id.pollAnswer1Bar);
			ImageView pollBar2 = (ImageView)rl.findViewById(R.id.pollAnswer2Bar);
			
			pollQuestion.setText(poll.getQuestion());
			pollAnswer1.setText(poll.getAnswer1());
			pollAnswer2.setText(poll.getAnswer2());
			pollNoOfAnswer1.setText(""+poll.getNoOfAnswer1());
			pollNoOfAnswer2.setText(""+poll.getNoOfAnswer2());
			if(poll.getVote() == 1){
				pollAnswer2.setTextColor(Color.GRAY);
				pollNoOfAnswer2.setTextColor(Color.GRAY);
				pollBar2.setBackgroundColor(Color.GRAY);
			}else if(poll.getVote() == 2){
				pollAnswer1.setTextColor(Color.GRAY);
				pollNoOfAnswer1.setTextColor(Color.GRAY);
				pollBar1.setBackgroundColor(Color.GRAY);
			}
			int noOfAnswer1 = poll.getNoOfAnswer1();
			int noOfAnswer2 = poll.getNoOfAnswer2();
			double totalVotes = noOfAnswer1 + noOfAnswer2;
			int maxBarHeight = 180; //px
			int barHeight1 = (int) (noOfAnswer1/totalVotes * maxBarHeight); 
			int barHeight2 = (int) (noOfAnswer2/totalVotes * maxBarHeight);
			Debugger.log("bar height 1 " + barHeight1 + " bar height 2 " + barHeight2);
			pollBar1.setLayoutParams(new LinearLayout.LayoutParams(50, barHeight1));
			pollBar2.setLayoutParams(new LinearLayout.LayoutParams(50, barHeight2));
			
			cardWrapper = new CardWrapper(rl, "poll", poll.getQuestion()); 
		}
		
		ArrayList<CardWrapper> cards= new ArrayList<CardWrapper>();
		cards.add(cardWrapper);
		return cards;
	}


}
