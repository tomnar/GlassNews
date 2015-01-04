package com.example.newsappglass3;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class Event implements Serializable{

	private EventType mType;
	private String mPlayer;
	private String mTeam;
	private String mTime;
	private String mComment;
	private String mHomeScore;
	private String mAwayScore;
	public Event(EventType type, String player, String team, String time, 
			String homeScore, String awayScore, String comment){
		mType = type;
		mPlayer = player;
		mTeam = team;
		mTime = time;
		mComment = comment;
		mHomeScore = homeScore;
		mAwayScore = awayScore;
	}
	public EventType getType(){
		return mType;
	}
	public String getPlayer(){
		return mPlayer;
	}
	public String getTeam(){
		return mTeam;
	}
	public String getTime(){
		return mTime;
	}
	public String getComment(){
		return mComment;
	}
	public String getHomeScore(){
		return mHomeScore;
	}
	public String getAwayScore(){
		return mAwayScore;
	}
	public Bitmap getBitmap(Context mContext){
		if(mType == EventType.REDCARD){
			return BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.red);
		}else if(mType == EventType.YELLOWCARD){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.yellow);
		}else if(mType == EventType.FREEKICK){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.ic_launcher);
		}else if(mType == EventType.GOALHOME){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.goal);
		}else if(mType == EventType.GOALAWAY){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.goal);
		}else if(mType == EventType.START){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.halftime);
		}else if(mType == EventType.HALFTIME){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.halftime);
		}else if(mType == EventType.SECONDHALF){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.halftime);
		}else if(mType == EventType.FINISH){
			return BitmapFactory.decodeResource(mContext.getResources(),
	                R.drawable.finish);
		}
		return null;
	}
	
	
}
