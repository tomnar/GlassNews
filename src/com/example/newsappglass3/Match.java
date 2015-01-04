package com.example.newsappglass3;

import java.io.Serializable;
import java.util.ArrayList;

import org.json.JSONArray;

import android.graphics.Bitmap;

public class Match implements Serializable {
	
	private String mHomeTeamName;
	private String mAwayTeamName;
	private String mHomeScore = "0";
	private String mAwayScore = "0";
	private String mStatus;
	private String mTournament;
	private Bitmap mHomeTeamLogo;
	private Bitmap mAwayTeamLogo;
	private boolean mFollow;
	private ArrayList<Event> mEvents;
	
	public Match(String homeTeamName, String awayTeamName, String homeScore, String awayScore,
			String status, String tournament){
		mHomeTeamName = homeTeamName;
		mAwayTeamName = awayTeamName;
		mHomeScore = homeScore;
		mAwayScore = awayScore;
		mStatus = status;
		mTournament = tournament;
		mFollow = false;
		mEvents = new ArrayList<Event>();
	}

	public String getHomeTeamName() {
		return mHomeTeamName;
	}
	public String getAwayTeamName() {
		return mAwayTeamName;
	}
	public String getHomeScore() {
		return mHomeScore;
	}
	public String getAwayScore() {
		return mAwayScore;
	}
	public String getStatus() {
		return mStatus;
	}
	public void setStatus(String status) {
		mStatus = status;
	}
	public String getTournament() {
		return mTournament;
	}
	public Bitmap getHomeTeamLogo() {
		return mHomeTeamLogo;
	}
	public void setHomeTeamLogo(Bitmap homeTeamLogo) {
		mHomeTeamLogo = homeTeamLogo;
	}
	public Bitmap getAwayTeamLogo() {
		return mAwayTeamLogo;
	}
	public void setAwayTeamLogo(Bitmap awayTeamLogo) {
		mAwayTeamLogo = awayTeamLogo;
	}
	public void setFollow(boolean follow){
		mFollow = follow;
	}
	public boolean getFollow(){
		return mFollow;
	}
	
	
	public void addEvent(Event event){
		mEvents.add(event);
	}
	public ArrayList<Event> getEvents(){
		return mEvents;
	}
	
	@Override
	public boolean equals(Object obj){
		Match otherMatch = (Match)obj;
		return mHomeTeamName.equals(otherMatch.getHomeTeamName())
				&& mAwayTeamName.equals(otherMatch.getAwayTeamName());
	}

	
	
	
	

}
