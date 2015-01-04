package com.example.newsappglass3;

import com.google.android.glass.widget.CardBuilder;

public class CardUtils {
	
	public static CardBuilder getCard(CardWrapper cardWrapper){
		Object obj = cardWrapper.getCard();
		CardBuilder card = null;
		if(obj instanceof CardBuilder){
			card = (CardBuilder)obj;
		}
		return card;
	}

}
