package com.example.newsappglass3;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.glass.widget.CardBuilder;

public class TournamentLogic {
	
	public static ArrayList<CardWrapper> createTournamentCards(Context context, HashMap<String, ArrayList<Match> > tournaments){
		ArrayList<CardWrapper> cards = new ArrayList<CardWrapper>();
		for(String key : tournaments.keySet()){
			CardBuilder card = new CardBuilder(context, CardBuilder.Layout.CAPTION);
			card.setText(key);
			if(key.equals("UEFA Champions League")){
				Bitmap background = BitmapFactory.decodeResource(context.getResources(), R.drawable.champions_league);
				card.addImage(background);
			}
			else if(key.equals("Premier League")){
				Bitmap background = BitmapFactory.decodeResource(context.getResources(), R.drawable.premier_league);
				card.addImage(background);
			}
			else if(key.equals("Super Liga")){
				Bitmap background = BitmapFactory.decodeResource(context.getResources(), R.drawable.premier_league);
				card.addImage(background);
			}
			else if(key.equals("Europa League")){
				Bitmap background = BitmapFactory.decodeResource(context.getResources(), R.drawable.europa_league);
				card.addImage(background);
			}
			card.setFootnote(tournaments.get(key).size() + " matches today");
			cards.add(new CardWrapper(card, "tournament", key));
		}
		return cards;
	}

}
